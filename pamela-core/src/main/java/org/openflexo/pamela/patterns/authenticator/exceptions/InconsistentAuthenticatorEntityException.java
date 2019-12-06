package org.openflexo.pamela.patterns.authenticator.exceptions;

import org.openflexo.pamela.exceptions.ModelDefinitionException;

public class InconsistentAuthenticatorEntityException extends ModelDefinitionException {
    public InconsistentAuthenticatorEntityException(String message) {
        super(message);
    }
}
