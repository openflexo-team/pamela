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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.toolbox.ChainedCollection;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * A {@link ValidationReport} contains all issues regarding to the validation of a whole objects tree computed from a root object.<br>
 * Embedding strategy is defined according to {@link Validable.getEmbeddedValidable()} API<br>
 * A {@link ValidationReport} is obtained from {@link ValidationModel.validate(Validable)} method
 * 
 * @author sylvain
 * 
 */
public class ValidationReport implements HasPropertyChangeSupport {

	public static final String DELETED_PROPERTY = "deleted";
	public static final String REPORT_MODE_PROPERTY = "reportMode";

	public static final String VALIDATION_START = "validationStart";
	public static final String VALIDATION_END = "validationEnd";
	public static final String VALIDATION_OBJECT = "validateObject";
	public static final String OBJECT_VALIDATION_START = "objectValidation";
	public static final String VALIDATE_WITH_RULE = "validateWithRule";

	public enum ReportMode {
		ALL, ERRORS, WARNINGS;

		public String getLocalizedName() {
			return name().toLowerCase();
		}
	}

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ValidationReport.class.getPackage().getName());

	private final Validable rootObject;

	private final PropertyChangeSupport pcSupport;

	// private final ValidationIssueVector _validationIssues;

	private final List<ValidationIssue<?, ?>> allIssues;
	private final List<InformationIssue<?, ?>> infoIssues;
	private final List<ValidationError<?, ?>> errors;
	private final List<ValidationWarning<?, ?>> warnings;
	private final Map<Validable, List<InformationIssue<?, ?>>> infoIssuesMap;
	private final Map<Validable, List<ValidationError<?, ?>>> errorsMap;
	private final Map<Validable, List<ValidationWarning<?, ?>>> warningsMap;

	private final ValidationModel validationModel;

	protected ReportMode mode = ReportMode.ALL;

	@SuppressWarnings("unchecked")
	protected ValidationReport(ValidationModel validationModel, Validable rootObject) throws InterruptedException {
		super();

		pcSupport = new PropertyChangeSupport(this);

		this.validationModel = validationModel;
		this.rootObject = rootObject;

		allIssues = new ArrayList<ValidationIssue<?, ?>>();

		infoIssues = new ArrayList<InformationIssue<?, ?>>();
		errors = new ArrayList<ValidationError<?, ?>>();
		warnings = new ArrayList<ValidationWarning<?, ?>>();

		infoIssuesMap = new HashMap<Validable, List<InformationIssue<?, ?>>>();
		errorsMap = new HashMap<Validable, List<ValidationError<?, ?>>>();
		warningsMap = new HashMap<Validable, List<ValidationWarning<?, ?>>>();

		List<ValidationIssue<?, ?>> issues = performDeepValidation(rootObject);

		if (issues.size() == 0) {
			addToValidationIssues(new InformationIssue(rootObject, "consistency_check_ok"));
		}

	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		return DELETED_PROPERTY;
	}

	public ReportMode getReportMode() {
		return mode;
	}

	public void setReportMode(ReportMode mode) {
		if (mode != this.mode) {
			ReportMode old = this.mode;
			this.mode = mode;
			getPropertyChangeSupport().firePropertyChange(REPORT_MODE_PROPERTY, old, mode);
			getPropertyChangeSupport().firePropertyChange("filteredIssues", null, getFilteredIssues());
		}
	}

	private List<ValidationIssue<?, ?>> performDeepValidation(Validable rootObject) throws InterruptedException {

		List<ValidationIssue<?, ?>> returned = new ArrayList<ValidationIssue<?, ?>>();

		// Get all the objects to validate
		Collection<Validable> allEmbeddedValidableObjects = retrieveAllEmbeddedValidableObjects(rootObject);

		// logger.info("For object " + object + " objects to validate are: " + allEmbeddedValidableObjects);

		// Remove duplicated objects
		Vector<Validable> objectsToValidate = new Vector<Validable>();
		for (Validable next : allEmbeddedValidableObjects) {
			if (!objectsToValidate.contains(next)) {
				objectsToValidate.add(next);
			}
		}

		// Compute validation steps and notify validation initialization
		int validationStepToNotify = 0;
		for (Enumeration<Validable> en = objectsToValidate.elements(); en.hasMoreElements();) {
			Validable next = en.nextElement();
			if (validationModel.shouldNotifyValidation(next)) {
				validationStepToNotify++;
			}
		}

		getValidationModel().getPropertyChangeSupport().firePropertyChange(VALIDATION_START, rootObject, validationStepToNotify);

		// Perform the validation
		for (Enumeration<Validable> en = objectsToValidate.elements(); en.hasMoreElements();) {
			Validable next = en.nextElement();
			if (validationModel.shouldNotifyValidation(next)) {
				getValidationModel().getPropertyChangeSupport().firePropertyChange(VALIDATION_OBJECT, null, next);
			}

			if (!next.isDeleted()) {
				returned.addAll(performValidation(next));
			}

			// Following allows task to be cancelled by throwing an InterruptedException
			Thread.sleep(1);

		}

		// Notify validation is finished
		getValidationModel().getPropertyChangeSupport().firePropertyChange(VALIDATION_END, null, rootObject);

		return returned;

	}

	private <V extends Validable> List<ValidationIssue<?, ?>> performValidation(V validable) {
		List<ValidationIssue<?, ?>> returned = new ArrayList<ValidationIssue<?, ?>>();

		ValidationRuleSet<? super V> ruleSet = getValidationModel().getRuleSet(validable);

		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Validating " + validable.toString() + " " + validable.toString());
		}

		if (getValidationModel().shouldNotifyValidationRules()) {
			getValidationModel().getPropertyChangeSupport().firePropertyChange(OBJECT_VALIDATION_START, 0, ruleSet.getRulesCount());
		}

		for (int i = 0; i < ruleSet.getRulesCount(); i++) {
			ValidationRule<?, ? super V> rule = ruleSet.getRuleAt(i);
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Applying rule " + rule.getRuleName());
			}

			// System.out.println("--> Applying rule " + rule.getRuleName() + " for " + validable);

			if (getValidationModel().shouldNotifyValidationRules()) {
				getValidationModel().getPropertyChangeSupport().firePropertyChange(VALIDATE_WITH_RULE, null, rule);
			}

			ValidationIssue<?, ?> issue = performRuleValidation((ValidationRule) rule, validable);

			if (issue != null) {
				returned.add(issue);
			}
		}

		return returned;
	}

	private <R extends ValidationRule<R, V>, V extends Validable> ValidationIssue<R, V> performRuleValidation(R rule, V next) {
		ValidationIssue<R, V> issue = null;
		try {
			issue = rule.getIsEnabled() ? rule.applyValidation(next) : null;
		} catch (Exception e) {
			logger.warning("Exception occured during validation: " + e.getMessage() + " object was " + next + " deleted="
					+ next.isDeleted());
			e.printStackTrace();
		}
		if (issue != null) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Adding issue " + issue);
			}
			issue.setCause(rule);
			addToValidationIssues(issue);
			if (getValidationModel().fixAutomaticallyIfOneFixProposal()) {
				if (issue instanceof ProblemIssue && ((ProblemIssue<R, V>) issue).getFixProposals().size() == 1) {
					if (logger.isLoggable(Level.INFO)) {
						logger.info("Fixing automatically...");
					}
					((ProblemIssue<R, V>) issue).getFixProposals().get(0).apply(false);
					addToValidationIssues(new InformationIssue<R, V>(next, "fixed_automatically:" + " " + issue.getMessage() + " : "
							+ (((ProblemIssue<R, V>) issue).getFixProposals()).get(0).getMessage()));
				} else if (issue instanceof CompoundIssue) {
					for (ValidationIssue<R, V> containedIssue : ((CompoundIssue<R, V>) issue).getContainedIssues()) {
						if (containedIssue instanceof ProblemIssue && ((ProblemIssue) containedIssue).getFixProposals().size() == 1) {
							addToValidationIssues(containedIssue);
							if (logger.isLoggable(Level.INFO)) {
								logger.info("Fixing automatically...");
							}
							((ProblemIssue<R, V>) containedIssue).getFixProposals().get(0).apply(false);
							addToValidationIssues(new InformationIssue<R, V>(containedIssue.getValidable(), "fixed_automatically:" + " "
									+ containedIssue.getMessage() + " : "
									+ ((ProblemIssue<R, V>) containedIssue).getFixProposals().get(0).getMessage()));
						}
					}
				}
			}
		}
		return issue;
	}

	/**
	 * 
	 * @param validable
	 *            the validable of which issues are to be examined
	 * @param validationReport
	 *            a ValidationReport object on which issues are appened or removed
	 */
	public void revalidate(Validable validable) {
		// TODO
	}

	/*public void revalidateAfterFixing(boolean isDeleteAction) {
		Vector<ValidationIssue> allIssuesToRemove = getValidationReport().issuesRegarding(getObject());
		for (Validable relatedValidable : getRelatedValidableObjects()) {
			allIssuesToRemove.addAll(getValidationReport().issuesRegarding(relatedValidable));
		}
		Collection<Validable> allEmbeddedValidableObjects = getValidationReport().getValidationModel().retrieveAllEmbeddedValidableObjects(
				getObject());
		if (allEmbeddedValidableObjects != null) {
			for (Validable embeddedValidable : allEmbeddedValidableObjects) {
				allIssuesToRemove.addAll(getValidationReport().issuesRegarding(embeddedValidable));
			}
		}
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Remove related issues");
		}
		getValidationReport().removeFromValidationIssues(allIssuesToRemove);
		if (!isDeleteAction) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Revalidate");
			}
			ValidationReport newReportForThisObject = getObject().validate(getValidationReport().getValidationModel());
			if (logger.isLoggable(Level.FINE)) {
				logger.finer("Found " + newReportForThisObject.getValidationIssues().size() + " new issues for this revalidated object");
			}
			for (Enumeration e = newReportForThisObject.getValidationIssues().elements(); e.hasMoreElements();) {
				ValidationIssue newIssue = (ValidationIssue) e.nextElement();
				getValidationReport().addToValidationIssues(newIssue);
			}
		}
		for (Validable relatedValidable : getRelatedValidableObjects()) {
			if (!(relatedValidable instanceof DeletableObject && ((DeletableObject) relatedValidable).isDeleted())) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Revalidate related");
				}
				ValidationReport newReportForRelatedObject = relatedValidable.validate(getValidationReport().getValidationModel());
				if (logger.isLoggable(Level.FINE)) {
					logger.finer("Found " + newReportForRelatedObject.getValidationIssues().size()
							+ " new issues for this revalidated related object");
				}
				for (Enumeration e2 = newReportForRelatedObject.getValidationIssues().elements(); e2.hasMoreElements();) {
					ValidationIssue newIssue = (ValidationIssue) e2.nextElement();
					getValidationReport().addToValidationIssues(newIssue);
				}
			}
		}
	}*/

	public Collection<Validable> retrieveAllEmbeddedValidableObjects(Validable o) {
		List<Validable> returned = new ArrayList<Validable>();
		appendAllEmbeddedValidableObjects(o, returned);
		return returned;
	}

	private void appendAllEmbeddedValidableObjects(Validable o, Collection<Validable> c) {
		if (o != null) {
			c.add(o);
			Collection<? extends Validable> embeddedObjects = o.getEmbeddedValidableObjects();
			if (embeddedObjects != null) {
				for (Validable o2 : embeddedObjects) {
					appendAllEmbeddedValidableObjects(o2, c);
				}
			}
		}
	}

	public List<? extends ValidationIssue<?, ?>> getFilteredIssues() {
		switch (mode) {
		case ALL:
			return allIssues;
		case ERRORS:
			return errors;
		case WARNINGS:
			return warnings;
		default:
			return allIssues;
		}
	}

	public int getIssuesCount() {
		return allIssues.size();
	}

	public int getInfosCount() {
		return infoIssues.size();
	}

	public int getWarningsCount() {
		return warnings.size();
	}

	public int getErrorsCount() {
		return errors.size();
	}

	public List<ValidationIssue<?, ?>> getAllIssues() {
		return allIssues;
	}

	public List<ValidationError<?, ?>> getErrors() {
		return errors;
	}

	public List<ValidationWarning<?, ?>> getWarnings() {
		return warnings;
	}

	public List<InformationIssue<?, ?>> getInfoIssues() {
		return infoIssues;
	}

	protected void addToValidationIssues(ValidationIssue<?, ?> issue) {
		if (issue instanceof CompoundIssue) {
			for (ValidationIssue<?, ?> anIssue : ((CompoundIssue<?, ?>) issue).getContainedIssues()) {
				addToValidationIssues(anIssue);
			}
		} else {
			issue.setValidationReport(this);
			allIssues.add(issue);
			if (issue instanceof InformationIssue) {
				infoIssues.add((InformationIssue<?, ?>) issue);
				List<InformationIssue<?, ?>> l = infoIssuesMap.get(issue.getValidable());
				if (l == null) {
					l = new ArrayList<InformationIssue<?, ?>>();
					infoIssuesMap.put(issue.getValidable(), l);
				}
				l.add((InformationIssue<?, ?>) issue);
				getPropertyChangeSupport().firePropertyChange("infosCount", getInfosCount() - 1, getInfosCount());
			}
			if (issue instanceof ValidationWarning) {
				warnings.add((ValidationWarning<?, ?>) issue);
				List<ValidationWarning<?, ?>> l = warningsMap.get(issue.getValidable());
				if (l == null) {
					l = new ArrayList<ValidationWarning<?, ?>>();
					warningsMap.put(issue.getValidable(), l);
				}
				l.add((ValidationWarning<?, ?>) issue);
				getPropertyChangeSupport().firePropertyChange("warningsCount", getWarningsCount() - 1, getWarningsCount());
			}
			if (issue instanceof ValidationError) {
				errors.add((ValidationError<?, ?>) issue);
				List<ValidationError<?, ?>> l = errorsMap.get(issue.getValidable());
				if (l == null) {
					l = new ArrayList<ValidationError<?, ?>>();
					errorsMap.put(issue.getValidable(), l);
				}
				l.add((ValidationError<?, ?>) issue);
				getPropertyChangeSupport().firePropertyChange("errorsCount", getErrorsCount() - 1, getErrorsCount());
			}
			getPropertyChangeSupport().firePropertyChange("issuesCount", getIssuesCount() - 1, getIssuesCount());
			getPropertyChangeSupport().firePropertyChange("allIssues", null, getAllIssues());
			getPropertyChangeSupport().firePropertyChange("filteredIssues", null, getFilteredIssues());

		}
	}

	protected void removeFromValidationIssues(ValidationIssue<?, ?> issue) {
		if (issue instanceof CompoundIssue) {
			for (ValidationIssue<?, ?> anIssue : ((CompoundIssue<?, ?>) issue).getContainedIssues()) {
				removeFromValidationIssues(anIssue);
			}
		} else {
			issue.setValidationReport(null);
			allIssues.remove(issue);
			if (issue instanceof InformationIssue) {
				infoIssues.remove(issue);
				List<InformationIssue<?, ?>> l = infoIssuesMap.get(issue.getValidable());
				if (l != null) {
					l.remove(issue);
				}
				getPropertyChangeSupport().firePropertyChange("infosCount", getInfosCount() + 1, getInfosCount());
			}
			if (issue instanceof ValidationWarning) {
				warnings.remove(issue);
				List<ValidationWarning<?, ?>> l = warningsMap.get(issue.getValidable());
				if (l != null) {
					l.remove(issue);
				}
				getPropertyChangeSupport().firePropertyChange("warningsCount", getWarningsCount() + 1, getWarningsCount());
			}
			if (issue instanceof ValidationError) {
				errors.remove(issue);
				List<ValidationError<?, ?>> l = errorsMap.get(issue.getValidable());
				if (l != null) {
					l.remove(issue);
				}
				getPropertyChangeSupport().firePropertyChange("errorsCount", getErrorsCount() + 1, getErrorsCount());
			}
			getPropertyChangeSupport().firePropertyChange("issuesCount", getIssuesCount() + 1, getIssuesCount());
			getPropertyChangeSupport().firePropertyChange("allIssues", null, getAllIssues());
			getPropertyChangeSupport().firePropertyChange("filteredIssues", null, getFilteredIssues());
		}
	}

	public ValidationModel getValidationModel() {
		return validationModel;
	}

	public Validable getRootObject() {
		return rootObject;
	}

	public Collection<ValidationIssue<?, ?>> issuesRegarding(Validable object) {

		ChainedCollection<ValidationIssue<?, ?>> returned = new ChainedCollection<ValidationIssue<?, ?>>();
		List<InformationIssue<?, ?>> infoIssuesList = infoIssuesMap.get(object);
		if (infoIssuesList != null && infoIssuesList.size() > 0) {
			returned.add(infoIssuesList);
		}
		List<ValidationWarning<?, ?>> warningsList = warningsMap.get(object);
		if (warningsList != null && warningsList.size() > 0) {
			returned.add(warningsList);
		}
		List<ValidationError<?, ?>> errorsList = errorsMap.get(object);
		if (errorsList != null && errorsList.size() > 0) {
			returned.add(errorsList);
		}
		return returned;
	}

	// TODO: perf issues
	public List<ValidationIssue<?, ?>> issuesRegarding(ValidationRule<?, ?> rule) {

		List<ValidationIssue<?, ?>> returned = new ArrayList<ValidationIssue<?, ?>>();

		for (ValidationIssue<?, ?> issue : getAllIssues()) {
			if (issue.getCause() == rule) {
				returned.add(issue);
			}
		}

		return returned;
	}

	public String reportAsString() {
		StringBuffer sb = new StringBuffer();
		for (ValidationIssue<?, ?> issue : getAllIssues()) {
			sb.append(issue.toString() + "\n");
		}
		return sb.toString();
	}

	public void delete() {
		for (ValidationIssue<?, ?> issue : new ArrayList<ValidationIssue<?, ?>>(getAllIssues())) {
			issue.delete();
		}
	}

	/*public String localizedForKey(String key) {
		if (validationModel != null) {
			return validationModel.localizedForKey(key);
		}
		return key;
	}*/
}
