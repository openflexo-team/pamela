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

package org.openflexo.pamela.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.openflexo.pamela.annotations.Parameter;

public class ModelInitializer {

	private org.openflexo.pamela.annotations.Initializer initializer;
	private Method initializingMethod;
	// The name of each parameter in the order they appear on the initializing method
	private List<String> parameters;

	public ModelInitializer(org.openflexo.pamela.annotations.Initializer initializer, Method initializingMethod) {
		this.initializer = initializer;
		this.initializingMethod = initializingMethod;
		this.parameters = new ArrayList<>(initializingMethod.getParameterTypes().length);
		for (Annotation[] annotations : initializingMethod.getParameterAnnotations()) {
			boolean found = false;
			for (Annotation annotation : annotations) {
				if (annotation.annotationType() == Parameter.class) {
					parameters.add(((Parameter) annotation).value());
					found = true;
					break;
				}
			}
			if (!found) {
				// In case we don't find any annotation on this parameter, we add null to keep the list consistent.
				// We may imagine that an implementing class requires an additional initializing property which does not belong
				// to the model.
				parameters.add(null);
			}
		}
	}

	public List<String> getParameters() {
		return parameters;
	}

	public Method getInitializingMethod() {
		return initializingMethod;
	}
}
