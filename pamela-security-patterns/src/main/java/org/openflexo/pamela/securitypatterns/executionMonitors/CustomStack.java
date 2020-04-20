package org.openflexo.pamela.securitypatterns.executionMonitors;

import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.PamelaUtils;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.patterns.ExecutionMonitor;

import java.lang.reflect.Method;
import java.util.Stack;

/**
 * Execution monitor storing a call stack for all method handled by the ProxyMethodHandler.
 * This monitor is used when there is a need for a pattern to know previous calls (check a call
 * sequence, check a calling instance or method, etc.)
 */
public class CustomStack extends ExecutionMonitor {

    public class Frame {
        Object instance;
        Method method;
        Object[] args;

        private Frame(Object instance, Method method, Object[] args){
            this.instance = instance;
            this.method = method;
            this.args = args;
        }

        public Object getInstance() {
            return instance;
        }

        public Method getMethod() {
            return method;
        }

        public Object[] getArgs() {
            return args;
        }
    }
    private Stack<Frame> customStack;

    public CustomStack(ModelContext context){
        super(context);
        this.customStack = new Stack<>();
    }

    @Override
    public void enteringMethod(Object instance, Method method, Object[] args){
        if (this.isMonitored(instance)){
            this.customStack.push(new Frame(instance,method,args));
        }
    }

    public Frame getFrame(int depth) {
        if (depth < this.customStack.size()){
            return this.customStack.elementAt(this.customStack.size() - depth - 1);
        }
        return null;
    }

    @Override
    public void throwingException(Object instance, Method method, Object[] args, Exception exception){
        if (this.isMonitored(instance)){
            Frame last = this.customStack.pop();
            if (last.instance != instance || !PamelaUtils.methodIsEquivalentTo(method,last.method) || args != last.args){
                throw new ModelExecutionException("CustomStack error: popping unregistered element with exception");
            }
        }
    }

    @Override
    public void leavingMethod(Object instance, Method method, Object[] args, Object returnValue){
        if (this.isMonitored(instance)) {
            Frame last = this.customStack.pop();
            if (last.instance != instance || !PamelaUtils.methodIsEquivalentTo(method, last.method) || args != last.args) {
                throw new ModelExecutionException("CustomStack error: popping unregistered element");
            }
        }
    }

    private boolean isMonitored(Object instance) {
        return !this.modelContext.getUpperEntities(instance).isEmpty();
    }
}
