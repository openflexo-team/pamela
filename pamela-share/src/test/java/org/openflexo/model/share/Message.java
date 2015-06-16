package org.openflexo.model.share;

import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;

import java.util.List;

/**
 * Message model
 */
@ModelEntity
public interface Message {

    @Getter("text")
    String getText();

    @Setter("text")
    void setText(String value);

    @Getter("replied")
    Message getReplied();

    @Setter("replied")
    void setReplied(Message value);

    @Getter(value = "replies", cardinality = Cardinality.LIST, inverse = "replied")
    List<Message> getReplies();

    @Adder("replies")
    void addReply(Message reply);

    @Remover("replies")
    void removeReply(Message reply);
}
