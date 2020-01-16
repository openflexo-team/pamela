package org.flexo.model;

import org.flexo.model.impl.TokenEdgeImpl;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.XMLElement;

@ModelEntity
@XMLElement(xmlTag = "TokenEdge")
@ImplementationClass(TokenEdgeImpl.class)
public interface TokenEdge extends Edge {

	@Override
	@Getter(PROCESS)
	public FlexoProcess getProcess();

}
