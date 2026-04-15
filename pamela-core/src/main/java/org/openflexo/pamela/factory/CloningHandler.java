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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.binding.javareflect.InvalidKeyValuePropertyException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.java.util.JavaBindingEvaluator;
import org.openflexo.pamela.CloneableProxyObject;
import org.openflexo.pamela.exceptions.InvalidDataException;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;

/**
 * Handles object cloning operations for a PAMELA object.<br>
 * Manages single and multi-object cloning, including two-phase clone (performClone + finalizeClone)
 * to correctly handle circular references within an object graph.<br>
 * Extracted from {@link ProxyMethodHandler} to isolate cloning concerns.
 *
 * @author sylvain
 * @param <I> type of object managed by the parent {@link ProxyMethodHandler}
 */
public class CloningHandler<I> {

	private final ProxyMethodHandler<I> handler;

	/** True when this object is currently being cloned (set during performClone phase) */
	boolean beingCloned = false;

	/** True when this object was created as a result of a clone operation */
	boolean createdByCloning = false;

	public CloningHandler(ProxyMethodHandler<I> handler) {
		this.handler = handler;
	}

	public boolean isBeingCloned() {
		return beingCloned;
	}

	public boolean isCreatedByCloning() {
		return createdByCloning;
	}

	/**
	 * Clone current object.<br>
	 * The supplied context determines the closure of the object graph. Properties marked as @CloningStrategy.CLONE
	 * that point outside the closure are nullified.
	 */
	public I cloneObject(Object... context) throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {

		if (context != null && context.length == 1 && context[0].getClass().isArray()) {
			context = (Object[]) context[0];
		}

		if (context != null && context.length > 0) {
			Object[] newContext = new Object[context.length + 1];
			for (int i = 0; i < context.length; i++) {
				newContext[i] = context[i];
			}
			newContext[context.length] = handler.getObject();
			context = newContext;
		}

		if (!(handler.getObject() instanceof CloneableProxyObject)) {
			throw new CloneNotSupportedException();
		}

		Hashtable<CloneableProxyObject, Object> clonedObjects = new Hashtable<>();
		Object returned = performClone(clonedObjects, context);
		for (CloneableProxyObject o : clonedObjects.keySet()) {
			ProxyMethodHandler<?> clonedObjectHandler = handler.getModelFactory().getHandler(o);
			clonedObjectHandler.getCloningHandler().finalizeClone(clonedObjects, context);
		}
		return (I) returned;
	}

	/**
	 * Clone multiple objects, using the list itself as the cloning context (closure).
	 */
	public List<Object> cloneObjects(Object... someObjects)
			throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {

		if (someObjects != null && someObjects.length == 1 && someObjects[0].getClass().isArray()) {
			someObjects = (Object[]) someObjects[0];
		}

		for (Object o : someObjects) {
			if (!(o instanceof CloneableProxyObject)) {
				throw new CloneNotSupportedException();
			}
		}

		Hashtable<CloneableProxyObject, Object> clonedObjects = new Hashtable<>();

		for (Object o : someObjects) {
			ProxyMethodHandler<?> clonedObjectHandler = handler.getModelFactory().getHandler(o);
			clonedObjectHandler.getCloningHandler().performClone(clonedObjects, someObjects);
		}

		for (CloneableProxyObject o : clonedObjects.keySet()) {
			ProxyMethodHandler<?> clonedObjectHandler = handler.getModelFactory().getHandler(o);
			clonedObjectHandler.getCloningHandler().finalizeClone(clonedObjects, someObjects);
		}

		List<Object> returned = new ArrayList<>();
		for (Object o : someObjects) {
			returned.add(clonedObjects.get(o));
		}

		return returned;
	}

	/**
	 * Phase 1 of cloning: creates the clone instance and copies SINGLE/non-model properties.
	 * Model-entity properties with CLONE strategy are registered for phase 2 (finalizeClone).
	 */
	Object performClone(Hashtable<CloneableProxyObject, Object> clonedObjects, Object... context)
			throws ModelExecutionException, ModelDefinitionException {

		boolean setIsBeingCloned = !beingCloned;
		beingCloned = true;
		Object returned = null;
		try {
			returned = handler.getModelFactory().newInstance(handler.getModelEntity().getImplementedInterface(), true);
			ProxyMethodHandler<?> clonedObjectHandler = handler.getModelFactory().getHandler(returned);
			clonedObjectHandler.getCloningHandler().createdByCloning = true;
			clonedObjectHandler.initialized = true;
			try {
				Iterator<ModelProperty<? super I>> properties = handler.getModelEntity().getPropertiesOrderedForCloning();
				while (properties.hasNext()) {
					ModelProperty p = properties.next();
					switch (p.getCardinality()) {
						case SINGLE:
							Object singleValue = handler.invokeGetter(p);
							switch (p.getCloningStrategy()) {
								case CLONE:
									if (ModelEntity.isModelEntity(p.getType()) && singleValue instanceof CloneableProxyObject) {
										if (isPartOfContext(singleValue, EmbeddingType.CLOSURE, context)) {
											appendToClonedObjects(clonedObjects, (CloneableProxyObject) singleValue);
										}
									}
									else {
										if (singleValue != null) {
											if (singleValue instanceof DataBinding) {
												clonedObjectHandler.invokeSetter(p, ((DataBinding<?>) singleValue).clone());
											}
											else {
												// TODO: handle primitive types and some basic types (eg. String)
												clonedObjectHandler.invokeSetter(p, singleValue);
											}
										}
										else {
											clonedObjectHandler.invokeSetter(p, null);
										}
									}
									break;
								case REFERENCE:
									clonedObjectHandler.invokeSetter(p, singleValue);
									break;
								case FACTORY:
									break;
								case CUSTOM_CLONE:
									try {
										Object computedValue = JavaBindingEvaluator.evaluateBinding(p.getStrategyTypeFactory(),
												handler.getObject());
										clonedObjectHandler.invokeSetter(p, computedValue);
									} catch (InvalidKeyValuePropertyException e1) {
										e1.printStackTrace();
									} catch (TypeMismatchException e1) {
										e1.printStackTrace();
									} catch (NullReferenceException e1) {
										e1.printStackTrace();
									} catch (ReflectiveOperationException e1) {
										e1.printStackTrace();
									}
									break;
								case IGNORE:
									break;
							}
							break;
						case LIST:
							List<?> values = (List<?>) handler.invokeGetter(p);
							if (values != null) {
								List<?> values2 = new ArrayList<>(values);
								for (Object value : values2) {
									switch (p.getCloningStrategy()) {
										case CLONE:
											if (ModelEntity.isModelEntity(p.getType()) && value instanceof CloneableProxyObject) {
												if (isPartOfContext(value, EmbeddingType.CLOSURE, context)) {
													appendToClonedObjects(clonedObjects, (CloneableProxyObject) value);
												}
											}
											break;
										case REFERENCE:
											clonedObjectHandler.invokeAdder(p, value);
											break;
										case FACTORY:
											break;
										case IGNORE:
											break;
									}
								}
							}
							break;
						default:
							break;
					}
				}

				clonedObjects.put((CloneableProxyObject) handler.getObject(), returned);

			} finally {
				clonedObjectHandler.getCloningHandler().createdByCloning = false;
			}
		} finally {
			if (setIsBeingCloned) {
				beingCloned = false;
			}
		}
		return returned;
	}

	/**
	 * Phase 2 of cloning: resolves model-entity property references within the cloned object graph.
	 */
	Object finalizeClone(Hashtable<CloneableProxyObject, Object> clonedObjects, Object... context)
			throws ModelExecutionException, ModelDefinitionException {

		Object clonedObject = clonedObjects.get(handler.getObject());
		ProxyMethodHandler<?> clonedObjectHandler = handler.getModelFactory().getHandler(clonedObject);
		clonedObjectHandler.getCloningHandler().createdByCloning = true;
		try {
			Iterator<ModelProperty<? super I>> properties = handler.getModelEntity().getPropertiesOrderedForCloning();
			while (properties.hasNext()) {
				ModelProperty p = properties.next();
				// TODO: cross-check that we should invoke continue
				// In the case of the deletedProperty, it is only normal that there are no setters.
				switch (p.getCardinality()) {
					case SINGLE:
						if (p.getCloningStrategy() != org.openflexo.pamela.annotations.CloningStrategy.StrategyType.IGNORE) {
							Object singleValue = handler.invokeGetter(p);
							switch (p.getCloningStrategy()) {
								case CLONE:
									if (handler.getModelFactory().getStringEncoder().isConvertable(p.getType())) {
										Object clonedValue = null;
										try {
											String clonedValueAsString = handler.getModelFactory().getStringEncoder().toString(singleValue);
											clonedValue = handler.getModelFactory().getStringEncoder().fromString(p.getType(),
													clonedValueAsString);
										} catch (InvalidDataException e) {
											throw new ModelExecutionException(e);
										}
										clonedObjectHandler.invokeSetter(p, clonedValue);
									}
									else if (ModelEntity.isModelEntity(p.getType()) && singleValue instanceof CloneableProxyObject) {
										Object clonedValue = clonedObjects.get(singleValue);
										if (!isPartOfContext(singleValue, EmbeddingType.CLOSURE, context)) {
											clonedValue = null;
										}
										clonedObjectHandler.invokeSetter(p, clonedValue);
									}
									break;
								case REFERENCE:
									Object referenceValue = singleValue != null ? clonedObjects.get(singleValue) : null;
									if (referenceValue == null) {
										referenceValue = singleValue;
									}
									clonedObjectHandler.invokeSetter(p, referenceValue);
									break;
								case CUSTOM_CLONE:
									break;
								case FACTORY:
									try {
										Object computedValue = JavaBindingEvaluator.evaluateBinding(p.getStrategyTypeFactory(), clonedObject);
										clonedObjectHandler.invokeSetter(p, computedValue);
									} catch (InvalidKeyValuePropertyException e1) {
										e1.printStackTrace();
									} catch (TypeMismatchException e1) {
										e1.printStackTrace();
									} catch (NullReferenceException e1) {
										e1.printStackTrace();
									} catch (ReflectiveOperationException e1) {
										e1.printStackTrace();
									}
									break;
								case IGNORE:
									break;
							}
						}
						break;
					case LIST:
						List<?> values = (List<?>) handler.invokeGetter(p);
						if (values != null) {
							List<?> valuesToClone = new ArrayList<>(values);
							for (Object value : valuesToClone) {
								switch (p.getCloningStrategy()) {
									case CLONE:
										if (handler.getModelFactory().getStringEncoder().isConvertable(p.getType())) {
											Object clonedValue = null;
											try {
												String clonedValueAsString = handler.getModelFactory().getStringEncoder().toString(value);
												clonedValue = handler.getModelFactory().getStringEncoder().fromString(p.getType(),
														clonedValueAsString);
											} catch (InvalidDataException e) {
												throw new ModelExecutionException(e);
											}
											clonedObjectHandler.invokeAdder(p, clonedValue);
										}
										else if (ModelEntity.isModelEntity(p.getType()) && value instanceof CloneableProxyObject) {
											Object clonedValue = clonedObjects.get(value);
											if (!isPartOfContext(value, EmbeddingType.CLOSURE, context)) {
												clonedValue = null;
											}
											if (clonedValue != null) {
												clonedObjectHandler.invokeAdder(p, clonedValue);
											}
										}
										break;
									case REFERENCE:
										Object referenceValue = value != null ? clonedObjects.get(value) : null;
										if (referenceValue == null) {
											referenceValue = value;
										}
										clonedObjectHandler.invokeAdder(p, referenceValue);
										break;
									case FACTORY:
										// TODO Not implemented
										break;
									case IGNORE:
										break;
								}
							}
						}
						break;
					default:
						break;
				}
			}
		} finally {
			clonedObjectHandler.getCloningHandler().createdByCloning = false;
		}

		return clonedObject;
	}

	/**
	 * Determines if a value belongs to the derived object graph closure defined by the context.
	 */
	private boolean isPartOfContext(Object aValue, EmbeddingType embeddingType, Object... context) {
		if (context == null || context.length == 0) {
			return true;
		}
		for (Object o : context) {
			if (aValue == o) {
				return true;
			}
			if (handler.getModelFactory().isEmbedddedIn(o, aValue, embeddingType, context)) {
				return true;
			}
		}
		return false;
	}

	private Object appendToClonedObjects(Hashtable<CloneableProxyObject, Object> clonedObjects,
			CloneableProxyObject objectToCloneOrReference) throws ModelExecutionException, ModelDefinitionException {
		Object returned = clonedObjects.get(objectToCloneOrReference);
		if (returned != null) {
			return returned;
		}
		ProxyMethodHandler<?> clonedValueHandler = handler.getModelFactory().getHandler(objectToCloneOrReference);
		return clonedValueHandler.getCloningHandler().performClone(clonedObjects);
	}
}
