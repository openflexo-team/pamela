package org.openflexo.pamela.patterns;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class AbstractPattern {
    public abstract boolean processMethodBeforeInvoke(Object self, Method method, Class klass) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException;
}
