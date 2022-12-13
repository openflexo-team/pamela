package org.openflexo.pamela.jml;

import org.openflexo.pamela.AssertionViolationException;
import org.openflexo.pamela.factory.ProxyMethodHandler;

/**
 * Thrown when a JML specification verification has failed
 * 
 * @author sylvain
 *
 */
@SuppressWarnings("serial")
public class JMLAssertionViolationException extends AssertionViolationException {

	public JMLAssertionViolationException(String message, ProxyMethodHandler<?> handler) {
		super(message, handler);
	}

}
