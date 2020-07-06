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

package org.openflexo.pamela;

import org.openflexo.pamela.factory.ProxyMethodHandler;

/**
 * Interface that PAMELA objects should extend in order to benefit from their default implementation handled by the
 * {@link ProxyMethodHandler} cloning facilities
 * 
 * @author sylvain
 * 
 */
public interface CloneableProxyObject {

	/**
	 * Clone current object, using meta informations provided by related class<br>
	 * All property should be annotated with a @CloningStrategy annotation which determine the way of handling this property Don't compute
	 * any closure, and clone all required objects
	 * 
	 * @return newly created clone object
	 */
	public Object cloneObject();

	/**
	 * Clone current object, using meta informations provided by related class<br>
	 * All property should be annotated with a @CloningStrategy annotation which determine the way of handling this property Supplied
	 * context is used to determine the closure of objects graph being constructed during this operation. If a property is marked as
	 * @CloningStrategy.CLONE but lead to an object outside scope of cloning (the closure being computed), then resulting value is
	 * nullified. When context is not set, don't compute any closure, and clone all required objects
	 * 
	 * @param context
	 * @return newly created clone object
	 */
	public Object cloneObject(Object... context);

	/**
	 * Returns true when <code>this</code> object is currently being created based on another object by using the "cloning" technique.
	 * 
	 * @return true if this object is currently being creating by cloning
	 */
	public boolean isCreatedByCloning();

	/**
	 * Returns true when <code>this</code> object is currently being cloned to create another object based on <code>this</code> object
	 * 
	 * @return true when <code>this</code> object is currently being cloned to create another object based on <code>this</code> object
	 */
	public boolean isBeingCloned();

}
