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

import org.openflexo.pamela.annotations.ComplexEmbedded;
import org.openflexo.pamela.annotations.Embedded;

/**
 * Interface that PAMELA objects should extend in order to benefit from their default implementation handled by the
 * {@link ProxyMethodHandler} deleting/undeleting facilities
 * 
 * @author sylvain
 * 
 */
public interface DeletableProxyObject {

	/**
	 * The deleted property identifier.
	 */
	public static final String DELETED = "deleted";

	/**
	 * Invokes the default deletion code handled by {@link ProxyMethodHandler}.
	 * 
	 * @see DeletableProxyObject#delete()
	 * @see DeletableProxyObject#isDeleted()
	 */
	// public boolean performSuperDelete();

	/**
	 * Invokes the default deletion code handled by {@link ProxyMethodHandler} with the provided <code>context</code>.
	 * 
	 * @param context
	 *            the deletion context. The context represents a list of objects which eventually will also be deleted. Objects in that
	 *            context may be deleted indirectly by deleting other objects, however, the invoker should make sure that those objects are
	 *            deleted by invoking one of the deletion methods.
	 * @see DeletableProxyObject#delete()
	 * @see DeletableProxyObject#isDeleted()
	 */
	public boolean performSuperDelete(Object... context);

	/**
	 * Invokes the default un-deletion code handled by {@link ProxyMethodHandler}.
	 * 
	 * @see DeletableProxyObject#delete()
	 * @see DeletableProxyObject#isDeleted()
	 */
	public boolean performSuperUndelete(boolean restoreProperties);

	/**
	 * Invokes the delete method as defined by the model entity associated with the class <code>modelEntityInterface</code>
	 * 
	 * @param modelEntityInterface
	 *            the class corresponding to the model entity from which deletion information should be gathered.
	 * @see DeletableProxyObject#delete()
	 * @see DeletableProxyObject#isDeleted()
	 */
	// public void performSuperDelete(Class<?> modelEntityInterface);

	/**
	 * Invokes the delete method as defined by the model entity associated with the class <code>modelEntityInterface</code>
	 * 
	 * @param modelEntityInterface
	 *            the class corresponding to the model entity from which deletion information should be gathered.
	 * @param context
	 *            the deletion context. The context represents a list of objects which eventually will also be deleted. Objects in that
	 *            context may be deleted indirectly by deleting other objects, however, the invoker should make sure that those objects are
	 *            deleted by invoking one of the deletion methods.
	 * @see DeletableProxyObject#delete(Object...)
	 * @see DeletableProxyObject#isDeleted()
	 */
	public void performSuperDelete(Class<?> modelEntityInterface, Object... context);

	/**
	 * Deletes the current object and all its embedded properties as defined by the {@link Embedded} and {@link ComplexEmbedded}
	 * annotations.
	 * 
	 * @see Embedded#deletionConditions()
	 * @see ComplexEmbedded#deletionConditions()
	 */
	// public boolean delete();

	/**
	 * Deletes the current object and all its embedded properties as defined by the {@link Embedded} and {@link ComplexEmbedded}
	 * annotations. Moreover, the provided <code>context</code> represents a list of objects that will also be eventually deleted and which
	 * should be taken into account when computing embedded objects according to the deletion conditions. Invoking this method may result in
	 * deleting indirectly the objects provided by the <code>context</code>, however the invoker should make sure that they have been
	 * actually deleted.
	 * 
	 * @param context
	 *            the list of objects that will also be deleted and which should be taken into account when computing embedded objects.
	 * @see Embedded#deletionConditions()
	 * @see ComplexEmbedded#deletionConditions()
	 */
	public boolean delete(Object... context);

	/**
	 * Un-deletes the current object
	 * 
	 * @see Embedded#deletionConditions()
	 * @see ComplexEmbedded#deletionConditions()
	 */
	public boolean undelete(boolean restoreProperties);

	/**
	 * Returns whether this object has been deleted or not.
	 * 
	 * @return true if this object has been deleted, false otherwise.
	 */
	public boolean isDeleted();

}
