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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.ExpressionEvaluator;
import org.openflexo.connie.java.expr.JavaExpressionEvaluator;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.factory.PamelaModel;

/**
 * Abstract base class for an instance of a {@link PatternDefinition}<br>
 *
 * @author Caine Silva, Sylvain Guerin
 *
 * @param <P>
 *            Type of {@link PatternDefinition} this instance is an instance of
 */
public abstract class PatternInstance<P extends PatternDefinition> implements BindingEvaluationContext {

	private P patternDefinition;
	private PamelaModel model;

	private List<PatternInstanceEvent> events = new ArrayList<>();

	public PatternInstance(P patternDefinition, PamelaModel model) {
		this.patternDefinition = patternDefinition;
		this.model = model;
		model.registerPatternInstance(this);
	}

	public P getPatternDefinition() {
		return patternDefinition;
	}

	public PamelaMetaModel getMetaModel() {
		return patternDefinition.getMetaModel();
	}

	public PamelaModel getModel() {
		return model;
	}

	protected void registerStakeHolder(Object stakeHolder, String role) {
		getModel().registerStakeHolderForPatternInstance(stakeHolder, role, this);
	}

	public abstract ReturnWrapper processMethodBeforeInvoke(Object instance, Method method, Object[] args)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException;

	public abstract void processMethodAfterInvoke(Object instance, Method method, Object returnValue, Object[] args)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException;

	/**
	 * Called to process related precondition before invoking supplied method<br>
	 * 
	 * @param precondition
	 * @param method
	 */
	public void invokePrecondition(PatternPrecondition precondition) throws PreconditionViolationException {
		System.out.println("Invoking precondition " + precondition);

		if (precondition.getAssertion().isValid()) {
			try {
				boolean assertResult = precondition.getAssertion().getBindingValue(this);
				if (!assertResult) {
					throw new PreconditionViolationException(precondition.getAnnotation());
				}
			} catch (TypeMismatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullReferenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReflectiveOperationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			System.err.println("Cannot evaluate assertion: " + precondition.getAssertionAsString());
		}

	}

	/**
	 * Called to process related postcondition after invoking supplied method<br>
	 * 
	 * @param postcondition
	 * @param method
	 */
	public void invokePostcondition(PatternPostcondition postcondition) throws PostconditionViolationException {
		System.out.println("Invoking postcondition " + postcondition);

		if (postcondition.getAssertion().isValid()) {
			try {
				boolean assertResult = postcondition.getAssertion().getBindingValue(this);
				if (!assertResult) {
					throw new PostconditionViolationException(postcondition.getAnnotation());
				}
			} catch (TypeMismatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullReferenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReflectiveOperationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			System.err.println("Cannot evaluate assertion: " + postcondition.getAssertionAsString());
		}
	}

	public void invokeExceptionHandler(PatternExceptionHandler exceptionHandler) throws Throwable {
		System.out.println("Invoking exceptionHandler " + exceptionHandler);

		if (exceptionHandler.getExecutionStatement().isValid()) {
			try {
				exceptionHandler.getExecutionStatement().getBindingValue(this);
			} catch (TypeMismatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullReferenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
				throw e.getCause();
			}
		}
		else {
			System.err.println("Cannot execute statement: " + exceptionHandler.getExecutionStatementAsString() + " reason: "
					+ exceptionHandler.getExecutionStatement().invalidBindingReason());
		}
	}

	@Override
	public ExpressionEvaluator getEvaluator() {
		return new JavaExpressionEvaluator(this);
	}

	@Override
	public Object getValue(BindingVariable bindingVariable) {
		if (bindingVariable.getVariableName().equals(PatternAssertion.DEFAULT_VARIABLE_NAME)) {
			return this;
		}
		return null;
	}

	public void triggerEvent(PatternInstanceEvent event) {
		events.add(event);
	}

	public List<PatternInstanceEvent> getEvents() {
		return events;
	}

	public <E extends PatternInstanceEvent> List<E> getEvents(Class<E> eventClass) {
		List<E> returned = new ArrayList<>();
		for (PatternInstanceEvent event : events) {
			if (eventClass.isAssignableFrom(event.getClass())) {
				returned.add((E) event);
			}
		}
		return returned;
	}
}
