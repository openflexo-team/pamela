package org.openflexo.model.share;

import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;

/**
 * Model for an AtomicEdit
 */
@ModelEntity(isAbstract = true)
@Imports({
        @Import(AddContent.class), @Import(CreateContent.class), @Import(DeleteContent.class),
        @Import(RemoveContent.class), @Import(SetContent.class),
        @Import(CompoundContent.class)
})
public interface ChangeContent {
    String UPDATED_OBJECT = "updatedObject";
    String MODEL_PROPERTY = "modelProperty";
    String OLD_VALUE = "oldValue";
}
