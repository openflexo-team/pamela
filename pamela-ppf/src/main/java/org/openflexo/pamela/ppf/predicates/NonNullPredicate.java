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

import java.util.logging.Logger;

import org.openflexo.pamela.factory.PamelaModel;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.model.ModelProperty;
import org.openflexo.pamela.ppf.PPFViolationException;
import org.openflexo.pamela.ppf.PropertyPredicate;
import org.openflexo.pamela.ppf.PropertyPredicateInstance;

/**
 * "Total" predicate : property value should not be null
 * 
 * @author sylvain
 *
 */
public class NonNullPredicate<I> extends PropertyPredicate<I> {

	private static final Logger logger = Logger.getLogger(NonNullPredicate.class.getPackage().getName());

	public NonNullPredicate(ModelProperty<I> property) {
		super(property);
	}

	@Override
	public NonNullPredicateInstance makeInstance(PamelaModel model) {
		return new NonNullPredicateInstance(model);
	}

	public class NonNullPredicateInstance extends PropertyPredicateInstance<I> {

		public NonNullPredicateInstance(PamelaModel model) {
			super(NonNullPredicate.this, model);
		}

		@Override
		public void check(ProxyMethodHandler<? extends I> proxyMethodHandler) {
			logger.info("Checking NonNullPredicate for " + getProperty() + " and object " + proxyMethodHandler.getObject());
			Object value = proxyMethodHandler.invokeGetter(getProperty());
			if (value == null) {
				throw new PPFViolationException("Property " + getProperty() + " not defined for " + proxyMethodHandler.getObject(),
						proxyMethodHandler);
			}
		}
	}
}
