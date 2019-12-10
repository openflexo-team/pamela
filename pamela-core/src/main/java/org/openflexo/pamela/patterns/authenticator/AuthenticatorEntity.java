package org.openflexo.pamela.patterns.authenticator;

import org.openflexo.pamela.patterns.authenticator.annotations.RequestAuthentication;

import java.lang.reflect.Method;
import java.util.HashMap;

public class AuthenticatorEntity {
    private AuthenticatorPattern pattern;
    private Class authenticatorClass;
    private Method requestMethod;
    private HashMap<Object, AuthenticatorInstance> instances;

    public AuthenticatorEntity(AuthenticatorPattern authenticatorPatternProperty, Class authenticatorClass) {
        this.pattern = authenticatorPatternProperty;
        this.authenticatorClass = authenticatorClass;
        this.instances = new HashMap<>();
        this.analyzeClass();
    }

    private void analyzeClass(){
        for (Method m : this.authenticatorClass.getMethods()){
            RequestAuthentication requestAnnotation = m.getAnnotation(RequestAuthentication.class);
            if (requestAnnotation != null && requestAnnotation.patternID().compareTo(this.pattern.getID()) == 0){
                this.processRequestAnnotation(m, requestAnnotation);
            }
        }
    }

    private void processRequestAnnotation(Method method, RequestAuthentication annotation){
        this.requestMethod = method;
    }

    public String getClassName() {
        return this.authenticatorClass.getName();
    }

    public Class getBaseClass(){
        return this.authenticatorClass;
    }

    public boolean isComplete() {
        return this.requestMethod != null;
    }

    public Method getMethod() {
        return this.requestMethod;
    }

    public void discoverInstance(Object instance) {
        this.instances.put(instance, new AuthenticatorInstance(instance, this));
        this.instances.get(instance).init();
    }

    public HashMap<Object, AuthenticatorInstance> getInstances() {
        return this.instances;
    }
}
