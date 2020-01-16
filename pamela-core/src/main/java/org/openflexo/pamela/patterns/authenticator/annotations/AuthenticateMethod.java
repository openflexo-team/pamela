package org.openflexo.pamela.patterns.authenticator.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation is used in the definition of an <code>Authenticator Pattern</code>.<br>
 * This annotation is to be put on method in a {@link AuthenticatorSubject} annotated class.<br>
 * The annotated method will call the {@link ProofOfIdentitySetter} method to update the associated field with the
 * returned <code>Proof of Identity</code> of the {@link RequestAuthentication} annotated method of the associated
 * {@link Authenticator} annotated class.
 *
 * @author C. SILVA
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface AuthenticateMethod {
    /**
     * @return The unique identifier of the associated Authenticator Pattern instance.
     */
    String patternID();
}
