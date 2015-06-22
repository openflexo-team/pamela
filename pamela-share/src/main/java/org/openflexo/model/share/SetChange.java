package org.openflexo.model.share;

import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Initializer;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Parameter;

/**
 * Model for a SetCommand.
 */
@ModelEntity
public interface SetChange extends Change {

    String NEW_VALUE = "newValue";

    @Initializer
    SetChange constructor(
            @Parameter(UPDATED_OBJECT) String updatedObject,
            @Parameter(MODEL_PROPERTY) String modelProperty,
            @Parameter(OLD_VALUE) ObjectDescription oldValue,
            @Parameter(NEW_VALUE) ObjectDescription newValue
    );

    @Getter(UPDATED_OBJECT)
    String getUpdatedObject();

    @Getter(MODEL_PROPERTY)
    String getModelProperty();

    @Getter(OLD_VALUE)
    ObjectDescription getOldValue();

    @Getter(NEW_VALUE)
    ObjectDescription getNewValue();

}
