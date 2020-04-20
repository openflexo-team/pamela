package org.openflexo.pamela.securitypatterns.authorization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation is used in the definition of an <code>Authorization Pattern</code>.<br>
 * This annotation is to be put on method in {@link AuthorizationSubject} {@link ProtectedResource} and annotated classes.<br>
 * This annotation is used to link subject's methods with resources methods. When a subject method is called, the associated resource
 * method will be called if teh subject is authorized to access the resource.
 *
 * @author Caine Silva, Sylvain Guerin
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface AccessResource {
    /**
     * @return The unique identifier of the associated Authorization Pattern instance.
     */
    String patternID();
    /**
     * @return The unique identifier linking subjects and resources' access methods.
     */
    String methodID();
}
