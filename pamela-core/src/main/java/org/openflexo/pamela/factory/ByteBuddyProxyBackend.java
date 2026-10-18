/**
 * 
 * Copyright (c) 2026, Openflexo
 * 
 * This file is part of Pamela-core, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.pamela.factory;

import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.TypeCache;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;

/**
 * Generation of PAMELA proxies with Byte Buddy (PAMELA-F-1).<br>
 * 
 * A proxy class extends the implementation class, implements the entity interface and {@link PamelaProxy}, and forwards every handled
 * method to the {@link PamelaMethodHandler} held in a field, with the contract of the javassist proxies of PAMELA 1.x (self, method, proceed, args). It is defined
 * through a {@link java.lang.invoke.MethodHandles.Lookup} in the package and the class loader of a "neighbor": the implementation class
 * when its loader sees the interfaces, the entity interface otherwise (always when there is no implementation class), so that no
 * intermediate class is needed whatever the packages and the class loaders of the two.
 * 
 * @author sylvain
 * 
 */
final class ByteBuddyProxyBackend implements ProxyBackend {

	static final String HANDLER_FIELD = PamelaProxyDispatcher.HANDLER_FIELD;

	private static final AtomicInteger COUNTER = new AtomicInteger();

	// Key of the cache when there is no filter: the methods are only computed when the class is generated
	private static final Object ALL_METHODS = new Object();

	private final TypeCache<List<Object>> cache = new TypeCache<>(TypeCache.Sort.WEAK);

	@Override
	public String getName() {
		return "bytebuddy";
	}

	@Override
	public ProxyClass getProxyClass(Class<?> superclass, Class<?>[] interfaces, Predicate<Method> filter) {
		Class<?> neighbor = neighbor(superclass, interfaces);
		Map<String, Method> handled = filter != null ? handledMethods(superclass, interfaces, filter, neighbor.getPackageName()) : null;
		List<Object> key = Arrays.asList(superclass, Arrays.asList(interfaces), handled != null ? handled.keySet() : ALL_METHODS);
		Class<?> proxyClass = cache.findOrInsert(neighbor.getClassLoader(), key,
				() -> generate(superclass, interfaces,
						handled != null ? handled : handledMethods(superclass, interfaces, null, neighbor.getPackageName()), neighbor),
				cache);
		return new ByteBuddyProxyClass(proxyClass);
	}

	/**
	 * The class in whose package and class loader the proxy is defined
	 */
	private static Class<?> neighbor(Class<?> superclass, Class<?>[] interfaces) {
		if (!superclass.getName().startsWith("java.") && seesAll(superclass.getClassLoader(), interfaces)) {
			return superclass;
		}
		if (interfaces.length > 0 && sees(interfaces[0].getClassLoader(), superclass) && seesAll(interfaces[0].getClassLoader(), interfaces)) {
			return interfaces[0];
		}
		throw new IllegalArgumentException("No class loader sees both " + superclass + " and " + Arrays.toString(interfaces));
	}

	private static boolean seesAll(ClassLoader loader, Class<?>[] types) {
		for (Class<?> type : types) {
			if (!sees(loader, type)) {
				return false;
			}
		}
		return true;
	}

	private static boolean sees(ClassLoader loader, Class<?> type) {
		try {
			return Class.forName(type.getName(), false, loader) == type;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	/**
	 * The methods to intercept, by JVM signature (name and descriptor), each with the declaration given to the handler.<br>
	 * 
	 * Same choices as javassist in PAMELA 1.x, which PAMELA relies on (for instance {@link org.openflexo.pamela.model.ModelEntity#getInitializers(Method)}
	 * looks up the exact declaration), in two steps:
	 * <ul>
	 * <li>which methods: the interfaces are scanned first, super interfaces before sub interfaces, then the super class hierarchy; the last
	 * declaration met wins, unless it would reduce the visibility, and the filter is applied to that declaration
	 * ({@code ProxyFactory.getMethods})</li>
	 * <li>which declaration the handler receives: the first found in the super class hierarchy, searched from its root, each class before
	 * its interfaces, then in the interfaces of the proxy ({@code RuntimeSupport.findSuperClassMethod})</li>
	 * </ul>
	 */
	static Map<String, Method> handledMethods(Class<?> superclass, Class<?>[] interfaces, Predicate<Method> filter, String proxyPackage) {
		Map<String, Method> all = new HashMap<>();
		Set<Class<?>> visited = new HashSet<>();
		for (Class<?> i : interfaces) {
			collect(all, i, visited);
		}
		collect(all, superclass, visited);
		Map<String, Method> returned = new TreeMap<>();
		for (Map.Entry<String, Method> e : all.entrySet()) {
			Method m = e.getValue();
			int mod = m.getModifiers();
			if (Modifier.isFinal(mod) || Modifier.isStatic(mod) || m.getName().equals("finalize")) {
				continue;
			}
			if (!Modifier.isPublic(mod) && !Modifier.isProtected(mod) && !m.getDeclaringClass().getPackageName().equals(proxyPackage)) {
				continue;
			}
			if (filter == null || filter.test(m)) {
				String descriptor = e.getKey().substring(m.getName().length() + 1);
				Method given = findSuperMethod(superclass, m.getName(), descriptor);
				for (int i = 0; given == null && i < interfaces.length; i++) {
					given = findSuperMethod(interfaces[i], m.getName(), descriptor);
				}
				returned.put(e.getKey(), given != null ? given : m);
			}
		}
		return returned;
	}

	private static void collect(Map<String, Method> all, Class<?> type, Set<Class<?>> visited) {
		if (!visited.add(type)) {
			return;
		}
		for (Class<?> i : type.getInterfaces()) {
			collect(all, i, visited);
		}
		if (type.getSuperclass() != null) {
			collect(all, type.getSuperclass(), visited);
		}
		for (Method m : type.getDeclaredMethods()) {
			if (Modifier.isPrivate(m.getModifiers()) || m.isSynthetic()) {
				continue;
			}
			Method old = all.put(key(m), m);
			if (old != null && Modifier.isPublic(old.getModifiers()) && !Modifier.isPublic(m.getModifiers())) {
				all.put(key(m), old);
			}
		}
	}

	private static Method findSuperMethod(Class<?> type, String name, String descriptor) {
		for (Method m : type.getDeclaredMethods()) {
			if (m.getName().equals(name) && new MethodDescription.ForLoadedMethod(m).getDescriptor().equals(descriptor)) {
				return m;
			}
		}
		if (type.getSuperclass() != null) {
			Method m = findSuperMethod(type.getSuperclass(), name, descriptor);
			if (m != null) {
				return m;
			}
		}
		for (Class<?> i : type.getInterfaces()) {
			Method m = findSuperMethod(i, name, descriptor);
			if (m != null) {
				return m;
			}
		}
		return null;
	}

	private static String key(Method m) {
		return m.getName() + ':' + new MethodDescription.ForLoadedMethod(m).getDescriptor();
	}

	private static String key(MethodDescription md) {
		return md.getName() + ':' + md.getDescriptor();
	}

	private static Class<?> generate(Class<?> superclass, Class<?>[] interfaces, Map<String, Method> handled, Class<?> neighbor)
			throws IllegalAccessException {
		String simpleName = interfaces.length > 0 ? interfaces[0].getSimpleName() : superclass.getSimpleName();
		String name = neighbor.getPackageName() + "." + simpleName + "$$Pamela$" + COUNTER.incrementAndGet();
		Class<?>[] implemented = Arrays.copyOf(interfaces, interfaces.length + 1);
		implemented[interfaces.length] = PamelaProxy.class;
		DynamicType.Builder<?> builder = new ByteBuddy().subclass(superclass, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
				.name(name).implement(implemented).defineField(HANDLER_FIELD, PamelaMethodHandler.class, Visibility.PRIVATE)
				.method(isDeclaredBy(PamelaProxy.class)).intercept(FieldAccessor.ofField(HANDLER_FIELD));
		// One interception per method, each giving the handler the declaration javassist would give. A method of the proxy may be seen
		// either with its own signature or, when it implements a generic declaration, with the erased signature of that declaration.
		for (Map.Entry<String, Method> e : handled.entrySet()) {
			String k = e.getKey();
			builder = builder
					.method(md -> !md.getDeclaringType().represents(PamelaProxy.class) && (k.equals(key(md)) || k.equals(key(md.asDefined()))))
					.intercept(MethodDelegation.to(new PamelaProxyDispatcher(e.getValue()), PamelaProxyDispatcher.class));
		}
		return builder.make()
				.load(neighbor.getClassLoader(),
						ClassLoadingStrategy.UsingLookup.of(MethodHandles.privateLookupIn(neighbor, MethodHandles.lookup())))
				.getLoaded();
	}

	private static final class ByteBuddyProxyClass implements ProxyClass {
		private final Class<?> proxyClass;
		private volatile Constructor<?> defaultConstructor;

		private ByteBuddyProxyClass(Class<?> proxyClass) {
			this.proxyClass = proxyClass;
		}

		@Override
		public Object newInstance(Class<?>[] paramTypes, Object[] args, PamelaMethodHandler handler) throws NoSuchMethodException,
				IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
			Constructor<?> constructor;
			if (paramTypes.length == 0) {
				constructor = defaultConstructor;
				if (constructor == null) {
					defaultConstructor = constructor = proxyClass.getConstructor();
				}
			}
			else {
				constructor = proxyClass.getConstructor(paramTypes);
			}
			Object returned = constructor.newInstance(args);
			((PamelaProxy) returned).pamela$setHandler(handler);
			return returned;
		}
	}

}
