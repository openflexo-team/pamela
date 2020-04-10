package org.openflexo.pamela.securitypatterns.authenticator.exceptions;

import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticatorSubject;

/**
 * This exception extends the {@link ModelDefinitionException}. <br>
 * It will be thrown when analyzing an {@link AuthenticatorSubject} annotated class with missing or invalid annotations.
 *
 * @author C. SILVA
 */
public class InconsistentSubjectEntityException extends ModelDefinitionException {
    /**
     * Constructor of the class.
     * @param message Message to be wrapped in the exception.
     */
    public InconsistentSubjectEntityException(String message) {
        super(message);
    }
}
