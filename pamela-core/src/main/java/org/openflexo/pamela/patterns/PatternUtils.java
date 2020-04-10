package org.openflexo.pamela.patterns;

import java.util.ArrayList;

/**
 * Library of static methods useful for the {@link PatternContext}
 *
 * @author C. SILVA
 */
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
