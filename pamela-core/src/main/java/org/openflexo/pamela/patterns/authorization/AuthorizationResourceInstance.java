package org.openflexo.pamela.patterns.authorization;

import javax.swing.text.html.parser.Entity;
import java.lang.reflect.Method;
import java.util.Collection;
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
            this.initializing = false;
        }
        catch (Exception e){

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
