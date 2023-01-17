/**
 * 
 * Copyright (c) 2013-2020, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of pamela-core, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.pamela.patterns;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.ParseException;
import org.openflexo.connie.binding.BindingPathElement;
import org.openflexo.connie.exception.TransformException;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.connie.expr.Expression;
import org.openflexo.connie.expr.ExpressionTransformer;
import org.openflexo.connie.expr.UnresolvedBindingVariable;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;

/**
 * Specification of a statement beeing triggered in the context of the execution of a {@link Method} related to a {@link PatternInstance}
 * 
 * @author sylvain
 *
 * @param <T>
 *            type returned by underlying execution statement
 */
public abstract class PatternStatement<T> extends PropertyChangedSupportDefaultImplementation implements Bindable {

	private final PatternDefinition patternDefinition;
	private final Method method;
	private final BindingModel bindingModel;
	private final DataBinding<T> executionStatement;

	public static final String DEFAULT_VARIABLE_NAME = "patternInstance";

	public PatternStatement(PatternDefinition patternDefinition, Method method, String assertionAsString) {
		this.patternDefinition = patternDefinition;
		this.method = method;
		bindingModel = new BindingModel();
		bindingModel.addToBindingVariables(new BindingVariable(DEFAULT_VARIABLE_NAME, patternDefinition.getInstanceClass()));
		executionStatement = new DataBinding<T>(normalizeBindingPath(assertionAsString), this, getExpectedType(),
				BindingDefinitionType.GET);
		// System.out.println("assertion: " + assertion);
		// System.out.println("valid: " + assertion.isValid());
		// System.out.println("reason: " + assertion.invalidBindingReason());
	}

	public abstract Type getExpectedType();

	public PatternDefinition getPatternDefinition() {
		return patternDefinition;
	}

	public Method getMethod() {
		return method;
	}

	public DataBinding<T> getExecutionStatement() {
		return executionStatement;
	}

	public String getExecutionStatementAsString() {
		return getExecutionStatement().toString();
	}

	@Override
	public BindingFactory getBindingFactory() {
		return patternDefinition.getBindingFactory();
	}

	@Override
	public BindingModel getBindingModel() {
		return bindingModel;
	}

	@Override
	public void notifiedBindingChanged(DataBinding<?> binding) {
		// TODO Auto-generated method stub
	}

	@Override
	public void notifiedBindingDecoded(DataBinding<?> binding) {
		// TODO Auto-generated method stub
	}

	private String normalizeBindingPath(String bindingPath) {

		if (bindingPath.contains("this")) {
			bindingPath = bindingPath.replaceAll("this", DEFAULT_VARIABLE_NAME);
		}

		Expression expression = null;
		try {
			expression = getBindingFactory().parseExpression(bindingPath, this);
			if (expression != null) {
				expression = expression.transform(new ExpressionTransformer() {
					@Override
					public Expression performTransformation(Expression e) throws TransformException {
						if (e instanceof BindingPath) {
							BindingPath bindingPath = (BindingPath) e;
							if (bindingPath.getBindingVariable() == null) {
								UnresolvedBindingVariable objectBV = new UnresolvedBindingVariable(DEFAULT_VARIABLE_NAME);
								bindingPath.setBindingVariable(objectBV);
								return bindingPath;
							}
							else if (!bindingPath.getBindingVariable().getVariableName().equals(DEFAULT_VARIABLE_NAME)) {
								UnresolvedBindingVariable objectBV = new UnresolvedBindingVariable(DEFAULT_VARIABLE_NAME);
								List<BindingPathElement> bp2 = new ArrayList<>(bindingPath.getBindingPath());
								bp2.add(0, getBindingFactory().makeSimplePathElement(objectBV,
										bindingPath.getBindingVariable().getVariableName(), PatternStatement.this));
								bindingPath.setBindingVariable(objectBV);
								bindingPath.setBindingPath(bp2);
							}
							return bindingPath;
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
