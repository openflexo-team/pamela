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

package org.openflexo.pamela.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.model.ModelProperty;
import org.openflexo.toolbox.StringUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Contains utils in the context of compare and merge features
 * 
 * @author sylvain
 *
 */
public class CompareAndMergeUtils {

	/**
	 * Called to update source object while comparing it to opposite object, (which must be of right type!), examining each property
	 * values.<br>
	 * Collections are handled while trying to match updated objects with a given strategy<br>
	 * Perform required changes on this object so that at the end of the call, equalsObject(object) shoud return true<br>
	 * Also perform required notifications, so that it is safe to call that method in a deployed environment
	 * 
	 * @param <I>
	 *            type of object beeing handled
	 * @param obj
	 *            object to update with, which must be of same type
	 * @return boolean indicating if update was successfull
	 */
	public static <I> boolean updateWith(ProxyMethodHandler<I> source, I obj) {
		BiMap<Object, Object> mappedObjects = HashBiMap.create();
		boolean returned = updateWith(source, obj, mappedObjects);
		// At the end of processing, perform a new pass to set external references
		updateReferences(source,mappedObjects);
		return returned;
	}

	/**
	 * Called to update source object while comparing it to opposite object, (which must be of right type!), examining each property
	 * values.<br>
	 * Collections are handled while trying to match updated objects with a given strategy<br>
	 * Perform required changes on this object so that at the end of the call, equalsObject(object) shoud return true<br>
	 * Also perform required notifications, so that it is safe to call that method in a deployed environment
	 * 
	 * @param <I>
	 *            type of object beeing handled
	 * @param obj
	 *            object to update with, which must be of same type
	 * @param mappedObjects
	 *            bi-directional map storing mapped objects
	 * @return boolean indicating if update was successfull
	 */
	private static <I> boolean updateWith(ProxyMethodHandler<I> source, I obj, BiMap<Object, Object> mappedObjects) {

		if (DEBUG)
			System.out.println(">>>>>>> updateWith() between " + source.getObject() + " and " + obj);

		if (source.getObject() == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		ProxyMethodHandler<?> oppositeObjectHandler = source.getModelFactory().getHandler(obj);
		if (oppositeObjectHandler == null) {
			// Other object is not handled by the same factory
			return false;
		}
		if (source.getModelEntity() != oppositeObjectHandler.getModelEntity()) {
			return false;
		}

		// System.out.println("Distance: " + getDistance(obj));

		if (mappedObjects.get(source.getObject()) != null) {
			if (mappedObjects.get(source.getObject()) != obj) {
				System.err.println("Mapped new value for " + source.getObject() + " was: " + mappedObjects.get(source.getObject()));
				mappedObjects.forcePut(source.getObject(), obj);
			}
		}
		else {
			// The inverse reference might be still registered, clear it
			mappedObjects.forcePut(source.getObject(), obj);
		}

		Iterator<ModelProperty<? super I>> properties;
		try {
			properties = source.getModelEntity().getProperties();
		} catch (ModelDefinitionException e) {
			return false;
		}

		while (properties.hasNext()) {
			ModelProperty p = properties.next();

			/* if (p.getPropertyIdentifier().equals("flexoConcepts")) {
				DEBUG = true;
			}
			else {
				DEBUG = false;
			} */

			if (!p.isDerived()) {

				if (DEBUG)
					System.out.println(" > " + p.getPropertyIdentifier());

				switch (p.getCardinality()) {
					case SINGLE:
						Object singleValue = source.invokeGetter(p);
						Object oppositeValue = oppositeObjectHandler.invokeGetter(p);
						if (DEBUG)
							System.out.println("Property " + p.getPropertyIdentifier() + " singleValue=" + singleValue + " oppositeValue="
									+ oppositeValue);

						if (!isEqual(singleValue, oppositeValue)) {

							if (p.getUpdater() != null) {
								source.invokeUpdater(p, oppositeValue);
							}
							else {
								if (p.getAccessedEntity() != null && singleValue instanceof AccessibleProxyObject) {
									if (oppositeValue == null) {
										source.invokeSetter(p, oppositeValue);
									}
									else {
										if (mappedObjects.get(singleValue) == oppositeValue) {
											// Cycle detected
										}
										else {
											if (!updateWith(source.getModelFactory().getHandler(singleValue), oppositeValue,
													mappedObjects)) {
												// updateWith() failed: we have to invoke setter
												source.invokeSetter(p, oppositeValue);
											}
										}
									}
								}
								else {
									source.invokeSetter(p, oppositeValue);
								}
							}
						}

						break;
					case LIST:
						Map<Object, Integer> reindex = new LinkedHashMap<>();
						List<Object> values = new ArrayList<>((List<Object>) source.invokeGetter(p));// invokeGetterForListCardinality(p);
						List<Object> oppositeValues = new ArrayList<>((List<Object>) oppositeObjectHandler.invokeGetter(p)); // invokeGetterForListCardinality(p);
						ListMatching matching = match(source, values, oppositeValues);
						if (DEBUG) {
							System.out.println(
									"Property " + p.getPropertyIdentifier() + " values=" + values + " oppositeValues=" + oppositeValues);
							System.out.println("Property " + p.getPropertyIdentifier() + " : matching=" + matching);
						}
						for (Matched m : matching.matchedList) {
							// System.out.println("match " + m.idx1 + " with " + m.idx2);
							Object o1 = values.get(m.idx1);
							Object o2 = oppositeValues.get(m.idx2);
							if (o1 instanceof AccessibleProxyObject) {
								if (mappedObjects.get(o1) == o2) {
									// Cycle detected
									// System.out.println("Cycle detected, abort");
								}
								else {
									updateWith(source.getModelFactory().getHandler(o1), o2, mappedObjects/*, outsideReferences*/);
								}
							}
							// Store desired index
							reindex.put(o1, m.idx2);
						}
						// Do it in reverse order to avoid IndexOutOfBoundException !!!
						for (int i = matching.removed.size() - 1; i >= 0; i--) {
							Removed r = matching.removed.get(i);
							Object removedObject = values.get(r.removedIndex);
							source.invokeRemover(p, removedObject);
						}

						// Do it in reverse order too, to keep original order in the case of multiple adds
						for (int i = matching.added.size() - 1; i >= 0; i--) {
							Added a = matching.added.get(i);
							Object addedObject = oppositeValues.get(a.originalIndex);
							if (addedObject instanceof AccessibleProxyObject) {
								// In this case, we must ensure that added object has correct references too
								ProxyMethodHandler<?> addedObjectHandler = source.getModelFactory().getHandler(addedObject);
								updateReferences(addedObjectHandler, mappedObjects/*, outsideReferences*/);
							}
							if (DEBUG)
								System.out.println("Add in " + source.getObject() + " : " + addedObject);
							source.invokeAdder(p, addedObject);
							// Store desired index
							reindex.put(addedObject, a.insertedIndex);
						}
						// Now handle eventual reindexing of property values
						for (Object o : reindex.keySet()) {
							int idx = reindex.get(o);
							if (values.indexOf(o) != idx && values.indexOf(o) != -1) {
								if (DEBUG)
									System.out.println("Moving " + values.indexOf(o) + " to " + idx);
								source.invokeReindexer(p, o, idx);
							}
						}

						break;
					default:
						break;
				}
			}

		}
		
		if (DEBUG) {
			System.out.println("<<<<<<< DONE updateWith " + source.getObject() + " with " + obj);
			System.out.println("Mapped objects:");
			for (Object object1 : mappedObjects.keySet()) {
				System.out.println(" *** " + object1 + " > " + mappedObjects.get(object1));
			}
		}
		return true;
	}

	/**
	 * Internally used to set external references that may be defined in supplied object handler with inverse references found in supplied
	 * {@link BiMap}
	 * 
	 * @param <I>
	 * @param objectHandler
	 *            object on which this method applies
	 * @param mappedObjects
	 *            the bi-map coding objects mapping
	 */
	private static <I> void updateReferences(ProxyMethodHandler<I> objectHandler, BiMap<Object, Object> mappedObjects) {
		updateReferences(objectHandler, mappedObjects, new HashSet<>());
	}

	/**
	 * Internally used to set external references that may be defined in supplied object handler with inverse references found in supplied
	 * {@link BiMap}
	 * 
	 * @param <I>
	 * @param objectHandler
	 *            object on which this method applies
	 * @param mappedObjects
	 *            the bi-map coding objects mapping
	 * @param processedObjects
	 *            objects already handled
	 * 
	 */
	private static <I> void updateReferences(ProxyMethodHandler<I> objectHandler, BiMap<Object, Object> mappedObjects,
			Set<Object> processedObjects) {

		if (objectHandler == null || processedObjects.contains(objectHandler.getObject())) {
			return;
		}

		processedObjects.add(objectHandler.getObject());

		if (DEBUG)
			System.out.println(">>>>>>> updateReferences() for " + objectHandler.getObject());

		Iterator<ModelProperty<? super I>> properties;
		try {
			properties = objectHandler.getModelEntity().getProperties();
		} catch (ModelDefinitionException e) {
			return;
		}

		while (properties.hasNext()) {
			ModelProperty p = properties.next();

			// When this property has inverse property, guarantee that this operation will be only one-way performed
			if (!p.isDerived()) {

				// if (DEBUG)
				// System.out.println(" > " + p.getPropertyIdentifier());

				switch (p.getCardinality()) {
					case SINGLE:
						Object singleValue = objectHandler.invokeGetter(p);
						Object validReference = mappedObjects.inverse().get(singleValue);
						if (validReference != null) {
							objectHandler.invokeSetter(p, validReference);
						}
						else {
							if (singleValue instanceof AccessibleProxyObject) {
								// Do it recursively
								updateReferences(objectHandler.getModelFactory().getHandler(singleValue), mappedObjects, processedObjects);
							}
						}
						break;

					case LIST:
						List<Object> values = (List<Object>) objectHandler.invokeGetter(p);
						for (Object value : new ArrayList<>(values)) {
							if (mappedObjects.inverse().get(value) != null) {
								Object referenceObject = mappedObjects.inverse().get(value);
								// We replace this value with the right reference object at right index
								int initialIndex = values.indexOf(value);
								objectHandler.invokeRemover(p, value);
								objectHandler.invokeAdder(p, referenceObject);
								objectHandler.invokeReindexer(p, referenceObject, initialIndex);
								// System.out.println(
								// "Replaced " + value + " by " + referenceObject + " at index " + initialIndex);
							}
							else {
								if (value instanceof AccessibleProxyObject) {
									// Do it recursively
									updateReferences(objectHandler.getModelFactory().getHandler(value), mappedObjects, processedObjects);
								}
							}
						}
				}
			}
		}

	}

	/**
	 * Compute optimal matching between two lists of objects
	 * 
	 * @param l1
	 * @param l2
	 * @return
	 */
	private static ListMatching match(ProxyMethodHandler<?> source, List<Object> l1, List<Object> l2) {
		ListMatching returned = bruteForceMatch(source, l1, l2, new HashMap<>());
		// System.out.println("MATCHING :\n" + returned);
		return returned;
	}

	/**
	 * Compute optimal matching between two lists of objects, given a supplied mapping
	 * 
	 * @param l1
	 * @param l2
	 * @return
	 */
	private static ListMatching match(ProxyMethodHandler<?> source, List<Object> l1, List<Object> l2, Map<Object, Object> visitedObjects) {

		ListMatching returned = bruteForceMatch(source, l1, l2, new HashMap<>(visitedObjects));
		// System.out.println("MATCHING :\n" + returned);
		return returned;
	}

	/**
	 * A functional algorithm, but not really optimal
	 * 
	 * @param l1
	 * @param l2
	 * @return
	 */
	private static ListMatching bruteForceMatch(ProxyMethodHandler<?> source, List<Object> l1, List<Object> l2,
			Map<Object, Object> mappedObjects) {
		ListMatching returned = new ListMatching();

		List<Object> list1 = new ArrayList<>(l1);
		List<Object> list2 = new ArrayList<>(l2);

		while (list1.size() > 0 && list2.size() > 0) {
			Matched matched = getBestMatch(source, list1, list2, mappedObjects);
			if (matched == null) {
				break;
			}
			Object o1 = list1.get(matched.idx1);
			Object o2 = list2.get(matched.idx2);
			matched.idx1 = l1.indexOf(list1.get(matched.idx1));
			matched.idx2 = l2.indexOf(list2.get(matched.idx2));
			// System.out.println(
			// "Matched " + getModelFactory().stringRepresentation(o1) + " and " + getModelFactory().stringRepresentation(o2));
			returned.matchedList.add(matched);
			list1.remove(o1);
			list2.remove(o2);
		}

		if (list1.size() > 0) {
			// Removed
			for (int i = 0; i < list1.size(); i++) {
				returned.removed.add(new Removed(l1.indexOf(list1.get(i))));
			}
		}

		if (list2.size() > 0) {
			// Added
			for (int i = list2.size() - 1; i >= 0; i--) {
				// We replaced following iteration with this reverse iteration to keep order of second list
				// for (int i = 0; i < list2.size(); i++) {
				int insertionIndex = -1;
				int originalIndex = l2.indexOf(list2.get(i));
				int current = originalIndex - 1;
				while (insertionIndex == -1 && current >= 0) {
					Matched m = returned.getMatchedForList2Index(current);
					if (m != null) {
						insertionIndex = m.idx1 + 1;
					}
					current--;
				}
				if (insertionIndex == -1) {
					insertionIndex = 0;
				}
				returned.added.add(new Added(l2.indexOf(list2.get(i)), insertionIndex));
			}
		}

		return returned;
	}

	/**
	 * Retrieve best match between the two lists.<br>
	 * Best match is represented by a couple of objects (one in each list) of exactely same type, whose distance is the minimal found.<br>
	 * A minimal distance is required as a threshold (here 0.7)
	 * 
	 * @param l1
	 * @param l2
	 * @return
	 */
	private static Matched getBestMatch(ProxyMethodHandler<?> source, List<Object> l1, List<Object> l2, Map<Object, Object> mappedObject) {
		Matched returned = null;
		double bestDistance = 0.7; // Double.POSITIVE_INFINITY;
		int m1 = 0, m2 = 0;
		for (int i = 0; i < l1.size(); i++) {
			Object o1 = l1.get(i);
			for (int j = 0; j < l2.size(); j++) {
				Object o2 = l2.get(j);
				if (o1 instanceof AccessibleProxyObject && o2 instanceof AccessibleProxyObject) {
					ProxyMethodHandler<?> h1 = source.getModelFactory().getHandler(o1);
					ProxyMethodHandler<?> h2 = source.getModelFactory().getHandler(o1);
					if (h1.getModelEntity() == h2.getModelEntity()) {
						// Matching is possible only for exact same type
						double d = getDistanceBetweenValues(source, o1, o2, mappedObject);
						if (d < bestDistance) {
							returned = new Matched(i, j);
							bestDistance = d;
							m1 = i;
							m2 = j;
						}
					}
				}
			}
		}
		return returned;
	}

	/**
	 * Stupid implementation, do not use it in production
	 * 
	 * @param l1
	 * @param l2
	 * @return
	 */
	private static ListMatching stupidMatch(ProxyMethodHandler<?> source, List<Object> l1, List<Object> l2,
			Map<Object, Object> mappedObjects) {
		// System.out.println("On matche les deux listes " + l1 + " et " + l2);
		ListMatching returned = new ListMatching();
		if (l1.size() <= l2.size()) {
			for (int i = 0; i < l1.size(); i++) {
				returned.matchedList.add(new Matched(i, i));
			}
			for (int i = l1.size(); i < l2.size(); i++) {
				returned.added.add(new Added(i, i));
			}
		}
		else if (l1.size() > l2.size()) {
			for (int i = 0; i < l1.size(); i++) {
				returned.matchedList.add(new Matched(i, i));
			}
			for (int i = l2.size(); i < l1.size(); i++) {
				returned.removed.add(new Removed(i));
			}
		}
		System.out.println("Return " + returned);
		return returned;
	}

	private static double getDistanceBetweenValues(ProxyMethodHandler<?> source, Object v1, Object v2, Map<Object, Object> visitedObjects) {
		if (v1 == null) {
			return (v2 == null ? 0.0 : 1.0);
		}
		if (v2 == null) {
			return (v1 == null ? 0.0 : 1.0);
		}
		if (v1 == v2) {
			return 0;
		}
		if (v1.equals(v2)) {
			return 0;
		}
		if (TypeUtils.isPrimitive(v1.getClass()) || v1 instanceof String) {
			String s1 = v1.toString();
			String s2 = v2.toString();
			return (double) StringUtils.levenshteinDistance(s1, s2) / (double) Math.max(s1.length(), s2.length());
		}
		if (v1 instanceof AccessibleProxyObject && v2 instanceof AccessibleProxyObject) {
			ProxyMethodHandler<?> handler = source.getModelFactory().getHandler(v1);
			// System.out.println("Distance between " + handler.getObject() + " and " + v2 + " visited=" + visitedObjects);
			return getDistance(handler, v2, visitedObjects);
		}
		return 1.0;
	}

	/**
	 * Compute the distance (double value between 0.0 and 1.0) between this object and an opposite object (which must be of right type!) If
	 * two objects are equals, return 0. If two objects are totally different, return 1.
	 * 
	 * @param factory
	 * @param o1
	 * @param o2
	 * @return
	 */
	public static double getDistance(PamelaModelFactory factory, Object o1, Object o2) {
		return getDistance(factory.getHandler(o1), o2);
	}

	public static boolean DEBUG = false;

	/**
	 * Compute the distance (double value between 0.0 and 1.0) between this object and an opposite object (which must be of right type!) If
	 * two objects are equals, return 0. If two objects are totally different, return 1.
	 * 
	 * @param object
	 * @return
	 */
	public static <I> double getDistance(ProxyMethodHandler<I> source, Object obj) {
		return getDistance(source, obj, new HashMap<>());
	}

	/**
	 * Compute the distance (double value between 0.0 and 1.0) between this object and an opposite object (which must be of right type!) If
	 * two objects are equals, return 0. If two objects are totally different, return 1.
	 * 
	 * @param object
	 * @return
	 */
	private static <I> double getDistance(ProxyMethodHandler<I> source, Object obj, Map visitedObjects) {
		if (source.getObject() == obj) {
			return 0.0;
		}
		if (obj == null) {
			return 1.0;
		}

		visitedObjects.put(source.getObject(), obj);

		// System.out.println("visitedObjects=" + visitedObjects);

		ProxyMethodHandler oppositeObjectHandler = source.getModelFactory().getHandler(obj);
		if (oppositeObjectHandler == null) {
			// Other object is not handled by the same factory
			return 1.0;
		}
		if (source.getModelEntity() != oppositeObjectHandler.getModelEntity()) {
			return 1.0;
		}

		Iterator<ModelProperty<? super I>> properties;
		try {
			properties = source.getModelEntity().getProperties();
		} catch (ModelDefinitionException e) {
			return 1.0;
		}

		double distance = 0.0;
		double totalPonderation = 0.0;

		while (properties.hasNext()) {
			ModelProperty p = properties.next();

			if (p.isSerializable()) {

				double propertyPonderation = 1.0;

				switch (p.getCardinality()) {
					case SINGLE:
						Object singleValue = source.invokeGetter(p);
						Object oppositeValue = oppositeObjectHandler.invokeGetter(p);
						propertyPonderation = getPropertyPonderation(p);

						if (DEBUG) {
							System.out.println("SINGLE Property " + p.getPropertyIdentifier() + " ponderation=" + propertyPonderation);
						}

						if (visitedObjects.get(singleValue) == oppositeValue) {
							// Cycle detected, don't go further
						}
						else if (singleValue != null || oppositeValue != null) {
							totalPonderation += propertyPonderation;
							if (!isEqual(singleValue, oppositeValue)) {
								double valueDistance = getDistanceBetweenValues(source, singleValue, oppositeValue, visitedObjects);
								distance = distance + valueDistance * propertyPonderation;
								// System.out.println("Property " + p.getPropertyIdentifier() + " distance=" + valueDistance + "
								// ponderation="
								// + propertyPonderation);
							}
							else {
								// System.out.println(
								// "Property " + p.getPropertyIdentifier() + " distance=0.0" + " ponderation=" + propertyPonderation);
							}
						}
						else {
							// null values are ignored and not taken under account
						}
						break;
					case LIST:
						List<Object> values = (List<Object>) source.invokeGetter(p); // invokeGetterForListCardinality(p);
						List<Object> oppositeValues = (List<Object>) oppositeObjectHandler.invokeGetter(p); // .invokeGetterForListCardinality(p);
						propertyPonderation = Math.max(values != null ? values.size() : 0,
								oppositeValues != null ? oppositeValues.size() : 0);

						if (DEBUG) {
							System.out.println("MULTIPLE Property " + p.getPropertyIdentifier() + " ponderation=" + propertyPonderation);
						}

						boolean allVisited = true;
						for (Object v : values) {
							if (!visitedObjects.containsKey(v)) {
								allVisited = false;
							}
						}
						if (allVisited) {
							// Ignore this because we have visited all the values
						}
						else {
							if ((values != null && values.size() > 0) || (oppositeValues != null && oppositeValues.size() > 0)) {
								totalPonderation += propertyPonderation;
								if (!isEqual(values, oppositeValues)) {
									double valueDistance = getDistanceBetweenListValues(source, values, oppositeValues, visitedObjects);
									distance = distance + valueDistance * propertyPonderation;
									// System.out.println("Property " + p.getPropertyIdentifier() + " distance=" + valueDistance + "
									// ponderation="
									// + propertyPonderation);
								}
								else {
									// System.out.println(
									// "Property " + p.getPropertyIdentifier() + " distance=0.0" + " ponderation=" + propertyPonderation);
								}
							}
							else {
								// null values are ignored and not taken under account
							}
						}
						break;
					default:
						break;
				}
			}
		}

		if (totalPonderation > 0) {
			return distance / totalPonderation;
		}

		return 0.0;
	}

	private static double getPropertyPonderation(ModelProperty<?> p) {
		double propertyPonderation = 1.0;
		if (TypeUtils.isPrimitive(p.getType()) || p.getType().equals(String.class) || p.isStringConvertable()) {
			propertyPonderation = 1.0;
		}
		if (p.getAccessedEntity() != null) {
			propertyPonderation = p.getAccessedEntity().getPropertiesSize();
		}
		// System.out.println("Ponderation for property: " + p.getPropertyIdentifier() + " " + propertyPonderation);
		return propertyPonderation;
	}

	private static double getDistanceBetweenListValues(ProxyMethodHandler<?> source, List<Object> l1, List<Object> l2,
			Map<Object, Object> visitedObjects) {
		if (l1 == null) {
			return (l2 == null ? 0.0 : 1.0);
		}
		if (l2 == null) {
			return (l1 == null ? 0.0 : 1.0);
		}
		if (l1 == l2) {
			return 0;
		}
		if (l1.equals(l2)) {
			return 0;
		}
		// System.out.println("On matche " + l1 + " et " + l2 + " visited:" + visitedObjects);
		// ListMatching matching = match(source, l1, l2);
		ListMatching matching = match(source, l1, l2, visitedObjects);
		// System.out.println("Matching=" + matching);
		double total = matching.added.size() + matching.removed.size() + matching.matchedList.size();
		double score = matching.added.size() + matching.removed.size();
		for (Matched m : matching.matchedList) {
			Object o1 = l1.get(m.idx1);
			Object o2 = l2.get(m.idx2);
			if (visitedObjects.get(o1) == o2) {
				// Cycle detected, don't go further
			}
			else {
				score += getDistanceBetweenValues(source, o1, o2, visitedObjects);
			}
		}
		return score / total;
	}

	protected static boolean isEqual(Object oldValue, Object newValue) {
		return IProxyMethodHandler.isEqual(oldValue, newValue);

	}

	static class Matched {
		int idx1 = -1;
		int idx2 = -1;

		public Matched(int idx1, int idx2) {
			super();
			this.idx1 = idx1;
			this.idx2 = idx2;
		}

		@Override
		public String toString() {
			return "Matched(" + idx1 + "," + idx2 + ")";
		}
	}

	static class Added {
		// Index of object in new list
		int originalIndex = -1;
		int insertedIndex = -1;

		public Added(int originalIndex, int insertedIndex) {
			super();
			this.originalIndex = originalIndex;
			this.insertedIndex = insertedIndex;
		}

		@Override
		public String toString() {
			return "Added(" + originalIndex + "," + insertedIndex + ")";
		}
	}

	static class Removed {
		// Index of object in initial list
		int removedIndex = -1;

		public Removed(int removedIndex) {
			super();
			this.removedIndex = removedIndex;
		}

		@Override
		public String toString() {
			return "Removed(" + removedIndex + ")";
		}
	}

	static class ListMatching {
		List<Removed> removed = new ArrayList<>();
		List<Added> added = new ArrayList<>();
		List<Matched> matchedList = new ArrayList<>();

		public Matched getMatchedForList2Index(int index) {
			for (Matched m : matchedList) {
				if (m.idx2 == index) {
					return m;
				}
			}
			return null;
		}

		public Matched getMatchedForList1Index(int index) {
			for (Matched m : matchedList) {
				if (m.idx1 == index) {
					return m;
				}
			}
			return null;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("Matched:" + matchedList);
			sb.append(" Added:" + added);
			sb.append(" Removed:" + removed);
			return sb.toString();
		}
	}

	// Kept for history (remove this later)
	/**
	 * Internally called to rebuild outside references after an updateWith() process
	 * 
	 * Behind this idea, we may have added to existing graph of objects some other objects, potentially referencing objects outside scope of
	 * initial object graphs, but reflecting same objects. The goal of that method is to replace those outside reference with objects of
	 * local scope.
	 * 
	 * @param <I>
	 *            type of object beeing handled
	 * @param mappedObjects
	 *            bi-directional map storing mapped objects
	 * @param objectHandler
	 *            handler of object beeing declared as outside graph of objects
	 */
	/*private static <I> void rebindOutsideReferences(BiMap<Object, Object> mappedObjects, ProxyMethodHandler<I> objectHandler,
			Set<Object> processedObjects) {
	
		if (objectHandler == null || processedObjects.contains(objectHandler.getObject())) {
			return;
		}
	
		processedObjects.add(objectHandler.getObject());
	
		Iterator<ModelProperty<? super I>> properties;
		try {
			properties = objectHandler.getModelEntity().getProperties();
		} catch (ModelDefinitionException e) {
			return;
		}
		while (properties.hasNext()) {
			ModelProperty p = properties.next();
			if (!p.isDerived()) {
				switch (p.getCardinality()) {
					case SINGLE:
						Object singleValue = objectHandler.invokeGetter(p);
						Object validReference = mappedObjects.inverse().get(singleValue);
						if (validReference != null) {
							objectHandler.invokeSetter(p, validReference);
						}
						else {
							if (singleValue instanceof AccessibleProxyObject) {
								// Do it recursively
								rebindOutsideReferences(mappedObjects, objectHandler.getModelFactory().getHandler(singleValue),
										processedObjects);
							}
						}
						break;
					case LIST:
						List<Object> values = (List<Object>) objectHandler.invokeGetter(p);
						for (Object value : new ArrayList<>(values)) {
							validReference = mappedObjects.inverse().get(value);
							if (validReference != null) {
								// We replace this value with the right reference object at right index
								List<Object> currentValues = (List<Object>) objectHandler.invokeGetter(p);
								int initialIndex = currentValues.indexOf(value);
								objectHandler.invokeRemover(p, value);
								objectHandler.invokeAdder(p, validReference);
								objectHandler.invokeReindexer(p, validReference, initialIndex);
								// System.out.println(
								// "Plutot que " + value + " c'est mieux de mettre " + referenceObject + " a l'index " + initialIndex);
							}
						}
	
						break;
					default:
						break;
				}
			}
		}
	}*/

}
