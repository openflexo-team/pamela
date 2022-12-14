package org.openflexo.pamela.model.property;

/**
 * Represent a property implementation managing some parameters
 * 
 * @author sylvain
 *
 * @param <I>
 *            type of annotation
 */
public interface ParameteredPropertyImplementation<PA> {

	PA getParameters();
}
