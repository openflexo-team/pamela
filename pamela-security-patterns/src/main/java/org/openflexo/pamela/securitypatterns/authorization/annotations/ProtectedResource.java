package org.openflexo.pamela.securitypatterns.authorization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used in the definition of an <code>Authorization Pattern</code>.<br>
 * This annotation is to be put on a class specifying a resource for the pattern.<br>
 * A {@link AuthorizationSubject} annotated class must have {@link ResourceID} and {@link PermissionCheckerGetter} annotated methods.
 *
 * @author Caine Silva, Sylvain Guerin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface ProtectedResource {
    /**
     * @return The unique identifier of the associated Authorization Pattern instance.
     */
    String patternID();
}
