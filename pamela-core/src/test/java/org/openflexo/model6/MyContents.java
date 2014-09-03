package org.openflexo.model6;

import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;

@ModelEntity
@ImplementationClass(MyContentsImpl.class)
public interface MyContents {

	@PropertyIdentifier(type = String.class)
	public static final String VALUE_KEY = "valeur";

	@Getter(VALUE_KEY)
	public String getValue();

	@Setter(VALUE_KEY)
	public void setValue(String val);

}
