package org.openflexo.pamela.jml;

import java.lang.reflect.Method;
import java.util.Stack;

import org.openflexo.pamela.factory.ProxyMethodHandler;

/**
 * Thrown when a JML specification verification has failed
 * 
 * @author sylvain
 *
 */
@SuppressWarnings("serial")
public class SpecificationsViolationException extends RuntimeException {

	private ProxyMethodHandler<?> handler;

	public SpecificationsViolationException(String message, ProxyMethodHandler<?> handler) {
		super(message);
		this.handler = handler;
	}

	public Object getObject() {
		return handler.getObject();
	}

	public Stack<Method> getAssertionCheckingStack() {
		return handler.getAssertionCheckingStack();
	}

	public void printMethodStack() {
		Stack<Method> stack = getAssertionCheckingStack();
		for (int i = stack.size() - 1; i >= 0; i--) {
			System.err.println("   *** " + stack.get(i));
		}
	}
}
