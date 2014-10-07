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

import org.openflexo.antar.binding.TypeUtils;
import org.openflexo.model.ModelContext;
import org.openflexo.model.ModelEntity;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Used to store and manage a set of {@link ValidationRule} associated to some types<br>
 * {@link ValidationRule} discovering is based on PAMELA models annotated with {@link DefineValidationRule} annotations
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
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

		ruleSets = new HashMap<Class<?>, ValidationRuleSet<?>>();

		try {
			searchAndRegisterValidationRules(modelContext);
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void searchAndRegisterValidationRules(ModelContext modelContext) throws ModelDefinitionException {

		validationModelFactory = new ModelFactory(modelContext);

		Iterator<ModelEntity> it = modelContext.getEntities();

		while (it.hasNext()) {
			ModelEntity e = it.next();
			// System.out.println("assertTrue(validationModel.getValidationModelFactory().getModelContext().getModelEntity("
			// + e.getImplementedInterface().toString().substring(10) + ".class) != null);");
			Class i = e.getImplementedInterface();
			ruleSets.put(i, new ValidationRuleSet<Validable>(i));
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

			} else {
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
	 */
	public ValidationReport validate(Validable object) {

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
		return validate(object).getErrorsCount() == 0;
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
			sortedClasses = new ArrayList<Class<?>>();
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

	private class ClassComparator implements Comparator<Class> {
		private final Collator collator;

		ClassComparator() {
			collator = Collator.getInstance();
		}

		@Override
		public int compare(Class o1, Class o2) {
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

	public final String localizedIssueMessage(ValidationIssue<?, ?> issue) {
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

	public final String localizedFixProposal(FixProposal<?, ?> proposal) {
		if (proposal == null) {
			return null;
		}
		return localizedInContext(proposal.getMessage(), proposal);
	}

	public static String asBindingExpression(String localized) {
		boolean someReplacementsWerePerformed = false;
		while (localized.contains("($")) {
			someReplacementsWerePerformed = true;
			int startIndex = localized.indexOf("($");
			int p = 1;
			int endIndex = -1;
			for (int i = startIndex + 2; i < localized.length(); i++) {
				if (localized.charAt(i) == '(') {
					p++;
				} else if (localized.charAt(i) == ')') {
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
