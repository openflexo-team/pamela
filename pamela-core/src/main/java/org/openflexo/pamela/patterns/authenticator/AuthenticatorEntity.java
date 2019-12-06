package org.openflexo.pamela.patterns.authenticator;

import org.openflexo.pamela.patterns.authenticator.annotations.RequestAuthentication;

import java.lang.reflect.Method;

public class AuthenticatorEntity {
    private AuthenticatorPattern pattern;
    private Class authenticatorClass;
    private Method requestMethod;

    public AuthenticatorEntity(AuthenticatorPattern authenticatorPatternProperty, Class authenticatorClass) {
        this.pattern = authenticatorPatternProperty;
        this.authenticatorClass = authenticatorClass;
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
}
