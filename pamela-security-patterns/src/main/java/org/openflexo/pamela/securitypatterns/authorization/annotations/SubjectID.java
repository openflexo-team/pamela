package org.openflexo.pamela.securitypatterns.authorization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used in the definition of an <code>Authorization Pattern</code>.<br>
 * This annotation is to be put on a method of a {@link AuthorizationSubject} annotated class allowing
 * for the retrieval of a resource identifier.<br>
 * A {@link AuthorizationSubject} annotated class can have an multiple (but at least one) {@link SubjectID} annotated method,
 * each of which is uniquely identified by the <code>paramID</code> of the annotation.
 *
 * @author Caine Silva, Sylvain Guerin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.PARAMETER})
public @interface SubjectID {
    /**
     * @return The unique identifier of the associated Authorization Pattern instance.
     */
    String patternID();

    /**
     * @return The unique identifier of the method as part of the set of identifier getters.
     */
    String paramID();
}
