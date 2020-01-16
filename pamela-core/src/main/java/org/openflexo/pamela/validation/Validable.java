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

package org.openflexo.pamela.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implemented by an object on which validation is available<br>
 * This API is really minimal since only embedding support is required to iterate over a collection of {@link Validable} objects
 * 
 * @author sylvain
 * 
 */
public interface Validable {

	/**
	 * Return an collection of all embedded objects on which the validation is to be performed
	 * 
	 * @return a Vector of Validable objects
	 */
	public Collection<? extends Validable> getEmbeddedValidableObjects();

	/**
	 * Return a flag indicating if this object was deleted
	 * 
	 * @return
	 */
	public boolean isDeleted();

	/**
	 * Return the count of all objects that will be validated, given a root {@link Validable} object<br>
	 * Deep exploration will be performed using {@link #getEmbeddedValidableObjects()} links
	 * 
	 * @param o
	 *            root object from where starts the exploration
	 * @return
	 */
	public static int countAllEmbeddedValidableObjects(Validable o) {
		return retrieveAllEmbeddedValidableObjects(o).size();
	}

	/**
	 * Return a collection containing all objects that will be validated, given a root {@link Validable} object<br>
	 * Deep exploration will be performed using {@link #getEmbeddedValidableObjects()} links
	 * 
	 * @param o
	 *            root object from where starts the exploration
	 * @return
	 */
	public static Collection<? extends Validable> retrieveAllEmbeddedValidableObjects(Validable o) {
		List<Validable> returned = new ArrayList<>();
		appendAllEmbeddedValidableObjects(o, returned);
		return returned;
	}

	public static void appendAllEmbeddedValidableObjects(Validable o, Collection<Validable> c) {
		if (o != null && !c.contains(o)) {
			c.add(o);
			Collection<? extends Validable> embeddedObjects = o.getEmbeddedValidableObjects();
			if (embeddedObjects != null) {
				for (Validable o2 : embeddedObjects) {
					appendAllEmbeddedValidableObjects(o2, c);
				}
			}
		}
	}

}
