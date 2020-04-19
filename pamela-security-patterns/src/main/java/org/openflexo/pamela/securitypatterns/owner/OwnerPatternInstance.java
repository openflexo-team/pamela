package org.openflexo.pamela.securitypatterns.owner;

import org.openflexo.pamela.PamelaUtils;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.patterns.PatternInstance;
import org.openflexo.pamela.patterns.ReturnWrapper;
import org.openflexo.toolbox.HasPropertyChangeSupport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OwnerPatternInstance<OO> extends PatternInstance<OwnerPatternDefinition> implements PropertyChangeListener {
    public final static String OWNED_OBJECT = "Owned Object";

    protected OO ownedObject;
    protected Object currentOwner;

    private boolean checking = false;
    private Method checkedMethod;
    private Object[] checkedArgs;
    private final boolean isPAMELA;

    public OwnerPatternInstance(OwnerPatternDefinition patternDefinition, OO instance) {
        super(patternDefinition);
        this.ownedObject = instance;
        this.registerStakeHolder(instance, OWNED_OBJECT);
        this.checking = true;
        this.retrieveCurrentOwmer();
        this.checking = false;
        if (instance instanceof HasPropertyChangeSupport && this.getPatternDefinition().ownerProperty != null){
            this.isPAMELA = true;
            ((HasPropertyChangeSupport)instance).getPropertyChangeSupport().addPropertyChangeListener(this);
        }
        else {
            this.isPAMELA = false;
        }
    }

    @Override
    public ReturnWrapper processMethodBeforeInvoke(Object instance, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (!checking && (!this.getPatternDefinition().pureMethods.contains(method) && !PamelaUtils.methodIsEquivalentTo(method, this.getPatternDefinition().ownerGetter))){
            this.checking = true;
            this.checkedMethod = method;
            this.checkedArgs = args;
            if (!this.isPAMELA){
                this.retrieveCurrentOwmer();
            }
            if (this.currentOwner == null || (!this.getPatternDefinition().instanceContext.customStack.isEmpty() && this.getPatternDefinition().instanceContext.customStack.firstElement() == this.currentOwner)){
                return new ReturnWrapper(true, null);
            }
            else {
                this.checking = false;
                if (!this.getPatternDefinition().instanceContext.customStack.isEmpty())this.getPatternDefinition().instanceContext.customStack.pop();
                throw new ModelExecutionException("Attempt to call a non pure method by non owner object.");
            }

        }
        return new ReturnWrapper(true, null);
    }

    private void retrieveCurrentOwmer() {
        try {
            this.currentOwner = this.getPatternDefinition().ownerGetter.invoke(this.ownedObject);
        }
        catch (IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
    }

    @Override
    public void processMethodAfterInvoke(Object instance, Method method, Object returnValue, Object[] args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (this.checking && PamelaUtils.methodIsEquivalentTo(method, this.checkedMethod) && this.checkedArgs == args){
            this.checking = false;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().compareTo(getPatternDefinition().ownerProperty.getPropertyIdentifier()) == 0){
            this.currentOwner = evt.getNewValue();
        }
    }
}
