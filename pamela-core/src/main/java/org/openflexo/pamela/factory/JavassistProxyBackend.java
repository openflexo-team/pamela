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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

/**
 * Generation of PAMELA proxies with javassist, to be removed with PAMELA-F-1.
 * 
 * @author sylvain
 * 
 */
final class JavassistProxyBackend implements ProxyBackend {

	// Stores on-the-fly generated classes to proxy the targeted implementation
	// class, but in the right package
	private static final Map<Class<?>, Map<Class<?>, Class<?>>> implementationProxyClasses = new HashMap<>();

	@Override
	public String getName() {
		return "javassist";
	}

	@Override
	public ProxyClass getProxyClass(Class<?> superclass, Class<?>[] interfaces, Predicate<Method> filter) {
		ProxyFactory proxyFactory = new ProxyFactory();
		Class<?> proxySuperclass = superclass;
		// Java 11 security issue
		// If the base implementation class is not in the same package than the
		// implemented interface, it fails
		// The workaround is to generate (or reuse) on the fly a proxy super classes (in
		// the right package !)
		if (interfaces.length > 0 && !interfaces[0].getPackage().equals(superclass.getPackage())) {
			proxySuperclass = retrieveProxyImplementationClass(interfaces[0], superclass);
		}
		proxyFactory.setSuperclass(proxySuperclass);
		if (interfaces.length > 0) {
			proxyFactory.setInterfaces(interfaces);
		}
		if (filter != null) {
			proxyFactory.setFilter(filter::test);
		}
		return (paramTypes, args, handler) -> proxyFactory.create(paramTypes, args, new HandlerAdapter(handler));
	}

	/**
	 * Generate (return when already existant) a base implementation class proxying the declared base implementation class, but in the
	 * right package (the same as the implemented interface)
	 * 
	 * @param implementedInterface
	 * @param superClass
	 * @return
	 */
	private static Class<?> retrieveProxyImplementationClass(Class<?> implementedInterface, Class<?> superClass) {

		String packageName = implementedInterface.getPackageName();

		synchronized (implementationProxyClasses) {
			Map<Class<?>, Class<?>> map = implementationProxyClasses.get(superClass);
			if (map == null) {
				map = new HashMap<>();
				implementationProxyClasses.put(superClass, map);
			}

			Class<?> returned = map.get(implementedInterface);
			if (returned == null) {
				ClassPool pool = ClassPool.getDefault();
				CtClass ctClass = pool
						.makeClass(packageName + "." + superClass.getSimpleName() + "_" + implementedInterface.getSimpleName() + "_" + "Proxy");
				try {
					ctClass.setSuperclass(pool.get(superClass.getName()));
				} catch (CannotCompileException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					returned = ctClass.toClass(implementedInterface);
				} catch (CannotCompileException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				map.put(implementedInterface, returned);
			}
			return returned;
		}
	}

	static boolean isProxy(Object object) {
		return object instanceof ProxyObject;
	}

	static PamelaMethodHandler handlerOf(Object object) {
		if (object instanceof ProxyObject) {
			MethodHandler handler = ((ProxyObject) object).getHandler();
			if (handler instanceof HandlerAdapter) {
				return ((HandlerAdapter) handler).handler;
			}
		}
		return null;
	}

	/**
	 * The javassist {@link MethodHandler} installed on a proxy, forwarding to its {@link PamelaMethodHandler}
	 */
	private static final class HandlerAdapter implements MethodHandler {

		private final PamelaMethodHandler handler;

		private HandlerAdapter(PamelaMethodHandler handler) {
			this.handler = handler;
		}

		@Override
		public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable {
			return handler.invoke(self, method, proceed, args);
		}
	}

}
