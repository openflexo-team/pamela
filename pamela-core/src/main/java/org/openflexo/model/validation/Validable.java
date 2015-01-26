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

package org.openflexo.model.validation;

import java.util.Collection;

/**
 * Implemented by an object on which validation is available<br>
 * This API is really minimal since only embedding support is required to iterate over a collection of {@link Validable} objects
 * 
 * @author sylvain
 * 
 */
public interface Validable {

	/**
	 * Return default validation model for this object
	 * 
	 * @return ValidationModel
	 */
	// public ValidationModel getDefaultValidationModel();

	/**
	 * Returns a flag indicating if this object is valid according to default validation model
	 * 
	 * @return boolean
	 */
	// public boolean isValid();

	/**
	 * Returns a flag indicating if this object is valid according to specified validation model
	 * 
	 * @return boolean
	 */
	// public boolean isValid(ValidationModel validationModel);

	/**
	 * Validates this object by building new ValidationReport object Default validation model is used to perform this validation.
	 */
	// public ValidationReport validate();

	/**
	 * Validates this object by building new ValidationReport object Supplied validation model is used to perform this validation.
	 */
	// public ValidationReport validate(ValidationModel validationModel);

	/**
	 * Validates this object by appending eventual issues to supplied ValidationReport. Default validation model is used to perform this
	 * validation.
	 * 
	 * @param report
	 *            , a ValidationReport object on which found issues are appened
	 */
	// public void validate(ValidationReport report);

	/**
	 * Validates this object by appending eventual issues to supplied ValidationReport. Supplied validation model is used to perform this
	 * validation.
	 * 
	 * @param report
	 *            , a ValidationReport object on which found issues are appened
	 */
	// public void validate(ValidationReport report, ValidationModel validationModel);

	/**
	 * Return an collection of all embedded objects on which the validation is to be performed
	 * 
	 * @return a Vector of Validable objects
	 */
	public Collection<? extends Validable> getEmbeddedValidableObjects();

	/**
	 * Return by deep recursion (see {@link #getEmbeddedValidableObjects()} a collection containing all validable objects contained in this
	 * Validable object
	 * 
	 * @return
	 */
	// public Collection<? extends Validable> getAllEmbeddedValidableObjects();

	/**
	 * Return a flag indicating if this object was deleted
	 * 
	 * @return
	 */
	public boolean isDeleted();
}
