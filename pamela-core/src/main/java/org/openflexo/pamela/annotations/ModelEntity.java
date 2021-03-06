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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openflexo.pamela.exceptions.ModelExecutionException;

/**
 * Used on a class or interface to indicate that this class/interface represents a {@link org.openflexo.pamela.model.ModelEntity}
 * 
 * A {@link org.openflexo.pamela.model.ModelEntity} may be abstract (semantics is here the same as in plain java: an abstract entity cannot
 * be instantiated)
 * 
 * @author sylvain
 *
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface ModelEntity {
	/**
	 * Defines the different initialization policy of the model entity.
	 * <ul>
	 * <li>NONE: It is not mandatory to invoke any initializer although there are some declared on the entity</li>
	 * <li>WARN_IF_NOT_INVOKED: It is not mandatory to invoke any initializer although there are some declared on the entity, but a warning
	 * will be output if a new instance of the entity is used in the model</li>
	 * <li>REQUIRED: It is mandatory to invoke an initializer of the entity if there is at least on initializer defined in the entity
	 * hierarchy. Using an instance of this entity without having initialized it will throw a ModelExcecutionException @see
	 * {@link ModelExecutionException}</li>
	 * 
	 * @author Guillaume
	 * 
	 */
	enum InitPolicy {

		NONE, WARN_IF_NOT_INVOKED, REQUIRED
	}

	boolean isAbstract() default false;

	boolean inheritInitializers() default false;

	InitPolicy initPolicy() default InitPolicy.REQUIRED;
}
