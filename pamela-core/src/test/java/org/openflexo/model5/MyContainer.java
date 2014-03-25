package org.openflexo.model5;

import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Setter;

@ModelEntity
public interface MyContainer<T extends MyContents> {

	public static final String CONTENTS = "contents";

	@Getter(value = CONTENTS)
	@Embedded
	public T getContents();

	@Setter(value = CONTENTS)
	public void setContents(T someContents);

}
