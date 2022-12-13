package org.openflexo.pamela.ppf;

import org.openflexo.pamela.AssertionViolationException;
import org.openflexo.pamela.factory.ProxyMethodHandler;

/**
 * Thrown when a property predicate verification has failed
 * 
 * @author sylvain
 *
 */
@SuppressWarnings("serial")
public class PPFViolationException extends AssertionViolationException {

	public PPFViolationException(String message, ProxyMethodHandler<?> handler) {
		super(message, handler);
	}

}
