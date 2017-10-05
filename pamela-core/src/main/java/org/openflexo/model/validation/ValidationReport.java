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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	// private final Validable rootObject;

	private final PropertyChangeSupport pcSupport;

	// private final ValidationIssueVector _validationIssues;

	/*private final List<ValidationIssue<?, ?>> allIssues;
	private final List<InformationIssue<?, ?>> infoIssues;
	private final List<ValidationError<?, ?>> errors;
	private final List<ValidationWarning<?, ?>> warnings;
	private final Map<Validable, List<InformationIssue<?, ?>>> infoIssuesMap;
	private final Map<Validable, List<ValidationError<?, ?>>> errorsMap;
	private final Map<Validable, List<ValidationWarning<?, ?>>> warningsMap;*/

	private final ValidationModel validationModel;

	protected ReportMode mode = ReportMode.ALL;

	private ValidationNode<?> rootNode;

	private final Map<Validable, ValidationNode<?>> nodes = new HashMap<>();

	public class ValidationNode<V extends Validable> {

		private final V object;

		private final List<InformationIssue<?, ? super V>> infoIssues = new ArrayList<>();
		private final List<ValidationError<?, ? super V>> errors = new ArrayList<>();
		private final List<ValidationWarning<?, ? super V>> warnings = new ArrayList<>();

		private ChainedCollection<ValidationIssue<?, ? super V>> allIssues = null;
		private ChainedCollection<ValidationError<?, ? super V>> allErrors = null;
		private ChainedCollection<ValidationWarning<?, ? super V>> allWarnings = null;
		private ChainedCollection<InformationIssue<?, ? super V>> allInfoIssues = null;

		private final List<ValidationNode<?>> childNodes;

		public ValidationNode(V object) {
			this.object = object;
			childNodes = new ArrayList<>();
			nodes.put(object, this);
		}

		public V getObject() {
			return object;
		}

		public List<ValidationNode<?>> getChildNodes() {
			return childNodes;
		}

		/*public Collection<Validable> retrieveAllEmbeddedValidableObjects(Validable o) {
			List<Validable> returned = new ArrayList<>();
			appendAllEmbeddedValidableObjects(o, returned);
			return returned;
		}
		
		private void appendAllEmbeddedValidableObjects(Validable o, Collection<Validable> c) {
			if (o != null && !c.contains(o)) {
				c.add(o);
				Collection<Validable> embeddedObjects = o.getEmbeddedValidableObjects();
				if (embeddedObjects != null) {
					for (Validable o2 : embeddedObjects) {
						appendAllEmbeddedValidableObjects(o2, c);
					}
				}
			}
		}*/

		private void validate() {

			_performValidate();

			Collection<Validable> embeddedValidableObjects = object.getEmbeddedValidableObjects();

			if (embeddedValidableObjects != null) {
				for (Validable embeddedValidable : embeddedValidableObjects) {
					ValidationNode<?> childNode = getValidationNode(embeddedValidable);
					if (childNode == null) {
						// System.out.println("Validate " + embeddedValidable + " in " + object);
						childNode = new ValidationNode<Validable>(embeddedValidable);
						childNodes.add(childNode);
						nodes.put(embeddedValidable, childNode);
						childNode.validate();
					}
				}
			}

			allIssues = null;
			allErrors = null;
			allWarnings = null;
			allInfoIssues = null;

		}

		private void _performValidate() {

			ValidationRuleSet<? super V> ruleSet = getValidationModel().getRuleSet(object);

			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Validating " + object.toString());
			}

			System.out.println("Validating " + object);

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

				/*ValidationIssue<?, ? super V> issue =*/ performRuleValidation((ValidationRule) rule);
				// rulesNb++;

				/*if (issue != null) {
					System.out.println("Found issue " + issue + " in " + object);
					addToValidationIssues(issue);
				}*/
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

			for (ValidationNode<?> childNode : childNodes) {
				childNode.revalidate();
			}

			if (getAllIssues().size() == 0) {
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

		public Collection<ValidationIssue<?, ? super V>> getAllIssues() {
			if (allIssues == null) {
				allIssues = new ChainedCollection<ValidationIssue<?, ? super V>>(getAllInfoIssues(), getAllErrors(), getAllWarnings());
				allIssues.setDebugName("AllIssuesFor" + object);
				if (rootNode == ValidationNode.this) {
					System.out.println("Je viens de creer " + allIssues.hashCode() + " avec ");
					for (Collection c : allIssues.getCollections()) {
						System.out.println("> " + c.hashCode() + " : " + c);
					}
				}
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
			System.out.println("Registering in " + object + " issue > " + issue);
			// Thread.dumpStack();
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

	// protected long startTime2;
	// protected long intermediateTime2;
	// protected long endTime2;
	// protected long rulesNb;

	public ValidationReport(ValidationModel validationModel, Validable rootObject) throws InterruptedException {
		super();

		pcSupport = new PropertyChangeSupport(this);

		this.validationModel = validationModel;
		/*this.rootObject = rootObject;
		
		allIssues = new ArrayList<>();
		
		infoIssues = new ArrayList<>();
		errors = new ArrayList<>();
		warnings = new ArrayList<>();
		
		infoIssuesMap = new HashMap<>();
		errorsMap = new HashMap<>();
		warningsMap = new HashMap<>();*/

		/*startTime2 = System.currentTimeMillis();
		
		if (this.getClass().getSimpleName().contains("FMLValidationReport")) {
			DataBinding.dbValidated = 0;
		}*/

		System.out.println("On cree le root node");
		rootNode = new ValidationNode<Validable>(rootObject);
		nodes.put(rootObject, rootNode);
		System.out.println(">>>>>>>> START validation");
		rootNode.validate();
		System.out.println(">>>>>>>> STOP validation");
		System.out.println("All: " + getAllIssues().size() + " : " + getAllIssues());
		System.out.println("Errors: " + getAllErrors().size() + " : " + getAllErrors());
		System.out.println("Warnings: " + getAllWarnings().size() + " : " + getAllWarnings());
		System.out.println("InfoIssues: " + getAllInfoIssues().size() + " : " + getAllInfoIssues());

		System.out.println(rootNode.debug(0));

		Collection aVoir = getAllIssues();
		System.out.println("On regarde");

		// List<ValidationIssue<?, ?>> issues = performDeepValidation(rootObject);

		// intermediateTime2 = System.currentTimeMillis();

		/*if (rootNode.getAllIssues().size() == 0) {
			addToValidationIssues(new InformationIssue<>(rootObject, "consistency_check_ok"));
		}*/

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

	/*private List<ValidationIssue<?, ?>> performDeepValidation(Validable rootObject) throws InterruptedException {
	
		// rulesNb = 0;
	
		List<ValidationIssue<?, ?>> returned = new ArrayList<>();
	
		// Gets all the objects to validate and removes duplicated objects
		Set<Validable> objectsToValidate = new LinkedHashSet<>(retrieveAllEmbeddedValidableObjects(rootObject));
	
		// System.out.println("On trouve " + objectsToValidate.size() + " a valider");
	
		// long start = System.currentTimeMillis();
	
		// Compute validation steps and notify validation initialization
		long validationStepToNotify = objectsToValidate.stream().filter(validationModel::shouldNotifyValidation)
				.collect(Collectors.counting());
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
	
		// long end = System.currentTimeMillis();
		// System.out.println("Pour valider mes " + objectsToValidate.size() + " objects, j'ai mis " + (end - start) + " milliseconds");
	
		// Notify validation is finished
		getValidationModel().getPropertyChangeSupport().firePropertyChange(VALIDATION_END, null, rootObject);
	
		getPropertyChangeSupport().firePropertyChange("allIssues", null, getAllIssues());
		getPropertyChangeSupport().firePropertyChange("filteredIssues", null, getFilteredIssues());
		getPropertyChangeSupport().firePropertyChange("errors", null, getErrors());
		getPropertyChangeSupport().firePropertyChange("warnings", null, getWarnings());
		getPropertyChangeSupport().firePropertyChange("infoIssues", null, getInfoIssues());
	
		return returned;
	
	}*/

	/*private <V extends Validable> List<ValidationIssue<?, ?>> performValidation(V validable) {
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
				logger.fine("Applying rule " + rule.getRuleName() + " for " + validable);
			}
	
			if (getValidationModel().shouldNotifyValidationRules()) {
				getValidationModel().getPropertyChangeSupport().firePropertyChange(VALIDATE_WITH_RULE, null, rule);
			}
	
			ValidationIssue<?, ?> issue = performRuleValidation((ValidationRule) rule, validable);
			// rulesNb++;
	
			if (issue != null) {
				returned.add(issue);
			}
		}
	
		return returned;
	}*/

	/*private <R extends ValidationRule<R, V>, V extends Validable> ValidationIssue<R, V> performRuleValidation(R rule, V next) {
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
					addToValidationIssues(new InformationIssue<>(next, "fixed_automatically:" + " " + issue.getMessage() + " : "
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
							addToValidationIssues(new InformationIssue<>(containedIssue.getValidable(),
									"fixed_automatically:" + " " + containedIssue.getMessage() + " : "
											+ ((ProblemIssue<R, V>) containedIssue).getFixProposals().get(0).getMessage()));
						}
					}
				}
			}
		}
		return issue;
	}*/

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

		rootNode.revalidate();

		/*for (ValidationIssue<?, ?> issue : new ArrayList<>(allIssues)) {
			issue.delete();
		}*/

		/*allIssues.clear();
		
		infoIssues.clear();
		errors.clear();
		warnings.clear();
		
		infoIssuesMap.clear();
		errorsMap.clear();
		warningsMap.clear();
		
		List<ValidationIssue<?, ?>> issues = performDeepValidation(rootObject);
		if (issues.size() == 0) {
			addToValidationIssues(new InformationIssue<>(rootObject, "consistency_check_ok"));
		}*/
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

		/*Collection<ValidationIssue<?, ?>> allIssuesToRemove = issuesRegarding(validable);
		Collection<Validable> allEmbeddedValidableObjects = retrieveAllEmbeddedValidableObjects(validable);
		if (allEmbeddedValidableObjects != null) {
			for (Validable embeddedValidable : allEmbeddedValidableObjects) {
				allIssuesToRemove.addAll(issuesRegarding(embeddedValidable));
			}
		}
		for (ValidationIssue<?, ?> issue : new ArrayList<>(allIssuesToRemove)) {
			removeFromValidationIssues(issue);
		}
		
		if (!validable.isDeleted()) {
			performDeepValidation(validable);
		}*/

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

	/*public Collection<Validable> retrieveAllEmbeddedValidableObjects(Validable o) {
		List<Validable> returned = new ArrayList<>();
		appendAllEmbeddedValidableObjects(o, returned);
		return returned;
	}
	
	private void appendAllEmbeddedValidableObjects(Validable o, Collection<Validable> c) {
		if (o != null && !c.contains(o)) {
			c.add(o);
			Collection<Validable> embeddedObjects = o.getEmbeddedValidableObjects();
			if (embeddedObjects != null) {
				for (Validable o2 : embeddedObjects) {
					appendAllEmbeddedValidableObjects(o2, c);
				}
			}
		}
	}*/

	public Collection<? extends ValidationIssue<?, ?>> getFilteredIssues() {
		// System.out.println("On retourne les filtered pour mode=" + mode);
		switch (mode) {
			case ALL:
				/*System.out.println("On retourne " + getAllIssues() + " size=" + getAllIssues().size());
				System.out.println("Alors que errors=" + getAllErrors().hashCode() + " : " + getAllErrors());
				System.out.println("Alors que warnings=" + getAllWarnings().hashCode() + " : " + getAllWarnings());
				System.out.println("Alors que infos=" + getAllInfoIssues().hashCode() + " : " + getAllInfoIssues());
				ChainedCollection<?> cc = (ChainedCollection) getAllIssues();
				for (Collection c : cc.getCollections()) {
					System.out.println("> " + c.hashCode() + " : " + c);
				}*/
				return getAllIssues();
			// return getAllWarnings();
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

	public <V extends Validable> Collection<ValidationIssue<?, ? super V>> issuesRegarding(V object) {

		ValidationNode<V> validationNode = getValidationNode(object);
		if (validationNode != null) {
			return validationNode.getAllIssues();
		}
		return Collections.emptyList();
	}

	public <V extends Validable> Collection<InformationIssue<?, ? super V>> infoIssuesRegarding(V object) {
		ValidationNode<V> validationNode = getValidationNode(object);
		if (validationNode != null) {
			return validationNode.getAllInfoIssues();
		}
		return Collections.emptyList();
	}

	public <V extends Validable> Collection<ValidationError<?, ? super V>> errorIssuesRegarding(V object) {
		ValidationNode<V> validationNode = getValidationNode(object);
		if (validationNode != null) {
			return validationNode.getAllErrors();
		}
		return Collections.emptyList();
	}

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

	/*public String localizedForKey(String key) {
		if (validationModel != null) {
			return validationModel.localizedForKey(key);
		}
		return key;
	}*/
}
