package org.openflexo.pamela.patterns.authorization;

import org.openflexo.pamela.exceptions.ModelExecutionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class AuthorizationResourceInstance {
    private Object instance;
    private AuthorizationResourceEntity entity;
    private boolean initializing;
    private boolean checking;
    private HashMap<String, Object> ids;
    private Object permissionChecker;

    public AuthorizationResourceInstance(Object instance, AuthorizationResourceEntity authorizationResourceEntity) {
        this.instance = instance;
        this.entity = authorizationResourceEntity;
        this.ids = new HashMap<>();
        this.initializing = false;
        this.checking = false;
    }

    void init() {
        try {
            this.initializing = true;
            for (String paramId : this.entity.getIdGetters().keySet()){
                this.ids.put(paramId,this.entity.getIdGetters().get(paramId).invoke(this.instance));
            }
            this.permissionChecker = this.entity.getCheckerGetter().invoke(this.instance);
            if (this.permissionChecker == null){
                throw new ModelExecutionException(String.format("Permission checker is not initialized for resource %s", this.instance.toString()));
            }
            this.initializing = false;
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    void checkBeforeInvoke(Method method, Object[] args) {
        if (!this.initializing && !this.checking){
            this.checking = true;
            this.checkInvariant();
            this.checkPrecondition(method, args);
            this.checking = false;
        }
    }

    private void checkPrecondition(Method method, Object[] args) {

    }

    private void checkInvariant() {
        this.checkCheckerIsFinal();
        this.checkIdIsFinal();
    }

    private void checkIdIsFinal() {
        try {
            for (String paramID : this.entity.getIdGetters().keySet()) {
                Object currentID = this.entity.getIdGetters().get(paramID).invoke(this.instance);
                if (this.ids.get(paramID) != null && !this.ids.get(paramID).equals(currentID)) {
                    throw new ModelExecutionException(String.format("Resource Invariant Violation: Id %s has changed since initialization", paramID));
                }
            }
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void checkCheckerIsFinal() {
        Object currentChecker = null;
        try {
            currentChecker = this.entity.getCheckerGetter().invoke(this.instance);
            if (this.permissionChecker != null && !permissionChecker.equals(currentChecker)){
                throw new ModelExecutionException("Resource Invariant Violation: PermissionChecker has changed since initialization");
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
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

    public HashMap<String, Object> getIds() {
        return this.ids;
    }

    public AuthorizationResourceEntity getEntity() {
        return this.entity;
    }

    public Object getInstance() {
        return this.instance;
    }

    public boolean isIdentifiedBy(HashMap<String, Object> resourceIDs) {
        for (String param : this.ids.keySet()){
            if (!resourceIDs.containsKey(param) || !resourceIDs.get(param).equals(this.ids.get(param))){
                return false;
            }
        }
        return true;
    }
}
