/**
 * 
 */
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

import java.util.Hashtable;
import java.util.Map;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.pamela.exceptions.InvalidDataException;
import org.openflexo.pamela.model.StringConverterLibrary;
import org.openflexo.pamela.model.StringConverterLibrary.Converter;

import com.google.common.primitives.Primitives;

public class StringEncoder {
	private Map<Class<?>, Converter<?>> converters = new Hashtable<>();

	private PamelaModelFactory pamelaModelFactory;

	public StringEncoder(PamelaModelFactory pamelaModelFactory) {
		this.pamelaModelFactory = pamelaModelFactory;
	}

	public String toString(Object object) throws InvalidDataException {
		if (object == null) {
			return null;
		}
		Class<? extends Object> aClass = object.getClass();
		ProxyMethodHandler<?> handler = null;
		if (pamelaModelFactory != null) {
			handler = pamelaModelFactory.getHandler(object);
		}
		if (handler != null) {
			aClass = handler.getModelEntity().getImplementedInterface();
		}
		Converter converter = converterForClass(aClass);
		if (converter != null) {
			return converter.convertToString(object);
		}
		if (object instanceof Enum) {
			return ((Enum<?>) object).name();
		}
		throw new InvalidDataException("Supplied value has no converter for type " + aClass.getName());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> T fromString(Class<T> type, String value) throws InvalidDataException {
		if (value == null) {
			return null;
		}
		if (type == null) {
			return null;
		}
		Converter<T> converter = converterForClass(type);
		if (converter != null) {
			return converter.convertFromString(value, pamelaModelFactory);
		}
		else if (type.isEnum()) {
			try {
				return (T) Enum.valueOf((Class<Enum>) type, value);
			} catch (IllegalArgumentException e) {
				System.err.println("Could not decode " + value + " as a " + type);
				return null;
			}
		}
		else {
			throw new InvalidDataException("Supplied value has no converter for type " + type.getName());
		}
	}

	/**
	 * Hereunder are all the non-static elements of this class. Only those should be used.
	 */

	public <T> Converter<T> addConverter(Converter<T> converter) {
		converters.put(converter.getConverterClass(), converter);
		if (Primitives.isWrapperType(converter.getConverterClass())) {
			converters.put(Primitives.unwrap(converter.getConverterClass()), converter);
		}
		return converter;
	}

	/**
	 * @param converter
	 */
	public void removeConverter(Converter<?> converter) {
		converters.remove(converter.getConverterClass());
	}

	public <T> Converter<T> converterForClass(Class<T> objectType) {
		// 1. We try with custom converters
		Converter<T> converterForClass = converterForClass(objectType, converters);
		if (converterForClass == null) {
			// 2. We try with model-defined converters
			converterForClass = converterForClass(objectType, StringConverterLibrary.getInstance().getConverters());
		}
		return converterForClass;
	}

	public static <T> Converter<T> converterForClass(Class<T> objectType, Map<Class<?>, Converter<?>> convertersMap) {
		Converter<?> returned;
		Class<?> candidate = objectType;
		// do {
		// returned = convertersMap.get(candidate);
		returned = TypeUtils.objectForClass(candidate, convertersMap, false);
		/*if (candidate.equals(Object.class)) {
			candidate = null;
		} else {
			candidate = candidate.getSuperclass();
		}*/
		// } while (returned == null && candidate != null);
		return (Converter<T>) returned;
	}

	public boolean isConvertable(Class<?> type) {
		return converterForClass(type) != null || type.isEnum();
	}

}
