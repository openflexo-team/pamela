package org.openflexo.pamela.patterns.authenticator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.openflexo.pamela.annotations.Getter;

/**
 * This annotation is used in the definition of an <code>Authenticator Pattern</code>. <br>
 * This annotation is to be put on {@link Getter} method in a {@link AuthenticatorSubject} annotated class. <br>
 * The annotated method should return the instance of the associated {@link Authenticator} annotated class. <br>
 * The associated filed must be initialized in constructor and must not change througout runtime.
 *
 * @author C. SILVA
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface AuthenticatorGetter {
    /**
     * @return The unique identifier of the associated Authenticator Pattern instance.
     */
    String patternID();
}
