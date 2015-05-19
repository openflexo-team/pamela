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

	/*public String getLocalizedMessage() {

		if (localizedMessage == null && getProblemIssue() != null && getProblemIssue().getValidationReport() != null && getObject() != null) {
			localizedMessage = getProblemIssue().getValidationReport().localizedForKey(getMessage());
		}
		return localizedMessage;
	}*/

	public void setMessage(String message) {
		this.message = message;
	}

	public V getValidable() {
		return getProblemIssue().getValidable();
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
