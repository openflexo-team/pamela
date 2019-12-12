package org.openflexo.pamela.patterns;

import org.openflexo.pamela.exceptions.ModelDefinitionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This abstract class represents a pattern. Its instances are to be wrapped in the patternContext.<br>
 * It has the responsibility of:
 * <ul><li>Handling method call before and after invoke to perform execution or check relevant to the pattern.</li>
 * <li>Relaying the instance discovery to the relevant component of the pattern.</li></ul>
 *
 *  @author C. SILVA
 */
public abstract class AbstractPattern {

    /**
     * Method called before every method invoke.
     * @param self Object on which the method is called
     * @param method method to be invoked
     * @param klass Class of the class tree of <code>self</code> involved in the pattern
     * @return true if the execution of the method should be continued after the call of this method
     * @throws InvocationTargetException in case of error during internal calls
     * @throws IllegalAccessException in case of error during internal calls
     * @throws NoSuchMethodException in case of error during internal calls
     */
    public abstract boolean processMethodBeforeInvoke(Object self, Method method, Class klass) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException;

    /**
     * Method called when a new pattern-related instance is discovered by the {@link PatternContext}
     * @param instance instance to discover
     * @param klass Class of the class tree of <code>self</code> involved in the pattern
     */
    public abstract void discoverInstance(Object instance, Class klass);

    /**
     * Method called after every method invoke
     * @param self Object on which the method is called
     * @param method Just-invoked method
     * @param klass Class of the class tree of <code>self</code> involved in the pattern
     * @param returnValue return Value of the just-invoked method
     */
    public abstract void processMethodAfterInvoke(Object self, Method method, Class klass, Object returnValue);

    /**
     * Identifies the entity type to instantiate with the given class, and if relevant, instantiate the associated entity.
     * @param baseClass Class to analyze
     * @throws ModelDefinitionException In case of error during the class analysis.
     */
    public abstract void attachClass(Class baseClass) throws ModelDefinitionException;
}
