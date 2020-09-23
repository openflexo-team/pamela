package org.openflexo.pamela.test.containment;

import org.openflexo.pamela.annotations.ModelEntity;

@ModelEntity
public interface EmbeddingB {

	public TestEmbeddedB getEmbedded();

}
