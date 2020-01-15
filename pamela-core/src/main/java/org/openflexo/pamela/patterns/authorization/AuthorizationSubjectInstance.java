package org.openflexo.pamela.patterns.authorization;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AuthorizationSubjectInstance {
    private AuthorizationSubjectEntity subjectEntity;
    private Object instance;
    private ArrayList<Object> ids;
    private boolean initializing;
    private boolean checking;

    public AuthorizationSubjectInstance(Object instance, AuthorizationSubjectEntity subjectEntity) {
        this.instance = instance;
        this.subjectEntity = subjectEntity;
        this.ids = new ArrayList<>();
        this.initializing = false;
        this.checking = false;
    }

    public void init() {
        try {
            this.initializing = true;
            for (Method m : this.subjectEntity.getIdGetters().values()){
                this.ids.add(m.invoke(this.instance));
            }
            this.initializing = false;
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    void checkBeforeInvoke(Method method) {
        if (!this.initializing && !this.checking){
            this.checking = true;

            this.checking = false;
        }
    }

    void checkAfterInvoke(Method method, Object returnValue) {
        if (!this.initializing && !this.checking){
            this.checking = true;

            this.checking = false;
        }

    }

    public ArrayList<Object> getIds() {
        return this.ids;
    }
}
