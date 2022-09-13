/*
 * Copyright (c) 2013-2017, Openflexo
 *
 * This file is part of Flexo-foundation, a component of the software infrastructure
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
 *           Additional permission under GNU GPL version 3 section 7
 *           If you modify this Program, or any covered work, by linking or
 *           combining it with software containing parts covered by the terms
 *           of EPL 1.0, the licensors of this Program grant you additional permission
 *           to convey the resulting work.
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

package org.openflexo.pamela.test.updatewith4;

import java.util.List;

import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Reindexer;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;

@ModelEntity
@ImplementationClass(ChildConcept.ChildConceptImpl.class)
public interface ChildConcept extends AbstractConcept {

	static final String PARENT = "parent";

	@Getter(value = PARENT, inverse = ParentConcept.CHILDREN)
	ParentConcept getParent();

	@Setter(PARENT)
	void setParent(ParentConcept value);

	static final String SIBLINGS = "siblings";

	@Getter(value = SIBLINGS, cardinality = Cardinality.LIST)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<ChildConcept> getSiblings();

	@Adder(SIBLINGS)
	public void addToSiblings(ChildConcept c);

	@Remover(SIBLINGS)
	public void removeFromSiblings(ChildConcept c);

	@Reindexer(SIBLINGS)
	public void moveSiblingsToIndex(ChildConcept c, int index);

	public static abstract class ChildConceptImpl implements ChildConcept {
		@Override
		public String toString() {
			return "ChildConcept[" + getValue() + "]-" + Integer.toHexString(hashCode());
		}
	}
}
