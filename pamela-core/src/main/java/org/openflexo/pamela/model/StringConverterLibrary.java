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

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import org.openflexo.pamela.converter.DataBindingConverter;
import org.openflexo.pamela.exceptions.InvalidDataException;
import org.openflexo.pamela.factory.PamelaModelFactory;

import com.google.common.primitives.Primitives;

public class StringConverterLibrary {

	private static final StringConverterLibrary instance = new StringConverterLibrary();

	public static StringConverterLibrary getInstance() {
		return instance;
	}

	private final Map<Class<?>, Converter<?>> converters;

	private final Map<Class<?>, Converter<?>> unmodifiableConverters;

	public StringConverterLibrary() {
		converters = new Hashtable<>();
		unmodifiableConverters = Collections.unmodifiableMap(converters);
		addConverter(new BooleanConverter());
		addConverter(new IntegerConverter());
		addConverter(new ShortConverter());
		addConverter(new LongConverter());
		addConverter(new FloatConverter());
		addConverter(new DoubleConverter());
		addConverter(new StringConverter());
		addConverter(new DateConverter());
		addConverter(new URLConverter());
		addConverter(new FileConverter());
		addConverter(new ClassConverter<>());
		addConverter(new PointConverter());
		addConverter(new ColorConverter());
		addConverter(new FontConverter());
		addConverter(new NumberConverter());
		addConverter(new DataBindingConverter());
		// addConverter(new ResourceLocationConverter());
	}

	public Map<Class<?>, Converter<?>> getConverters() {
		return unmodifiableConverters;
	}

	public <T> Converter<T> getConverter(Class<T> type) {
		return (Converter<T>) converters.get(type);
	}

	public boolean hasConverter(Class<?> type) {
		return converters.containsKey(type);
	}

	synchronized void addConverter(Converter<?> converter) {
		converters.put(converter.getConverterClass(), converter);
		if (Primitives.isWrapperType(converter.getConverterClass())) {
			converters.put(Primitives.unwrap(converter.getConverterClass()), converter);
		}
	}

	/**
	 * Abstract class defining a converter to and from a String for a given class
	 * 
	 * @author sguerin
	 */
	public static abstract class Converter<T> {

		protected Class<? super T> converterClass;

		public Converter(Class<? super T> aClass) {
			super();
			converterClass = aClass;
		}

		public Class<? super T> getConverterClass() {
			return converterClass;
		}

		public abstract T convertFromString(String value, PamelaModelFactory factory) throws InvalidDataException;

		public abstract String convertToString(T value);

	}

	/**
	 * Class defining how to convert Boolean from/to String
	 * 
	 * @author sguerin
	 */
	private static class BooleanConverter extends Converter<Boolean> {

		private BooleanConverter() {
			super(Boolean.class);
		}

		@Override
		public Boolean convertFromString(String value, PamelaModelFactory factory) {
			return Boolean.valueOf(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes"));
		}

		@Override
		public String convertToString(Boolean value) {
			return value.toString();
		}

	}

	/**
	 * Class defining how to convert Integer from/to String
	 * 
	 * @author sguerin
	 */
	private static class NumberConverter extends Converter<Number> {

		private NumberConverter() {
			super(Number.class);
		}

		@Override
		public Number convertFromString(String value, PamelaModelFactory factory) {
			try {
				Number returned = Integer.parseInt(value);
				// System.out.println("Build a integer: "+value);
				return returned;
			} catch (NumberFormatException e1) {
				try {
					Number returned = Long.parseLong(value);
					// System.out.println("Build a long: "+value);
					return returned;
				} catch (NumberFormatException e2) {
					try {
						Number returned = Float.parseFloat(value);
						// System.out.println("Build a float: "+value);
						return returned;
					} catch (NumberFormatException e3) {
						try {
							Number returned = Double.parseDouble(value);
							// System.out.println("Build a double: "+value);
							return returned;
						} catch (NumberFormatException e4) {}
					}
				}
			}

			return null;
		}

		@Override
		public String convertToString(Number value) {
			return value.toString();
		}

	}

	/**
	 * Class defining how to convert Integer from/to String
	 * 
	 * @author sguerin
	 */
	private static class IntegerConverter extends Converter<Integer> {

		private IntegerConverter() {
			super(Integer.class);
		}

		@Override
		public Integer convertFromString(String value, PamelaModelFactory factory) {
			return Integer.valueOf(value);
		}

		@Override
		public String convertToString(Integer value) {
			return value.toString();
		}

	}

	/**
	 * Class defining how to convert Short from/to String
	 * 
	 * @author sguerin
	 */
	private static class ShortConverter extends Converter<Short> {

		private ShortConverter() {
			super(Short.class);
		}

		@Override
		public Short convertFromString(String value, PamelaModelFactory factory) {
			return Short.valueOf(value);
		}

		@Override
		public String convertToString(Short value) {
			return value.toString();
		}

	}

	/**
	 * Class defining how to convert Long from/to String
	 * 
	 * @author sguerin
	 */
	private static class LongConverter extends Converter<Long> {

		protected LongConverter() {
			super(Long.class);
		}

		@Override
		public Long convertFromString(String value, PamelaModelFactory factory) {
			return Long.valueOf(value);
		}

		@Override
		public String convertToString(Long value) {
			return value.toString();
		}

	}

	/**
	 * Class defining how to convert Float from/to String
	 * 
	 * @author sguerin
	 */
	private static class FloatConverter extends Converter<Float> {

		protected FloatConverter() {
			super(Float.class);
		}

		@Override
		public Float convertFromString(String value, PamelaModelFactory factory) {
			return Float.valueOf(value);
		}

		@Override
		public String convertToString(Float value) {
			return value.toString();
		}

	}

	/**
	 * Class defining how to convert Double from/to String
	 * 
	 * @author sguerin
	 */
	private static class DoubleConverter extends Converter<Double> {

		protected DoubleConverter() {
			super(Double.class);
		}

		@Override
		public Double convertFromString(String value, PamelaModelFactory factory) {
			try {
				return Double.valueOf(value);
			} catch (NumberFormatException e) {
				if (value.equals("POSITIVE_INFINITY")) {
					return Double.POSITIVE_INFINITY;
				}
				else if (value.equals("NEGATIVE_INFINITY")) {
					return Double.NEGATIVE_INFINITY;
				}
				throw e;
			}
		}

		@Override
		public String convertToString(Double value) {
			return value.toString();
		}

	}

	/**
	 * Class defining how to convert String from/to String (easy !)
	 * 
	 * @author sguerin
	 */
	private static class StringConverter extends Converter<String> {

		protected StringConverter() {
			super(String.class);
		}

		@Override
		public String convertFromString(String value, PamelaModelFactory factory) {
			return value;
		}

		@Override
		public String convertToString(String value) {
			return value;
		}

	}

	/**
	 * Class defining how to convert String from/to Date
	 * 
	 * @author sguerin
	 */
	private static class DateConverter extends Converter<Date> {

		/** Specify date format */
		protected String _dateFormat = new SimpleDateFormat().toPattern();

		private DateConverter() {
			super(Date.class);
		}

		@Override
		public Date convertFromString(String value, PamelaModelFactory factory) {
			try {
				return tryToConvertFromString(value);
			} catch (ParseException e) {
				SimpleDateFormat formatter = new SimpleDateFormat(_dateFormat);
				Date currentTime = new Date();
				String dateString = formatter.format(currentTime);
				System.err.println("Supplied value is not parsable as a date. " + " Date format should be for example " + dateString);
				return null;
			}
		}

		public Date tryToConvertFromString(String value) throws ParseException {
			Date returned = null;
			StringTokenizer st = new StringTokenizer(value, ",");
			String dateFormat = _dateFormat;
			String dateAsString = null;
			if (st.hasMoreTokens()) {
				dateFormat = st.nextToken();
			}
			if (st.hasMoreTokens()) {
				dateAsString = st.nextToken();
			}
			if (dateAsString != null) {
				try {
					returned = new SimpleDateFormat(dateFormat).parse(dateAsString);
				} catch (IllegalArgumentException e) {
					throw new ParseException("While parsing supposed date format: " + e.getMessage(), 0);
				}
			}
			if (returned == null) {
				throw new ParseException("Cannot parse as a date " + value, 0);
			}
			return returned;
		}

		@Override
		public String convertToString(Date date) {
			if (date != null) {
				return _dateFormat + "," + new SimpleDateFormat(_dateFormat).format(date);
			}
			return null;
		}

		/**
		 * Return a string representation of a date, according to valid date format
		 */
		public String getDateRepresentation(Date date) {
			if (date != null) {
				return new SimpleDateFormat(_dateFormat).format(date);
			}
			return null;
		}

	}

	/**
	 * Class defining how to convert String from/to URL
	 * 
	 * @author sguerin
	 */
	private static class URLConverter extends Converter<URL> {

		private URLConverter() {
			super(URL.class);
		}

		@Override
		public URL convertFromString(String value, PamelaModelFactory factory) {
			try {
				return new URL(value);
			} catch (MalformedURLException e) {
				System.err.println("Supplied value is not parsable as an URL:" + value);
				return null;
			}
		}

		@Override
		public String convertToString(URL anURL) {
			if (anURL != null) {
				return anURL.toExternalForm();
			}
			return null;
		}

	}

	/**
	 * Class defining how to convert String from/to File
	 * 
	 * @author sguerin
	 */
	private static class FileConverter extends Converter<File> {

		private FileConverter() {
			super(File.class);
		}

		@Override
		public File convertFromString(String value, PamelaModelFactory factory) {
			return new File(value);
		}

		@Override
		public String convertToString(File aFile) {
			if (aFile != null) {
				return aFile.getAbsolutePath();
			}
			return null;
		}

	}

	/**
	 * Class defining how to convert String from/to Class
	 * 
	 * @author sguerin
	 */
	private static class ClassConverter<T> extends Converter<Class<T>> {

		private ClassConverter() {
			super(Class.class);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Class<T> convertFromString(String value, PamelaModelFactory factory) throws InvalidDataException {
			try {
				return (Class) Class.forName(value);
			} catch (ClassNotFoundException e) {
				// Warns about the exception
				throw new InvalidDataException("Supplied value represents a class not found: " + value);
			}
		}

		@Override
		public String convertToString(Class<T> aClass) {
			if (aClass != null) {
				return aClass.getName();
			}
			return null;
		}

	}

	/**
	 * Class defining how to convert String from/to Point
	 * 
	 * @author sguerin
	 */
	private static class PointConverter extends Converter<Point> {

		private PointConverter() {
			super(Point.class);
		}

		@Override
		public Point convertFromString(String value, PamelaModelFactory factory) {
			try {
				Point returned = new Point();
				StringTokenizer st = new StringTokenizer(value, ",");
				if (st.hasMoreTokens()) {
					returned.x = Integer.parseInt(st.nextToken());
				}
				if (st.hasMoreTokens()) {
					returned.y = Integer.parseInt(st.nextToken());
				}
				return returned;
			} catch (NumberFormatException e) {
				// Warns about the exception
				System.err.println("Supplied value is not parsable as a Point:" + value);
				return null;
			}
		}

		@Override
		public String convertToString(Point aPoint) {
			if (aPoint != null) {
				return aPoint.x + "," + aPoint.y;
			}
			return null;
		}

	}

	/**
	 * Class defining how to convert String from/to Color
	 * 
	 * @author sguerin
	 */
	private static class ColorConverter extends Converter<Color> {

		private ColorConverter() {
			super(Color.class);
		}

		@Override
		public Color convertFromString(String value, PamelaModelFactory factory) {
			return new Color(redFromString(value), greenFromString(value), blueFromString(value));
		}

		@Override
		public String convertToString(Color aColor) {
			return aColor.getRed() + "," + aColor.getGreen() + "," + aColor.getBlue();

		}

		private static int redFromString(String s) {
			return Integer.parseInt(s.substring(0, s.indexOf(",")));
		}

		private static int greenFromString(String s) {
			return Integer.parseInt(s.substring(s.indexOf(",") + 1, s.lastIndexOf(",")));
		}

		private static int blueFromString(String s) {
			return Integer.parseInt(s.substring(s.lastIndexOf(",") + 1));
		}

	}

	/**
	 * Class defining how to convert String from/to Font
	 * 
	 * @author sguerin
	 */
	private static class FontConverter extends Converter<Font> {

		private FontConverter() {
			super(Font.class);
		}

		@Override
		public Font convertFromString(String value, PamelaModelFactory factory) {
			return new Font(nameFromString(value), styleFromString(value), sizeFromString(value));
		}

		@Override
		public String convertToString(Font aFont) {
			return aFont.getName() + "," + aFont.getStyle() + "," + aFont.getSize();
		}

		private static String nameFromString(String s) {
			return s.substring(0, s.indexOf(","));
		}

		private static int styleFromString(String s) {
			return Integer.parseInt(s.substring(s.indexOf(",") + 1, s.lastIndexOf(",")));
		}

		private static int sizeFromString(String s) {
			return Integer.parseInt(s.substring(s.lastIndexOf(",") + 1));
		}

	}

}
