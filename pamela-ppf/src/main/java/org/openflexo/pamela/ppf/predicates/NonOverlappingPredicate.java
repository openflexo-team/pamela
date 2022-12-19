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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
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

/**
 * "Non-overlapping" predicate : two different objects cannot have the same partial values (applies to multiple cardinalities)<br>
 * (for all x,x' in X, f(x) intersect f(x') != emptySet => x=x')
 * 
 * @author sylvain
 *
 */
public class NonOverlappingPredicate<I> extends PropertyPredicate<I> {

	private static final Logger logger = Logger.getLogger(NonOverlappingPredicate.class.getPackage().getName());

	public NonOverlappingPredicate(ModelProperty<I> property) {
		super(property);
	}

	@Override
	public NonOverlappingPredicateInstance makeInstance(PamelaModel model) {
		return new NonOverlappingPredicateInstance(model);
	}

	public class NonOverlappingPredicateInstance extends PropertyPredicateInstance<I> implements PropertyChangeListener {

		public NonOverlappingPredicateInstance(PamelaModel model) {
			super(NonOverlappingPredicate.this, model);
		}

		@Override
		public void check(ProxyMethodHandler<? extends I> proxyMethodHandler) {
			logger.info("Checking NonOverlappingPredicate for " + getProperty() + " and object " + proxyMethodHandler.getObject());
			// Object value = proxyMethodHandler.invokeGetter(getProperty());

			if (!duplicates.isEmpty()) {
				String message = null;
				for (Object valueCommonToMultipleObjects : new ArrayList<>(duplicates)) {
					if (objectForPropertyMap.get(valueCommonToMultipleObjects).contains(proxyMethodHandler.getObject())) {
						message = "Multiple objects sharing value " + valueCommonToMultipleObjects + " for non-overlapping property "
								+ getProperty() + " : " + objectForPropertyMap.get(valueCommonToMultipleObjects);
					}
				}
				if (message == null) {
					message = "Multiple objects for non-overlapping property " + getProperty() + " : " + duplicates;
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

		private Map<Object, Set<I>> objectForPropertyMap = new Hashtable<>();
		private Set<Object> duplicates = new HashSet<>();

		@Override
		public void propertyChange(PropertyChangeEvent evt) {

			// System.out.println("propertyChange() with " + evt.getPropertyName() + " evt: " + evt);

			I sourceObject = (I) evt.getSource();
			ProxyMethodHandler<? extends I> proxyMethodHandler = getHandler(sourceObject);
			List<Object> propertyValues = (List) proxyMethodHandler.invokeGetter(getProperty());

			// Look if some duplicates may be not duplicates anymore
			if (evt.getOldValue() != null && evt.getNewValue() == null) {
				for (Object valueCommonToMultipleObjects : new ArrayList<>(duplicates)) {
					if (valueCommonToMultipleObjects.equals(evt.getOldValue())) {
						Set<I> objectsSharingSameValue = objectForPropertyMap.get(valueCommonToMultipleObjects);
						if (objectsSharingSameValue.contains(sourceObject)) {
							objectsSharingSameValue.remove(sourceObject);
							if (objectsSharingSameValue.size() <= 1) {
								// No more duplicates for value
								duplicates.remove(valueCommonToMultipleObjects);
							}
						}
					}
				}
			}
			for (Object pValue : propertyValues) {
				Set<I> objectsForValue = objectForPropertyMap.get(pValue);
				if (objectsForValue == null) {
					objectsForValue = new HashSet<>();
					objectForPropertyMap.put(pValue, objectsForValue);
				}
				if (objectsForValue.size() > 0 && !objectsForValue.contains(sourceObject)) {
					// Value already present
					objectsForValue.add(sourceObject);
					duplicates.add(pValue);
				}
				else {
					objectsForValue.add(sourceObject);
				}

			}

		}

	}
}
