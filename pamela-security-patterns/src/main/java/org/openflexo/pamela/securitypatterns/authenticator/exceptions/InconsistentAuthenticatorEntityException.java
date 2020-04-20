package org.openflexo.pamela.securitypatterns.authenticator.exceptions;

import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.Authenticator;

/**
 * This exception extends the {@link ModelDefinitionException}. <br>
 * It will be thrown when analyzing an {@link Authenticator} annotated class with missing or invalid annotations.
 *
 * @author Caine Silva, Sylvain Guerin
 */
public class InconsistentAuthenticatorEntityException extends ModelDefinitionException {
    /**
     * Constructor of the class.
     * @param message Message to be wrapped in the exception.
     */
    public InconsistentAuthenticatorEntityException(String message) {
        super(message);
    }
}
