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
public abstract class ValidationIssue<R extends ValidationRule<R, V>, V extends Validable>
		implements HasPropertyChangeSupport, PropertyChangeListener {

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
	 * Default behaviour is to return null
	 * 
	 * @return
	 */
	public String getDetailedInformations() {
		/*if (getCause() != null) {
			return getCause().getRuleDescription();
		}*/
		return null;
	}
}
