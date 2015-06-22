package org.openflexo.model.share;

import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Initializer;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Parameter;

/**
 * Model for a CreateCommand.
 */
@ModelEntity()
public interface CreateChange extends Change {

    String CREATED_OBJECT = "createdObject";

    @Initializer
    CreateChange constructor(
            @Parameter(CREATED_OBJECT) String createdObject
    );

    @Getter(CREATED_OBJECT)
    String getCreatedObject();
}
