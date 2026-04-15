/**
 *
 * Copyright (c) 2013-2015, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 *
 * This file is part of Pamela-core, a component of the software infrastructure
 * developed at Openflexo.
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

package org.openflexo.pamela.factory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.exceptions.InvalidDataException;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.model.ModelProperty;
import org.openflexo.pamela.model.PamelaVisitor;
import org.openflexo.pamela.model.PamelaVisitor.VisitingStrategy;

/**
 * Handles object graph operations for a PAMELA object:<br>
 * - Deep equality comparison (equalsObject)<br>
 * - Graph-based update (updateWith)<br>
 * - Visitor pattern traversal (accept)<br>
 * - Embedded/referenced object discovery<br>
 * Extracted from {@link ProxyMethodHandler} to isolate object graph concerns.
 *
 * @author sylvain
 * @param <I> type of object managed by the parent {@link ProxyMethodHandler}
 */
public class ObjectGraphHandler<I> {

	private final ProxyMethodHandler<I> handler;

	public ObjectGraphHandler(ProxyMethodHandler<I> handler) {
		this.handler = handler;
	}

	// =========================================================================
	// Visitor
	// =========================================================================

	public Object acceptVisitor(PamelaVisitor pamelaVisitor) {
		return acceptVisitor(pamelaVisitor, VisitingStrategy.Embedding);
	}

	public Object acceptVisitor(PamelaVisitor pamelaVisitor, VisitingStrategy visitingStrategy) {
		switch (visitingStrategy) {
			case Embedding:
				acceptVisitorEmbeddingStrategy((AccessibleProxyObject) handler.getObject(), pamelaVisitor, new HashSet<>());
				break;
			case Exhaustive:
				acceptVisitorExhaustiveStrategy((AccessibleProxyObject) handler.getObject(), pamelaVisitor, new HashSet<>());
				break;
			default:
				break;
		}
		return null;
	}

	private static void acceptVisitorEmbeddingStrategy(AccessibleProxyObject object, PamelaVisitor pamelaVisitor,
			Set<Object> visitedObjects) {
		if (!visitedObjects.contains(object)) {
			visitedObjects.add(object);
			pamelaVisitor.visit(object);
		}
		List<? extends AccessibleProxyObject> directEmbeddedObjects = object.getEmbeddedObjects();
		if (directEmbeddedObjects != null) {
			for (AccessibleProxyObject embeddedObject : directEmbeddedObjects) {
				if (!visitedObjects.contains(embeddedObject)) {
					acceptVisitorEmbeddingStrategy(embeddedObject, pamelaVisitor, visitedObjects);
				}
			}
		}
	}

	private static void acceptVisitorExhaustiveStrategy(AccessibleProxyObject object, PamelaVisitor pamelaVisitor,
			Set<Object> visitedObjects) {
		if (!visitedObjects.contains(object)) {
			visitedObjects.add(object);
			pamelaVisitor.visit(object);
		}
		List<? extends AccessibleProxyObject> directReferencedObjects = object.getReferencedObjects();
		if (directReferencedObjects != null) {
			for (AccessibleProxyObject referencedObject : directReferencedObjects) {
				if (!visitedObjects.contains(referencedObject)) {
					acceptVisitorExhaustiveStrategy(referencedObject, pamelaVisitor, visitedObjects);
				}
			}
		}
	}

	// =========================================================================
	// Embedded / referenced object discovery
	// =========================================================================

	public List<AccessibleProxyObject> getDirectEmbeddedObjects() {
		List<AccessibleProxyObject> returned = new ArrayList<>();
		Iterator<ModelProperty<? super I>> properties;
		try {
			properties = handler.getModelEntity().getProperties();
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			return null;
		}
		while (properties.hasNext()) {
			ModelProperty<? super I> p = properties.next();
			if (p.getEmbedded() != null) {
				switch (p.getCardinality()) {
					case SINGLE:
						Object oValue = handler.invokeGetter(p);
						if (oValue instanceof AccessibleProxyObject) {
							returned.add((AccessibleProxyObject) oValue);
						}
						break;
					case LIST:
						List<?> values = (List<?>) handler.invokeGetter(p);
						if (values != null) {
							for (Object o : values) {
								if (o instanceof AccessibleProxyObject) {
									returned.add((AccessibleProxyObject) o);
								}
							}
						}
						break;
					default:
						break;
				}
			}
		}
		return returned;
	}

	public List<AccessibleProxyObject> getReferencedObjects() {
		List<AccessibleProxyObject> returned = new ArrayList<>();
		Iterator<ModelProperty<? super I>> properties;
		try {
			properties = handler.getModelEntity().getProperties();
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			return null;
		}
		while (properties.hasNext()) {
			ModelProperty<? super I> p = properties.next();
			switch (p.getCardinality()) {
				case SINGLE:
					Object oValue = handler.invokeGetter(p);
					if (oValue instanceof AccessibleProxyObject) {
						returned.add((AccessibleProxyObject) oValue);
					}
					break;
				case LIST:
					List<?> values = (List<?>) handler.invokeGetter(p);
					if (values != null) {
						for (Object o : values) {
							if (o instanceof AccessibleProxyObject) {
								returned.add((AccessibleProxyObject) o);
							}
						}
					}
					break;
				default:
					break;
			}
		}
		return returned;
	}

	// =========================================================================
	// Equality
	// =========================================================================

	/** Value pair used to detect cycles during equality comparison */
	static class Compared<O> {
		final O o1;
		final O o2;

		Compared(O o1, O o2) {
			this.o1 = o1;
			this.o2 = o2;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((o1 == null) ? 0 : o1.hashCode());
			result = prime * result + ((o2 == null) ? 0 : o2.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null || getClass() != obj.getClass())
				return false;
			Compared<?> other = (Compared<?>) obj;
			if (o1 == null ? other.o1 != null : !o1.equals(other.o1))
				return false;
			return o2 == null ? other.o2 == null : o2.equals(other.o2);
		}
	}

	public boolean equalsObject(Object obj) {
		return equalsObject(obj, new HashSet<>(), (property) -> true);
	}

	public boolean equalsObject(Object obj, Function<ModelProperty, Boolean> considerProperty) {
		return equalsObject(obj, new HashSet<>(), considerProperty);
	}

	boolean equalsObject(Object obj, Set<Compared> seen, Function<ModelProperty, Boolean> considerProperty) {
		seen.add(new Compared(handler.getObject(), obj));

		if (handler.getObject() == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		ProxyMethodHandler<?> oppositeObjectHandler = handler.getModelFactory().getHandler(obj);
		if (oppositeObjectHandler == null) {
			return false;
		}
		if (handler.getModelEntity() != oppositeObjectHandler.getModelEntity()) {
			return false;
		}

		Iterator<ModelProperty<? super I>> properties;
		try {
			properties = handler.getModelEntity().getProperties();
		} catch (ModelDefinitionException e) {
			return false;
		}
		while (properties.hasNext()) {
			ModelProperty p = properties.next();
			if (considerProperty.apply(p) && p.isRelevantForEqualityComputation()) {
				switch (p.getCardinality()) {
					case SINGLE:
						Object singleValue = handler.invokeGetter(p);
						Object oppositeValue = oppositeObjectHandler.invokeGetter(p);
						if (p.isStringConvertable()) {
							try {
								String singleValueAsString = handler.getModelFactory().getStringEncoder().toString(singleValue);
								String oppositeValueAsString = handler.getModelFactory().getStringEncoder().toString(oppositeValue);
								if ((singleValueAsString == null && oppositeValueAsString != null)
										|| (singleValueAsString != null && !singleValueAsString.equals(oppositeValueAsString))) {
									return false;
								}
							} catch (InvalidDataException e) {
								e.printStackTrace();
							}
						}
						else {
							if (!seen.contains(new Compared(singleValue, oppositeValue))
									&& !_isEqual(singleValue, oppositeValue, seen, considerProperty)) {
								return false;
							}
						}
						break;
					case LIST:
						List<Object> values = (List<Object>) handler.invokeGetter(p);
						List<Object> oppositeValues = (List<Object>) oppositeObjectHandler.invokeGetter(p);
						if (!_isEqual(values, oppositeValues, seen, considerProperty)) {
							return false;
						}
						break;
					default:
						break;
				}
			}
		}
		return true;
	}

	private boolean _isEqual(Object oldValue, Object newValue, Set<Compared> seen, Function<ModelProperty, Boolean> considerProperty) {
		seen.add(new Compared(oldValue, newValue));

		if (oldValue == null) {
			return newValue == null;
		}
		if (oldValue == newValue) {
			return true;
		}
		if (oldValue instanceof AccessibleProxyObject && newValue instanceof AccessibleProxyObject) {
			ProxyMethodHandler<Object> h = handler.getModelFactory().getHandler(oldValue);
			if (h != null) {
				return h.getObjectGraphHandler().equalsObject(newValue, seen, considerProperty);
			}
			else {
				System.err.println("Unexpected object without ProxyMethodHandler : " + oldValue);
				return false;
			}
		}
		if (oldValue instanceof List && newValue instanceof List) {
			List<Object> l1 = (List<Object>) oldValue;
			List<Object> l2 = (List<Object>) newValue;
			if (l1.size() != l2.size()) {
				return false;
			}
			for (int i = 0; i < l1.size(); i++) {
				Object v1 = l1.get(i);
				Object v2 = l2.get(i);
				if (!seen.contains(new Compared(v1, v2)) && !_isEqual(v1, v2, seen, considerProperty)) {
					return false;
				}
			}
			return true;
		}
		return oldValue.equals(newValue);
	}

	// =========================================================================
	// Update
	// =========================================================================

	/**
	 * Updates the current object to match the supplied object.<br>
	 * After this call, equalsObject(obj) should return true.
	 */
	public boolean updateWith(I obj) {
		return CompareAndMergeUtils.updateWith(handler, obj);
	}
}
