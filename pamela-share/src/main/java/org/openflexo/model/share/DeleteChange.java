package org.openflexo.model.share;

import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Initializer;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Parameter;

/**
 * Model for a DeleteCommand.
 *
 */
@ModelEntity
public interface DeleteChange extends Change {

    String DELETED_OBJECT = "deletedObject";

    @Initializer
    DeleteChange constructor(
            @Parameter(DELETED_OBJECT) String deletedObject
    );

    @Getter(DELETED_OBJECT)
    String getDeletedObject();

}
