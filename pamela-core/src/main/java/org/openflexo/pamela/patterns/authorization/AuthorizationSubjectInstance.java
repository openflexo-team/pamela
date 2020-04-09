package org.openflexo.pamela.patterns.authorization;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.openflexo.pamela.exceptions.ModelExecutionException;

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

    void checkBeforeInvoke(Method method, Object[] args) {
        if (!this.initializing && !this.checking){
            this.checking = true;
            this.checkInvariant();
            this.checkPrecondition(method,args);
            this.checking = false;
        }
    }

    private void checkPrecondition(Method method, Object[] args) {
    }

    private void checkInvariant() {
        this.checkIdIsFinal();
    }

    private void checkIdIsFinal() {
        int i = 0;
        try {
            for (Method m : this.subjectEntity.getIdGetters().values()) {
                Object currentID = m.invoke(this.instance);
                if (this.ids.get(i) != null && !this.ids.get(i).equals(currentID)){
                    throw new ModelExecutionException("Subject Invariant breach: Id has changed since initialization");
                }
                i++;
            }
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    void checkAfterInvoke(Method method, Object[] args, Object returnValue) {
        if (!this.initializing && !this.checking){
            this.checking = true;
            this.checkInvariant();
            this.checkPostCondition(method,args,returnValue);
            this.checking = false;
        }

    }

    private void checkPostCondition(Method method, Object[] args, Object returnValue) {
    }

    public ArrayList<Object> getIds() {
        return this.ids;
    }
}
