package org.openflexo.pamela.securitypatterns.authenticator.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openflexo.pamela.annotations.Setter;

/**
 * This annotation is used in the definition of an <code>Authenticator Pattern</code>. <br>
 * This annotation is to be put on {@link Setter} annotated method in a {@link AuthenticatorSubject} annotated class. <br>
 * The associated field must always be either its initial value (set in constructor) or the result of the {@link RequestAuthentication}
 * method of the pattern.
 *
 * @author C. SILVA
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface ProofOfIdentitySetter {
    /**
     * @return The unique identifier of the associated Authenticator Pattern instance.
     */
    String patternID();
}
