package org.openflexo.model.share;

import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;

import java.util.List;

/**
 * Compound Content model
 */
@ModelEntity
@Imports({
        @Import(Change.class),
        @Import(AddChange.class), @Import(CreateChange.class), @Import(DeleteChange.class),
        @Import(RemoveChange.class), @Import(SetChange.class), @Import(ObjectDescription.class)
})
public interface CompoundContent {

    @Getter("presentationName")
    String getPresentationName();

    @Setter("presentationName")
    void setPresentationName(String value);

    @Getter(value = "changes", cardinality = Cardinality.LIST)
    List<Change> getChanges();

    @Setter("changes")
    void setChanges(List<Change> changes);

    @Adder("changes")
    void addToChanges(Change change);

    @Remover("changes")
    void removeFromChanges(Change change);

}
