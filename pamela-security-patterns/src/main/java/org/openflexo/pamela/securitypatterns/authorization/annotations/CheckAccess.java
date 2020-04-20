package org.openflexo.pamela.securitypatterns.authorization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used in the definition of an <code>Authorization Pattern</code>.<br>
 * This annotation is to be put on the method which will be called to check every access
 * to a {@link ProtectedResource} annotated class.<br>
 * The parameter of the annotated method should be annotated with the relevant {@link SubjectID}, {@link ResourceID} and
 * {@link MethodID} annotations.
 *
 * @author Caine Silva, Sylvain Guerin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface CheckAccess {
    /**
     * @return The unique identifier of the associated Authorization Pattern instance.
     */
    String patternID();
}
