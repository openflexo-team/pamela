package org.openflexo.pamela.test.containment;

import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;

@ModelEntity
public interface TestContainer<T extends TestEmbedded> {

	public static final String EMBEDDED = "embedded";

	@Getter(value = EMBEDDED)
	@Embedded
	public T getEmbedded();

	@Setter(value = EMBEDDED)
	public void setEmbedded(T embedded);

}
