package org.openflexo.pamela.securitypatterns.singleAccessPoint.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
@Deprecated
public @interface Checkpoint {
    String patternID();
}
