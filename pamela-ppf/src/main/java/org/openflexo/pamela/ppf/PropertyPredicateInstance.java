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
package org.openflexo.pamela.ppf;

import org.openflexo.pamela.factory.PamelaModel;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.model.ModelEntity;

import javassist.util.proxy.ProxyObject;

/**
 * An "instance" of a {@link PropertyPredicate} related to a {@link PamelaModel}
 * 
 * @author sylvain
 *
 */
public abstract class PropertyPredicateInstance<I> {

	private final PropertyPredicate<I> predicate;
	private final PamelaModel model;

	public PropertyPredicateInstance(PropertyPredicate<I> propertyPredicate, PamelaModel model) {
		predicate = propertyPredicate;
		this.model = model;
	}

	public PropertyPredicate<I> getPredicate() {
		return predicate;
	}

	public PamelaModel getModel() {
		return model;
	}

	public <I> ProxyMethodHandler<I> getHandler(I object) {
		if (object instanceof ProxyObject) {
			if (((ProxyObject) object).getHandler() instanceof ProxyMethodHandler) {
				return (ProxyMethodHandler<I>) ((ProxyObject) object).getHandler();
			}
		}
		return null;
	}

	public abstract void check(ProxyMethodHandler<? extends I> proxyMethodHandler) throws PPFViolationException;

	public void notifiedNewSourceInstance(I newInstance, ModelEntity<I> modelEntity, PamelaModelFactory modelFactory) {
		// Does nothing by default
	}

	public <T> void notifiedNewDestinationInstance(T newInstance, ModelEntity<T> modelEntity, PamelaModelFactory modelFactory) {
		// Does nothing by default
	}

}
