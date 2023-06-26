/**
 * 
 * Copyright (c) 2013-2020, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of pamela-security-patterns, a component of the software infrastructure 
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

package org.openflexo.pamela.ppf.patterns.xor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.factory.PamelaModel;
import org.openflexo.pamela.factory.PamelaUtils;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;
import org.openflexo.pamela.patterns.PatternInstance;
import org.openflexo.pamela.patterns.ReturnWrapper;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Represent an instance of a given {@link XOrAssociationDefinition}
 * 
 * @author Sylvain Guerin
 *
 */
public class XOrAssociationInstance<I> extends PatternInstance<XOrAssociationDefinition<I>> implements PropertyChangeListener {

	private final I entity;

	public XOrAssociationInstance(XOrAssociationDefinition<I> definition, PamelaModel model, I entity)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		super(definition, model);
		this.entity = entity;
		registerStakeHolder(entity, XOrAssociationDefinition.SOURCE_ROLE);
		if (entity instanceof HasPropertyChangeSupport) {
			((HasPropertyChangeSupport) entity).getPropertyChangeSupport().addPropertyChangeListener(this);
		}
	}

	public boolean isValid() {
		// Perform here required checks
		return entity != null;
	}

	public I getEntity() {
		return entity;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == entity) {
			System.out.println("propertyChange from subject " + evt.getPropertyName() + " evt=" + evt);
		}
	}

	/**
	 * Method called before every method of interest is about to be invoked. Performs the execution, if relevant.
	 * 
	 * @param instance
	 *            Object on which the method is called
	 * @param method
	 *            Called method
	 * @param args
	 * @return a {@link ReturnWrapper} wrapping true if the execution of the invoke should go one after the call, false if not.
	 * @throws InvocationTargetException
	 *             if an error occurred when internally invoking a method
	 * @throws IllegalAccessException
	 *             if an error occurred when internally invoking a method
	 * @throws NoSuchMethodException
	 *             if an error occurred when internally invoking a method
	 */
	@Override
	public ReturnWrapper processMethodBeforeInvoke(Object instance, Method method, Object[] args)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		// We never check on entry
		return new ReturnWrapper(true, null);
	}

	@Override
	public void processMethodAfterInvoke(Object instance, Method method, Object returnValue, Object[] args)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {

		if (isChecking) {
			// Avoid stack overflow
			return;
		}

		if (PamelaUtils.methodIsEquivalentTo(method, getPatternDefinition().property1.getGetterMethod())) {
			return;
		}
		if (PamelaUtils.methodIsEquivalentTo(method, getPatternDefinition().property2.getGetterMethod())) {
			return;
		}

		ModelEntity modelEntity = getModelEntity(instance);
		if (modelEntity != null && isAssertionCheckingEnabled(instance) && modelEntity.isMethodToBeMonitored(method)) {
			checkAfterInvoke((I) instance, method, returnValue, args);
		}
	}

	private boolean isChecking = false;

	/**
	 * Method called before after all method invoke. It performs the invariant and postcondition checks.
	 * 
	 * @param method
	 *            Method which will be invoked
	 * @param returnValue
	 *            returnValue of the method
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	void checkAfterInvoke(I instance, Method method, Object returnValue, Object[] args)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (isValid()) {
			isChecking = true;
			try {
				checkXor(instance);
			} finally {
				isChecking = false;
			}
		}

	}

	private boolean checkXor(I instance) {

		ProxyMethodHandler<I> h = getProxyMethodHandler(instance);
		ModelProperty<? super I> p1 = getPatternDefinition().property1;
		ModelProperty<? super I> p2 = getPatternDefinition().property2;

		if (getPatternDefinition().property1.getCardinality() == Cardinality.SINGLE) {
			Object o1 = h.invokeGetter(getPatternDefinition().property1);
			if (getPatternDefinition().property1.getCardinality() == Cardinality.SINGLE) {
				Object o2 = h.invokeGetter(getPatternDefinition().property2);
				if (o1 == null && o2 == null) {
					throw new ModelExecutionException("XOrAssociation Violation: " + p1.getPropertyIdentifier() + " and "
							+ p2.getPropertyIdentifier() + " are both null for " + instance);
				}
				if (o1 != null && o2 != null) {
					throw new ModelExecutionException("XOrAssociation Violation: " + p1.getPropertyIdentifier() + " and "
							+ p2.getPropertyIdentifier() + " are both not null for " + instance);
				}
			}
			else {
				List<?> l2 = (List) h.invokeGetter(getPatternDefinition().property2);
				if (o1 == null && (l2 == null) || l2.isEmpty()) {
					throw new ModelExecutionException("XOrAssociation Violation: " + p1.getPropertyIdentifier() + " and "
							+ p2.getPropertyIdentifier() + " are both null for " + instance);
				}
				if (o1 != null && (l2 != null && l2.contains(o1))) {
					throw new ModelExecutionException("XOrAssociation Violation: " + p1.getPropertyIdentifier() + " and "
							+ p2.getPropertyIdentifier() + " are intersecting for " + instance + " with " + o1);
				}
			}
		}
		else {
			List<?> l1 = (List) h.invokeGetter(getPatternDefinition().property1);
			if (getPatternDefinition().property1.getCardinality() == Cardinality.SINGLE) {
				Object o2 = h.invokeGetter(getPatternDefinition().property2);
				if (o2 == null && (l1 == null) || l1.isEmpty()) {
					throw new ModelExecutionException("XOrAssociation Violation: " + p1.getPropertyIdentifier() + " and "
							+ p2.getPropertyIdentifier() + " are both null for " + instance);
				}
				if (o2 != null && (l1 != null && l1.contains(o2))) {
					throw new ModelExecutionException("XOrAssociation Violation: " + p1.getPropertyIdentifier() + " and "
							+ p2.getPropertyIdentifier() + " are intersecting for " + instance + " with " + o2);
				}
			}
			else {
				List<?> l2 = (List) h.invokeGetter(getPatternDefinition().property2);
				if (((l1 == null) || l1.isEmpty()) && ((l2 == null) || l2.isEmpty())) {
					throw new ModelExecutionException("XOrAssociation Violation: " + p1.getPropertyIdentifier() + " and "
							+ p2.getPropertyIdentifier() + " are both empty for " + instance);
				}
				for (Object o1 : l1) {
					if (l2 != null && l2.contains(o1)) {
						throw new ModelExecutionException("XOrAssociation Violation: " + p1.getPropertyIdentifier() + " and "
								+ p2.getPropertyIdentifier() + " are intersecting for " + instance + " with " + o1);
					}
				}
			}
		}
		return true;
	}

}
