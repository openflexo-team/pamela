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

	public ValidationWarning(R rule, V anObject, String aMessage, String aDetailedMessage) {
		super(rule, anObject, aMessage, aDetailedMessage);
	}

	public ValidationWarning(R rule, V anObject, String aMessage, String aDetailedMessage, FixProposal<R, V> proposal) {
		super(rule, anObject, aMessage, aDetailedMessage, proposal);
	}

	public ValidationWarning(R rule, V anObject, String aMessage, String aDetailedMessage, List<FixProposal<R, V>> fixProposals) {
		super(rule, anObject, aMessage, aDetailedMessage, fixProposals);
	}

	@Override
	public String toString() {
		return "VALIDATION / WARNING:   " + getMessage() + (getDetailedMessage() != null ? " details: " + getDetailedMessage() : "");
	}

}
