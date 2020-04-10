package org.openflexo.pamela.securitypatterns.authenticator;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.openflexo.pamela.securitypatterns.authenticator.annotations.Authenticator;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.RequestAuthentication;

/**
 * This class wraps all the static authenticator pattern related information extracted from parsing a
 * {@link Authenticator} annotated class. <br>
 * It has the responsibility of:
 * <ul><li>Parsing the class.</li>
 * <li>Saving the relevant pattern related information.</li>
 * <li>Discovering and saving at runtime the known instance of the associated class.</li></ul>
 *
 *  @author C. SILVA
 */

public class AuthenticatorEntity {
    private final AuthenticatorPattern pattern;
    private final Class authenticatorClass;
    private Method requestMethod;
    private final HashMap<Object, AuthenticatorInstance> instances;

    /**
     * Constructor of the class.<br>
     * The constructor performs the class analysis.
     * @param authenticatorPattern Reference of the associated {@link AuthenticatorPattern}
     * @param authenticatorClass {@link Authenticator} annotated class to analyze
     */
    AuthenticatorEntity(AuthenticatorPattern authenticatorPattern, Class authenticatorClass) {
        this.pattern = authenticatorPattern;
        this.authenticatorClass = authenticatorClass;
        this.instances = new HashMap<>();
        this.analyzeClass();
    }

    /**
     * Creates an {@link AuthenticatorInstance} with the given object and adds it to its known instances.
     * @param instance Object to discover. This object should be instance of the <code>baseClass</code>
     */
    void discoverInstance(Object instance) {
        this.instances.put(instance, new AuthenticatorInstance(instance, this));
        this.instances.get(instance).init();
    }

    /**
     * @return the {@link RequestAuthentication} annotated method object.
     */
    public Method getRequestMethod() {
        return this.requestMethod;
    }

    /**
     * @return the {@link Authenticator} annotated class used to instantiate this class
     */
    public Class getBaseClass(){
        return this.authenticatorClass;
    }

    /**
     * @return a {@link HashMap} containing known authenticator instance references as keys and the associated {@link AuthenticatorInstance}
     */
    HashMap<Object, AuthenticatorInstance> getInstances() {
        return this.instances;
    }

    /**
     * @return true when the <code>baseClass</code> has been parsed successfully, false otherwise
     */
    boolean isComplete() {
        return this.requestMethod != null;
    }

    /**
     * Performs the class analysis.
     */
    private void analyzeClass(){
        for (Method m : this.authenticatorClass.getMethods()){
            RequestAuthentication requestAnnotation = m.getAnnotation(RequestAuthentication.class);
            if (requestAnnotation != null && requestAnnotation.patternID().compareTo(this.pattern.getID()) == 0){
                this.processRequestAnnotation(m, requestAnnotation);
            }
        }
    }

    /**
     * Saves the {@link RequestAuthentication} annotated method.
     * @param method {@link RequestAuthentication} annotated method
     * @param annotation {@link RequestAuthentication} annotation
     */
    private void processRequestAnnotation(Method method, RequestAuthentication annotation){
        this.requestMethod = method;
    }
}
