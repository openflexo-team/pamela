package org.openflexo.model6;

import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

@ModelEntity
@ImplementationClass(MyContainerImpl.class)
@XMLElement
public interface MyContainer {

	@PropertyIdentifier(type = String.class)
	public static final String CONTENTS = "contents";

	@PropertyIdentifier(type = String.class)
	public static final String CONTENT_URI = "content_uri";

	@Getter(value = CONTENTS)
	public String getContents();

	@Setter(value = CONTENTS)
	public void setContents(String someContents);

	@Getter(value = CONTENT_URI)
	@XMLAttribute
	public String getContentURI();

	@Setter(value = CONTENT_URI)
	public void setContentURI(String uri);

}
