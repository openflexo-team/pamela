package org.openflexo.pamela.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.test.containment.TestContainerA;
import org.openflexo.pamela.test.proxybackend.base.BaseObject;
import org.openflexo.pamela.test.proxybackend.model.Item;
import org.openflexo.pamela.test.proxybackend.model.ItemImpl;
import org.openflexo.pamela.test.proxybackend.model.impl.OtherItemImpl;
import org.openflexo.pamela.test.tests1.ActivityNode;
import org.openflexo.pamela.test.tests1.FlexoProcess;
import org.openflexo.pamela.test.tests1.FlexoProcessImpl;

import com.google.common.base.Defaults;

/**
 * Tests the generation of PAMELA proxies by each {@link ProxyBackend} (PAMELA-F-1): packages and class loaders of the interface and the
 * implementation, super calls, construction, exceptions, cache, and the parity of the Byte Buddy backend with javassist on the method
 * declaration given to the handler.
 */
@RunWith(Parameterized.class)
public class ProxyBackendTest {

	@Parameters(name = "{0}")
	public static Collection<Object[]> backends() {
		return Arrays.asList(new Object[][] { { "bytebuddy", new ByteBuddyProxyBackend() }, { "javassist", new JavassistProxyBackend() } });
	}

	private static final String CHILD_PACKAGE = "org.openflexo.pamela.test.proxybackend.child.";

	private static final Predicate<Method> ALL = m -> true;

	private final ProxyBackend backend;

	public ProxyBackendTest(String name, ProxyBackend backend) {
		this.backend = backend;
	}

	/**
	 * A handler storing properties and calling the super implementation when there is one
	 */
	public static class StoringHandler implements PamelaMethodHandler {
		public final Map<String, Method> proceeds = new HashMap<>();
		private final Map<String, Object> values = new HashMap<>();

		@Override
		public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable {
			proceeds.put(method.getName(), proceed);
			if (proceed != null) {
				try {
					return proceed.invoke(self, args);
				} catch (InvocationTargetException e) {
					throw e.getTargetException();
				}
			}
			if (method.getAnnotation(Initializer.class) != null) {
				values.put("Name", args[0]);
				return self;
			}
			String name = method.getName();
			if (name.startsWith("set")) {
				values.put(name.substring(3), args[0]);
				return null;
			}
			if (name.startsWith("get")) {
				return values.get(name.substring(3));
			}
			throw new UnsupportedOperationException(method.toString());
		}
	}

	/**
	 * A child-first class loader for one package, as the JarClassLoader of technology adapters
	 */
	static class ChildFirstLoader extends ClassLoader {
		ChildFirstLoader(ClassLoader parent) {
			super(parent);
		}

		@Override
		protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			synchronized (getClassLoadingLock(name)) {
				if (!name.startsWith(CHILD_PACKAGE)) {
					return super.loadClass(name, resolve);
				}
				Class<?> c = findLoadedClass(name);
				if (c == null) {
					try (InputStream is = getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
						byte[] b = is.readAllBytes();
						c = defineClass(name, b, 0, b.length);
					} catch (IOException e) {
						throw new ClassNotFoundException(name, e);
					}
				}
				if (resolve) {
					resolveClass(c);
				}
				return c;
			}
		}
	}

	private Object newProxy(Class<?> superclass, Class<?> iface, PamelaMethodHandler handler) throws Exception {
		return backend.getProxyClass(superclass, new Class<?>[] { iface }, ALL).newInstance(new Class<?>[0], new Object[0], handler);
	}

	// ---------------------------------------------------------------------------------------------------------------
	// Packages and class loaders
	// ---------------------------------------------------------------------------------------------------------------

	@Test
	public void testImplementationInThePackageOfTheInterface() throws Exception {
		StoringHandler handler = new StoringHandler();
		Item item = (Item) newProxy(ItemImpl.class, Item.class, handler);
		item.setName("foo");
		assertEquals("foo", item.getName());
		assertEquals("Item foo", item.describe());
		assertSame(handler, PamelaProxies.methodHandlerOf(item));
		assertTrue(PamelaProxies.isProxy(item));
		// PamelaMetaModel.getUpperEntities reads the interfaces of the proxy class: the entity interface comes first
		assertSame(Item.class, item.getClass().getInterfaces()[0]);
	}

	@Test
	public void testNoImplementationClass() throws Exception {
		Item item = (Item) newProxy(Object.class, Item.class, new StoringHandler());
		assertSame(item, item.init("bar"));
		assertEquals("bar", item.getName());
	}

	@Test
	public void testImplementationInAnotherPackage() throws Exception {
		Item item = (Item) newProxy(OtherItemImpl.class, Item.class, new StoringHandler());
		item.setName("foo");
		assertEquals("Other foo", item.describe());
		assertTrue(OtherItemImpl.class.isAssignableFrom(item.getClass()));
	}

	@Test
	public void testInterfaceInAChildClassLoader() throws Exception {
		ChildFirstLoader child = new ChildFirstLoader(getClass().getClassLoader());
		Class<?> childItem = child.loadClass(CHILD_PACKAGE + "ChildItem");
		Object item = newProxy(BaseObject.class, childItem, new StoringHandler());
		assertSame(child, item.getClass().getClassLoader());
		childItem.getMethod("setName", String.class).invoke(item, "foo");
		assertEquals("foo", childItem.getMethod("getName").invoke(item));
		assertEquals("base", childItem.getMethod("base").invoke(item));
	}

	@Test
	public void testImplementationInAChildClassLoader() throws Exception {
		ChildFirstLoader child = new ChildFirstLoader(getClass().getClassLoader());
		Class<?> childImpl = child.loadClass(CHILD_PACKAGE + "ChildItemImpl");
		Item item = (Item) newProxy(childImpl, Item.class, new StoringHandler());
		if (backend instanceof JavassistProxyBackend) {
			// PAMELA-D-4: the intermediate class of the javassist work-around extends the copy of the implementation class found by name
			// in the default ClassPool, not the one of the child loader
			assertTrue(!childImpl.isAssignableFrom(item.getClass()));
			return;
		}
		assertTrue(childImpl.isAssignableFrom(item.getClass()));
		item.setName("foo");
		assertEquals("Item foo", item.describe());
		assertEquals("child", item.label());
	}

	// ---------------------------------------------------------------------------------------------------------------
	// Semantics PAMELA relies on
	// ---------------------------------------------------------------------------------------------------------------

	@Test
	public void testProceed() throws Exception {
		StoringHandler handler = new StoringHandler();
		Item item = (Item) newProxy(ItemImpl.class, Item.class, handler);
		item.setName("foo");
		// StoringHandler, in another package than the proxy, invokes proceed without setAccessible
		assertEquals("Item foo", item.describe());
		assertNotNull(handler.proceeds.get("describe"));
		assertNull(handler.proceeds.get("getName"));
	}

	@Test
	public void testConstructorRunsBeforeTheHandlerIsInstalled() throws Exception {
		ItemImpl item = (ItemImpl) newProxy(ItemImpl.class, Item.class, new StoringHandler());
		assertEquals("label", item.labelAtConstruction);
	}

	@Test
	public void testCheckedExceptionGoesThrough() throws Exception {
		Item item = (Item) newProxy(ItemImpl.class, Item.class, new StoringHandler());
		try {
			item.fail();
			fail("IOException expected");
		} catch (IOException e) {
			assertEquals("expected", e.getMessage());
		}
	}

	@Test
	public void testConcurrentGenerationGivesOneClass() throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(8);
		try {
			List<Callable<Class<?>>> tasks = new ArrayList<>();
			for (int i = 0; i < 32; i++) {
				tasks.add(() -> newProxy(OtherItemImpl.class, Item.class, new StoringHandler()).getClass());
			}
			List<Class<?>> classes = new ArrayList<>();
			for (Future<Class<?>> f : executor.invokeAll(tasks)) {
				classes.add(f.get());
			}
			assertEquals(1, classes.stream().distinct().count());
		} finally {
			executor.shutdown();
		}
	}

	// ---------------------------------------------------------------------------------------------------------------
	// Parity with javassist: the declaration given to the handler, for every method
	// ---------------------------------------------------------------------------------------------------------------

	/**
	 * Invoke every public method of supplied proxy, and return, for each, the declaration given to the handler
	 */
	static Map<String, String> givenDeclarations(ProxyBackend backend, Class<?> superclass, Class<?> iface) throws Exception {
		Map<String, String> given = new TreeMap<>();
		String[] current = new String[1];
		Thread testThread = Thread.currentThread();
		PamelaMethodHandler handler = (self, method, proceed, args) -> {
			// javassist also intercepts finalize(), called by the garbage collector on another thread
			if (Thread.currentThread() != testThread) {
				return null;
			}
			given.put(current[0], method.getDeclaringClass().getSimpleName() + "." + method.getName() + Arrays.toString(method.getParameterTypes()));
			return Defaults.defaultValue(method.getReturnType());
		};
		Object proxy = backend.getProxyClass(superclass, new Class<?>[] { iface }, ALL).newInstance(new Class<?>[0], new Object[0], handler);
		for (Method m : proxy.getClass().getMethods()) {
			if (m.getDeclaringClass() == Object.class || Modifier.isStatic(m.getModifiers()) || m.getName().startsWith("pamela$")
					|| m.getName().contains("$accessor$") || m.getName().startsWith("_d") || m.getName().equals("getHandler")
					|| m.getName().equals("setHandler")) {
				continue;
			}
			current[0] = m.getName() + Arrays.toString(m.getParameterTypes()) + ":" + m.getReturnType().getSimpleName();
			Object[] args = new Object[m.getParameterCount()];
			for (int i = 0; i < args.length; i++) {
				args[i] = Defaults.defaultValue(m.getParameterTypes()[i]);
			}
			try {
				m.invoke(proxy, args);
			} catch (InvocationTargetException e) {
				given.put(current[0], "threw " + e.getTargetException());
			}
		}
		return given;
	}

	@Test
	public void testSameDeclarationsAsJavassist() throws Exception {
		if (backend instanceof JavassistProxyBackend) {
			return;
		}
		Class<?>[][] cases = { { FlexoProcessImpl.class, FlexoProcess.class }, { Object.class, ActivityNode.class },
				{ Object.class, TestContainerA.class }, { ItemImpl.class, Item.class }, { OtherItemImpl.class, Item.class },
				{ Object.class, Item.class } };
		for (Class<?>[] c : cases) {
			Map<String, String> expected = givenDeclarations(new JavassistProxyBackend(), c[0], c[1]);
			Map<String, String> actual = givenDeclarations(backend, c[0], c[1]);
			assertTrue("nothing tested for " + Arrays.toString(c), expected.size() > 3);
			Map<String, String> common = new TreeMap<>(actual);
			common.keySet().retainAll(expected.keySet());
			assertEquals("declarations given to the handler for " + Arrays.toString(c), expected, common);
			// Byte Buddy also declares the signatures of a generic interface resolved with its type arguments
			// (setEmbedded(TestEmbeddedA) for TestContainer<TestEmbeddedA>): they give the declaration of the erased method
			for (Map.Entry<String, String> e : actual.entrySet()) {
				if (!expected.containsKey(e.getKey())) {
					assertTrue(e.getKey() + " gives " + e.getValue(), expected.containsValue(e.getValue()));
				}
			}
		}
	}
}
