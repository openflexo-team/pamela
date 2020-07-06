package org.openflexo.pamela.jml;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openflexo.connie.BindingVariable;
import org.openflexo.pamela.annotations.jml.MethodParameter;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;

public abstract class JMLExpressionBasedOnMethod<T, I> extends JMLExpressionBased<T, I> {

	private final Method method;
	private List<ParameterBindingVariable> parametersBindingVariables;

	public JMLExpressionBasedOnMethod(ModelEntity<I> entity, Method method) {
		super(entity);
		this.method = method;

		ModelProperty<? super I> property = entity.getPropertyForMethod(method);

		parametersBindingVariables = new ArrayList<>();

		int parameterIndex = 0;
		for (Annotation[] annots : method.getParameterAnnotations()) {
			if (annots.length > 0) {
				for (Annotation annotation : annots) {
					if (annotation instanceof MethodParameter) {
						MethodParameter paramAnnot = (MethodParameter) annotation;
						Type paramType = method.getGenericParameterTypes()[parameterIndex];
						ParameterBindingVariable parameterBindingVariable = new ParameterBindingVariable(paramAnnot.value(), paramType,
								parameterIndex);
						parametersBindingVariables.add(parameterBindingVariable);
						bindingModel.addToBindingVariables(parameterBindingVariable);
					}
				}
			}

			parameterIndex++;
		}

	}

	protected Map<String, Object> buildMethodArguments(Object[] args) {
		Map<String, Object> returned = new HashMap<>();
		for (int i = 0; i < args.length; i++) {
			returned.put(parametersBindingVariables.get(i).getVariableName(), args[i]);
		}
		return returned;
	}

	public Method getMethod() {
		return method;
	}

	public class ParameterBindingVariable extends BindingVariable {

		private int index;

		public ParameterBindingVariable(String variableName, Type type, int index) {
			super(variableName, type);
			this.index = index;
		}

		public int getIndex() {
			return index;
		}
	}

}
