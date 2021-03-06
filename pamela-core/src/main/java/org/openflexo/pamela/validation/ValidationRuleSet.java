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
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * This is the set of rules beeing applicable to any instance of V class<br>
 * Inheritance is managed here
 * 
 * @author sylvain
 * 
 * @param <V>
 *            type of Validable
 */
public class ValidationRuleSet<V extends Validable> implements HasPropertyChangeSupport, Iterable<ValidationRule<?, ? super V>> {

	private static final Logger logger = Logger.getLogger(ValidationRuleSet.class.getPackage().getName());

	public static final String DELETED_PROPERTY = "deleted";

	private final Class<V> declaredType;

	/**
	 * This is the list of explicitely declared ValidationRule associated to declared type (not inherited one).
	 */
	private final List<ValidationRule<?, V>> declaredRules;

	private final List<ValidationRuleSet<? super V>> parentRuleSets;

	// Unused private ChainedCollection<ValidationRule<?, ? super V>> allRules;

	private final PropertyChangeSupport pcSupport;

	public ValidationRuleSet(Class<V> type) {
		super();

		pcSupport = new PropertyChangeSupport(this);

		declaredType = type;
		declaredRules = new ArrayList<>();
		parentRuleSets = new ArrayList<>();

		for (Class<?> c : type.getDeclaredClasses()) {
			DefineValidationRule annotation = c.getAnnotation(DefineValidationRule.class);
			if (annotation != null && ValidationRule.class.isAssignableFrom(c)) {
				try {
					@SuppressWarnings("unchecked")
					Class<? extends ValidationRule<?, V>> validationRuleClass = (Class<? extends ValidationRule<?, V>>) c;
					// System.out.println("Found validation rule: " + validationRuleClass);
					Constructor<? extends ValidationRule<?, V>> constructor;
					constructor = validationRuleClass.getConstructor();
					ValidationRule<?, V> rule = constructor.newInstance();
					declaredRules.add(rule);
				} catch (Exception e) {
					e.printStackTrace();
					logger.warning("Unexpected exception: " + e.getMessage() + " Cannot add rule " + c);
				}
			}
		}
	}

	public List<ValidationRuleSet<? super V>> getParentRuleSets() {
		return parentRuleSets;
	}

	protected void addParentRuleSet(ValidationRuleSet<? super V> parentRuleSet) {
		parentRuleSets.add(parentRuleSet);
		getPropertyChangeSupport().firePropertyChange("rulesCount", getRulesCount() - parentRuleSet.getRulesCount(), getRulesCount());
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		return DELETED_PROPERTY;
	}

	public void delete() {
		declaredRules.clear();
		parentRuleSets.clear();
		if (getPropertyChangeSupport() != null) {
			getPropertyChangeSupport().firePropertyChange(DELETED_PROPERTY, this, null);
		}
	}

	/**
	 * Return declared rules for specified type<br>
	 * Does not return inherited rules
	 * 
	 * @return
	 */
	public List<ValidationRule<?, V>> getDeclaredRules() {
		return declaredRules;
	}

	/**
	 * Build and return a collection of all rules for specified type<br>
	 * Does return inherited rules (This method is really costfull and should not be called in a performance context, use
	 * getSize()/getElementAt(int) instead)
	 * 
	 * FD: Beware, this method is necessary due to the way some GUI component get the validation rules, in normal code I would advocate the
	 * use of the iterator instead
	 * 
	 * @return
	 */
	public List<ValidationRule<?, ? super V>> getRules() {
		List<ValidationRule<?, ? super V>> returned = new ArrayList<>();
		for (int i = 0; i < getRulesCount(); i++) {
			returned.add(getRuleAt(i));
		}
		return returned;
	}

	public boolean containsRuleClass(Class<? extends ValidationRule/*<?, ?>*/> ruleClass) {
		for (int i = 0; i < getRulesCount(); i++) {
			if (getRuleAt(i).getClass().equals(ruleClass)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Implements
	 * 
	 * @see javax.swing.ListModel#getSize()
	 */
	public int getRulesCount() {
		int returned = getDeclaredRules().size();
		for (ValidationRuleSet<?> parentRuleSet : parentRuleSets) {
			returned += parentRuleSet.getRulesCount();
		}
		return returned;
	}

	private ValidationRule<?, ? super V> getRuleAt(int index) {
		if (index < 0) {
			return null;
		}
		if (index < getDeclaredRules().size()) {
			return getDeclaredRules().get(index);
		}
		int localIndex = index - getDeclaredRules().size();
		for (ValidationRuleSet<? super V> parentRuleSet : parentRuleSets) {
			if (localIndex < parentRuleSet.getRulesCount()) {
				return parentRuleSet.getRuleAt(localIndex);
			}
			localIndex = localIndex - parentRuleSet.getRulesCount();
		}

		logger.warning("Could not find ValidationRule at index " + index);
		return null;
	}

	public Class<V> getDeclaredType() {
		return declaredType;
	}

	public String getTypeName() {
		if (getDeclaredType() != null) {
			return getDeclaredType().getSimpleName();
		}
		return null;
	}

	@Override
	public Iterator<ValidationRule<?, ? super V>> iterator() {
		return new Iterator<ValidationRule<?, ? super V>>() {
			private int index = 0;

			@Override
			public boolean hasNext() {
				return this.index < getRulesCount();
			}

			@Override
			public ValidationRule<?, ? super V> next() {
				if (!hasNext())
					throw new NoSuchElementException();
				if (this.index < getDeclaredRules().size()) {
					ValidationRule<?, ? super V> result = getDeclaredRules().get(index);
					this.index++;
					return result;
				}
				int localIndex = index - getDeclaredRules().size();
				for (ValidationRuleSet<? super V> parentRuleSet : parentRuleSets) {
					if (localIndex < parentRuleSet.getRulesCount()) {
						this.index++;
						return parentRuleSet.getRuleAt(localIndex);
					}
					localIndex = localIndex - parentRuleSet.getRulesCount();
				}
				return null;
			}
		};
	}
}
