package org.flexo.model.impl;

import javassist.util.proxy.ProxyObject;

import org.flexo.model.TestModelObject;
import org.openflexo.pamela.ModelEntity;
import org.openflexo.pamela.factory.ProxyMethodHandler;

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
