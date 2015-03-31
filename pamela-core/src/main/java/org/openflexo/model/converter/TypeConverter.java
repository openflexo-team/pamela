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

import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.CustomTypeFactory;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.model.StringConverterLibrary.Converter;
import org.openflexo.model.exceptions.InvalidDataException;
import org.openflexo.model.factory.ModelFactory;

public class TypeConverter extends Converter<Type> {

	private final Map<Class<? extends CustomType>, CustomTypeFactory<?>> factories;

	private final List<CustomType> deserializedTypes = new ArrayList<CustomType>();

	public TypeConverter(Map<Class<? extends CustomType>, CustomTypeFactory<?>> factories) {
		super(Type.class);
		this.factories = factories;
	}

	@Override
	public Type convertFromString(String value, ModelFactory factory) throws InvalidDataException {

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

			System.out.println("Found " + customTypeClass);

			CustomTypeFactory<?> customTypeFactory = factories.get(customTypeClass);

			if (customTypeFactory == null) {
				throw new InvalidDataException("Supplied value represents a type with no known factory:" + customTypeClass);
			}

			CustomType returned = customTypeFactory.makeCustomType(configuration);

			deserializedTypes.add(returned);

			return returned;

			/*try {
				Constructor<?> noArgConstructor = customTypeClass.getConstructor();
				returned = (CustomType) noArgConstructor.newInstance();
			} catch (NoSuchMethodException e) {
				System.out.println("Pas de constructeur sans argument");
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

			try {
				if (customTypeClass.getConstructors().length > 0) {
					Constructor<?> constructorWithArgs = customTypeClass.getConstructors()[0];
					Object[] args = new Object[constructorWithArgs.getGenericParameterTypes().length];
					for (int i = 0; i < args.length; i++) {
						args[i] = null;
					}
					returned = (CustomType) constructorWithArgs.newInstance(args);
				} else {
					System.out.println("Pas de constructeurs");
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}*/

			/*System.out.println("return " + returned);

			if (returned != null) {
				System.out.println("set config " + configuration);
				returned.setSerializedConfiguration(configuration, factory);
			}

			return null;*/
		}

		else {
			try {
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
		} else {
			return TypeUtils.fullQualifiedRepresentation(value);
		}
	}

	public void startDeserializing() {
		deserializedTypes.clear();
	}

	public void stopDeserializing() {
		// We iterate on all type that have been deserialized and try to resolve types that are not fully resolved
		for (CustomType t : deserializedTypes) {
			if (!t.isResolved()) {
				CustomTypeFactory<?> customTypeFactory = factories.get(t.getClass());
				t.resolve(customTypeFactory);
			}
		}
	}
}
