package org.openflexo.pamela.securitypatterns.owner.model;

import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;

@ModelEntity
public interface MyOwner {
    String OBJECT =  "object";

    @Getter(value = OBJECT, defaultValue = "")
    MyObject getObject();

    @Setter(value = OBJECT)
    void setObject(MyObject val);

    default void m1(){
        getObject().method1();
    }

    default int m2(){
        return getObject().method2(42);
    }

    default void initOK(){
        getObject().setOK(false);
    }
}
