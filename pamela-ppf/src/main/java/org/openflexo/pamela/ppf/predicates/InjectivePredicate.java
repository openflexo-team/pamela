/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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
package org.openflexo.pamela.ppf.predicates;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.openflexo.pamela.factory.PamelaModel;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;
import org.openflexo.pamela.ppf.PPFViolationException;
import org.openflexo.pamela.ppf.PropertyPredicate;
import org.openflexo.pamela.ppf.PropertyPredicateInstance;
import org.openflexo.toolbox.HasPropertyChangeSupport;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * "Injective" predicate : two different objects cannot have the same value<br>
 * (for all x,x' in X, f(x)=f(x') => x=x')
 * 
 * @author sylvain
 *
 */
public class InjectivePredicate<I> extends PropertyPredicate<I> {

	private static final Logger logger = Logger.getLogger(InjectivePredicate.class.getPackage().getName());

	public InjectivePredicate(ModelProperty<I> property) {
		super(property);
	}

	@Override
	public InjectivePredicateInstance makeInstance(PamelaModel model) {
		return new InjectivePredicateInstance(model);
	}

	public class InjectivePredicateInstance extends PropertyPredicateInstance<I> implements PropertyChangeListener {

		public InjectivePredicateInstance(PamelaModel model) {
			super(InjectivePredicate.this, model);
		}

		@Override
		public void check(ProxyMethodHandler<? extends I> proxyMethodHandler) {
			logger.info("Checking InjectivePredicate for " + getProperty() + " and object " + proxyMethodHandler.getObject());
			Object value = proxyMethodHandler.invokeGetter(getProperty());

			Collection<? extends I> sources = proxyMethodHandler.getModelFactory().getModel()
					.getInstances(proxyMethodHandler.getModelEntity());
			Collection<?> destinations = proxyMethodHandler.getModelFactory().getModel().getInstances(getProperty().getAccessedEntity());

			// System.out.println("sources: " + sources);
			// System.out.println("destinations: " + destinations);

			if (!duplicates.isEmpty()) {
				String message = null;
				for (Object valueCommonToMultipleObjects : new ArrayList<>(duplicates.keySet())) {
					if (duplicates.get(valueCommonToMultipleObjects).contains(proxyMethodHandler.getObject())) {
						message = "Multiple objects sharing value " + valueCommonToMultipleObjects + " for injective property "
								+ getProperty() + " : " + duplicates.get(valueCommonToMultipleObjects);
					}
				}
				if (message == null) {
					message = "Multiple objects for injective property " + getProperty() + " : " + duplicates;
				}
				throw new PPFViolationException(message, proxyMethodHandler);
			}

		}

		@Override
		public void notifiedNewSourceInstance(I newInstance, ModelEntity<I> modelEntity, PamelaModelFactory modelFactory) {
			// System.out.println("******* nouvelle source: " + newInstance);
			if (newInstance instanceof HasPropertyChangeSupport) {
				((HasPropertyChangeSupport) newInstance).getPropertyChangeSupport()
						.addPropertyChangeListener(getProperty().getPropertyIdentifier(), this);
			}
		}

		@Override
		public <T> void notifiedNewDestinationInstance(T newInstance, ModelEntity<T> modelEntity, PamelaModelFactory modelFactory) {
			// System.out.println("******* nouvelle destination: " + newInstance);
		}

		private BiMap<I, Object> propertyBiMap = HashBiMap.create();
		private Map<Object, Set<I>> duplicates = new Hashtable<>();

		@Override
		public void propertyChange(PropertyChangeEvent evt) {

			// System.out.println("propertyChange() with " + evt.getPropertyName() + " evt: " + evt);

			I sourceObject = (I) evt.getSource();
			ProxyMethodHandler<? extends I> proxyMethodHandler = getHandler(sourceObject);
			Object propertyValue = proxyMethodHandler.invokeGetter(getProperty());
			// Look if some duplicates may be not duplicates anymore
			for (Object valueCommonToMultipleObjects : new ArrayList<>(duplicates.keySet())) {
				if (!valueCommonToMultipleObjects.equals(propertyValue)) {
					Set<I> objectsSharingSameValue = duplicates.get(valueCommonToMultipleObjects);
					if (objectsSharingSameValue.contains(sourceObject)) {
						objectsSharingSameValue.remove(sourceObject);
						if (objectsSharingSameValue.size() <= 1) {
							// No more duplicates for value
							duplicates.remove(valueCommonToMultipleObjects);
						}
					}
				}

			}
			try {
				propertyBiMap.put(sourceObject, propertyValue);
			} catch (IllegalArgumentException e) {
				// Value already present
				I opposite = propertyBiMap.inverse().get(propertyValue);
				Set<I> objectsWithSameValues = duplicates.get(propertyValue);
				if (objectsWithSameValues == null) {
					objectsWithSameValues = new HashSet<>();
					duplicates.put(propertyValue, objectsWithSameValues);
				}
				objectsWithSameValues.add(sourceObject);
				objectsWithSameValues.add(opposite);
			}

		}

	}

}
