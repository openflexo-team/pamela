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

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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

	protected ValidationReport(ValidationModel validationModel, Validable rootObject) throws InterruptedException {
		super();

		pcSupport = new PropertyChangeSupport(this);

		this.validationModel = validationModel;
		this.rootObject = rootObject;

		allIssues = new ArrayList<>();

		infoIssues = new ArrayList<>();
		errors = new ArrayList<>();
		warnings = new ArrayList<>();

		infoIssuesMap = new HashMap<>();
		errorsMap = new HashMap<>();
		warningsMap = new HashMap<>();

		List<ValidationIssue<?, ?>> issues = performDeepValidation(rootObject);

		if (issues.size() == 0) {
			addToValidationIssues(new InformationIssue<>(rootObject, "consistency_check_ok"));
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

		List<ValidationIssue<?, ?>> returned = new ArrayList<>();

		// Gets all the objects to validate and removes duplicated objects
		Set<Validable> objectsToValidate = new LinkedHashSet<>(retrieveAllEmbeddedValidableObjects(rootObject));

		// Compute validation steps and notify validation initialization
		long validationStepToNotify = objectsToValidate.stream().filter(validationModel::shouldNotifyValidation).collect(Collectors.counting());
		getValidationModel().getPropertyChangeSupport().firePropertyChange(VALIDATION_START, rootObject, validationStepToNotify);

		// Perform the validation
		for (Validable validable : objectsToValidate) {
			if (validationModel.shouldNotifyValidation(validable)) {
				getValidationModel().getPropertyChangeSupport().firePropertyChange(VALIDATION_OBJECT, null, validable);
			}

			if (!validable.isDeleted()) {
				returned.addAll(performValidation(validable));
			}

			// Following allows task to be cancelled by throwing an InterruptedException
			Thread.sleep(0);

		}

		// Notify validation is finished
		getValidationModel().getPropertyChangeSupport().firePropertyChange(VALIDATION_END, null, rootObject);

		return returned;

	}

	private <V extends Validable> List<ValidationIssue<?, ?>> performValidation(V validable) {
		List<ValidationIssue<?, ?>> returned = new ArrayList<>();

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
			logger.warning(
					"Exception occured during validation: " + e.getMessage() + " object was " + next + " deleted=" + next.isDeleted());
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
				}
				else if (issue instanceof CompoundIssue) {
					for (ValidationIssue<R, V> containedIssue : ((CompoundIssue<R, V>) issue).getContainedIssues()) {
						if (containedIssue instanceof ProblemIssue && ((ProblemIssue<?, ?>) containedIssue).getFixProposals().size() == 1) {
							addToValidationIssues(containedIssue);
							if (logger.isLoggable(Level.INFO)) {
								logger.info("Fixing automatically...");
							}
							((ProblemIssue<R, V>) containedIssue).getFixProposals().get(0).apply(false);
							addToValidationIssues(new InformationIssue<R, V>(containedIssue.getValidable(),
									"fixed_automatically:" + " " + containedIssue.getMessage() + " : "
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
		List<Validable> returned = new ArrayList<>();
		appendAllEmbeddedValidableObjects(o, returned);
		return returned;
	}

	private void appendAllEmbeddedValidableObjects(Validable o, Collection<Validable> c) {
		if (o != null && !c.contains(o)) {
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
		}
		else {
			issue.setValidationReport(this);
			allIssues.add(issue);
			if (issue instanceof InformationIssue) {
				infoIssues.add((InformationIssue<?, ?>) issue);
				List<InformationIssue<?, ?>> l = infoIssuesMap.get(issue.getValidable());
				if (l == null) {
					l = new ArrayList<>();
					infoIssuesMap.put(issue.getValidable(), l);
				}
				l.add((InformationIssue<?, ?>) issue);
				getPropertyChangeSupport().firePropertyChange("infosCount", getInfosCount() - 1, getInfosCount());
			}
			if (issue instanceof ValidationWarning) {
				warnings.add((ValidationWarning<?, ?>) issue);
				List<ValidationWarning<?, ?>> l = warningsMap.get(issue.getValidable());
				if (l == null) {
					l = new ArrayList<>();
					warningsMap.put(issue.getValidable(), l);
				}
				l.add((ValidationWarning<?, ?>) issue);
				getPropertyChangeSupport().firePropertyChange("warningsCount", getWarningsCount() - 1, getWarningsCount());
			}
			if (issue instanceof ValidationError) {
				errors.add((ValidationError<?, ?>) issue);
				List<ValidationError<?, ?>> l = errorsMap.get(issue.getValidable());
				if (l == null) {
					l = new ArrayList<>();
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
		}
		else {
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

		ChainedCollection<ValidationIssue<?, ?>> returned = new ChainedCollection<>();
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

		List<ValidationIssue<?, ?>> returned = new ArrayList<>();

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
		for (ValidationIssue<?, ?> issue : new ArrayList<>(getAllIssues())) {
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
