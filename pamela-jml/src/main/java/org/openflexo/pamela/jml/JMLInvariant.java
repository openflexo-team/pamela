package org.openflexo.pamela.jml;

import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.jml.annotations.Invariant;
import org.openflexo.pamela.model.ModelEntity;

/**
 * An invariant definition in the context of JML
 * 
 * @author sylvain
 *
 * @param <I>
 */
public class JMLInvariant<I> extends JMLExpressionBased<Boolean, I> {

	public JMLInvariant(Invariant annotation, ModelEntity<I> entity) {
		super(entity);
		init(annotation.value(), Boolean.class);
	}

	public void check(ProxyMethodHandler<I> proxyMethodHandler) throws JMLAssertionViolationException {
		Boolean eval;
		try {
			eval = evaluate(proxyMethodHandler.getObject(), null);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JMLAssertionViolationException("Cannot evaluate Invariant " + getExpression() + " : " + e.getMessage(),
					proxyMethodHandler);
		}
		if (eval == null) {
			throw new JMLAssertionViolationException("Cannot evaluate Invariant " + getExpression(), proxyMethodHandler);
		}
		if (!eval) {
			throw new JMLAssertionViolationException("Invariant violation: " + getExpression(), proxyMethodHandler);
		}
	}
}
