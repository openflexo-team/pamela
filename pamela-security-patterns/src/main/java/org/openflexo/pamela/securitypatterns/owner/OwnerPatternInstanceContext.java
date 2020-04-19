package org.openflexo.pamela.securitypatterns.owner;

import org.openflexo.pamela.ModelEntity;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.patterns.PatternInstance;
import org.openflexo.pamela.patterns.ReturnWrapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Stack;

public class OwnerPatternInstanceContext extends PatternInstance<OwnerPatternDefinition> {
    public final static String POTENTIAL_OWNER = "Potential Owner";

    protected Stack<Object> customStack;
    protected HashMap<Object, String> db;

    public OwnerPatternInstanceContext(OwnerPatternDefinition patternDefinition) {
        super(patternDefinition);
        this.customStack = new Stack<>();
        this.db = new HashMap<>();
    }

    protected void attachInstance(Object instance, ModelEntity<?> modelEntity){
        this.db.put(instance, instance.toString());
        this.registerStakeHolder(instance, POTENTIAL_OWNER);
    }

    @Override
    public ReturnWrapper processMethodBeforeInvoke(Object instance, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        this.customStack.push(instance);
        return new ReturnWrapper(true, null);
    }

    @Override
    public void processMethodAfterInvoke(Object instance, Method method, Object returnValue, Object[] args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (this.customStack.firstElement() == instance){
            this.customStack.pop();
        }
        else {
            throw new ModelExecutionException("PROBLEM WITH OWNER CONTEXT STACK");
        }
    }
}
