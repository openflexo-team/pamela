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

package org.openflexo.pamela.factory;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Namespace;

public class PamelaConstants {
	public static final String NAMESPACE_PREFIX = "p";
	public static final String NS = "http://www.openflexo.org/pamela/";
	public static final Namespace NAMESPACE = Namespace.getNamespace(NAMESPACE_PREFIX, NS);
	public static final String CLASS_ATTRIBUTE = "class";
	public static final String MODEL_ENTITY_ATTRIBUTE = "modelEntity";

	public static final String Q_CLASS_ATTRIBUTE = NAMESPACE_PREFIX + ":" + CLASS_ATTRIBUTE;
	public static final String Q_MODEL_ENTITY_ATTRIBUTE = NAMESPACE_PREFIX + ":" + MODEL_ENTITY_ATTRIBUTE;

	public static final Set<String> PAMELA_ATTRIBUTES = Stream.of(CLASS_ATTRIBUTE, MODEL_ENTITY_ATTRIBUTE).collect(Collectors.toSet());

	public static boolean isPamelaAttribute(String namespace, String name) {
		return PamelaConstants.NS.equals(namespace) && PamelaConstants.PAMELA_ATTRIBUTES.contains(name);
	}
}
