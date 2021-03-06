package org.openflexo.pamela.securitypatterns.singleAccessPoint.model;

import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.securitypatterns.singleAccessPoint.SingleAccessPointPatternDefinition;
import org.openflexo.pamela.securitypatterns.singleAccessPoint.annotations.*;

@ModelEntity
@SingleAccessPointClient(patternID = ProtectedSystem.PATTERN_ID)
public interface Accessor {
    String TOKEN = "token";
    String SYSTEM = "system";

    @Getter(value = SYSTEM, defaultValue = "")
    ProtectedSystem getSystem();

    @Setter(value = SYSTEM)
    void setSystem(ProtectedSystem val);

    @RequiredForAccess(patternID = ProtectedSystem.PATTERN_ID, paramID = ProtectedSystem.PARAM_ID)
    @Getter(value = TOKEN, defaultValue = "-1")
    int getToken();

    @Setter(TOKEN)
    void setToken(int val);

    default void first(){
        System.out.println("Trying to call method1 from the accessor");
        getSystem().method1();
    }

    default int second(){
        System.out.println("Trying to call method2 from the accessor");
        return getSystem().method2(18);
    }

    default boolean HasChecked(){
        return getSystem().HasChecked();
    }

    default int getCounter(){
        return getSystem().getCounter();
    }

    default void setHasChecked(boolean val){
        getSystem().setHasChecked(val);
    }

    default boolean getCheck1(){
        return getSystem().getCheck1();
    }
}
