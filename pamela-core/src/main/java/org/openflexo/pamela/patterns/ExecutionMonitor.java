package org.openflexo.pamela.patterns;

import java.lang.reflect.Method;


public interface ExecutionMonitor {

    void enteringMethod(Object instance, Method method, Object[] args);

    void throwingException(Object instance, Method method, Object[] args, Exception exception);

    void leavingMethod(Object instance, Method method, Object[] args, Object returnValue);
}
