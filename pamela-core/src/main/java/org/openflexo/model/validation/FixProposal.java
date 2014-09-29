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

import java.util.logging.Logger;

/**
 * Abstract automatic fix proposal for a validation issue
 * 
 * @author sguerin
 * 
 */
public abstract class FixProposal<R extends ValidationRule<R, V>, V extends Validable> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FixProposal.class.getPackage().getName());

	private String message;

	private String localizedMessage;

	private ProblemIssue<R, V> _issue;

	public FixProposal(String aMessage) {
		super();
		message = aMessage;
	}

	public String getMessage() {
		return message;
	}

	public String getLocalizedMessage() {

		if (localizedMessage == null && getProblemIssue() != null && getProblemIssue().getValidationReport() != null && getObject() != null) {
			localizedMessage = getProblemIssue().getValidationReport().localizedForKey(getMessage());
		}
		return localizedMessage;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public V getObject() {
		return getProblemIssue().getObject();
	}

	public void apply() {
		apply(true);
	}

	public void apply(boolean revalidateAfterFixing) {
		if (getProblemIssue() == null) {
			return;
		}
		ValidationReport validationReport = getProblemIssue().getValidationReport();
		fixAction();
		if (revalidateAfterFixing) {
			getProblemIssue().revalidateAfterFixing();
		}
		validationReport.getPropertyChangeSupport().firePropertyChange("filteredIssues", null, validationReport.getFilteredIssues());
	}

	protected abstract void fixAction();

	public void setProblemIssue(ProblemIssue<R, V> issue) {
		_issue = issue;
	}

	public ProblemIssue<R, V> getProblemIssue() {
		return _issue;
	}

	public boolean askConfirmation() {
		return false;
	}

}
