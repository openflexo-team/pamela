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

package org.openflexo.model.validation;

import java.util.Collection;
import java.util.Hashtable;
import java.util.logging.Logger;

/**
 * Automatic fix proposal with parameters
 * 
 * @author sguerin
 * 
 */
public abstract class ParameteredFixProposal<R extends ValidationRule<R, V>, V extends Validable> extends FixProposal<R, V> {

	private static final Logger logger = Logger.getLogger(ParameteredFixProposal.class.getPackage().getName());

	private Hashtable<String, ParameterDefinition<?>> parameters;

	public ParameteredFixProposal(String aMessage, ParameterDefinition<?>[] parameters) {
		super(aMessage);
		this.parameters = new Hashtable<String, ParameterDefinition<?>>();
		for (int i = 0; i < parameters.length; i++) {
			this.parameters.put(parameters[i].getName(), parameters[i]);
		}
	}

	public ParameteredFixProposal(String aMessage, String paramName, String paramLabel, String paramDefaultValue) {
		this(aMessage, singleParameterWith(paramName, paramLabel, paramDefaultValue));
	}

	public StringParameter getStringParameter(String parameterName) {
		for (ParameterDefinition<?> p : getParameters()) {
			if (p instanceof StringParameter && ((StringParameter) p).getName().equals(parameterName)) {
				return (StringParameter) p;
			}
		}
		return null;
	}

	private static ParameterDefinition<?>[] singleParameterWith(String paramName, String paramLabel, String paramDefaultValue) {
		ParameterDefinition<?>[] returned = { new StringParameter(paramName, paramLabel, paramDefaultValue) };
		return returned;
	}

	public Object getValueForParameter(String name) {
		return ((ParameterDefinition<?>) parameters.get(name)).getValue();
	}

	public Collection<ParameterDefinition<?>> getParameters() {
		return parameters.values();
	}

	public void updateBeforeApply() {
		// Override
	}

	public static class ParameterDefinition<T> {

		private String name;
		private String label;
		private T value;

		public ParameterDefinition(String name, String label, T value) {
			super();
			this.name = name;
			this.label = label;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public T getValue() {
			return value;
		}

		public void setValue(T value) {
			this.value = value;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}
	}

	public static class StringParameter extends ParameterDefinition<String> {

		public StringParameter(String name, String label, String value) {
			super(name, label, value);
		}
	}
}
