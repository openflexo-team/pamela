package org.openflexo.model.share;

import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Initializer;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Parameter;

/**
 * Model for a CreateCommand.
 */
@ModelEntity()
public interface CreateContent extends ChangeContent {

    String CREATED_OBJECT = "createdObject";

    @Initializer
    CreateContent construtor(
            @Parameter(CREATED_OBJECT) String createdObject
    );

    @Getter(CREATED_OBJECT)
    String getCreatedObject();
}
