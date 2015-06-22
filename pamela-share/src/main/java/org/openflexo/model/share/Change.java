package org.openflexo.model.share;

import org.openflexo.model.annotations.ModelEntity;

/**
 * Model for an AtomicEdit
 */
@ModelEntity(isAbstract = true)
public interface Change {
    String UPDATED_OBJECT = "updatedObject";
    String MODEL_PROPERTY = "modelProperty";
    String OLD_VALUE = "oldValue";
}
