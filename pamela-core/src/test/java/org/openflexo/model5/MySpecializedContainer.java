package org.openflexo.model5;

import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ModelEntity;

@ModelEntity
public interface MySpecializedContainer extends MyContainer<MyContents> {

	@Override
	@Getter(value = CONTENTS, ignoreType = true)
	@Embedded
	public MySpecializedContents getContents();

	@Override
	public void setContents(MyContents someContents);

}
