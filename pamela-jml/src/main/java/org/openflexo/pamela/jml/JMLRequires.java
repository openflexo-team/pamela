package org.openflexo.pamela.jml;

import java.lang.reflect.Method;

import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.jml.annotations.Requires;
import org.openflexo.pamela.model.ModelEntity;

/**
 * A pre-condition definition in the context of JML
 * 
 * @author sylvain
 *
 * @param <I>
 */
public class JMLRequires<I> extends JMLExpressionBasedOnMethod<Boolean, I> {

	public JMLRequires(Requires annotation, ModelEntity<I> entity, Method method) {
		super(entity, method);
		init(annotation.value(), Boolean.class);
	}

	public void check(ProxyMethodHandler<I> proxyMethodHandler, Object[] args) throws JMLAssertionViolationException {
		Boolean eval;
		try {
			eval = evaluate(proxyMethodHandler.getObject(), buildMethodArguments(args));
		} catch (Exception e) {
			e.printStackTrace();
			throw new JMLAssertionViolationException(
					"Cannot evaluate Requires " + getExpression() + " : " + e.getMessage() + " while executing " + getMethod(),
					proxyMethodHandler);
		}
		if (eval == null) {
			throw new JMLAssertionViolationException("Cannot evaluate Requires " + getExpression() + " while executing " + getMethod(),
					proxyMethodHandler);
		}
		if (!eval) {
			throw new JMLAssertionViolationException("Requires violation: " + getExpression() + " while executing " + getMethod(),
					proxyMethodHandler);
		}
	}

}
