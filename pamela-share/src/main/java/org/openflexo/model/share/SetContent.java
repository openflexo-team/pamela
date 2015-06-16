package org.openflexo.model.share;

import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Initializer;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Parameter;

/**
 * Model for a SetCommand.
 */
@ModelEntity
public interface SetContent extends ChangeContent {

    String NEW_VALUE = "newValue";

    @Initializer
    SetContent construtor(
            @Parameter(UPDATED_OBJECT) String updatedObject,
            @Parameter(MODEL_PROPERTY) String modelProperty,
            @Parameter(OLD_VALUE) String oldValue,
            @Parameter(NEW_VALUE) String newValue
    );

    @Getter(UPDATED_OBJECT)
    String getUpdatedObject();

    @Getter(MODEL_PROPERTY)
    String getModelProperty();

    @Getter(OLD_VALUE)
    String getOldValue();

    @Getter(NEW_VALUE)
    String getNewValue();

}
