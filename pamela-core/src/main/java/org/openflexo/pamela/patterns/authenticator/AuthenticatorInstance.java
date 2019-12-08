package org.openflexo.pamela.patterns.authenticator;

public class AuthenticatorInstance {
    private Object instance;
    private AuthenticatorEntity entity;

    public AuthenticatorInstance(Object instance, AuthenticatorEntity entity){
        this.entity = entity;
        this.instance = instance;
    }

    public void init(){

    }
}
