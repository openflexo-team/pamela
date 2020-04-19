package org.openflexo.pamela.securitypatterns.singleAccessPoint;

import org.openflexo.pamela.ModelEntity;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.patterns.PatternInstance;
import org.openflexo.pamela.patterns.ReturnWrapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Stack;

@Deprecated
public class SingleAccessPointPatternInstanceContext extends PatternInstance<SingleAccessPointPatternDefinition> {
    public final static String ACCESSOR = "SAP Accessor";

    protected Stack<Object> accessorStack;
    protected HashMap<Object, ModelEntity<?>> accessorDB;

    public SingleAccessPointPatternInstanceContext(SingleAccessPointPatternDefinition patternDefinition) {
        super(patternDefinition);
        this.accessorStack = new Stack<>();
        this.accessorDB = new HashMap<>();
    }

    @Override
    public ReturnWrapper processMethodBeforeInvoke(Object instance, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        this.accessorStack.push(instance);
        return new ReturnWrapper(true, null);
    }

    @Override
    public void processMethodAfterInvoke(Object instance, Method method, Object returnValue, Object[] args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (this.accessorStack.firstElement() == instance){
            this.accessorStack.pop();
        }
        else {
            throw new ModelExecutionException("PROBLEM WITH SAP CONTEXT STACK");
        }
    }

    public void attachInstance(Object newInstance, ModelEntity<?> modelEntity) {
        this.registerStakeHolder(newInstance ,ACCESSOR);
        this.accessorDB.put(newInstance, modelEntity);
    }
}
