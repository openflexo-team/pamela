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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Represents a validation issue requiring attention embedded in a validation report
 * 
 * @author sylvain
 * 
 */
public abstract class ProblemIssue<R extends ValidationRule<R, V>, V extends Validable> extends ValidationIssue<R, V> {

	private static final Logger logger = Logger.getLogger(ProblemIssue.class.getPackage().getName());

	public static final String RELATED_VALIDABLES_PROPERTY = "relatedValidableObjects";
	public static final String FIX_PROPOSALS_PROPERTY = "fixProposals";

	private List<FixProposal<R, V>> fixProposals;

	private R validationRule;

	private List<Validable> relatedValidableObjects;

	public ProblemIssue(R rule, V anObject, String aMessage) {
		this(rule, anObject, aMessage, (String) null);
	}

	public ProblemIssue(R rule, V anObject, String aMessage, String aDetailedMessage) {
		super(anObject, aMessage, aDetailedMessage);
		validationRule = rule;
		fixProposals = new ArrayList<FixProposal<R, V>>();
		relatedValidableObjects = new ArrayList<Validable>();
	}

	public ProblemIssue(R rule, V anObject, String aMessage, FixProposal<R, V> proposal) {
		this(rule, anObject, aMessage, null, proposal);
	}

	public ProblemIssue(R rule, V anObject, String aMessage, List<FixProposal<R, V>> fixProposals) {
		this(rule, anObject, aMessage, null, fixProposals);
	}

	public ProblemIssue(R rule, V anObject, String aMessage, String aDetailedMessage, FixProposal<R, V> proposal) {
		this(rule, anObject, aMessage, aDetailedMessage);
		if (proposal != null) {
			addToFixProposals(proposal);
		}
	}

	public ProblemIssue(R rule, V anObject, String aMessage, String aDetailedMessage, List<FixProposal<R, V>> fixProposals) {
		this(rule, anObject, aMessage, aDetailedMessage);
		if (fixProposals != null) {
			for (FixProposal<R, V> fp : fixProposals) {
				addToFixProposals(fp);
			}
		}
	}

	public List<FixProposal<R, V>> getFixProposals() {
		return fixProposals;
	}

	public <FP extends FixProposal<R, V>> List<FP> getFixProposals(Class<? extends FP> fixProposalClass) {
		List<FP> returned = new ArrayList<FP>();
		for (FixProposal<R, V> fixProposal : getFixProposals()) {
			if (fixProposalClass.isAssignableFrom(fixProposal.getClass())) {
				returned.add((FP) fixProposal);
			}
		}
		return returned;
	}

	public void addToFixProposals(FixProposal<R, V> proposal) {
		fixProposals.add(proposal);
		proposal.setProblemIssue(this);
		getPropertyChangeSupport().firePropertyChange(FIX_PROPOSALS_PROPERTY, null, proposal);
	}

	public boolean isFixable() {
		return fixProposals.size() > 0;
	}

	public R getValidationRule() {
		return validationRule;
	}

	public List<Validable> getRelatedValidableObjects() {
		return relatedValidableObjects;
	}

	/*public void setRelatedValidableObjects(List<Validable> relatedValidableObjects) {
		this.relatedValidableObjects = relatedValidableObjects;
	}*/

	public void addToRelatedValidableObjects(Validable relatedValidable) {
		relatedValidableObjects.add(relatedValidable);
		if (relatedValidable instanceof HasPropertyChangeSupport) {
			((HasPropertyChangeSupport) relatedValidable).getPropertyChangeSupport().addPropertyChangeListener(this);
		}
		getPropertyChangeSupport().firePropertyChange(RELATED_VALIDABLES_PROPERTY, null, relatedValidable);
	}

	public void addToRelatedValidableObjects(List<? extends Validable> someObjects) {
		for (Validable v : someObjects) {
			addToRelatedValidableObjects(v);
		}
	}

	public void removeFromRelatedValidableObjects(Validable relatedValidable) {
		relatedValidableObjects.remove(relatedValidable);
		if (relatedValidable instanceof HasPropertyChangeSupport) {
			((HasPropertyChangeSupport) relatedValidable).getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		getPropertyChangeSupport().firePropertyChange(RELATED_VALIDABLES_PROPERTY, relatedValidable, null);
	}

	@Override
	public void revalidateAfterFixing() {
		ValidationReport validationReport = getValidationReport();

		if (validationReport == null) {
			return;
		}

		Collection<ValidationIssue<?, ?>> allIssuesToRemove = validationReport.issuesRegarding(getValidable());
		for (Validable relatedValidable : getRelatedValidableObjects()) {
			allIssuesToRemove.addAll(validationReport.issuesRegarding(relatedValidable));
		}
		Collection<Validable> allEmbeddedValidableObjects = validationReport.retrieveAllEmbeddedValidableObjects(getValidable());
		if (allEmbeddedValidableObjects != null) {
			for (Validable embeddedValidable : allEmbeddedValidableObjects) {
				allIssuesToRemove.addAll(validationReport.issuesRegarding(embeddedValidable));
			}
		}
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Remove related issues");
		}

		for (ValidationIssue<?, ?> issue : new ArrayList<ValidationIssue<?, ?>>(allIssuesToRemove)) {
			validationReport.removeFromValidationIssues(issue);
		}

		if (!getValidable().isDeleted()) {
			validationReport.revalidate(getValidable());
		}

		for (Validable relatedValidable : getRelatedValidableObjects()) {
			if (!relatedValidable.isDeleted()) {
				validationReport.revalidate(relatedValidable);
			}
		}
	}

	@Override
	public boolean isProblemIssue() {
		return true;
	}

}
