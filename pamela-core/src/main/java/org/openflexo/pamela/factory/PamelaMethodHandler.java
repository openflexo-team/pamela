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

import java.lang.reflect.Method;

/**
 * Receives every intercepted invocation on a PAMELA proxy.<br>
 * 
 * This is the PAMELA contract between a generated proxy class and the object handling its behaviour ({@link ProxyMethodHandler} for a
 * PAMELA instance, {@link DelegateImplementation} for a partial implementation), independent of the bytecode library generating the proxy.
 * 
 * @author sylvain
 * 
 */
public interface PamelaMethodHandler {

	/**
	 * Called when an intercepted method is invoked on a proxy
	 * 
	 * @param self
	 *            the proxy on which the method is invoked
	 * @param method
	 *            the invoked method, as declared by the implemented interface or by the implementation class
	 * @param proceed
	 *            a method invoking the implementation inherited from the super class, or null when that implementation is abstract
	 * @param args
	 *            the arguments of the invocation
	 * @return the value returned by the invocation
	 * @throws Throwable
	 *             any exception raised by the invocation
	 */
	public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable;

}
