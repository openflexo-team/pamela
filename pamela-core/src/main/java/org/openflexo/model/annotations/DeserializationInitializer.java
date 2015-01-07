package org.openflexo.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openflexo.model.factory.ModelFactory;

/**
 * Annotation used to indicates that annotated method should be called immediately after the object has been created in a deserialization
 * phase<br>
 * Method might take no argument, or also may take the {@link ModelFactory} as argument
 * 
 * @author sylvain
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DeserializationInitializer {

}
