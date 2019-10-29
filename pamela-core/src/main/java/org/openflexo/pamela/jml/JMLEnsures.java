package org.openflexo.pamela.jml;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.pamela.ModelEntity;
import org.openflexo.pamela.annotations.jml.Ensures;
import org.openflexo.pamela.factory.ProxyMethodHandler;

public class JMLEnsures<I> extends JMLExpressionBasedOnMethod<Boolean, I> {

	public JMLEnsures(Ensures annotation, ModelEntity<I> entity, Method method) {
		super(entity, method);
		init(annotation.value(), Boolean.class);
	}

	public Map<String, Object> checkOnEntry(ProxyMethodHandler<I> proxyMethodHandler, Object[] args)
			throws SpecificationsViolationException {
		Map<String, Object> returned = new HashMap<>();

		// Iterate on all history binding variables to compute their value while entering in supplied method
		for (HistoryBindingVariable historyBindingVariable : historyBindingVariables) {
			// System.out.println("Evaluate on entry: " + historyBindingVariable.getVariableName() + " with expression "
			// + historyBindingVariable.getValueExpression());
			try {
				Object historyValue = historyBindingVariable.evaluate(proxyMethodHandler.getObject(), buildMethodArguments(args));
				// System.out.println(historyBindingVariable.getVariableName() + " = " + historyValue);
				returned.put(historyBindingVariable.getVariableName(), historyValue);
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return returned;
	}

	public void checkOnExit(ProxyMethodHandler<I> proxyMethodHandler, Object[] args, Map<String, Object> historyValues)
			throws SpecificationsViolationException {
		Boolean eval;
		try {
			Map<String, Object> values = buildMethodArguments(args);
			for (String key : historyValues.keySet()) {
				values.put(key, historyValues.get(key));
			}
			eval = evaluate(proxyMethodHandler.getObject(), values);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SpecificationsViolationException(
					"Cannot evaluate Ensures " + getExpression() + " : " + e.getMessage() + " while executing " + getMethod(),
					proxyMethodHandler);
		}
		if (eval == null) {
			throw new SpecificationsViolationException("Cannot evaluate Ensures " + getExpression() + " while executing " + getMethod(),
					proxyMethodHandler);
		}
		if (!eval) {
			throw new SpecificationsViolationException("Ensures violation: " + getExpression() + " while executing " + getMethod(),
					proxyMethodHandler);
		}
	}

}
