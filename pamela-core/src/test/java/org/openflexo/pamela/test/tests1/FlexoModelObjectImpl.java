package org.openflexo.pamela.test.tests1;

import javassist.util.proxy.ProxyObject;

import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.model.ModelEntity;

public abstract class FlexoModelObjectImpl implements TestModelObject {

	@Override
	public String deriveName() {
		return getName() + "1";
	}

	@Override
	public String toString() {
		if (this instanceof ProxyObject) {
			ProxyMethodHandler handler = (ProxyMethodHandler) ((ProxyObject) this).getHandler();
			ModelEntity factory = handler.getModelEntity();
			return factory.getImplementedInterface().getSimpleName() + "(id=" + getFlexoID() + "," + getName() + ")";
		}
		return super.toString();
	}

}
