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

import java.util.List;

import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.model.PAMELAVisitor;
import org.openflexo.pamela.model.PAMELAVisitor.VisitingStrategy;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Interface that PAMELA objects should extend in order to benefit from their default implementation handled by the
 * {@link ProxyMethodHandler}.<br>
 * All methods starting with 'performSuper' are method-accessors allowing implementing classes to call the default behaviour. These methods
 * should be considered as <code>protected</code> instead of <code>public</code> (but Java interfaces does not allow that). Therefore, these
 * method should never be invoked externally, ie, by a class which is not implementing this interface.
 * 
 * @author Guillaume
 * 
 */
public interface AccessibleProxyObject extends HasPropertyChangeSupport, KeyValueCoding {

	/**
	 * Invokes the getter for the property with the given <code>propertyIdentifier</code>.
	 * 
	 * @param propertyIdentifier
	 *            the identifier of the property
	 * @return the value of the getter of the property identified by <code>propertyIdentifier</code>.
	 */
	public Object performSuperGetter(String propertyIdentifier);

	/**
	 * Invokes the setter for the property with the given <code>propertyIdentifier</code>.
	 * 
	 * @param propertyIdentifier
	 *            the identifier of the property
	 */
	public void performSuperSetter(String propertyIdentifier, Object value);

	/**
	 * Invokes the adder for the property with the given <code>propertyIdentifier</code>.
	 * 
	 * @param propertyIdentifier
	 *            the identifier of the property
	 * @param value
	 *            the value to add
	 */
	public void performSuperAdder(String propertyIdentifier, Object value);

	/**
	 * Invokes the adder for the property with the given <code>propertyIdentifier</code>. Object will be inserted at supplied index
	 * 
	 * @param propertyIdentifier
	 *            the identifier of the property
	 * @param value
	 *            the value to add
	 * @param index
	 *            index where to add the object
	 */
	public void performSuperAdder(String propertyIdentifier, Object value, int index);

	/**
	 * Invokes the remover for the property with the given <code>propertyIdentifier</code>.
	 * 
	 * @param propertyIdentifier
	 *            the identifier of the property
	 * @param value
	 *            the value to remove
	 */
	public void performSuperRemover(String propertyIdentifier, Object value);

	/**
	 * Returns the super getter as defined by the model entity associated with the class <code>modelEntityInterface</code>. This method is
	 * useful only in the case of conflicting multiply-inherited model properties.
	 * 
	 * @param propertyIdentifier
	 *            the identifier of the property
	 * @param modelEntityInterface
	 *            the class corresponding to the model entity on which the model property should be looked up.
	 * @return the getter value as defined by the model entity associated with the class <code>modelEntityInterface</code>.
	 * @see AccessibleProxyObject#performSuperGetter(String)
	 */
	public Object performSuperGetter(String propertyIdentifier, Class<?> modelEntityInterface);

	/**
	 * Invokes the super setter as defined by the model entity associated with the class <code>modelEntityInterface</code>. This method is
	 * useful only in the case of conflicting multiply-inherited model properties.
	 * 
	 * @param propertyIdentifier
	 *            the identifier of the property
	 * @param modelEntityInterface
	 *            the class corresponding to the model entity on which the model property should be looked up.
	 * @param value
	 *            the value to set
	 * @see AccessibleProxyObject#performSuperSetter(String, Object)
	 */
	public void performSuperSetter(String propertyIdentifier, Object value, Class<?> modelEntityInterface);

	/**
	 * Invokes the super adder as defined by the model entity associated with the class <code>modelEntityInterface</code>. This method is
	 * useful only in the case of conflicting multiply-inherited model properties.
	 * 
	 * @param propertyIdentifier
	 *            the identifier of the property
	 * @param modelEntityInterface
	 *            the class corresponding to the model entity on which the model property should be looked up.
	 * @param value
	 *            the value to add
	 * @see AccessibleProxyObject#performSuperAdder(String, Object)
	 */
	public void performSuperAdder(String propertyIdentifier, Object value, Class<?> modelEntityInterface);

	/**
	 * Invokes the super adder as defined by the model entity associated with the class <code>modelEntityInterface</code>. This method is
	 * useful only in the case of conflicting multiply-inherited model properties.
	 * 
	 * @param propertyIdentifier
	 *            the identifier of the property
	 * @param modelEntityInterface
	 *            the class corresponding to the model entity on which the model property should be looked up.
	 * @param value
	 *            the value to remove
	 * @see AccessibleProxyObject#performSuperRemover(String, Object)
	 */
	public void performSuperRemover(String propertyIdentifier, Object value, Class<?> modelEntityInterface);

	/**
	 * Invokes the default <code>setModified</code> code handled by {@link ProxyMethodHandler}
	 * 
	 * @param modified
	 *            the value to set on the <code>modified</code> flag.
	 */
	public void performSuperSetModified(boolean modified);

	/**
	 * Invokes the default code for the finder identified by the given <code>finderIdentifier</code> for the provided <code>value</code>.
	 * 
	 * @param finderIdentifier
	 *            the identifier of the finder to run
	 * @param value
	 *            the value for which to look
	 * @return the object or list of objects found.
	 */
	public Object performSuperFinder(String finderIdentifier, Object value);

	/**
	 * Invokes the default code for the finder identified by the given <code>finderIdentifier</code> for the provided <code>value</code> as
	 * it has been declared on the model entity identified by the provided <code>modelEntityInterface</code>.
	 * 
	 * @param finderIdentifier
	 *            the identifier of the finder to run
	 * @param value
	 *            the value for which to look
	 * @param modelEntityInterface
	 *            the class corresponding to the model entity from which the finder information should be gathered.
	 * @return the object or list of objects found.
	 */
	public Object performSuperFinder(String finderIdentifier, Object value, Class<?> modelEntityInterface);

	/**
	 * Returns true if this object is currently being serialized
	 * 
	 * @return true if it is being serialized
	 */
	public boolean isSerializing();

	/**
	 * Returns true if this object is currently being deserialized
	 * 
	 * @return true if it being deserialized
	 */
	public boolean isDeserializing();

	/**
	 * Returns true if this object has been modified since:
	 * <ul>
	 * <li>it has been instantiated for the first time, or</li>
	 * <li>it has been serialized, or</li>
	 * <li>it has been deserialized.</li>
	 * </ul>
	 * 
	 * @return true if this object has been modified
	 */
	public boolean isModified();

	/**
	 * Sets the value of the <code>modified</code> flag.
	 * 
	 * @param modified
	 *            the value to set
	 */
	public void setModified(boolean modified);

	/**
	 * Return whether supplied object is equals to this, regarding persistant properties defined as PAMELA model
	 * 
	 * @param obj
	 *            object to compare with, which should be of same type (otherwise return false)
	 * @return
	 */
	public boolean equalsObject(Object obj);

	/**
	 * Called to update current object while comparing it to opposite object, (which must be of right type!), examining each property
	 * values.<br>
	 * Collections are handled while trying to match updated objects with a given strategy<br>
	 * Perform required changes on this object so that at the end of the call, equalsObject(object) should return true<br>
	 * Also perform required notifications, so that it is safe to call that method in a deployed environment
	 * 
	 * @param obj
	 *            object to update with, which must be of same type
	 */
	public void updateWith(Object obj);

	/**
	 * Destroy current object<br>
	 * After invoking this, the object won't be accessible.<br>
	 * To implements deleting/undeleting facilities, use {@link DeletableProxyObject} interface instead
	 */
	public void destroy();

	/**
	 * Called to be visited by a {@link PAMELAVisitor}
	 * 
	 * Default strategy is Embedding
	 * 
	 * @param visitor
	 */
	public void accept(PAMELAVisitor visitor);

	/**
	 * Called to be visited by a {@link PAMELAVisitor}
	 * 
	 * @param visitor
	 * @param strategy
	 */
	public void accept(PAMELAVisitor visitor, VisitingStrategy strategy);

	/**
	 * Return the list of all objects beeing directely referenced as embedded in this object
	 * 
	 * @return
	 */
	public List<? extends AccessibleProxyObject> getEmbeddedObjects();

	/**
	 * Return the list of all objects beeing directely referenced in this object
	 * 
	 * @return
	 */
	public List<? extends AccessibleProxyObject> getReferencedObjects();
}
