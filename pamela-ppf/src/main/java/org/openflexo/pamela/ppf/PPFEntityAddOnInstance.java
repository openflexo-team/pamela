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

import org.openflexo.pamela.addon.EntityAddOnInstance;
import org.openflexo.pamela.factory.PamelaModel;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;

/**
 * Represents an instance of {@link PPFEntityAddOn} applied to a given {@link PamelaModel}
 * 
 * @author sylvain
 *
 * @param <I>
 */
public class PPFEntityAddOnInstance<I> extends EntityAddOnInstance<I, PPFEntityAddOn<I>, PPFAddOn> {

	private static final Logger logger = Logger.getLogger(PPFEntityAddOnInstance.class.getPackage().getName());

	private Map<ModelProperty<? super I>, List<PropertyPredicateInstance<? super I>>> predicateInstances;

	public PPFEntityAddOnInstance(PPFEntityAddOn<I> ppfEntityAddOn, PamelaModel model) {
		super(ppfEntityAddOn, model);
		predicateInstances = new HashMap<>();
		for (ModelProperty<? super I> modelProperty : ppfEntityAddOn.getPredicates().keySet()) {
			List<PropertyPredicateInstance<? super I>> l = new ArrayList<>();
			predicateInstances.put(modelProperty, l);
			for (PropertyPredicate<? super I> propertyPredicate : ppfEntityAddOn.getPredicates().get(modelProperty)) {
				l.add(propertyPredicate.makeInstance(model));
			}
		}
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
		for (ModelProperty<? super I> modelProperty : predicateInstances.keySet()) {
			checkPredicatesForProperty(modelProperty, proxyMethodHandler);
		}
	}

	private void checkPredicatesForProperty(ModelProperty<? super I> modelProperty, ProxyMethodHandler<I> proxyMethodHandler)
			throws PPFViolationException {
		List<PropertyPredicateInstance<? super I>> propertyPredicateInstances = predicateInstances.get(modelProperty);
		if (propertyPredicateInstances != null) {
			for (PropertyPredicateInstance<? super I> propertyPredicateInstance : propertyPredicateInstances) {
				checkPredicate(propertyPredicateInstance, proxyMethodHandler);
			}
		}
	}

	private void checkPredicate(PropertyPredicateInstance<? super I> propertyPredicateInstance, ProxyMethodHandler<I> proxyMethodHandler)
			throws PPFViolationException {
		propertyPredicateInstance.check(proxyMethodHandler);
	}

	@Override
	public <T> void notifiedNewInstance(T newInstance, ModelEntity<T> modelEntity, PamelaModelFactory modelFactory) {
		if (modelEntity == getEntityAddOn().getModelEntity()) {
			for (List<PropertyPredicateInstance<? super I>> listPredicates : predicateInstances.values()) {
				for (PropertyPredicateInstance<? super I> propertyPredicate : listPredicates) {
					propertyPredicate.notifiedNewSourceInstance((I) newInstance, (ModelEntity) modelEntity, modelFactory);
				}
			}
		}
		for (ModelProperty<? super I> modelProperty : predicateInstances.keySet()) {
			if (modelProperty.getAccessedEntity() == modelEntity) {
				for (PropertyPredicateInstance<? super I> propertyPredicateInstance : predicateInstances.get(modelProperty)) {
					propertyPredicateInstance.notifiedNewDestinationInstance(newInstance, modelEntity, modelFactory);
				}
			}
		}

	}
}
