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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Represents a validation issue embedded in a validation report
 * 
 * @author sylvain
 * 
 */
public abstract class ValidationIssue<R extends ValidationRule<R, V>, V extends Validable> implements HasPropertyChangeSupport,
		PropertyChangeListener {

	public static final String DELETED_PROPERTY = "deleted";

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ValidationIssue.class.getPackage().getName());

	private final V validable;
	private String message;
	private ValidationReport validationReport;
	private R cause;

	private final PropertyChangeSupport pcSupport;

	public ValidationIssue(V anObject, String aMessage) {
		validable = anObject;
		message = aMessage;
		pcSupport = new PropertyChangeSupport(this);
		if (validable instanceof HasPropertyChangeSupport) {
			((HasPropertyChangeSupport) validable).getPropertyChangeSupport().addPropertyChangeListener(this);
		}
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public V getValidable() {
		return validable;
	}

	public void setValidationReport(ValidationReport report) {
		validationReport = report;
	}

	public ValidationReport getValidationReport() {
		return validationReport;
	}

	private String _typeName;

	public String getTypeName() {
		if (_typeName == null) {
			StringTokenizer st = new StringTokenizer(getValidable().getClass().getName(), ".");
			while (st.hasMoreTokens()) {
				_typeName = st.nextToken();
			}
		}
		return _typeName;
	}

	@Override
	public abstract String toString();

	public void setCause(R rule) {
		cause = rule;
	}

	public R getCause() {
		return cause;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		if (evt.getSource() == validable) {
			if (validable.isDeleted()) {
				delete();
			}
		}
	}

	public void delete() {
		if (validable instanceof HasPropertyChangeSupport) {
			((HasPropertyChangeSupport) validable).getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		validationReport.removeFromValidationIssues(this);
		if (getPropertyChangeSupport() != null) {
			getPropertyChangeSupport().firePropertyChange(DELETED_PROPERTY, this, null);
		}
	}

	@Override
	public String getDeletedProperty() {
		return DELETED_PROPERTY;
	}

	public void revalidateAfterFixing() {
		ValidationReport validationReport = getValidationReport();

		if (validationReport == null) {
			return;
		}

		Collection<ValidationIssue<?, ?>> allIssuesToRemove = validationReport.issuesRegarding(getValidable());
		Collection<Validable> allEmbeddedValidableObjects = validationReport.retrieveAllEmbeddedValidableObjects(getValidable());
		if (allEmbeddedValidableObjects != null) {
			for (Validable embeddedValidable : allEmbeddedValidableObjects) {
				allIssuesToRemove.addAll(validationReport.issuesRegarding(embeddedValidable));
			}
		}
		for (ValidationIssue<?, ?> issue : allIssuesToRemove) {
			validationReport.removeFromValidationIssues(issue);
		}

		if (!getValidable().isDeleted()) {
			validationReport.revalidate(getValidable());
		}
	}

	public boolean isProblemIssue() {
		return false;
	}

	/**
	 * Return detailed informations for this issue<br>
	 * Default behaviour is to return rule's description
	 * 
	 * @return
	 */
	public String getDetailedInformations() {
		if (getCause() != null) {
			return getCause().getRuleDescription();
		}
		return null;
	}
}