package org.openflexo.pamela;

import java.lang.reflect.Method;
import java.util.Stack;

import org.openflexo.pamela.factory.ProxyMethodHandler;

/**
 * A {@link RuntimeException} which is thrown when an assertion failed in the context of monitoring
 * 
 * @author sylvain
 *
 */
@SuppressWarnings("serial")
public abstract class AssertionViolationException extends RuntimeException {

	private ProxyMethodHandler<?> handler;

	public AssertionViolationException(String message, ProxyMethodHandler<?> handler) {
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
