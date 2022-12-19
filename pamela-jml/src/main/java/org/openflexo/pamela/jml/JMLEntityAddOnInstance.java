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
import java.util.HashMap;
import java.util.Map;

import org.openflexo.pamela.addon.EntityAddOnInstance;
import org.openflexo.pamela.factory.PamelaModel;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;

/**
 * Represents an instance of {@link JMLEntityAddOn} applied to a given {@link PamelaModel}
 * 
 * @author sylvain
 *
 * @param <I>
 */
public class JMLEntityAddOnInstance<I> extends EntityAddOnInstance<I, JMLEntityAddOn<I>, JMLAddOn> {

	private Map<Method, Map<String, Object>> historyValues;

	public JMLEntityAddOnInstance(JMLEntityAddOn<I> jmlEntityAddOn, PamelaModel model) {
		super(jmlEntityAddOn, model);
		historyValues = new HashMap<>();
	}

	@Override
	public void checkOnMethodEntry(Method method, ProxyMethodHandler<I> proxyMethodHandler, Object[] args) {

		// We first check invariants
		checkInvariant(proxyMethodHandler);

		JMLMethodDefinition<? super I> jmlMethodDefinition = getEntityAddOn().getJMLMethodDefinition(method);
		if (jmlMethodDefinition != null) {
			// We check pre-conditions
			ModelProperty<? super I> property = getModelEntity().getPropertyForMethod(method);
			if (jmlMethodDefinition.getRequires() != null) {
				// System.out.println("Check pre-condition " + jmlMethodDefinition.getRequires().getExpression());
				((JMLRequires) jmlMethodDefinition.getRequires()).check(proxyMethodHandler, args);
			}
			// And we prepare the postconditions
			if (jmlMethodDefinition.getEnsures() != null) {
				// System.out.println("Init post-condition " + jmlMethodDefinition.getEnsures().getExpression());
				Map<String, Object> historyValuesForThisMethod = ((JMLEnsures) jmlMethodDefinition.getEnsures())
						.checkOnEntry(proxyMethodHandler, args);
				historyValues.put(method, historyValuesForThisMethod);
			}
		}
	}

	@Override
	public void checkOnMethodExit(Method method, ProxyMethodHandler<I> proxyMethodHandler, Object[] args) {

		// We first check invariants
		checkInvariant(proxyMethodHandler);

		JMLMethodDefinition<? super I> jmlMethodDefinition = getEntityAddOn().getJMLMethodDefinition(method);
		if (jmlMethodDefinition != null) {
			ModelProperty<? super I> property = getModelEntity().getPropertyForMethod(method);
			// And we check post-conditions
			if (jmlMethodDefinition.getEnsures() != null) {
				// System.out.println("Check post-condition " + jmlMethodDefinition.getEnsures().getExpression());
				((JMLEnsures) jmlMethodDefinition.getEnsures()).checkOnExit(proxyMethodHandler, args, historyValues.get(method));
			}
		}
	}

	private void checkInvariant(ProxyMethodHandler<I> proxyMethodHandler) {
		if (getEntityAddOn().getInvariant() != null) {
			getEntityAddOn().getInvariant().check(proxyMethodHandler);
		}
	}

	@Override
	public <T> void notifiedNewInstance(T newInstance, ModelEntity<T> modelEntity, PamelaModelFactory modelFactory) {
		// Not relevant for JML
	}
}
