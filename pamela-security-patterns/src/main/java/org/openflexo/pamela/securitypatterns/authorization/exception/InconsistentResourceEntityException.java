package org.openflexo.pamela.securitypatterns.authorization.exception;

import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.securitypatterns.authorization.annotations.ProtectedResource;

/**
 * This exception extends the {@link ModelDefinitionException}. <br>
 * It will be thrown when analyzing an {@link ProtectedResource} annotated class with missing or invalid annotations.
 *
 * @author C. SILVA
 */
public class InconsistentResourceEntityException extends ModelDefinitionException {
    /**
     * Constructor of the class.
     * @param message Message to be wrapped in the exception.
     */
    public InconsistentResourceEntityException(String message) {
        super(message);
    }
}
