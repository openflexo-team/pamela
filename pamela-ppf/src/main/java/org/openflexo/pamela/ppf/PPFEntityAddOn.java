/**
 * 
 * Copyright (c) 2013-2020, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of pamela-core, a component of the software infrastructure 
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
package org.openflexo.pamela.ppf;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.openflexo.pamela.addon.EntityAddOn;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;
import org.openflexo.pamela.ppf.annotations.Card;
import org.openflexo.pamela.ppf.annotations.Irreflexive;
import org.openflexo.pamela.ppf.annotations.NonEmpty;
import org.openflexo.pamela.ppf.annotations.NonNull;
import org.openflexo.pamela.ppf.predicates.CardPredicate;
import org.openflexo.pamela.ppf.predicates.IrreflexivePredicate;
import org.openflexo.pamela.ppf.predicates.NonEmptyPredicate;
import org.openflexo.pamela.ppf.predicates.NonNullPredicate;

/**
 * Extends {@link ModelEntity} by providing required information and behaviour required for managing PPF in the context of a ModelEntity
 * 
 * @author sylvain
 *
 * @param <I>
 */
public class PPFEntityAddOn<I> extends EntityAddOn<I, PPFAddOn> {

	private static final Logger logger = Logger.getLogger(PPFEntityAddOn.class.getPackage().getName());

	private Map<ModelProperty<? super I>, List<PropertyPredicate<? super I>>> predicates;

	public static boolean hasPPFAnnotations(Class<?> aClass) {
		for (Method method : aClass.getDeclaredMethods()) {
			if (PropertyPredicate.hasPPFAnnotations(method)) {
				return true;
			}
		}
		return false;
	}

	public PPFEntityAddOn(ModelEntity<I> modelEntity, PPFAddOn ppfAddOn) throws ModelDefinitionException {
		super(modelEntity, ppfAddOn);
		predicates = new HashMap<>();
		discoverPredicates();
	}

	private void discoverPredicates() throws ModelDefinitionException {
		for (Method m : getImplementedInterface().getDeclaredMethods()) {
			discoverPredicates(m);
		}
	}

	private void discoverPredicates(Method method) throws ModelDefinitionException {
		if (PropertyPredicate.hasPPFAnnotations(method)) {
			ModelProperty<? super I> modelProperty = getModelEntity().getPropertyForMethod(method);
			if (modelProperty == null) {
				logger.warning("Found PPF annotation in non-property method " + method);
				return;
			}
			List<PropertyPredicate<? super I>> predicatesForProperty = predicates.get(modelProperty);
			if (predicatesForProperty == null) {
				predicatesForProperty = new ArrayList<>();
				predicates.put(modelProperty, predicatesForProperty);
			}
			if (method.isAnnotationPresent(NonNull.class)) {
				predicatesForProperty.add(new NonNullPredicate<>(modelProperty));
			}
			if (method.isAnnotationPresent(NonEmpty.class) && modelProperty.getCardinality() == Cardinality.LIST) {
				predicatesForProperty.add(new NonEmptyPredicate<>(modelProperty));
			}
			if (method.isAnnotationPresent(Card.class) && modelProperty.getCardinality() == Cardinality.LIST) {
				predicatesForProperty.add(new CardPredicate<>(modelProperty, method.getAnnotation(Card.class)));
			}
			if (method.isAnnotationPresent(Irreflexive.class)) {
				predicatesForProperty.add(new IrreflexivePredicate<>(modelProperty));
			}
		}
	}

	/**
	 * Indicates if supplied method should be intercepted in the context of JML : only methods with JML definition are concerned
	 * 
	 * @param method
	 * @return
	 */
	@Override
	public boolean isMethodToBeIntercepted(Method method) {
		return isMethodToBeMonitored(method);
	}

	/**
	 * Indicates if supplied method execution should trigger a monitoring : only methods with JML definition are concerned
	 * 
	 * @param method
	 * @return
	 */
	@Override
	public boolean isMethodToBeMonitored(Method method) {
		ModelProperty<? super I> modelProperty = getModelEntity().getPropertyForMethod(method);
		if (modelProperty != null) {
			return predicates.get(modelProperty) != null;
		}
		return false;
	}

	@Override
	public void checkOnMethodEntry(Method method, ProxyMethodHandler<I> proxyMethodHandler, Object[] args) throws PPFViolationException {
		checkAllPredicates(proxyMethodHandler);
	}

	@Override
	public void checkOnMethodExit(Method method, ProxyMethodHandler<I> proxyMethodHandler, Object[] args) throws PPFViolationException {
		checkAllPredicates(proxyMethodHandler);
	}

	private void checkAllPredicates(ProxyMethodHandler<I> proxyMethodHandler) throws PPFViolationException {
		for (ModelProperty<? super I> modelProperty : predicates.keySet()) {
			checkPredicatesForProperty(modelProperty, proxyMethodHandler);
		}
	}

	private void checkPredicatesForProperty(ModelProperty<? super I> modelProperty, ProxyMethodHandler<I> proxyMethodHandler)
			throws PPFViolationException {
		List<PropertyPredicate<? super I>> propertyPredicates = predicates.get(modelProperty);
		if (propertyPredicates != null) {
			for (PropertyPredicate<? super I> propertyPredicate : propertyPredicates) {
				checkPredicate(propertyPredicate, proxyMethodHandler);
			}
		}
	}

	private void checkPredicate(PropertyPredicate<? super I> propertyPredicate, ProxyMethodHandler<I> proxyMethodHandler)
			throws PPFViolationException {
		propertyPredicate.check(proxyMethodHandler);
	}

}
