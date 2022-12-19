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

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.pamela.factory.PamelaModel;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.model.ModelProperty;
import org.openflexo.pamela.ppf.PPFViolationException;
import org.openflexo.pamela.ppf.PropertyPredicate;
import org.openflexo.pamela.ppf.PropertyPredicateInstance;
import org.openflexo.pamela.ppf.annotations.Card;

/**
 * "Card" predicate : cardinality should be in a given range
 * 
 * @author sylvain
 * 
 */
public class CardPredicate<I> extends PropertyPredicate<I> {

	private static final Logger logger = Logger.getLogger(CardPredicate.class.getPackage().getName());

	private int min;
	private int max;

	public CardPredicate(ModelProperty<I> property, Card annotation) {
		super(property);
		min = annotation.min();
		max = annotation.max();
	}

	@Override
	public CardPredicateInstance makeInstance(PamelaModel model) {
		return new CardPredicateInstance(model);
	}

	public class CardPredicateInstance extends PropertyPredicateInstance<I> {

		public CardPredicateInstance(PamelaModel model) {
			super(CardPredicate.this, model);
		}

		@Override
		public void check(ProxyMethodHandler<? extends I> proxyMethodHandler) throws PPFViolationException {
			logger.info("Checking CardPredicate for " + getProperty() + " and object " + proxyMethodHandler.getObject());
			Object value = proxyMethodHandler.invokeGetter(getProperty());
			if (value == null) {
				throw new PPFViolationException("Property " + getProperty() + " not defined for " + proxyMethodHandler.getObject(),
						proxyMethodHandler);
			}
			if (value instanceof List) {
				int cardinality = ((List) value).size();
				if (cardinality < min || cardinality > max) {
					throw new PPFViolationException(
							"Property " + getProperty() + " not in range (" + min + ":" + max + ") for " + proxyMethodHandler.getObject(),
							proxyMethodHandler);
				}
			}
			else {
				throw new PPFViolationException(
						"Unexpected property value " + value + " for " + getProperty() + " for " + proxyMethodHandler.getObject(),
						proxyMethodHandler);
			}
		}
	}
}
