package org.openflexo.pamela.patterns.authenticator.exceptions;

import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.patterns.authenticator.annotations.Authenticator;

/**
 * @author C. SILVA
 *
 * This exception extends the {@link ModelDefinitionException}.
 * It will be thrown when analyzing an {@link Authenticator} annotated class with missing or invalid annotations.
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
