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
package org.openflexo.pamela.addon;

import java.lang.reflect.Method;

import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.model.ModelEntity;

/**
 * Represents a piece of software used in the context of a {@link PamelaAddOn}, and that is associated with a {@link ModelEntity}
 * 
 * Stores and manages all data that are related to a {@link ModelEntity} in the context of a {@link PamelaAddOn}.
 * 
 * @author sylvain
 *
 * @param <I>
 *            java type addressed by this entity
 * @param <AO>
 *            type of {@link PamelaAddOn}
 */
public abstract class EntityAddOn<I, AO extends PamelaAddOn<AO>> {

	private final ModelEntity<I> modelEntity;
	private final AO addOn;

	/**
	 * Build a new {@link EntityAddOn} for related {@link ModelEntity} and {@link PamelaAddOn}
	 * 
	 * @param modelEntity
	 * @param addOn
	 */
	public EntityAddOn(ModelEntity<I> modelEntity, AO addOn) {
		this.modelEntity = modelEntity;
		this.addOn = addOn;
	}

	/**
	 * Return related {@link PamelaAddOn} (final)
	 * 
	 * @return
	 */
	public final AO getAddOn() {
		return addOn;
	}

	/**
	 * Return related {@link ModelEntity} (final)
	 * 
	 * @return
	 */
	public ModelEntity<I> getModelEntity() {
		return modelEntity;
	}

	/**
	 * Return the most specialized interface (PAMELA final interface) describing this {@link ModelEntity} add-on
	 * 
	 * @return
	 */
	public Class<I> getImplementedInterface() {
		return modelEntity.getImplementedInterface();
	}

	/**
	 * Indicates if supplied method should be intercepted in the context of this {@link EntityAddOn}
	 * 
	 * @param method
	 * @return
	 */
	public abstract boolean isMethodToBeIntercepted(Method method);

	/**
	 * Indicates if supplied method execution should trigger a monitoring
	 * 
	 * @param method
	 * @return
	 */
	public abstract boolean isMethodToBeMonitored(Method method);

	/**
	 * Perform monitoring by triggering assertion checking for this {@link EntityAddOn} in the entry of related {@link Method}, asserting
	 * this {@link Method} is to be monitored
	 * 
	 * @param method
	 *            The method in which we enter
	 * @param proxyMethodHandler
	 *            Handler of the related object
	 * @param args
	 *            Arguments of the call
	 * @return
	 */
	public abstract void checkOnMethodEntry(Method method, ProxyMethodHandler<I> proxyMethodHandler, Object[] args);

	/**
	 * Perform monitoring by triggering assertion checking for this {@link EntityAddOn} in the exit of related {@link Method}, asserting
	 * this {@link Method} is to be monitored
	 * 
	 * @param method
	 *            The method from which we exit
	 * @param proxyMethodHandler
	 *            Handler of the related object
	 * @param args
	 *            Arguments of the call
	 * @return
	 */
	public abstract void checkOnMethodExit(Method method, ProxyMethodHandler<I> proxyMethodHandler, Object[] args);

}
