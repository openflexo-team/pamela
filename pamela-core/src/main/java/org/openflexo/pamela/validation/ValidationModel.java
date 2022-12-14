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

import java.beans.PropertyChangeSupport;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Used to store and manage a set of {@link ValidationRule} associated to some types<br>
 * {@link ValidationRule} discovering is based on PAMELA models annotated with {@link DefineValidationRule} annotations<br>
 * Note that class inheritance is supported
 * 
 * @author sylvain
 * 
 */
public abstract class ValidationModel implements HasPropertyChangeSupport {

	private static final Logger logger = Logger.getLogger(ValidationModel.class.getPackage().getName());

	public static final String DELETED_PROPERTY = "deleted";

	private final Map<Class<?>, ValidationRuleSet<?>> ruleSets;
	private ModelFactory validationModelFactory;
	private List<Class<?>> sortedClasses;
	private ValidationRuleFilter ruleFilter = null;

	private final PropertyChangeSupport pcSupport;

	public ValidationModel(ModelContext modelContext) {
		super();

		pcSupport = new PropertyChangeSupport(this);

		ruleSets = new HashMap<>();

		searchAndRegisterValidationRules(modelContext);
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		return DELETED_PROPERTY;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void searchAndRegisterValidationRules(ModelContext modelContext) {

		validationModelFactory = new ModelFactory(modelContext);

		Iterator<ModelEntity> it = modelContext.getEntities();

		while (it.hasNext()) {
			ModelEntity e = it.next();
			// System.out.println("assertTrue(validationModel.getValidationModelFactory().getModelContext().getModelEntity("
			// + e.getImplementedInterface().toString().substring(10) + ".class) != null);");
			Class i = e.getImplementedInterface();
			ruleSets.put(i, new ValidationRuleSet<>(i));
		}

		// Now manage inheritance
		it = modelContext.getEntities();
		while (it.hasNext()) {
			ModelEntity e = it.next();
			// System.out.println("assertTrue(validationModel.getValidationModelFactory().getModelContext().getModelEntity("
			// + e.getImplementedInterface().toString().substring(10) + ".class) != null);");
			Class i = e.getImplementedInterface();
			manageInheritanceFor(i, ruleSets.get(i));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void manageInheritanceFor(Class<?> cl, ValidationRuleSet<?> originRuleSet) {
		for (Class<?> superInterface : cl.getInterfaces()) {
			if (ruleSets.get(superInterface) != null && originRuleSet.getDeclaredType() != superInterface) {
				// System.out.println("Found " + originRuleSet.getDeclaredType() + " inherits from " + superInterface);
				originRuleSet.addParentRuleSet((ValidationRuleSet) ruleSets.get(superInterface));

			}
			else {
				manageInheritanceFor(superInterface, originRuleSet);
			}
		}
	}

	public ModelFactory getValidationModelFactory() {
		return validationModelFactory;
	}

	/**
	 * Validates supplied {@link Validable}<br>
	 * Found issues are appened in a newly created ValidationReport.<br>
	 * Supplied validation model is used to perform this validation.
	 * 
	 * @param validable
	 * @param validationModel
	 * @return the ValidationReport, object on which found issues are appened
	 * @throws InterruptedException
	 */
	public ValidationReport validate(Validable object) throws InterruptedException {

		return new ValidationReport(this, object);
	}

	/**
	 * Validate supplied Validable object by returning boolean indicating if validation throw errors (warnings are not considered as invalid
	 * model).
	 * 
	 * @param object
	 * @return
	 */
	public boolean isValid(Validable object) {
		try {
			return validate(object).getAllErrors().size() == 0;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}

	public <V extends Validable> ValidationRuleSet<? super V> getRuleSet(V validable) {
		return (ValidationRuleSet<? super V>) getRuleSet(validable.getClass());
	}

	public <V extends Validable> ValidationRuleSet<? super V> getRuleSet(Class<V> validableClass) {
		return (ValidationRuleSet<? super V>) TypeUtils.objectForClass(validableClass, ruleSets, false);
	}

	public ValidationRuleSet<?> getGenericRuleSet(Class<?> validableClass) {
		if (validableClass == null) {
			return null;
		}
		if (Validable.class.isAssignableFrom(validableClass)) {
			return getRuleSet((Class<? extends Validable>) validableClass);
		}
		return null;
	}

	/**
	 * Return a boolean indicating if validation of supplied object must be notified
	 * 
	 * @param next
	 * @return a boolean
	 */
	protected abstract boolean shouldNotifyValidation(Validable next);

	/**
	 * Return a boolean indicating if validation of each rule must be notified
	 * 
	 * @param next
	 * @return a boolean
	 */
	protected boolean shouldNotifyValidationRules() {
		return false;
	}

	public abstract boolean fixAutomaticallyIfOneFixProposal();

	public List<Class<?>> getSortedClasses() {
		if (sortedClasses == null) {
			sortedClasses = new ArrayList<>();
			sortedClasses.addAll(ruleSets.keySet());
			Collections.sort(sortedClasses, new ClassComparator());
		}
		return sortedClasses;
	}

	public ValidationRuleFilter getRuleFilter() {
		return ruleFilter;
	}

	public void setRuleFilter(ValidationRuleFilter ruleFilter) {
		if (this.ruleFilter != ruleFilter) {
			this.ruleFilter = ruleFilter;
			for (Class<?> c : getSortedClasses()) {
				ValidationRuleSet<?> ruleSet = getRuleSet((Class<? extends Validable>) c);
				for (ValidationRule<?, ?> rule : ruleSet.getDeclaredRules()) {
					rule.setIsEnabled(ruleFilter != null ? ruleFilter.accept(rule) : true);
				}
			}
		}
	}

	private class ClassComparator implements Comparator<Class<?>> {
		private final Collator collator;

		ClassComparator() {
			collator = Collator.getInstance();
		}

		@Override
		public int compare(Class<?> o1, Class<?> o2) {
			String className1 = null;
			String className2 = null;
			StringTokenizer st1 = new StringTokenizer(o1.getName(), ".");
			while (st1.hasMoreTokens()) {
				className1 = st1.nextToken();
			}
			StringTokenizer st2 = new StringTokenizer(o2.getName(), ".");
			while (st2.hasMoreTokens()) {
				className2 = st2.nextToken();
			}
			if (className1 != null && className2 != null) {
				return collator.compare(className1, className2);
			}
			return 0;
		}

	}

	public abstract String localizedInContext(String key, Object context);

	public final String localizedRuleName(ValidationRule<?, ?> validationRule) {
		if (validationRule == null) {
			return null;
		}
		return localizedInContext(validationRule.getRuleName(), validationRule);
	}

	public final String localizedRuleDescription(ValidationRule<?, ?> validationRule) {
		if (validationRule == null) {
			return null;
		}
		return localizedInContext(validationRule.getRuleDescription(), validationRule);
	}

	public String localizedIssueMessage(ValidationIssue<?, ?> issue) {
		if (issue == null) {
			return null;
		}
		return localizedInContext(issue.getMessage(), issue);
	}

	public final String localizedIssueDetailedInformations(ValidationIssue<?, ?> issue) {
		if (issue == null) {
			return null;
		}
		return localizedInContext(issue.getDetailedInformations(), issue);
	}

	// We dont type here, otherwise it does not work on FixIssuePanel.fib
	public final String localizedFixProposal(FixProposal proposal) {
		if (proposal == null) {
			return null;
		}
		return localizedInContext(proposal.getMessage(), proposal);
	}

	public static String asBindingExpression(String localized) {
		boolean someReplacementsWerePerformed = false;
		if (localized == null) {
			return "null";
		}
		while (localized.contains("($")) {
			someReplacementsWerePerformed = true;
			int startIndex = localized.indexOf("($");
			int p = 1;
			int endIndex = -1;
			for (int i = startIndex + 2; i < localized.length(); i++) {
				if (localized.charAt(i) == '(') {
					p++;
				}
				else if (localized.charAt(i) == ')') {
					p--;
				}
				if (p == 0) {
					endIndex = i;
					break;
				}
			}

			localized = localized.substring(0, startIndex) + "\"+" + localized.substring(startIndex + 2, endIndex) + "+\""
					+ localized.substring(endIndex + 1);
		}
		if (someReplacementsWerePerformed) {
			localized = '"' + localized + '"';
		}
		return localized;
	}

	public static void main(String[] args) {
		test("coucou");
		test("coucou ($coucou)");
		test("coucou ($coucou) coucou2");
		test("coucou ($coucou)");

		test("binding_'($binding.bindingName)'_is_not_valid: ($binding)");
	}

	private static void test(String s) {
		System.out.println("for s=[" + s + "] get [" + asBindingExpression(s) + "]");
	}
}
