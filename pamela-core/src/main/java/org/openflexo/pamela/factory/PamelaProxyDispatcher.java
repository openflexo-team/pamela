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

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperMethod;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * The entry point of a method intercepted by a proxy of the Byte Buddy backend ({@link ByteBuddyProxyBackend}): one instance per
 * intercepted method, holding the declaration given to the handler.<br>
 * 
 * Internal to PAMELA, public only because it is called from the generated classes, which live in the packages of the entities.
 * 
 * @author sylvain
 * 
 */
public final class PamelaProxyDispatcher {

	static final String HANDLER_FIELD = "pamela$handler";

	// The method given to the handler
	private final Method method;

	PamelaProxyDispatcher(Method method) {
		this.method = method;
	}

	@RuntimeType
	public Object intercept(@This Object self, @SuperMethod(nullIfImpossible = true) Method proceed, @AllArguments Object[] args,
			@FieldValue(HANDLER_FIELD) PamelaMethodHandler handler) throws Throwable {
		if (handler == null) {
			// The constructor is running: same rule as the default handler of javassist in PAMELA 1.x
			if (proceed == null) {
				throw new IllegalStateException(method + " invoked by a constructor, before the PAMELA handler is installed");
			}
			return proceed.invoke(self, args);
		}
		return handler.invoke(self, method, proceed, args);
	}

}
