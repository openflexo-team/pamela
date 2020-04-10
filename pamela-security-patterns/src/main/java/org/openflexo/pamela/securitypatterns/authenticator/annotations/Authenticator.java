package org.openflexo.pamela.securitypatterns.authenticator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used in the definition of an <code>Authenticator Pattern</code>. <br>
 * This annotation is to be put on the authenticator class. <br>
 * An <code>Authenticator Pattern</code> should have exactly one {@link Authenticator} annotated class.
 *
 * @author C. SILVA
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface Authenticator {
    /**
     * @return The unique identifier of the associated Authenticator Pattern instance.
     */
    String patternID();
}
