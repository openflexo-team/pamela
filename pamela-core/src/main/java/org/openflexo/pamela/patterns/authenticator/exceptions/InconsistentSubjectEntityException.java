package org.openflexo.pamela.patterns.authenticator.exceptions;

import org.openflexo.pamela.exceptions.ModelDefinitionException;

public class InconsistentSubjectEntityException extends ModelDefinitionException {
    public InconsistentSubjectEntityException(String message) {
        super(message);
    }
}
