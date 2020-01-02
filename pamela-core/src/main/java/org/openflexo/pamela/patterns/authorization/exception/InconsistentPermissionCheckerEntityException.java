package org.openflexo.pamela.patterns.authorization.exception;

import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.patterns.authorization.annotations.AuthorizationChecker;

/**
 * This exception extends the {@link ModelDefinitionException}. <br>
 * It will be thrown when analyzing an {@link AuthorizationChecker} annotated class with missing or invalid annotations.
 *
 * @author C. SILVA
 */
public class InconsistentPermissionCheckerEntityException extends ModelDefinitionException {
    /**
     * Constructor of the class.
     * @param message Message to be wrapped in the exception.
     */
    public InconsistentPermissionCheckerEntityException(String message) {
        super(message);
    }
}
