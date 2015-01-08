package org.openflexo.model.exceptions;

import java.lang.reflect.Method;

import org.openflexo.model.ModelEntity;
import org.openflexo.model.factory.ModelFactory;

/**
 * Thrown when a method of interface has been detected with no possible implementations with supplied factory
 * 
 * @author sylvain
 * 
 */
@SuppressWarnings("serial")
public class MissingImplementationException extends Exception {

	public MissingImplementationException(ModelEntity<?> entity, Method method, ModelFactory factory) {
		super("No implementation found for entity: " + entity.getImplementedInterface() + " method=" + method);
	}

}
