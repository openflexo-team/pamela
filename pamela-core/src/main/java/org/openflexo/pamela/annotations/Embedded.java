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

package org.openflexo.pamela.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
/**
 * This annotation is associated with a model property. It defines whether a model property should be considered as embedded or not. It also allows
 * to define a set of properties that must be present in the context, in order to consider this model property value as 'embedded'.
 * @author Guillaume
 *
 */
public @interface Embedded {
	/**
	 * The list of model properties that must be included in order to consider this property as embedded. <br/>
	 * <br/>
	 * Let's take the simple example of an entity 'Node' and an entity 'Edge'.<br/>
	 * 
	 * 'Node' has two properties:
	 * <ul>
	 * <li>'incomingEdge' of the type 'Edge'</li>
	 * <li>'outgoingEdge' of the type 'Edge'</li>
	 * </ul>
	 * 'Edge has two properties:
	 * <ul>
	 * <li>'startNode' of the type 'Node'</li>
	 * <li>'endNode' of the type 'Node'</li>
	 * </ul>
	 * 
	 * When cloning a 'Node', we expect that we also clone the 'incomingEdge' only if its 'startNode' is also embedded. In order to indicate
	 * this to PAMELA, we will add a closureCondition: 'startNode'.
	 * 
	 * @return the list of model properties of the corresponding model property value that must be embedded in order to consider this model
	 *         property to be embedded.
	 */
    String[] closureConditions() default {};

	/**
	 * The list of model properties that must be included in order to also delete this property. <br/>
	 * <br/>
	 * Let's take the simple example of an entity 'Node' and an entity 'Edge'.<br/>
	 * 
	 * 'Node' has two properties:
	 * <ul>
	 * <li>'incomingEdge' of the type 'Edge'</li>
	 * <li>'outgoingEdge' of the type 'Edge'</li>
	 * </ul>
	 * 'Edge has two properties:
	 * <ul>
	 * <li>'startNode' of the type 'Node'</li>
	 * <li>'endNode' of the type 'Node'</li>
	 * </ul>
	 * 
	 * When deleting a 'Node', we could say that we allow an edge to live without a start or end node, but that it must be attached to at
	 * least one of them. If both 'startNode' and 'endNode' are deleted, then we want the edge to also be deleted. In order to indicate this
	 * to PAMELA, we will add the deletionCondition: 'startNode'.
	 * 
	 * @return the list of model properties of the corresponding model property value that must be embedded in order to consider this model
	 *         property to be deleted.
	 */
    String[] deletionConditions() default {};
}
