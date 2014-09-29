/*
 * (c) Copyright 2012-2014 Openflexo
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.model.validation;

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

	public String getNameKey() {
		return ruleName;
	}

	public String getDescriptionKey() {
		return ruleDescription;
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

			System.out.println(">>>>>>> setIsEnabled for " + this + " with " + v);
			Thread.dumpStack();

			isEnabled = v;
			getPropertyChangeSupport().firePropertyChange("isEnabled", !isEnabled, isEnabled);
		}
	}
}
