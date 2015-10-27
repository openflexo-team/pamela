/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface Finder {

	String NO_RECURSION = "---";
	String DEFAULT_VALUE = "name";
	String DEFAULT_NAME = "name";

	String name() default DEFAULT_NAME;

	/**
	 * The name of the property on which the finder should iterate to find the object
	 * 
	 * @return
	 */
    String collection();

	/**
	 * The attribute which should match the finder argument
	 * 
	 * @return
	 */
    String attribute() default DEFAULT_VALUE;

	/**
	 * Wheter this finder returns a single object or several
	 * 
	 * @return
	 */
    boolean isMultiValued() default false;

	/**
	 * Whether this finder should try to perform recursive search. If yes, value should be a dot ('.') separated path to get from one object
	 * to the corresponding collection.
	 * 
	 * @return
	 */
    String recursion() default NO_RECURSION;

	/**
	 * In case of recursion, this flag indicates to search first in the immediate collection and then to go in depth by "recursing".
	 * 
	 * @return
	 */
    boolean iterateFirstRecurseThen() default true;
}
