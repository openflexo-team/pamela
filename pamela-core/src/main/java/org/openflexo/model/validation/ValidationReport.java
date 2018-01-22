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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.openflexo.connie.annotations.NotificationUnsafe;
import org.openflexo.toolbox.ChainedCollection;
import org.openflexo.toolbox.HasPropertyChangeSupport;
import org.openflexo.toolbox.StringUtils;

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

	private final PropertyChangeSupport pcSupport;

	private final ValidationModel validationModel;

	protected ReportMode mode = ReportMode.ALL;

	private ValidationNode<?> rootNode;

	private final Map<Validable, ValidationNode<?>> nodes = new HashMap<>();

	public class ValidationNode<V extends Validable> implements PropertyChangeListener {

		private final V object;

		private final List<InformationIssue<?, ? super V>> infoIssues = new ArrayList<>();
		private final List<ValidationError<?, ? super V>> errors = new ArrayList<>();
		private final List<ValidationWarning<?, ? super V>> warnings = new ArrayList<>();

		private ChainedCollection<ValidationIssue<?, ? super V>> allIssues = null;
		private ChainedCollection<ValidationError<?, ? super V>> allErrors = null;
		private ChainedCollection<ValidationWarning<?, ? super V>> allWarnings = null;
		private ChainedCollection<InformationIssue<?, ? super V>> allInfoIssues = null;

		private ValidationNode<?> parentNode;
		private final List<ValidationNode<?>> childNodes;

		public ValidationNode(V object, ValidationNode<?> parentNode) {
			this.object = object;
			this.parentNode = parentNode;
			childNodes = new ArrayList<>();
			nodes.put(object, this);
			if (object instanceof HasPropertyChangeSupport) {
				((HasPropertyChangeSupport) object).getPropertyChangeSupport().addPropertyChangeListener(this);
			}
		}

		private boolean isDeleted = false;

		public void delete() {
			if (isDeleted) {
				return;
			}
			isDeleted = true;
			if (object instanceof HasPropertyChangeSupport) {
				((HasPropertyChangeSupport) object).getPropertyChangeSupport().removePropertyChangeListener(this);
			}
			clear();
			for (ValidationNode<?> childNode : new ArrayList<>(childNodes)) {
				childNode.delete();
			}
			childNodes.clear();
			if (parentNode != null) {
				parentNode.childNodes.remove(this);
				parentNode.clearIssuesAfterStructuralModifications();
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(((HasPropertyChangeSupport) object).getDeletedProperty())) {
				delete();
			}
		}

		public V getObject() {
			return object;
		}

		public List<ValidationNode<?>> getChildNodes() {
			return childNodes;
		}

		private void validate() {

			_performValidate();

			_updateChildren();

			allIssues = null;
			allErrors = null;
			allWarnings = null;
			allInfoIssues = null;

		}

		private boolean _updateChildren() {

			boolean childrenWereAdded = false;
			Collection<? extends Validable> embeddedValidableObjects = object.getEmbeddedValidableObjects();

			if (embeddedValidableObjects != null) {
				for (Validable embeddedValidable : new ArrayList<>(embeddedValidableObjects)) {
					ValidationNode<?> childNode = getValidationNode(embeddedValidable);
					if (childNode == null) {
						// System.out.println("Validate " + embeddedValidable + " in " + object);
						childNode = new ValidationNode<Validable>(embeddedValidable, this);
						childNodes.add(childNode);
						nodes.put(embeddedValidable, childNode);
						childNode.validate();
						childrenWereAdded = true;
					}
				}
			}

			return childrenWereAdded;
		}

		private void _performValidate() {

			ValidationRuleSet<? super V> ruleSet = getValidationModel().getRuleSet(object);

			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Validating " + object.toString());
			}

			// System.out.println("Validating " + object);

			if (getValidationModel().shouldNotifyValidation(object)) {
				getValidationModel().getPropertyChangeSupport().firePropertyChange(VALIDATION_OBJECT, null, object);
			}

			if (getValidationModel().shouldNotifyValidationRules()) {
				getValidationModel().getPropertyChangeSupport().firePropertyChange(OBJECT_VALIDATION_START, 0, ruleSet.getRulesCount());
			}

			for (int i = 0; i < ruleSet.getRulesCount(); i++) {
				ValidationRule<?, ? super V> rule = ruleSet.getRuleAt(i);
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Applying rule " + rule.getRuleName() + " for " + object);
				}

				if (getValidationModel().shouldNotifyValidationRules()) {
					getValidationModel().getPropertyChangeSupport().firePropertyChange(VALIDATE_WITH_RULE, null, rule);
				}

				performRuleValidation((ValidationRule) rule);
			}

		}

		private void clear() {
			for (InformationIssue<?, ? super V> issue : new ArrayList<>(infoIssues)) {
				removeFromValidationIssues(issue);
			}
			for (ValidationError<?, ? super V> error : new ArrayList<>(errors)) {
				removeFromValidationIssues(error);
			}
			for (ValidationWarning<?, ? super V> warning : new ArrayList<>(warnings)) {
				removeFromValidationIssues(warning);
			}

			infoIssues.clear();
			errors.clear();
			warnings.clear();

			for (ValidationNode<?> childNode : childNodes) {
				childNode.clear();
			}

		}

		private void revalidate() throws InterruptedException {

			clear();

			_performValidate();

			if (_updateChildren()) {
				clearIssuesAfterStructuralModifications();
			}

			for (ValidationNode<?> childNode : childNodes) {
				childNode.revalidate();
			}

			if (getObject() == getRootObject() && getAllIssues().size() == 0) {
				addToValidationIssues(new InformationIssue<>(object, "consistency_check_ok"));
			}
		}

		private <R extends ValidationRule<R, ? super V>> ValidationIssue<R, ? super V> performRuleValidation(R rule) {
			ValidationIssue<R, ? super V> issue = null;
			try {
				issue = rule.getIsEnabled() ? rule.applyValidation(object) : null;
			} catch (Exception e) {
				logger.warning("Exception occured during validation: " + e.getMessage() + " object was " + object + " deleted="
						+ object.isDeleted());
				e.printStackTrace();
				issue = new ValidationError(rule, object, "Unexpected exception: " + e.getMessage());
			}
			if (issue != null) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Adding issue " + issue);
				}
				issue.setCause(rule);
				addToValidationIssues(issue);
				if (getValidationModel().fixAutomaticallyIfOneFixProposal()) {
					if (issue instanceof ProblemIssue && ((ProblemIssue<R, ? super V>) issue).getFixProposals().size() == 1) {
						if (logger.isLoggable(Level.INFO)) {
							logger.info("Fixing automatically...");
						}
						((ProblemIssue<R, ? super V>) issue).getFixProposals().get(0).apply(false);
						addToValidationIssues(new InformationIssue<>(object, "fixed_automatically:" + " " + issue.getMessage() + " : "
								+ (((ProblemIssue<R, ? super V>) issue).getFixProposals()).get(0).getMessage()));
					}
					else if (issue instanceof CompoundIssue) {
						for (ValidationIssue<R, ? super V> containedIssue : ((CompoundIssue<R, ? super V>) issue).getContainedIssues()) {
							if (containedIssue instanceof ProblemIssue
									&& ((ProblemIssue<?, ?>) containedIssue).getFixProposals().size() == 1) {
								addToValidationIssues(containedIssue);
								if (logger.isLoggable(Level.INFO)) {
									logger.info("Fixing automatically...");
								}
								((ProblemIssue<R, ? super V>) containedIssue).getFixProposals().get(0).apply(false);
								addToValidationIssues(new InformationIssue<>(containedIssue.getValidable(),
										"fixed_automatically:" + " " + containedIssue.getMessage() + " : "
												+ ((ProblemIssue<R, ? super V>) containedIssue).getFixProposals().get(0).getMessage()));
							}
						}
					}
				}
			}
			return issue;
		}

		public Collection<InformationIssue<?, ? super V>> getInfoIssues() {
			return infoIssues;
		}

		public Collection<ValidationError<?, ? super V>> getErrors() {
			return errors;
		}

		public Collection<ValidationWarning<?, ? super V>> getWarnings() {
			return warnings;
		}

		private void clearIssuesAfterStructuralModifications() {
			allIssues = null;
			allErrors = null;
			allWarnings = null;
			allInfoIssues = null;
			if (parentNode != null) {
				parentNode.clearIssuesAfterStructuralModifications();
			}
		}

		public Collection<ValidationIssue<?, ? super V>> getAllIssues() {
			if (allIssues == null) {
				allIssues = new ChainedCollection<ValidationIssue<?, ? super V>>(getAllInfoIssues(), getAllErrors(), getAllWarnings());
				allIssues.setDebugName("AllIssuesFor" + object);
			}
			return allIssues;
		}

		public Collection<ValidationError<?, ? super V>> getAllErrors() {
			if (allErrors == null) {
				Collection<ValidationError<?, ? super V>>[] childErrors = new Collection[getChildNodes().size()];
				for (int i = 0; i < getChildNodes().size(); i++) {
					childErrors[i] = (Collection) getChildNodes().get(i).getAllErrors();
				}
				allErrors = new ChainedCollection<>(childErrors);
				allErrors.setDebugName("AllErrorsFor" + object);
				allErrors.add(errors);
				allIssues = null;
			}
			return allErrors;
		}

		public Collection<ValidationWarning<?, ? super V>> getAllWarnings() {
			if (allWarnings == null) {
				Collection<ValidationWarning<?, ? super V>>[] childWarnings = new Collection[getChildNodes().size()];
				for (int i = 0; i < getChildNodes().size(); i++) {
					childWarnings[i] = (Collection) getChildNodes().get(i).getAllWarnings();
				}
				allWarnings = new ChainedCollection<>(childWarnings);
				allWarnings.setDebugName("AllWarningsFor" + object);
				allWarnings.add(warnings);
				allIssues = null;
			}
			return allWarnings;
		}

		public Collection<InformationIssue<?, ? super V>> getAllInfoIssues() {
			if (allInfoIssues == null) {
				Collection<InformationIssue<?, ? super V>>[] childInfos = new Collection[getChildNodes().size()];
				for (int i = 0; i < getChildNodes().size(); i++) {
					childInfos[i] = (Collection) getChildNodes().get(i).getAllInfoIssues();
				}
				allInfoIssues = new ChainedCollection<>(childInfos);
				allInfoIssues.setDebugName("AllInfosFor" + object);
				allInfoIssues.add(infoIssues);
				allIssues = null;
			}
			return allInfoIssues;
		}

		protected void addToValidationIssues(ValidationIssue<?, ? super V> issue) {
			if (issue instanceof CompoundIssue) {
				for (ValidationIssue<?, ? super V> anIssue : ((CompoundIssue<?, ? super V>) issue).getContainedIssues()) {
					addToValidationIssues(anIssue);
				}
			}
			else {
				internallyRegisterIssue(issue);
			}
		}

		protected void removeFromValidationIssues(ValidationIssue<?, ?> issue) {
			if (issue instanceof CompoundIssue) {
				for (ValidationIssue<?, ?> anIssue : ((CompoundIssue<?, ?>) issue).getContainedIssues()) {
					removeFromValidationIssues(anIssue);
				}
			}
			else {
				internallyUnregisterIssue(issue);
			}
		}

		private void internallyRegisterIssue(ValidationIssue<?, ? super V> issue) {
			// System.out.println("Registering in " + object + " issue > " + issue);
			issue.setValidationReport(ValidationReport.this);
			if (issue instanceof InformationIssue) {
				infoIssues.add((InformationIssue<?, ? super V>) issue);
			}
			if (issue instanceof ValidationWarning) {
				warnings.add((ValidationWarning<?, ? super V>) issue);
			}
			if (issue instanceof ValidationError) {
				errors.add((ValidationError<?, ? super V>) issue);
			}
		}

		private void internallyUnregisterIssue(ValidationIssue<?, ?> issue) {
			issue.setValidationReport(null);
			if (issue instanceof InformationIssue) {
				infoIssues.remove(issue);
			}
			if (issue instanceof ValidationWarning) {
				warnings.remove(issue);
			}
			if (issue instanceof ValidationError) {
				errors.remove(issue);
			}
		}

		public String debug(int indent) {
			StringBuffer sb = new StringBuffer();
			sb.append(StringUtils.buildWhiteSpaceIndentation(indent * 2) + " > " + getErrors().size() + "/" + getWarnings().size() + "/"
					+ getInfoIssues().size() + " " + object);
			for (ValidationNode<?> child : getChildNodes()) {
				sb.append("\n" + child.debug(indent + 1));
			}
			return sb.toString();
		}

	}

	public ValidationReport(ValidationModel validationModel, Validable rootObject) throws InterruptedException {
		super();

		pcSupport = new PropertyChangeSupport(this);

		this.validationModel = validationModel;

		rootNode = new ValidationNode<Validable>(rootObject, null);
		nodes.put(rootObject, rootNode);
		// System.out.println(">>>>>>>> START validation");
		rootNode.validate();
		// System.out.println(">>>>>>>> STOP validation");
		// System.out.println("All: " + getAllIssues().size() + " : " + getAllIssues());
		// System.out.println("Errors: " + getAllErrors().size() + " : " + getAllErrors());
		// System.out.println("Warnings: " + getAllWarnings().size() + " : " + getAllWarnings());
		// System.out.println("InfoIssues: " + getAllInfoIssues().size() + " : " + getAllInfoIssues());

		// System.out.println(rootNode.debug(0));

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

	protected <V extends Validable> ValidationNode<V> getValidationNode(V object) {
		return (ValidationNode<V>) nodes.get(object);
	}

	/**
	 * 
	 * @param validable
	 *            the validable of which issues are to be examined
	 * @param validationReport
	 *            a ValidationReport object on which issues are appened or removed
	 * @throws InterruptedException
	 */
	public void revalidate() throws InterruptedException {

		// Gets all the objects to validate and removes duplicated objects
		Set<Validable> objectsToValidate = new LinkedHashSet(Validable.retrieveAllEmbeddedValidableObjects(getRootObject()));

		// Compute validation steps and notify validation initialization
		long validationStepToNotify = objectsToValidate.stream().filter(validationModel::shouldNotifyValidation)
				.collect(Collectors.counting());
		getValidationModel().getPropertyChangeSupport().firePropertyChange(VALIDATION_START, getRootObject(), validationStepToNotify);

		rootNode.revalidate();

		// Notify validation is finished
		getValidationModel().getPropertyChangeSupport().firePropertyChange(VALIDATION_END, null, getRootObject());

		getPropertyChangeSupport().firePropertyChange("filteredIssues", null, getFilteredIssues());
		getPropertyChangeSupport().firePropertyChange("allErrors", null, getAllErrors());
		getPropertyChangeSupport().firePropertyChange("allWarnings", null, getAllWarnings());
		getPropertyChangeSupport().firePropertyChange("allInfoIssues", null, getAllInfoIssues());
		getPropertyChangeSupport().firePropertyChange("allIssues", null, getAllIssues());

		getPropertyChangeSupport().firePropertyChange("issuesRegarding(V)", false, true);
		getPropertyChangeSupport().firePropertyChange("infoIssuesRegarding(V)", false, true);
		getPropertyChangeSupport().firePropertyChange("errorIssuesRegarding(V)", false, true);
		getPropertyChangeSupport().firePropertyChange("warningIssuesRegarding(V)", false, true);

	}

	/**
	 * 
	 * @param validable
	 *            the validable of which issues are to be examined
	 * @param validationReport
	 *            a ValidationReport object on which issues are appened or removed
	 * @throws InterruptedException
	 */
	public <V extends Validable> void revalidate(V validable) throws InterruptedException {

		ValidationNode<V> validationNode = getValidationNode(validable);
		if (validationNode != null) {
			validationNode.revalidate();
		}

	}

	public Collection<? extends ValidationIssue<?, ?>> getFilteredIssues() {
		switch (mode) {
			case ALL:
				return getAllIssues();
			case ERRORS:
				return getAllErrors();
			case WARNINGS:
				return getAllWarnings();
			default:
				return getAllIssues();
		}
	}

	public int getErrorsCount() {
		return getAllErrors().size();
	}

	public Collection<ValidationIssue<?, ?>> getAllIssues() {
		return (Collection) rootNode.getAllIssues();
	}

	public Collection<ValidationError<?, ?>> getAllErrors() {
		return (Collection) rootNode.getAllErrors();
	}

	public Collection<ValidationWarning<?, ?>> getAllWarnings() {
		return (Collection) rootNode.getAllWarnings();
	}

	public Collection<InformationIssue<?, ?>> getAllInfoIssues() {
		return (Collection) rootNode.getAllInfoIssues();
	}

	public ValidationModel getValidationModel() {
		return validationModel;
	}

	public Validable getRootObject() {
		return rootNode.getObject();
	}

	@NotificationUnsafe
	public <V extends Validable> Collection<ValidationIssue<?, ? super V>> issuesRegarding(V object) {

		ValidationNode<V> validationNode = getValidationNode(object);
		if (validationNode != null) {
			return validationNode.getAllIssues();
		}
		return Collections.emptyList();
	}

	@NotificationUnsafe
	public <V extends Validable> Collection<InformationIssue<?, ? super V>> infoIssuesRegarding(V object) {
		ValidationNode<V> validationNode = getValidationNode(object);
		if (validationNode != null) {
			return validationNode.getAllInfoIssues();
		}
		return Collections.emptyList();
	}

	@NotificationUnsafe
	public <V extends Validable> Collection<ValidationError<?, ? super V>> errorIssuesRegarding(V object) {
		ValidationNode<V> validationNode = getValidationNode(object);
		if (validationNode != null) {
			return validationNode.getAllErrors();
		}
		return Collections.emptyList();
	}

	@NotificationUnsafe
	public <V extends Validable> Collection<ValidationWarning<?, ? super V>> warningIssuesRegarding(V object) {
		ValidationNode<V> validationNode = getValidationNode(object);
		if (validationNode != null) {
			return validationNode.getAllWarnings();
		}
		return Collections.emptyList();
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

		rootNode.clear();

	}

}
