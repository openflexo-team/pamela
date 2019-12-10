package org.openflexo.pamela.patterns.authenticator;

import java.lang.reflect.Method;

public class AuthenticatorInstance {
    private Object instance;
    private AuthenticatorEntity entity;
    private boolean initializing;

    public AuthenticatorInstance(Object instance, AuthenticatorEntity entity){
        this.entity = entity;
        this.instance = instance;
        this.initializing = false;
    }

    public void init(){
        this.initializing = true;


        this.initializing = false;
    }

    public void checkBeforeInvoke(Method method) {

    }

    public void checkAfterInvoke(Method method, Object returnValue) {

    }
}
