package org.openflexo.pamela.securitypatterns.authenticator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used in the definition of an <code>Authenticator Pattern</code>.<br>
 * This annotation is to be put on method in a {@link AuthenticatorSubject} annotated class and indicates that calling this method requires
 * for the subject to be authenticated
 *
 * @author C. SILVA
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface RequiresAuthentication {
}
