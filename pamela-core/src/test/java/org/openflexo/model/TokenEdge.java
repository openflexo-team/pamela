package org.openflexo.model;

import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.impl.TokenEdgeImpl;

@ModelEntity
@XMLElement(xmlTag = "TokenEdge")
@ImplementationClass(TokenEdgeImpl.class)
public interface TokenEdge extends Edge {

	@Override
	@Getter(PROCESS)
	public FlexoProcess getProcess();

}
