/**
 * 
 * Copyright (c) 2013-2020, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of pamela-core, a component of the software infrastructure 
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

package org.openflexo.pamela.patterns;

import java.util.ArrayList;

/**
 * Library of static methods useful for the {@link PatternContext}
 *
 * @author C. SILVA
 */
@Deprecated
public class PatternUtils {

	/**
	 * Search a class to extract its class tree.
	 * 
	 * @param baseClass
	 *            class to search
	 * @return a list containing all superclasses and interfaces of <code>baseClass</code>
	 */
	public static ArrayList<Class> getClassHierarchy(Class baseClass) {
		ArrayList<Class> returned = new ArrayList<>();
		Class currentClass = baseClass;
		while (currentClass != null) {
			if (!returned.contains(currentClass)) {
				returned.add(currentClass);
			}
			PatternUtils.searchInterfaces(currentClass, returned);
			currentClass = currentClass.getSuperclass();
		}
		return returned;
	}

	/**
	 * Search for interfaces implemented by the <code>baseClass</code> and adds them to <code>list</code>
	 * 
	 * @param baseClass
	 *            class to search
	 * @param list
	 *            list to complete with discovered interfaces
	 */
	private static void searchInterfaces(Class baseClass, ArrayList<Class> list) {
		for (Class interf : baseClass.getInterfaces()) {
			if (!list.contains(interf)) {
				list.add(interf);
			}
			searchInterfaces(interf, list);
		}
	}
}
