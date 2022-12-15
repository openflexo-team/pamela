package org.openflexo.pamela.securitypatterns.owner;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.factory.PamelaModel;
import org.openflexo.pamela.patterns.ExecutionMonitor;
import org.openflexo.pamela.patterns.PatternInstance;
import org.openflexo.pamela.patterns.ReturnWrapper;
import org.openflexo.pamela.securitypatterns.executionMonitors.CustomStack;
import org.openflexo.toolbox.HasPropertyChangeSupport;

public class OwnerPatternInstance<OO> extends PatternInstance<OwnerPatternDefinition> implements PropertyChangeListener {
	public final static String OWNED_OBJECT = "Owned Object";

	protected OO ownedObject;
	protected Object currentOwner;

	private boolean checking = false;
	private final boolean isPAMELA;

	protected CustomStack customStack;

	public OwnerPatternInstance(OwnerPatternDefinition patternDefinition, PamelaModel model, OO instance) {
		super(patternDefinition, model);
		this.ownedObject = instance;
		this.registerStakeHolder(instance, OWNED_OBJECT);
		this.checking = true;
		this.retrieveCurrentOwmer();
		this.checking = false;
		if (instance instanceof HasPropertyChangeSupport && this.getPatternDefinition().ownerProperty != null) {
			this.isPAMELA = true;
			((HasPropertyChangeSupport) instance).getPropertyChangeSupport().addPropertyChangeListener(this);
		}
		else {
			this.isPAMELA = false;
		}
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
			if (!this.isPAMELA) {
				this.retrieveCurrentOwmer();
			}
			CustomStack.Frame callingFrame = getCustomStack().getFrame(1);
			if (this.currentOwner == null || // no current owner
					this.getPatternDefinition().pureMethods.contains(method) || // pure method
					(callingFrame != null && callingFrame.getInstance() == instance) || // nested call
					(callingFrame != null && callingFrame.getInstance() == this.currentOwner)) { // valid Owner
				this.checking = false;
				return new ReturnWrapper(true, null);
			}
			this.checking = false;
			throw new ModelExecutionException("Method " + method + " called by non owner object");
		}
		return new ReturnWrapper(true, null);
	}

	private void retrieveCurrentOwmer() {
		try {
			this.currentOwner = this.getPatternDefinition().ownerGetter.invoke(this.ownedObject);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void processMethodAfterInvoke(Object instance, Method method, Object returnValue, Object[] args)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().compareTo(getPatternDefinition().ownerProperty.getPropertyIdentifier()) == 0) {
			this.currentOwner = evt.getNewValue();
		}
	}
}
