package org.openflexo.pamela.patterns.authenticator;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class SubjectInstance {
    private Object instance;
    private SubjectEntity entity;
    private Object authenticatorInstance;
    private ArrayList<Object> authInfos;

    public SubjectInstance(Object instance, SubjectEntity entity){
        this.instance = instance;
        this.entity = entity;
        this.authInfos = new ArrayList<>();
    }

    public void init(){
        try {
            for (Method m : this.entity.getAuthInfoGetters().values()){
                Object info = m.invoke(instance , new Object[] {});
                this.authInfos.add(info);
            }
            //this.authenticatorInstance = entity.getAuthenticatorGetter().invoke(instance, null);
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }
}
