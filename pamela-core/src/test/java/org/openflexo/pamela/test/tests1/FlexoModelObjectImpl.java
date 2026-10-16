package org.openflexo.pamela.test.tests1;


import org.openflexo.pamela.factory.PamelaProxies;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.model.ModelEntity;

public abstract class FlexoModelObjectImpl implements TestModelObject {

	@Override
	public String deriveName() {
		return getName() + "1";
	}

	@Override
	public String toString() {
		ProxyMethodHandler<?> handler = PamelaProxies.handlerOf(this);
		if (handler != null) {
			ModelEntity factory = handler.getModelEntity();
			return factory.getImplementedInterface().getSimpleName() + "(id=" + getFlexoID() + "," + getName() + ")";
		}
		return super.toString();
	}

}
