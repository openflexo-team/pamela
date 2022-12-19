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

import org.openflexo.pamela.AssertionViolationException;
import org.openflexo.pamela.factory.PamelaModel;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.model.ModelEntity;

/**
 * Represents an instance of {@link PamelaAddOn} applied to a given {@link PamelaModel}
 * 
 * @author sylvain
 *
 * @param <AO>
 *            type of {@link PamelaAddOn}
 */
public abstract class EntityAddOnInstance<I, EAO extends EntityAddOn<I, AO>, AO extends PamelaAddOn<AO>> {

	private final EAO entityAddOn;
	private final PamelaModel pamelaModel;

	/**
	 * Build a new {@link EntityAddOnInstance} for related {@link EntityAddOn} and {@link PamelaModel}
	 * 
	 * @param modelEntity
	 * @param addOn
	 */
	public EntityAddOnInstance(EAO entityAddOn, PamelaModel pamelaModel) {
		this.entityAddOn = entityAddOn;
		this.pamelaModel = pamelaModel;
	}

	public EAO getEntityAddOn() {
		return entityAddOn;
	}

	public PamelaModel getPamelaModel() {
		return pamelaModel;
	}

	public ModelEntity<I> getModelEntity() {
		return getEntityAddOn().getModelEntity();
	}

	/**
	 * 
	 * @param <T>
	 * @param newInstance
	 * @param modelEntity
	 * @param modelFactory
	 */
	public abstract <T> void notifiedNewInstance(T newInstance, ModelEntity<T> modelEntity, PamelaModelFactory modelFactory);

	/**
	 * Perform monitoring by triggering assertion checking for this {@link EntityAddOnInstance} in the entry of related {@link Method},
	 * asserting this {@link Method} is to be monitored
	 * 
	 * @param method
	 *            The method in which we enter
	 * @param proxyMethodHandler
	 *            Handler of the related object
	 * @param args
	 *            Arguments of the call
	 * @return
	 */
	public abstract void checkOnMethodEntry(Method method, ProxyMethodHandler<I> proxyMethodHandler, Object[] args)
			throws AssertionViolationException;

	/**
	 * Perform monitoring by triggering assertion checking for this {@link EntityAddOnInstance} in the exit of related {@link Method},
	 * asserting this {@link Method} is to be monitored
	 * 
	 * @param method
	 *            The method from which we exit
	 * @param proxyMethodHandler
	 *            Handler of the related object
	 * @param args
	 *            Arguments of the call
	 * @return
	 */
	public abstract void checkOnMethodExit(Method method, ProxyMethodHandler<I> proxyMethodHandler, Object[] args)
			throws AssertionViolationException;

}
