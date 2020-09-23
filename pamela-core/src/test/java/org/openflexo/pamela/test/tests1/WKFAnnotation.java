package org.openflexo.pamela.test.tests1;

import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Parameter;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.StringConverter;
import org.openflexo.pamela.exceptions.InvalidDataException;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.model.StringConverterLibrary.Converter;

@ModelEntity
public interface WKFAnnotation extends TestModelObject {

	public static final String TEXT = "text";

	@Override
	@Initializer
	public WKFAnnotation init();

	@Override
	@Initializer
	public WKFAnnotation init(@Parameter(TEXT) String text);

	@Getter(TEXT)
	public String getText();

	@Setter(TEXT)
	public void setText(String s);

	@StringConverter
	public static final Converter<WKFAnnotation> CONVERTER = new WKFAnnotationConverter();

	public static class WKFAnnotationConverter extends Converter<WKFAnnotation> {

		public WKFAnnotationConverter() {
			super(WKFAnnotation.class);
		}

		@Override
		public WKFAnnotation convertFromString(String value, ModelFactory factory) throws InvalidDataException {
			return factory.newInstance(WKFAnnotation.class, value);
		}

		@Override
		public String convertToString(WKFAnnotation value) {
			return value.getText();
		}

	}

}
