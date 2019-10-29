package org.openflexo.pamela.jml;

import org.openflexo.pamela.ModelEntity;
import org.openflexo.pamela.annotations.jml.Invariant;
import org.openflexo.pamela.factory.ProxyMethodHandler;

public class JMLInvariant<I> extends JMLExpressionBased<Boolean, I> {

	public JMLInvariant(Invariant annotation, ModelEntity<I> entity) {
		super(entity);
		init(annotation.value(), Boolean.class);
	}

	public void check(ProxyMethodHandler<I> proxyMethodHandler) throws SpecificationsViolationException {
		Boolean eval;
		try {
			eval = evaluate(proxyMethodHandler.getObject(), null);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SpecificationsViolationException("Cannot evaluate Invariant " + getExpression() + " : " + e.getMessage(),
					proxyMethodHandler);
		}
		if (eval == null) {
			throw new SpecificationsViolationException("Cannot evaluate Invariant " + getExpression(), proxyMethodHandler);
		}
		if (!eval) {
			throw new SpecificationsViolationException("Invariant violation: " + getExpression(), proxyMethodHandler);
		}
	}
}
