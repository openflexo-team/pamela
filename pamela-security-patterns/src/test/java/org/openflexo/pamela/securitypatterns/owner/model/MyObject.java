package org.openflexo.pamela.securitypatterns.owner.model;

import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.securitypatterns.owner.annotations.OwnedObject;
import org.openflexo.pamela.securitypatterns.owner.annotations.Owner;
import org.openflexo.pamela.securitypatterns.owner.annotations.Pure;

@ModelEntity
@OwnedObject(patternID = MyObject.PATTERN_ID)
public interface MyObject {
    String PATTERN_ID = "patternID";
    String OWNER = "owner";
    String OK = "ok";

    @Getter(value = OK, defaultValue = "false")
    @Pure(patternID = PATTERN_ID)
    boolean getOK();

    @Setter(OK)
    void setOK(boolean val);

    @Getter(value = OWNER)
    @Owner(patternID = PATTERN_ID)
    @Pure(patternID = PATTERN_ID)
    MyOwner getOwner();

    @Setter(OWNER)
    void setOwner(MyOwner val);

    default void method1(){
        setOK(true);
        System.out.println("Method1");
    }

    default int method2(int toto){
        setOK(true);
        System.out.println("Method2");
        return toto;
    }

}
