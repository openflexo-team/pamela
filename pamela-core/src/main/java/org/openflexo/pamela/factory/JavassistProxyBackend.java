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

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

/**
 * Generation of PAMELA proxies with javassist.<br>
 * 
 * This class, {@link PamelaModelFactory} and {@link DelegateImplementation} are the only ones referring to javassist, which is to be
 * replaced (PAMELA-F-1).
 * 
 * @author sylvain
 * 
 */
final class JavassistProxyBackend {

	private JavassistProxyBackend() {
	}

	/**
	 * Instantiate the proxy class built by supplied factory, and install supplied handler on the new instance
	 */
	static Object create(ProxyFactory proxyFactory, Class<?>[] paramTypes, Object[] args, PamelaMethodHandler handler)
			throws NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException,
			InvocationTargetException {
		return proxyFactory.create(paramTypes, args, new HandlerAdapter(handler));
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
