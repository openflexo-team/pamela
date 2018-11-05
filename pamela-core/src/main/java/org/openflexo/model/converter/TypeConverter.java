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

package org.openflexo.model.converter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.CustomTypeFactory;
import org.openflexo.connie.type.ParameterizedTypeImpl;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.connie.type.WilcardTypeImpl;
import org.openflexo.model.StringConverterLibrary.Converter;
import org.openflexo.model.exceptions.InvalidDataException;
import org.openflexo.model.factory.ModelFactory;

public class TypeConverter extends Converter<Type> {

	private final Map<Class<? extends CustomType>, CustomTypeFactory<?>> factories;

	private final List<CustomType> deserializedTypes = new ArrayList<>();

	public TypeConverter(Map<Class<? extends CustomType>, CustomTypeFactory<?>> factories) {
		super(Type.class);
		this.factories = factories;
	}

	@Override
	public Type convertFromString(String value, ModelFactory factory) throws InvalidDataException {

		value = value.trim();
		if (StringUtils.isEmpty(value)) {
			return null;
		}

		if (value.equals("?")) {
			return new WilcardTypeImpl();
		}

		if (value.startsWith("? extends ")) {
			String upperBoundsList = value.substring(10);
			List<String> upperBoundsListAsString = tokenizeAsTypeList(upperBoundsList);
			Type[] upperBounds = new Type[upperBoundsListAsString.size()];
			for (int i = 0; i < upperBoundsListAsString.size(); i++) {
				String typeAsString = upperBoundsListAsString.get(i);
				upperBounds[i] = convertFromString(typeAsString, factory);
			}
			return new WilcardTypeImpl(upperBounds, new Type[0]);
		}

		if (value.indexOf("<") > -1) {
			String baseClassName = value.substring(0, value.indexOf("<"));
			String configuration = value.substring(value.indexOf("<") + 1, value.length() - 1);
			Class<? extends CustomType> customTypeClass;
			try {
				customTypeClass = (Class<? extends CustomType>) Class.forName(baseClassName);
			} catch (ClassNotFoundException e) {
				// Warns about the exception
				throw new InvalidDataException("Supplied value represents a type not found: " + value);
			}

			// Attempt to decode as java type
			if (customTypeClass.getTypeParameters() != null && customTypeClass.getTypeParameters().length > 0) {
				List<String> typesAsString = tokenizeAsTypeList(configuration);
				if (customTypeClass.getTypeParameters().length == typesAsString.size()) {
					Type[] parameters = new Type[typesAsString.size()];
					for (int i = 0; i < typesAsString.size(); i++) {
						String typeAsString = typesAsString.get(i);
						try {
							parameters[i] = convertFromString(typeAsString, factory);
						} catch (InvalidDataException typeNotFound) {
							// In this case, we don't try to retrieve the type
							parameters[i] = new WilcardTypeImpl();
						}
					}
					return new ParameterizedTypeImpl(customTypeClass, parameters);
				}
			}

			if (factories == null) {
				System.err.println("No custom type factories found while deserializing " + value + " as " + customTypeClass);
				throw new InvalidDataException("No custom type factories found while deserializing " + value + " as " + customTypeClass);
			}

			CustomTypeFactory<?> customTypeFactory = factories.get(customTypeClass);

			if (customTypeFactory == null) {
				throw new InvalidDataException("Supplied value represents a type with no known factory:" + customTypeClass);
			}

			CustomType returned = customTypeFactory.makeCustomType(configuration);

			deserializedTypes.add(returned);

			return returned;
		}

		else {
			// TODO: this should handled from a proper way
			// Here is a quick fix
			try {
				if (value.equals("boolean")) {
					return Boolean.class;
				}
				else if (value.equals("int")) {
					return Integer.class;
				}
				else if (value.equals("long")) {
					return Long.class;
				}
				else if (value.equals("float")) {
					return Float.class;
				}
				else if (value.equals("double")) {
					return Double.class;
				}
				return Class.forName(value);
			} catch (ClassNotFoundException e) {
				// Warns about the exception
				throw new InvalidDataException("Supplied value represents a type not found: " + value);
			}
		}
	}

	@Override
	public String convertToString(Type value) {

		if (value instanceof CustomType) {
			return value.getClass().getName() + "<" + ((CustomType) value).getSerializationRepresentation() + ">";
		}
		else {
			return TypeUtils.fullQualifiedRepresentation(value);
		}
	}

	public void startDeserializing() {
		deserializedTypes.clear();
	}

	public void stopDeserializing() {
		// We iterate on all type that have been deserialized and try to resolve types that are not fully resolved
		for (CustomType t : new ArrayList<>(deserializedTypes)) {
			// System.out.println("> type: " + t.getSerializationRepresentation());
			if (!t.isResolved()) {
				// System.out.println("resolve");
				CustomTypeFactory<?> customTypeFactory = factories.get(t.getClass());
				t.resolve(customTypeFactory);
			}
		}
	}

	/**
	 * Tokenize supplied string while escaping some characters ('<' and '>')
	 * 
	 * @param s
	 * @return
	 */
	private static List<String> tokenizeAsTypeList(String s) {
		List<String> returned = new ArrayList<>();
		String current = "";
		int level = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '<') {
				level++;
				current += c;
			}
			else if (c == '>') {
				level--;
				current += c;
			}
			else if (level == 0 && c == ',') {
				returned.add(current);
				current = "";
			}
			else {
				current += c;
			}
		}
		if (StringUtils.isNoneEmpty(current)) {
			returned.add(current);
		}
		return returned;
	}
}
