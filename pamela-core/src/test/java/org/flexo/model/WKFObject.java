package org.flexo.model;

import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Modify;
import org.openflexo.pamela.annotations.Setter;

@ModelEntity(isAbstract = true)
@Modify(forward = WKFObject.PROCESS, synchWithForward = true)
public interface WKFObject extends TestModelObject {

	public static final String PROCESS = "process";

	@Getter(PROCESS)
	@CloningStrategy(StrategyType.IGNORE)
	public FlexoProcess getProcess();

	@Setter(PROCESS)
	public void setProcess(FlexoProcess aProcess);

	@Override
	@Setter(TestModelObject.FLEXO_ID)
	public void setFlexoID(String flexoID);
}
