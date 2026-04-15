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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.pamela.annotations.PastingPoint;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * Handles clipboard (copy/paste) operations for a PAMELA object.<br>
 * Extracted from {@link ProxyMethodHandler} to isolate clipboard concerns.
 *
 * @author sylvain
 * @param <I> type of object managed by the parent {@link ProxyMethodHandler}
 */
public class ClipboardHandler<I> {

	private final ProxyMethodHandler<I> handler;

	public ClipboardHandler(ProxyMethodHandler<I> handler) {
		this.handler = handler;
	}

	/**
	 * Return boolean indicating if supplied clipboard is valid for pasting in object monitored by this handler
	 */
	public static <I> boolean isPastable(Clipboard clipboard, ModelEntity<I> modelEntity) {
		if (clipboard.getTypes().length == 0) {
			return false;
		}
		for (Class<?> type : clipboard.getTypes()) {
			Collection<ModelProperty<? super I>> propertiesAssignableFrom = modelEntity.getPropertiesAssignableFrom(type);
			Collection<ModelProperty<? super I>> pastingPointProperties = Collections2.filter(propertiesAssignableFrom,
					new Predicate<ModelProperty<?>>() {
						@Override
						public boolean apply(ModelProperty<?> arg0) {
							return arg0.getAddPastingPoint() != null || arg0.getSetPastingPoint() != null;
						}
					});
			if (pastingPointProperties.size() == 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Return boolean indicating if supplied clipboard is valid for pasting in the object managed by this handler
	 */
	public boolean isPastable(Clipboard clipboard) {
		return isPastable(clipboard, handler.getModelEntity());
	}

	/**
	 * Paste supplied clipboard in the object managed by this handler.<br>
	 * Returns pasted objects (a single object for a single contents clipboard, and a list for multiple contents).
	 */
	public Object paste(Clipboard clipboard) throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {

		List<Object> returned = null;
		if (!clipboard.isSingleObject()) {
			returned = new ArrayList<>();
		}

		boolean somethingWasPasted = false;
		for (Class<?> type : clipboard.getTypes()) {

			Collection<ModelProperty<? super I>> propertiesAssignableFrom = handler.getModelEntity().getPropertiesAssignableFrom(type);
			Collection<ModelProperty<? super I>> pastingPointProperties = Collections2.filter(propertiesAssignableFrom,
					new Predicate<ModelProperty<?>>() {
						@Override
						public boolean apply(ModelProperty<?> arg0) {
							return arg0.getAddPastingPoint() != null || arg0.getSetPastingPoint() != null;
						}
					});

			ModelProperty<? super I> pastingProperty;

			if (pastingPointProperties.size() == 0) {
				throw new ClipboardOperationException("Pasting operation: no property is compatible with pasting type " + type);
			}
			else if (pastingPointProperties.size() > 1) {
				List<ModelProperty<? super I>> list = new ArrayList<>(pastingPointProperties);
				Collections.sort(list, new Comparator<ModelProperty<? super I>>() {
					@Override
					public int compare(ModelProperty<? super I> o1, ModelProperty<? super I> o2) {
						int p1 = o1.getAddPastingPoint() != null ? o1.getAddPastingPoint().priority() : o1.getSetPastingPoint().priority();
						int p2 = o2.getAddPastingPoint() != null ? o2.getAddPastingPoint().priority() : o2.getSetPastingPoint().priority();
						return p1 - p2;
					}
				});
				pastingProperty = list.get(0);
			}
			else {
				pastingProperty = pastingPointProperties.iterator().next();
			}

			Object pastedContents = paste(clipboard, pastingProperty);
			if (clipboard.isSingleObject()) {
				clipboard.consume();
				return pastedContents;
			}
			else if (pastedContents != null) {
				returned.addAll((List) pastedContents);
				somethingWasPasted = true;
			}
		}

		if (!somethingWasPasted) {
			throw new ClipboardOperationException("Cannot paste here: no pasting point found");
		}

		clipboard.consume();
		return returned;
	}

	/**
	 * Paste using supplied clipboard and property, asserting a pasting point could be found.<br>
	 * Returns pasted objects for supplied property.
	 */
	public Object paste(Clipboard clipboard, ModelProperty<? super I> modelProperty)
			throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {

		if (modelProperty.getSetPastingPoint() == null && modelProperty.getAddPastingPoint() == null) {
			throw new ClipboardOperationException("Cannot paste here: no pasting point found");
		}
		if (modelProperty.getSetPastingPoint() != null && modelProperty.getAddPastingPoint() != null) {
			throw new ClipboardOperationException(
					"Ambiguous pasting operations: both add and set operations are available for property " + modelProperty);
		}
		if (modelProperty.getSetPastingPoint() != null) {
			return paste(clipboard, modelProperty, modelProperty.getSetPastingPoint());
		}
		else {
			return paste(clipboard, modelProperty, modelProperty.getAddPastingPoint());
		}
	}

	/**
	 * Paste using supplied clipboard and property, at the specified pasting point.<br>
	 * Returns pasted objects for supplied property.
	 */
	public Object paste(Clipboard clipboard, ModelProperty<? super I> modelProperty, PastingPoint pp)
			throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {

		ModelEntity<?> entity = handler.getModelEntity();
		if (pp == null) {
			throw new ClipboardOperationException("Cannot paste here: no pasting point found");
		}
		if (modelProperty == null) {
			throw new ClipboardOperationException("Cannot paste here: no suitable property found");
		}

		if (entity.hasProperty(modelProperty) || modelProperty.getModelEntity().isAncestorOf(entity)) {
			if (modelProperty.getSetPastingPoint() == pp) {
				if (!clipboard.isSingleObject()) {
					throw new ClipboardOperationException("Cannot paste here: multiple cardinality clipboard for a SINGLE property");
				}
				Object valueToSet = clipboard.getSingleContents();
				handler.invokeSetter(modelProperty, valueToSet);
				return valueToSet;
			}
			else if (modelProperty.getAddPastingPoint() == pp) {
				if (clipboard.isSingleObject()) {
					Object valueToAdd = clipboard.getSingleContents();
					handler.invokeAdder(modelProperty, valueToAdd);
					return valueToAdd;
				}
				else {
					List<Object> returned = new ArrayList<>();
					for (Object o : clipboard.getMultipleContents()) {
						if (TypeUtils.isTypeAssignableFrom(modelProperty.getType(), o.getClass())) {
							handler.invokeAdder(modelProperty, o);
							returned.add(o);
						}
					}
					return returned;
				}
			}
		}

		return null;
	}
}
