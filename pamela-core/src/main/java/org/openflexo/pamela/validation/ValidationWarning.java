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

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents a validation issue requiring attention embedded in a validation report. A warning may signify a conception problem.
 * 
 * @author sylvain
 * 
 */
public class ValidationWarning<R extends ValidationRule<R, V>, V extends Validable> extends ProblemIssue<R, V> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ValidationWarning.class.getPackage().getName());

	public ValidationWarning(R rule, V anObject, String aMessage) {
		super(rule, anObject, aMessage);
	}

	public ValidationWarning(R rule, V anObject, String aMessage, FixProposal<R, V> proposal) {
		super(rule, anObject, aMessage, proposal);
	}

	public ValidationWarning(R rule, V anObject, String aMessage, List<FixProposal<R, V>> fixProposals) {
		super(rule, anObject, aMessage, fixProposals);
	}

	public ValidationWarning(R rule, V anObject, String aMessage, FixProposal<R, V>... fixProposals) {
		super(rule, anObject, aMessage, Arrays.asList(fixProposals));
	}

	@Override
	public String toString() {
		return "VALIDATION / WARNING:   " + getMessage() + " OBJECT=" + getValidable();
	}

}
