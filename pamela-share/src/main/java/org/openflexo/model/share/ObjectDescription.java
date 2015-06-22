package org.openflexo.model.share;

import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Initializer;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Parameter;

/**
 * Model for a SetCommand.
 */
@ModelEntity
public interface ObjectDescription {

    String TYPE = "type";
    String CONTENT = "content";

    @Initializer
    ObjectDescription constructor(
            @Parameter(TYPE) String type,
            @Parameter(CONTENT) String content
    );

    @Getter(TYPE)
    String getType();

    @Getter(CONTENT)
    String getContent();

}
