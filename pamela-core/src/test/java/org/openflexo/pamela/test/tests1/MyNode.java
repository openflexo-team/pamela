package org.openflexo.pamela.test.tests1;

import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.StringConverter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.converter.DurationConverter;
import org.openflexo.pamela.converter.FileFormatConverter;
import org.openflexo.pamela.converter.LevelConverter;
import org.openflexo.pamela.model.StringConverterLibrary.Converter;
import org.openflexo.toolbox.Duration;
import org.openflexo.toolbox.FileFormat;

import java.util.logging.Level;

@ModelEntity
@XMLElement
public interface MyNode extends AbstractNode {

	String MY_PROPERTY = "myProperty";

    String MY_DURATION = "myDuration";

    String MY_FILEFORMAT = "myFileFormat";

    String MY_LEVEL = "myLevel";

    String MY_TYPE = "myType";

    @StringConverter
    Converter<Duration> DURATION_CONVERTER = new DurationConverter();

    @StringConverter
    Converter<FileFormat> FILE_FORMAT_CONVERTER = new FileFormatConverter();

    @StringConverter
    Converter<Level> LEVEL_CONVERTER_CONVERTER = new LevelConverter();

	@Getter(MY_PROPERTY)
	@XMLAttribute
    String getMyProperty();

	@Setter(MY_PROPERTY)
    void setMyProperty(String property);

	@Getter(MY_DURATION)
	@XMLAttribute
    Duration getMyDuration();

	@Setter(MY_DURATION)
    void setMyDuration(Duration duration);

	@Getter(MY_FILEFORMAT)
	@XMLAttribute
    FileFormat getMyFileFormat();

	@Setter(MY_FILEFORMAT)
    void setMyFileformat(FileFormat fileformat);

	@Getter(MY_LEVEL)
	@XMLAttribute
    Level getMyLevel();

	@Setter(MY_LEVEL)
    void setMyLevel(Level level);

}
