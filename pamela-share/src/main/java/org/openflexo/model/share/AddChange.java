package org.openflexo.model.share;

import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Initializer;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Parameter;

/**
 * Model for a AddCommand.
 */
@ModelEntity
public interface AddChange extends Change {

    String ADDED_VALUE = "addedValue";
    String INDEX = "index";

    @Initializer
    AddChange constructor(
            @Parameter(UPDATED_OBJECT) String updatedObject,
            @Parameter(MODEL_PROPERTY) String modelProperty,
            @Parameter(ADDED_VALUE) String addedValue,
            @Parameter(INDEX) Integer index
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
