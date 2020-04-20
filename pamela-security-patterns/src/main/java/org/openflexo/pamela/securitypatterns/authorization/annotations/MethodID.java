package org.openflexo.pamela.securitypatterns.authorization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used in the definition of an <code>Authorization Pattern</code>.<br>
 * This annotation is to be put on the parameter of the {@link CheckAccess} annotated method which corresponds to the
 * access method identifier.<br>
 *
 * @author Caine Silva, Sylvain Guerin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.PARAMETER)
public @interface MethodID {
    /**
     * @return The unique identifier of the associated Authorization Pattern instance.
     */
    String patternID();
}
