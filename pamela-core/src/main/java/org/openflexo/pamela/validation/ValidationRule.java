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

import java.beans.PropertyChangeSupport;
import java.util.logging.Logger;

import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Represent a validation rule<br>
 * A validation rule is expressed as a Java class instantiated in a {@link ValidationModel}
 * 
 * @author sylvain
 * 
 */
public abstract class ValidationRule<R extends ValidationRule<R, V>, V extends Validable> implements HasPropertyChangeSupport {

	public static final String DELETED_PROPERTY = "deleted";

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ValidationRule.class.getPackage().getName());

	protected String ruleName;

	private boolean isEnabled = true;

	private final String ruleDescription;

	private final Class<? super V> _objectType;

	private String _typeName;

	private final PropertyChangeSupport pcSupport;

	public ValidationRule(Class<? super V> objectType, String ruleName) {
		super();
		this.ruleName = ruleName;
		ruleDescription = ruleName + "_description";
		_objectType = objectType;
		pcSupport = new PropertyChangeSupport(this);
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		return DELETED_PROPERTY;
	}

	public String getRuleName() {
		return ruleName;
	}

	public String getRuleDescription() {
		return ruleDescription;
	}

	public abstract ValidationIssue<R, V> applyValidation(final V object);

	public Class<? super V> getObjectType() {
		return _objectType;
	}

	public String getTypeName() {
		if (_typeName == null) {
			_typeName = _objectType.getSimpleName();
		}
		return _typeName;
	}

	public boolean getIsEnabled() {
		return isEnabled;
	}

	public void setIsEnabled(boolean v) {
		if (v != isEnabled) {
			isEnabled = v;
			getPropertyChangeSupport().firePropertyChange("isEnabled", !isEnabled, isEnabled);
		}
	}
}
