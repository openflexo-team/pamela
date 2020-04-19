package org.openflexo.pamela.securitypatterns.singleAccessPoint;

import org.openflexo.pamela.ModelContextLibrary;
import org.openflexo.pamela.PamelaUtils;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.patterns.PatternInstance;
import org.openflexo.pamela.patterns.ReturnWrapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SingleAccessPointPatternInstance<S> extends PatternInstance<SingleAccessPointPatternDefinition> {
    public static String PROTECTED_SYSTEM = "SAP Protected System";

    private S systemInstance;
    private boolean checking;
    private Method checkedMethod;
    private Object checkedInstance;
    private Object[] checkedArgs;

    public SingleAccessPointPatternInstance(SingleAccessPointPatternDefinition patternDefinition, S systemInstance) {
        super(patternDefinition);
        this.systemInstance = systemInstance;
        this.registerStakeHolder(systemInstance, PROTECTED_SYSTEM);
        this.checking = false;
    }

    @Override
    public ReturnWrapper processMethodBeforeInvoke(Object instance, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (!this.checking && !this.getPatternDefinition().getInstanceContext().accessorStack.isEmpty()){
            System.out.println(method);
            this.checking = true;
            this.checkedMethod = method;
            this.checkedInstance = instance;
            this.checkedArgs = args;
            Object callingInstance = this.getPatternDefinition().getInstanceContext().accessorStack.firstElement();
            SingleAccessPointPatternDefinition.AccessorWrapper callerWrapper = this.getPatternDefinition().getAccessorEntities().get(this.getPatternDefinition().getInstanceContext().accessorDB.get(callingInstance));
            Object[] checkParams = new Object[this.getPatternDefinition().getCheckpoint().getParameterCount()];
            for (String paramID : this.getPatternDefinition().getCheckpointParams().keySet()){
                int i = this.getPatternDefinition().getCheckpointParams().get(paramID);
                checkParams[i] = callerWrapper.getGettersMap().get(paramID).invoke(callingInstance);
            }
            boolean authorized =  (boolean)(this.getPatternDefinition().getCheckpoint().invoke(this.systemInstance, checkParams));
            if (authorized){
                return new ReturnWrapper(true, null);
            }
            else {
                this.checking = false;
                this.getPatternDefinition().getInstanceContext().accessorStack.pop();
                throw new ModelExecutionException("Unauthorized access to protected system.");
            }
        }


        return new ReturnWrapper(true,null);
    }

    @Override
    public void processMethodAfterInvoke(Object instance, Method method, Object returnValue, Object[] args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (this.checking && PamelaUtils.methodIsEquivalentTo(method, this.checkedMethod) && this.checkedInstance == instance && this.checkedArgs == args){
            this.checking = false;
        }
    }
}
