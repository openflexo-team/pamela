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
package org.openflexo.pamela.jml;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openflexo.pamela.addon.EntityAddOn;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModel;
import org.openflexo.pamela.factory.PamelaUtils;
import org.openflexo.pamela.jml.annotations.Invariant;
import org.openflexo.pamela.model.ModelEntity;

/**
 * Extends {@link ModelEntity} by providing required information and behaviour required for managing JML in the context of a ModelEntity
 * 
 * @author sylvain
 *
 * @param <I>
 */
public class JMLEntityAddOn<I> extends EntityAddOn<I, JMLAddOn> {

	private Map<String, JMLMethodDefinition> jmlMethods = new HashMap<>();
	private JMLInvariant<I> invariant;

	public static boolean hasJMLAnnotations(Class<?> aClass) {
		if (aClass.isAnnotationPresent(Invariant.class)) {
			return true;
		}
		for (Method method : aClass.getDeclaredMethods()) {
			if (JMLMethodDefinition.hasJMLAnnotations(method)) {
				return true;
			}
		}
		return false;
	}

	public JMLEntityAddOn(ModelEntity<I> modelEntity, JMLAddOn jmlAddOn) throws ModelDefinitionException {
		super(modelEntity, jmlAddOn);
		registerJMLAnnotations();
	}

	@Override
	public JMLEntityAddOnInstance<I> instantiate(PamelaModel model) {
		return new JMLEntityAddOnInstance<I>(this, model);
	}

	private void registerJMLAnnotations() throws ModelDefinitionException {
		if (getImplementedInterface().isAnnotationPresent(Invariant.class)) {
			invariant = new JMLInvariant<>(getImplementedInterface().getAnnotation(Invariant.class), getModelEntity());
		}
		for (Method m : getImplementedInterface().getDeclaredMethods()) {
			registerJMLAnnotations(m);
		}
	}

	private JMLMethodDefinition<I> registerJMLAnnotations(Method method) throws ModelDefinitionException {
		if (JMLMethodDefinition.hasJMLAnnotations(method)) {
			JMLMethodDefinition<I> returned = new JMLMethodDefinition<>(method, getModelEntity());
			jmlMethods.put(returned.getSignature(), returned);
			return returned;
		}
		return null;
	}

	public JMLMethodDefinition<? super I> getJMLMethodDefinition(Method method) {
		JMLMethodDefinition<? super I> returned = jmlMethods.get(PamelaUtils.getSignature(method, getImplementedInterface(), true));
		if (returned == null) {
			try {
				if (getModelEntity().getDirectSuperEntities() != null) {
					for (ModelEntity<? super I> superEntity : getModelEntity().getDirectSuperEntities()) {
						JMLEntityAddOn<? super I> superEntityAddOn = (JMLEntityAddOn<? super I>) superEntity.getEntityAddOn(getAddOn());
						if (superEntityAddOn != null) {
							returned = superEntityAddOn.getJMLMethodDefinition(method);
						}
						if (returned != null) {
							return returned;
						}
					}
				}
			} catch (ModelDefinitionException e) {
				e.printStackTrace();
			}
		}
		return returned;
	}

	public JMLInvariant<I> getInvariant() {
		return invariant;
	}

	/**
	 * Indicates if supplied method should be intercepted in the context of JML : only methods with JML definition are concerned
	 * 
	 * @param method
	 * @return
	 */
	@Override
	public boolean isMethodToBeIntercepted(Method method) {
		return getJMLMethodDefinition(method) != null;
	}

	/**
	 * Indicates if supplied method execution should trigger a monitoring : only methods with JML definition are concerned
	 * 
	 * @param method
	 * @return
	 */
	@Override
	public boolean isMethodToBeMonitored(Method method) {
		return getJMLMethodDefinition(method) != null;
	}

	@Override
	public Set<ModelEntity<?>> getEntitiesToMonitor() {
		return Collections.singleton(getModelEntity());
	}

}
