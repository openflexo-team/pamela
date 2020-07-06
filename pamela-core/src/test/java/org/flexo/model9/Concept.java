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

package org.flexo.model9;

import java.util.List;

import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.CloneableProxyObject;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PastingPoint;
import org.openflexo.pamela.annotations.PropertyImplementation;
import org.openflexo.pamela.annotations.Reindexer;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;

@ModelEntity
@XMLElement
public interface Concept extends AccessibleProxyObject, CloneableProxyObject {

	static final String VALUE = "value";
	static final String SUB_CONCEPTS = "someSubConcepts";

	@Getter(value = VALUE)
	@PropertyImplementation(MyStrangePropertyImplementation.class)
	String getValue();

	@Setter(VALUE)
	public void setValue(String value);

	@Getter(value = SUB_CONCEPTS, cardinality = Cardinality.LIST, inverse = SubConcept.MAIN_CONCEPT)
	@PropertyImplementation(MyListCardinalityPropertyImplementation.class)
	@XMLElement
	@Embedded
	List<SubConcept> getSubConcepts();

	@Adder(SUB_CONCEPTS)
	@PastingPoint
	void addToSubConcepts(SubConcept subConcept);

	@Remover(SUB_CONCEPTS)
	void removeFromSubConcepts(SubConcept subConcept);

	@Reindexer(SUB_CONCEPTS)
	void reindexSubConcepts(SubConcept subConcept, int index);

}
