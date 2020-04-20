package org.openflexo.pamela.patterns;

import org.openflexo.pamela.ModelContext;

import java.lang.reflect.Method;

/**
 * Interface specifying an execution monitor. Such an entity is notified every time a method is handled by the {@link org.openflexo.pamela.factory.ProxyMethodHandler}.
 */
public abstract class ExecutionMonitor {
    protected ModelContext modelContext;

    public ExecutionMonitor(ModelContext context){
        this.modelContext = context;
        this.modelContext.addExecutionMonitor(this);
    }

    /**
     * Notification when entering the invoke method of the {@link org.openflexo.pamela.factory.ProxyMethodHandler}.
     * @param instance instance on which the method is called.
     * @param method called method.
     * @param args arguments passed to the called method.
     */
    public abstract void enteringMethod(Object instance, Method method, Object[] args);

    /**
     * Notification when throwing an exception while executing patterns on called method.
     * @param instance instance on which the method is called.
     * @param method called method.
     * @param args arguments passed to the called method.
     * @param exception exception thrown by the pattern handling.
     */
    public abstract void throwingException(Object instance, Method method, Object[] args, Exception exception);

    /**
     * Notification when leaving the invoke method of the {@link org.openflexo.pamela.factory.ProxyMethodHandler}.
     * @param instance instance on which the method is called.
     * @param method called method.
     * @param args arguments passed to the called method.
     * @param returnValue value about to be returned by the method.
     */
    public abstract void leavingMethod(Object instance, Method method, Object[] args, Object returnValue);
}
