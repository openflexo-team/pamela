package org.openflexo.pamela.jml;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DefaultBindable;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TransformException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.BindingValue;
import org.openflexo.connie.expr.BindingValue.AbstractBindingPathElement;
import org.openflexo.connie.expr.BindingValue.MethodCallBindingPathElement;
import org.openflexo.connie.expr.BindingValue.NormalBindingPathElement;
import org.openflexo.connie.expr.Expression;
import org.openflexo.connie.expr.ExpressionTransformer;
import org.openflexo.connie.expr.parser.ExpressionParser;
import org.openflexo.connie.expr.parser.ParseException;
import org.openflexo.connie.java.JavaBindingFactory;
import org.openflexo.kvc.InvalidKeyValuePropertyException;
import org.openflexo.pamela.ModelEntity;

public abstract class JMLExpressionBased<T, I> extends DefaultBindable {

	private static final BindingFactory BINDING_FACTORY = new JavaBindingFactory();

	private final ModelEntity<I> entity;
	private DataBinding<T> expression;

	protected BindingModel bindingModel;

	protected List<HistoryBindingVariable> historyBindingVariables;

	public JMLExpressionBased(ModelEntity<I> entity) {
		this.entity = entity;
		bindingModel = new BindingModel();
		bindingModel.addToBindingVariables(new BindingVariable("object", entity.getImplementedInterface()));
		historyBindingVariables = new ArrayList<>();
	}

	protected void init(String expressionAsString, Class<T> type) {
		String normalizedBindingPath = extractHistoryVariableAndNormalizeBindingPath(expressionAsString);

		expression = new DataBinding<>(normalizedBindingPath, this, type, DataBinding.BindingDefinitionType.GET);

		// System.out.println("Binding = " + binding + " valid=" + binding.isValid() + " as " + binding.getClass());
		if (!expression.isValid()) {
			System.err.println("not valid: " + expression.invalidBindingReason());
			throw new InvalidKeyValuePropertyException(
					"Cannot interpret " + normalizedBindingPath + " for object of type " + entity.getImplementedInterface());
		}
	}

	public T evaluate(I object, Map<String, Object> values)
			throws TypeMismatchException, NullReferenceException, InvocationTargetException {
		return expression.getBindingValue(new BindingEvaluationContext() {

			@Override
			public Object getValue(BindingVariable variable) {
				if (variable.getVariableName().equals("object")) {
					return object;
				}
				else {
					T returned = (T) values.get(variable.getVariableName());
					if (returned == null) {
						System.err.println("??? Tiens je trouve rien pour " + variable);
					}
					return returned;
				}
			}
		});
	}

	// public abstract void check(I object, Method method, Object[] args) throws SpecificationsViolationException;

	public DataBinding<T> getExpression() {
		return expression;
	}

	@Override
	public BindingModel getBindingModel() {
		return bindingModel;
	}

	@Override
	public BindingFactory getBindingFactory() {
		return BINDING_FACTORY;
	}

	@Override
	public void notifiedBindingChanged(DataBinding<?> dataBinding) {
	}

	@Override
	public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
	}

	public class HistoryBindingVariable extends BindingVariable {

		private DataBinding<?> valueExpression;

		public HistoryBindingVariable(String variableName, Expression expression) {
			super(variableName, Object.class);
			valueExpression = new DataBinding<Object>(expression.toString(), JMLExpressionBased.this, type,
					DataBinding.BindingDefinitionType.GET);
		}

		@Override
		public Type getType() {
			return valueExpression.getAnalyzedType();
		}

		public DataBinding<?> getValueExpression() {
			return valueExpression;
		}

		public Object evaluate(I object, Map<String, Object> values)
				throws TypeMismatchException, NullReferenceException, InvocationTargetException {
			return valueExpression.getBindingValue(new BindingEvaluationContext() {

				@Override
				public Object getValue(BindingVariable variable) {
					if (variable.getVariableName().equals("object")) {
						return object;
					}
					else {
						T returned = (T) values.get(variable.getVariableName());
						if (returned == null) {
							System.err.println("??? Tiens je trouve rien pour " + variable);
						}
						return returned;
					}
				}
			});
		}

	}

	private static final String HISTORY_VARIABLE_NAME = "HISTORY_VARIABLE";
	private static int CURRENT_HISTORY_VARIABLE_ID = 0;

	private String extractHistoryVariableAndNormalizeBindingPath(String bindingPath) {
		Expression expression = null;
		try {
			bindingPath = bindingPath.replace("/old", HISTORY_VARIABLE_NAME);
			expression = ExpressionParser.parse(bindingPath);
			if (expression != null) {
				expression = expression.transform(new ExpressionTransformer() {
					@Override
					public Expression performTransformation(Expression e) throws TransformException {
						if (e instanceof BindingValue) {
							BindingValue bv = (BindingValue) e;
							if (bv.getParsedBindingPath().size() > 0) {
								AbstractBindingPathElement firstPathElement = bv.getParsedBindingPath().get(0);

								if (firstPathElement instanceof MethodCallBindingPathElement
										&& ((MethodCallBindingPathElement) firstPathElement).method.equals(HISTORY_VARIABLE_NAME)) {
									try {
										MethodCallBindingPathElement methodCall = (MethodCallBindingPathElement) firstPathElement;
										CURRENT_HISTORY_VARIABLE_ID++;
										String variableName = HISTORY_VARIABLE_NAME + CURRENT_HISTORY_VARIABLE_ID;
										HistoryBindingVariable newHistoryVariable = new HistoryBindingVariable(variableName,
												methodCall.args.get(0));
										historyBindingVariables.add(newHistoryVariable);
										bindingModel.addToBindingVariables(newHistoryVariable);
										// System.out.println("variableName=" + variableName);
										// System.out.println("args=" + methodCall.args.get(0));
										return new BindingValue(variableName);
									} catch (ParseException e1) {
										e1.printStackTrace();
										return null;
									}
								}

								else if (!(firstPathElement instanceof NormalBindingPathElement) || (bindingModel
										.bindingVariableNamed(((NormalBindingPathElement) firstPathElement).property) == null)) {
									bv.getParsedBindingPath().add(0, new NormalBindingPathElement("object"));
									bv.clearSerializationRepresentation();
								}

							}
							return bv;
						}
						return e;
					}
				});
				return expression.toString();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (TransformException e) {
			e.printStackTrace();
		}
		return null;
	}

}
