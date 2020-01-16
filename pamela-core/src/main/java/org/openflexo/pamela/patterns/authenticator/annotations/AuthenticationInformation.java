package org.openflexo.pamela.patterns.authenticator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.openflexo.pamela.annotations.Getter;

/**
 * This annotation is used in the definition of an <code>Authenticator Pattern</code>. <br>
 * This annotation is to be put on a {@link Getter} method in a {@link AuthenticatorSubject} annotated class and on the
 * corresponding parameter of the {@link RequestAuthentication} annotated method of the associated {@link Authenticator}
 * class. <br>
 * The associated field is supposed to be initialized in constructor and must not change throughout runtime. <br>
 * A class can have multiple {@link AuthenticationInformation} annotations. The set of all of these fields is
 * supposed to be unique. (i.e. two different instances must have a different set of <code>Authentication Information</code>).
 *
 * @author C. SILVA
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.PARAMETER})
public @interface AuthenticationInformation {
    /**
     * @return The unique identifier of the associated Authenticator Pattern instance.
     */
    String patternID();
    /**
     * @return The identifier allowing the pattern to link the {@link AuthenticationInformation} getter
     * with the {@link RequestAuthentication} parameter.
     */
    String paramID();
}
