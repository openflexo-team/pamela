/**
 * 
 * Copyright (c) 2014, Openflexo
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


package org.openflexo.pamela.test.persistence;

import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.CloneableProxyObject;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * A model entity with the three kinds of property the persistence rule distinguishes (PAMELA-F-3)
 */
@ModelEntity
@XMLElement
public interface Document extends AccessibleProxyObject, CloneableProxyObject {

	String NAME = "name";
	String NOTE = "note";
	String CACHE = "cache";
	String LENGTH = "length";

	/** Persistent in both modes: it carries an XML mapping */
	@Getter(value = NAME)
	@XMLAttribute
	String getName();

	@Setter(value = NAME)
	void setName(String name);

	/** Persistent only under the rule: neither derived nor transient, but no XML mapping */
	@Getter(value = NOTE)
	String getNote();

	@Setter(value = NOTE)
	void setNote(String note);

	/** Never persistent: transient */
	@Getter(value = CACHE, isTransient = true)
	String getCache();

	@Setter(value = CACHE)
	void setCache(String cache);

	/** Never persistent: derived */
	@Getter(value = LENGTH, isDerived = true)
	Integer getLength();

	@Setter(value = LENGTH)
	void setLength(Integer length);
}
