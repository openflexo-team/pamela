package org.openflexo.model.share;

import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Initializer;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Parameter;

/**
 * Model for a AddCommand.
 */
@ModelEntity
public interface RemoveContent extends ChangeContent {

    String REMOVED_VALUE = "removedValue";

    @Initializer
    RemoveContent construtor(
            @Parameter(UPDATED_OBJECT) String updatedObject,
            @Parameter(MODEL_PROPERTY) String modelProperty,
            @Parameter(REMOVED_VALUE) String addedValue
    );

    @Getter(UPDATED_OBJECT)
    String getUpdatedObject();

    @Getter(MODEL_PROPERTY)
    String getModelProperty();

    @Getter(REMOVED_VALUE)
    String getRemovedValue();

}
