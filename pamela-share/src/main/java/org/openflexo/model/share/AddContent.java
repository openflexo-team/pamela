package org.openflexo.model.share;

import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Initializer;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Parameter;

/**
 * Model for a AddCommand.
 */
@ModelEntity
public interface AddContent extends ChangeContent {

    String ADDED_VALUE = "addedValue";
    String INDEX = "index";

    @Initializer
    AddContent construtor(
            @Parameter(UPDATED_OBJECT) String updatedObject,
            @Parameter(MODEL_PROPERTY) String modelProperty,
            @Parameter(ADDED_VALUE) String addedValue,
            @Parameter(INDEX) int index
    );

    @Initializer
    AddContent construtor(
            @Parameter(UPDATED_OBJECT) String updatedObject,
            @Parameter(MODEL_PROPERTY) String modelProperty,
            @Parameter(ADDED_VALUE) String addedValue
    );

    @Getter(UPDATED_OBJECT)
    String getUpdatedObject();

    @Getter(MODEL_PROPERTY)
    String getModelProperty();

    @Getter(ADDED_VALUE)
    String getAddedValue();

    @Getter(INDEX)
    Integer getIndex();

}
