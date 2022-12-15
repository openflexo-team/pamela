package org.openflexo.pamela.securitypatterns.singleAccessPoint;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.factory.PamelaModel;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.patterns.ExecutionMonitor;
import org.openflexo.pamela.patterns.PatternInstance;
import org.openflexo.pamela.patterns.ReturnWrapper;
import org.openflexo.pamela.securitypatterns.executionMonitors.CustomStack;

public class SingleAccessPointPatternInstance<S> extends PatternInstance<SingleAccessPointPatternDefinition> {
	public static String PROTECTED_SYSTEM = "SAP Protected System";

	private S systemInstance;
	private boolean checking;

	protected CustomStack customStack;

	public SingleAccessPointPatternInstance(SingleAccessPointPatternDefinition patternDefinition, PamelaModel model, S systemInstance) {
		super(patternDefinition, model);
		this.systemInstance = systemInstance;
		this.registerStakeHolder(systemInstance, PROTECTED_SYSTEM);
		this.checking = false;
		for (ExecutionMonitor monitor : getModel().getExecutionMonitors()) {
			if (monitor instanceof CustomStack) {
				customStack = (CustomStack) monitor;
			}
		}
		if (customStack == null) {
			customStack = new CustomStack(getModel());
		}
	}

	private CustomStack getCustomStack() {
		return customStack;
	}

	@Override
	public ReturnWrapper processMethodBeforeInvoke(Object instance, Method method, Object[] args)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		if (!this.checking) {
			this.checking = true;
			CustomStack.Frame callingFrame = getCustomStack().getFrame(1);
			if (callingFrame == null) {
				this.checking = false;
				throw new ModelExecutionException("Calling method from protected system from non accessor entity");
			}
			Object callingInstance = callingFrame.getInstance();
			if (callingInstance == instance) { // nested call
				this.checking = false;
				return new ReturnWrapper(true, null);
			}
			SingleAccessPointPatternDefinition.AccessorWrapper callerWrapper = null;
			for (ModelEntity<?> modelEntity : this.getMetaModel().getUpperEntities(callingInstance)) {
				callerWrapper = this.getPatternDefinition().getAccessorEntities().get(modelEntity);
			}
			if (callerWrapper == null) {
				this.checking = false;
				throw new ModelExecutionException("Calling method from protected system from non accessor entity");
			}
			Object[] checkParams = new Object[this.getPatternDefinition().getCheckpoint().getParameterCount()];
			for (String paramID : this.getPatternDefinition().getCheckpointParams().keySet()) {
				int i = this.getPatternDefinition().getCheckpointParams().get(paramID);
				checkParams[i] = callerWrapper.getGettersMap().get(paramID).invoke(callingInstance);
			}
			boolean authorized = (boolean) (this.getPatternDefinition().getCheckpoint().invoke(this.systemInstance, checkParams));
			if (authorized) {
				this.checking = false;
				return new ReturnWrapper(true, null);
			}
			this.checking = false;
			throw new ModelExecutionException("Unauthorized access to protected system");
		}
		return new ReturnWrapper(true, null);
	}

	@Override
	public void processMethodAfterInvoke(Object instance, Method method, Object returnValue, Object[] args)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {

	}
}
