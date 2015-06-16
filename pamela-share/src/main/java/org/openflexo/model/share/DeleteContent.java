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
public interface DeleteContent extends ChangeContent {

    String DELETED_OBJECT = "deletedObject";

    @Initializer
    DeleteContent construtor(
            @Parameter(DELETED_OBJECT) String deletedObject
    );

    @Getter(DELETED_OBJECT)
    String getDeletedObject();

}
