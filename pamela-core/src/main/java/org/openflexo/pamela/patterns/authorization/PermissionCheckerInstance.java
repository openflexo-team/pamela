package org.openflexo.pamela.patterns.authorization;

import java.lang.reflect.Method;

public class PermissionCheckerInstance {
    private Object instance;
    private PermissionCheckerEntity entity;
    private boolean initializing;
    private boolean checking;

    public PermissionCheckerInstance(Object instance, PermissionCheckerEntity permissionCheckerEntity) {
        this.entity = permissionCheckerEntity;
        this.instance = instance;
        this.initializing = false;
        this.checking = false;
    }

    void init() {
        try {
            this.initializing = true;
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
}
