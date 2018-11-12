/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.pamela.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openflexo.pamela.StringEncoder;
import org.openflexo.pamela.StringConverterLibrary.Converter;
import org.openflexo.pamela.factory.ModelFactory;

/**
 * Annotation for a getter
 * 
 * @author Guillaume
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface Getter {

	enum Cardinality {
		SINGLE, LIST, MAP;
	}

	String UNDEFINED = "";

	/**
	 * The property identifier of this getter
	 * 
	 * @return the property identifier of this getter
	 */
	String value();

	/**
	 * The cardinality of this getter
	 * 
	 * @return the cardinality of the getter
	 */
	Cardinality cardinality() default Cardinality.SINGLE;

	/**
	 * The inverse property identifier of this getter. Depending on the cardinality of this property and its inverse, this property will be
	 * either set or added to the inverse property of this property value.
	 * 
	 * @return
	 */
	String inverse() default UNDEFINED;

	/**
	 * A string convertable value that is set by default on the property identified by this getter
	 * 
	 * @return the string converted default value.
	 */
	String defaultValue() default UNDEFINED;

	/**
	 * Indicates that the type returned by this getter can be converted to a string using a {@link Converter}. Upon
	 * serialization/deserialization, the {@link ModelFactory} will provide, through its {@link StringEncoder}, an appropriate
	 * {@link Converter}. Failing to do that will result in an Exception
	 * 
	 * The default value is <code>false</code>
	 * 
	 * @return true if the type returned by this getter can be converted to a string.
	 */
	boolean isStringConvertable() default false;

	/**
	 * Indicates that the type returned by this getter should not be included in the model. This flag allows to manipulate types that are
	 * unknown to PAMELA. If set to true, PAMELA will not try to interpret those classes and will not be able to serialize them
	 * 
	 * @return true if PAMELA should not import the type of the property identified by this getter.
	 */
	boolean ignoreType() default false;

	/**
	 * Only relevant for multiple cardinality properties. When set to true, indicates that many occurences of same object might be present
	 * in the property (you can add the same object multiple times)<br>
	 * Note that the equals() semantics is used here
	 * 
	 * @return
	 */
	boolean allowsMultipleOccurences() default false;

	class GetterImpl implements Getter {
		private final String value;
		private final Cardinality cardinality;
		private final String inverse;
		private final String defaultValue;
		private final boolean stringConvertable;
		private final boolean ignoreType;
		private final boolean allowsMultipleOccurences;

		public GetterImpl(String value, Cardinality cardinality, String inverse, String defaultValue, boolean stringConvertable,
				boolean ignoreType, boolean allowsMultipleOccurences) {
			this.value = value;
			this.cardinality = cardinality;
			this.inverse = inverse;
			this.defaultValue = defaultValue;
			this.stringConvertable = stringConvertable;
			this.ignoreType = ignoreType;
			this.allowsMultipleOccurences = allowsMultipleOccurences;
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return Getter.class;
		}

		@Override
		public String value() {
			return value;
		}

		@Override
		public Cardinality cardinality() {
			return cardinality;
		}

		@Override
		public String defaultValue() {
			return defaultValue;
		}

		@Override
		public String inverse() {
			return inverse;
		}

		@Override
		public boolean isStringConvertable() {
			return stringConvertable;
		}

		@Override
		public boolean ignoreType() {
			return ignoreType;
		}

		@Override
		public boolean allowsMultipleOccurences() {
			return allowsMultipleOccurences;
		}
	}
}
