package org.openflexo.pamela.securitypatterns.authorization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used in the definition of an <code>Authorization Pattern</code>.<br>
 * This annotation is to be put on a the method of an {@link ProtectedResource} annotated class which allows for the retrieval of
 * the permission checker instance.<br>
 *
 * @author Caine Silva, Sylvain Guerin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface PermissionCheckerGetter {
    /**
     * @return The unique identifier of the associated Authorization Pattern instance.
     */
    String patternID();
}
