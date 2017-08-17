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

package org.openflexo.model8;

import java.util.List;

import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.factory.AccessibleProxyObject;
import org.openflexo.model.factory.CloneableProxyObject;

@ModelEntity
@XMLElement
public interface ConceptA extends AccessibleProxyObject, CloneableProxyObject {

	static final String CONCEPT_B = "conceptB";
	static final String CONCEPT_C = "conceptC";
	static final String VALUE = "value";

	@Getter(VALUE)
	@XMLAttribute
	String getValue();

	@Setter(VALUE)
	public void setValue(String value);

	@Getter(CONCEPT_B)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	@XMLElement
	ConceptB getConceptB();

	@Setter(CONCEPT_B)
	void setConceptB(ConceptB value);

	@Getter(value = CONCEPT_C, cardinality = Cardinality.LIST)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	@XMLElement
	public List<ConceptC> getConceptCs();

	@Adder(CONCEPT_C)
	public void addToConceptCs(ConceptC c);

	@Remover(CONCEPT_C)
	public void removeFromConceptCs(ConceptC c);

	// @Reindexer(CONCEPT_C)
	// public void moveConceptCToIndex(ConceptC c, int index);

}
