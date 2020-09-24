package org.openflexo.pamela.test.containment;

import org.openflexo.pamela.annotations.ModelEntity;

@ModelEntity
public interface TestContainerA extends TestContainer<TestEmbeddedA> {

	/*@Override
	@Getter(value = EMBEDDED)
	@Embedded
	public TestEmbeddedA getEmbedded();

	@Override
	@Setter(value = EMBEDDED)
	public void setEmbedded(TestEmbeddedA embedded);*/

}
