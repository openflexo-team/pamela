/**
 * 
 * Copyright (c) 2014, Openflexo
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
import java.lang.reflect.Type;
import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.openflexo.connie.type.TypeUtils;

/**
 * Some utils for PAMELA
 * 
 * @author sylvain
 *
 */
public class PamelaUtils {

	/**
	 * Return boolean indicating if supplied methods are equivalent
	 * 
	 * @param method
	 * @param to
	 * @return
	 */
	public static boolean methodIsEquivalentTo(@Nonnull Method method, @Nullable Method to) {
		if (to == null) {
			return method == null;
		}
		return method.getName().equals(to.getName())/* && method.getReturnType().equals(to.getReturnType())*/
				&& Arrays.equals(method.getParameterTypes(), to.getParameterTypes());
	}

	/**
	 * Return boolean indicating if method m1 overrides method m2 in the supplied context
	 * 
	 * @param m1
	 * @param m2
	 * @param context
	 * @return
	 */
	public static boolean methodOverrides(@Nonnull Method m1, @Nonnull Method m2, @Nonnull Type context) {

		if (!m2.getDeclaringClass().isAssignableFrom(m1.getDeclaringClass())) {
			return false;
		}

		if (!m1.getName().equals(m2.getName())) {
			return false;
		}
		if (m1.getGenericParameterTypes().length != m2.getGenericParameterTypes().length) {
			return false;
		}
		// names and number of arguments are same

		Type[] m1Types = new Type[m1.getGenericParameterTypes().length];
		Type[] m2Types = new Type[m2.getGenericParameterTypes().length];
		for (int i = 0; i < m1.getGenericParameterTypes().length; i++) {
			m1Types[i] = TypeUtils.makeInstantiatedType(m1.getGenericParameterTypes()[i], context);
			m2Types[i] = TypeUtils.makeInstantiatedType(m2.getGenericParameterTypes()[i], context);
		}

		return Arrays.equals(m1Types, m2Types);
	}

	public static String getSignature(Method method, Type declaringType, boolean fullyQualified) {
		StringBuffer signature = new StringBuffer();
		signature.append(method.getName());
		signature.append("(");
		signature.append(getParameterListAsString(method, declaringType, fullyQualified));
		signature.append(")");
		return signature.toString();
	}

	private static String getParameterListAsString(Method method, Type declaringType, boolean fullyQualified) {
		StringBuilder returned = new StringBuilder();
		boolean isFirst = true;
		for (Type p : method.getGenericParameterTypes()) {
			Type contextualParamType = TypeUtils.makeInstantiatedType(p, declaringType);
			returned.append((isFirst ? "" : ",") + (fullyQualified ? TypeUtils.fullQualifiedRepresentation(contextualParamType)
					: TypeUtils.simpleRepresentation(contextualParamType)));
			isFirst = false;
		}
		return returned.toString();
	}

}
