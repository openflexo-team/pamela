package org.openflexo.model6;

import org.openflexo.model.factory.ModelFactory;

public abstract class MyContentsImpl implements MyContents {

	private String something = null;

	public static MyContents fromString(ModelFactory MF, String someString) {

		MyContents inst = MF.newInstance(MyContents.class);
		inst.setValue(someString);
		return inst;
	}

	@Override
	public void setValue(String some) {
		something = some;
	}

	@Override
	public String getValue() {
		return something;
	}

}
