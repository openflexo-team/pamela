package org.openflexo.pamela.securitypatterns.singleAccessPoint.model;

import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.securitypatterns.singleAccessPoint.annotations.*;

@ModelEntity
@SingleAccessPointSystem(patternID = ProtectedSystem.PATTERN_ID)
public interface ProtectedSystem {
    String PATTERN_ID = "SAP";
    String PARAM_ID = "token";
    String CHECK1 = "c1";
    String CHECK = "c0";
    String COUNTER = "counter";

    @Getter(value = COUNTER, defaultValue = "0")
    int getCounter();

    @Setter(value = COUNTER)
    void setCounter(int val);

    @Getter(value = CHECK1, defaultValue = "false")
    boolean getCheck1();

    @Setter(CHECK1)
    void setCheck1(boolean val);

    @Getter(value = CHECK, defaultValue = "false")
    boolean HasChecked();

    @Setter(CHECK)
    void setHasChecked(boolean val);

    default void method1(){
        setCheck1(true);
    }

    default int method2(int arg){
        return arg;
    }


    @Checkpoint(patternID = PATTERN_ID)
    default boolean onEntry(@RequiredForAccess(patternID = PATTERN_ID, paramID = PARAM_ID ) int token){
        System.out.println("Checking");
        setHasChecked(true);
        setCounter(getCounter() + 1);
        return token==42;
    }

}
