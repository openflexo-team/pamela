package org.openflexo.pamela.patterns.authenticator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author C. SILVA
 *
 * This annotation is used in the definition of an <code>Authenticator Pattern</code>.
 * This annotation is to be put on method in a {@link Authenticator} annotated class.
 * The annotated method will call the {@link ProofOfIdentitySetter} should check the {@link AuthenticationInformation}
 * annotated parameters and return the adequate Proof of Identity.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface RequestAuthentication {
    /**
     * @return The unique identifier of the associated Authenticator Pattern instance.
     */
    String patternID();
}
