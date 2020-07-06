package org.openflexo.pamela.jml;

import java.lang.reflect.Method;

import org.openflexo.pamela.annotations.jml.Requires;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.model.ModelEntity;

public class JMLRequires<I> extends JMLExpressionBasedOnMethod<Boolean, I> {

	public JMLRequires(Requires annotation, ModelEntity<I> entity, Method method) {
		super(entity, method);
		init(annotation.value(), Boolean.class);
	}

	public void check(ProxyMethodHandler<I> proxyMethodHandler, Object[] args) throws SpecificationsViolationException {
		Boolean eval;
		try {
			eval = evaluate(proxyMethodHandler.getObject(), buildMethodArguments(args));
		} catch (Exception e) {
			e.printStackTrace();
			throw new SpecificationsViolationException(
					"Cannot evaluate Requires " + getExpression() + " : " + e.getMessage() + " while executing " + getMethod(),
					proxyMethodHandler);
		}
		if (eval == null) {
			throw new SpecificationsViolationException("Cannot evaluate Requires " + getExpression() + " while executing " + getMethod(),
					proxyMethodHandler);
		}
		if (!eval) {
			throw new SpecificationsViolationException("Requires violation: " + getExpression() + " while executing " + getMethod(),
					proxyMethodHandler);
		}
	}

}
