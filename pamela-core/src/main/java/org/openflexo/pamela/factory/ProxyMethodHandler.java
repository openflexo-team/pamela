/**
 *
 */
/**
 *
 * Copyright (c) 2013-2015, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 *
 * This file is part of Pamela-core, a component of the software infrastructure 
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

package org.openflexo.pamela.factory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.annotation.Nonnull;

import org.openflexo.connie.BindingEvaluator;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.kvc.InvalidKeyValuePropertyException;
import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.ModelEntity;
import org.openflexo.pamela.ModelProperty;
import org.openflexo.pamela.PamelaUtils;
import org.openflexo.pamela.StringEncoder;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.ComplexEmbedded;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Finder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.annotations.PastingPoint;
import org.openflexo.pamela.annotations.Reindexer;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.exceptions.InvalidDataException;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.exceptions.NoSuchEntityException;
import org.openflexo.pamela.exceptions.UnitializedEntityException;
import org.openflexo.pamela.factory.ModelFactory.PAMELAProxyFactory;
import org.openflexo.pamela.factory.PAMELAVisitor.VisitingStrategy;
import org.openflexo.pamela.jml.JMLEnsures;
import org.openflexo.pamela.jml.JMLMethodDefinition;
import org.openflexo.pamela.jml.JMLRequires;
import org.openflexo.pamela.jml.SpecificationsViolationException;
import org.openflexo.pamela.patterns.ExecutionMonitor;
import org.openflexo.pamela.patterns.PatternInstance;
import org.openflexo.pamela.patterns.ReturnWrapper;
import org.openflexo.pamela.undo.AddCommand;
import org.openflexo.pamela.undo.CreateCommand;
import org.openflexo.pamela.undo.DeleteCommand;
import org.openflexo.pamela.undo.RemoveCommand;
import org.openflexo.pamela.undo.SetCommand;
import org.openflexo.pamela.undo.UndoManager;
import org.openflexo.toolbox.HasPropertyChangeSupport;

import com.google.common.base.Defaults;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;

/**
 * Invocation Handler in the core of PAMELA
 * 
 * This is the class where method call dispatching is performed.
 * 
 * @author sylvain
 *
 * @param <I>
 *            type of object this invocation handler manages
 */
public class ProxyMethodHandler<I> extends IProxyMethodHandler implements MethodHandler, PropertyChangeListener {

	/**
	 * Object this invocation handler manages
	 */
	private I object;

	/**
	 * This map contains all scheduled set for a given property<br>
	 * We need to retain values beeing set in case of bidirectional inverse properties patterns, to avoid infinite loop
	 */
	private final Map<ModelProperty<? super I>, Object> scheduledSets = new HashMap<>();

	private boolean destroyed = false;
	private boolean deleted = false;
	protected boolean deleting = false;
	protected boolean undeleting = false;
	protected boolean initialized = false;
	private boolean serializing = false;
	private boolean deserializing = false;
	protected boolean createdByCloning = false;
	private boolean beingCloned = false;
	private boolean modified = false;
	private PropertyChangeSupport propertyChangeSupport;
	protected boolean initializing;

	private Map<String, PropertyImplementation<? super I, ?>> propertyImplementations;

	private List<DelegateImplementation<? super I>> delegateImplementations;

	private final PAMELAProxyFactory<I> pamelaProxyFactory;
	private final EditingContext editingContext;

	private Stack<Method> assertionCheckingStack = new Stack<>();
	private Map<Method, Map<String, Object>> historyValues;

	public ProxyMethodHandler(PAMELAProxyFactory<I> pamelaProxyFactory, EditingContext editingContext) throws ModelDefinitionException {
		this.pamelaProxyFactory = pamelaProxyFactory;
		this.editingContext = editingContext;
		// values = new HashMap<>(getModelEntity().getPropertiesSize(), 1.0f);
		historyValues = new HashMap<>();
		propertyImplementations = new HashMap<>(getModelEntity().getPropertiesSize(), 1.0f);
		initialized = !getModelEntity().hasInitializers();
		initDelegateImplementations();
	}

	private void initDelegateImplementations() throws ModelDefinitionException {
		// System.out.println("***** init delegate implementations");
		delegateImplementations = new ArrayList<>();
		initDelegateImplementations(getModelEntity());
	}

	private void initDelegateImplementations(ModelEntity<? super I> entity) throws ModelDefinitionException {
		if (entity.getDelegateImplementations().size() > 0) {
			// System.out.println("***** init delegate implementations for " + entity.getImplementedInterface());
			for (Class<? super I> delegateImplementationClass : entity.getDelegateImplementations().keySet()) {
				try {
					DelegateImplementation<? super I> delegateImplementation = new DelegateImplementation(this, delegateImplementationClass,
							entity.getDelegateImplementations().get(delegateImplementationClass));
					delegateImplementations.add(delegateImplementation);
				} catch (Exception e) {
					e.printStackTrace();
					throw new ModelDefinitionException(e.getMessage());
				}
			}
		}
		if (entity.getDirectSuperEntities() != null) {
			for (ModelEntity<? super I> superEntity : entity.getDirectSuperEntities()) {
				initDelegateImplementations(superEntity);
			}
		}

	}

	public I getObject() {
		return object;
	}

	public void setObject(I object) {
		this.object = object;
	}

	public UndoManager getUndoManager() {
		if (getEditingContext() != null) {
			return getEditingContext().getUndoManager();
		}
		return null;
	}

	public EditingContext getEditingContext() {
		return editingContext;
	}

	public ModelFactory getModelFactory() {
		return pamelaProxyFactory.getModelFactory();
	}

	final public ModelEntity<I> getModelEntity() {
		return pamelaProxyFactory.getModelEntity();
	}

	public PAMELAProxyFactory<I> getPamelaProxyFactory() {
		return pamelaProxyFactory;
	}

	public Class<?> getSuperClass() {
		return pamelaProxyFactory.getSuperclass();
	}

	public Class<?> getOverridingSuperClass() {
		return pamelaProxyFactory.getOverridingSuperClass();
	}

	private ModelContext getModelContext() {
		return getModelFactory().getModelContext();
	}

	@Override
	public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable {
		boolean assertionChecking = false;
		boolean keepGoing = true;
		Object invoke = null;

		if (enableAssertionChecking) {
			assertionChecking = checkOnEntry(method, args);
		}

		for (ExecutionMonitor monitor : getModelFactory().getModelContext().getExecutionMonitors()) {
			monitor.enteringMethod(self, method, args);
		}

		Set<PatternInstance<?>> patternInstances = getModelFactory().getModelContext().getPatternInstances(self);
		if (patternInstances != null) {
			for (PatternInstance<?> patternInstance : patternInstances) {
				try {
					ReturnWrapper returnWrapper = patternInstance.processMethodBeforeInvoke(self, method, args);
					if (returnWrapper != null && !returnWrapper.mustContinue()) {
						keepGoing = false;
						invoke = returnWrapper.getReturnValue();
					}
				} catch (InvocationTargetException e) {
					e.getTargetException().printStackTrace();
					for (ExecutionMonitor monitor : getModelFactory().getModelContext().getExecutionMonitors()) {
						monitor.throwingException(self, method, args, e);
					}
					throw e.getTargetException();
				}
			}
		}

		/*ArrayList<PatternClassWrapper> patternsOfInterest = patternContext.getRelatedPatternsFromInstance(self);
		for (PatternClassWrapper wrapper : patternsOfInterest) {
			ReturnWrapper returnWrapper = wrapper.getPattern().processMethodBeforeInvoke(self, method, wrapper.getKlass(), args);
			if (!returnWrapper.mustContinue()) {
				keepGoing = false;
				invoke = returnWrapper.getReturnValue();
			}
		}*/

		if (keepGoing) {
			invoke = _invoke(self, method, proceed, args);
			if (method.getReturnType().isPrimitive() && invoke == null) {
				// Avoids an NPE
				invoke = Defaults.defaultValue(method.getReturnType());
			}
		}

		if (patternInstances != null) {
			for (PatternInstance<?> patternInstance : patternInstances) {
				try {
					patternInstance.processMethodAfterInvoke(self, method, invoke, args);
				} catch (InvocationTargetException e) {
					e.getTargetException().printStackTrace();
					for (ExecutionMonitor monitor : getModelFactory().getModelContext().getExecutionMonitors()) {
						monitor.throwingException(self, method, args, e);
					}
					throw e.getTargetException();
				}
			}
		}

		for (ExecutionMonitor monitor : getModelFactory().getModelContext().getExecutionMonitors()) {
			monitor.leavingMethod(self, method, args, invoke);
		}

		/*for (PatternClassWrapper wrapper : patternsOfInterest) {
			wrapper.getPattern().processMethodAfterInvoke(self, method, wrapper.getKlass(), invoke, args);
		}*/

		if (enableAssertionChecking && assertionChecking) {
			checkOnExit(method, args);
		}

		return invoke;
	}

	private Object _invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable {

		// System.out.println("_invoke " + method);

		// First, we iterate on all delegate implementations to look for eventual partial implementation (in this case, prioritar)
		for (DelegateImplementation<? super I> delegateImplementation : delegateImplementations) {
			if (delegateImplementation.handleMethod(method)) {
				// This delegate provides an implementation of that method, use it
				// System.out.println("Delegating implementation of method=" + method + " to delegate "
				// + delegateImplementation.getDelegateImplementationClass());
				return delegateImplementation.invoke(self, method, proceed, args);
			}
		}

		if (proceed != null) {
			ModelProperty<? super I> property = getModelEntity().getPropertyForMethod(method);
			boolean callSetModifiedAtTheEnd = false;
			if (property != null) {
				if (PamelaUtils.methodIsEquivalentTo(method, property.getSetterMethod())) {
					// We have found a concrete implementation of that method as a setter call
					// We will invoke it, but also notify UndoManager, and call setModified() after setter invoking
					// System.out.println("DETECTS SET with " + proceed + " instead of " + method);
					Object oldValue = invokeGetter(property);
					if (getUndoManager() != null) {
						if (oldValue != args[0]) {
							getUndoManager().addEdit(
									new SetCommand<>(getObject(), getModelEntity(), property, oldValue, args[0], getModelFactory()));
						}
					}
					if (property.isSerializable()) {
						callSetModifiedAtTheEnd = true;
					}
				}
				if (PamelaUtils.methodIsEquivalentTo(method, property.getAdderMethod())) {
					// We have found a concrete implementation of that method as a adder call
					// We will invoke it, but also notify UndoManager, and call setModified() after adder invoking
					// System.out.println("DETECTS ADD with " + proceed + " instead of " + method);
					if (getUndoManager() != null) {
						getUndoManager().addEdit(new AddCommand<>(getObject(), getModelEntity(), property, args[0], getModelFactory()));
					}
					if (property.isSerializable()) {
						callSetModifiedAtTheEnd = true;
					}
				}
				if (PamelaUtils.methodIsEquivalentTo(method, property.getRemoverMethod())) {
					// We have found a concrete implementation of that method as a remover call
					// We will invoke it, but also notify UndoManager, and call setModified() after remover invoking
					// System.out.println("DETECTS REMOVE with " + proceed + " instead of " + method);
					if (getUndoManager() != null) {
						getUndoManager().addEdit(new RemoveCommand<>(getObject(), getModelEntity(), property, args[0], getModelFactory()));
					}
					if (property.isSerializable()) {
						callSetModifiedAtTheEnd = true;
					}
				}
			}
			try {
				// Now we invoke the found concrete implementation
				Object returned = proceed.invoke(self, args);
				// Then we call setModified() if required
				if (callSetModifiedAtTheEnd) {
					invokeSetModified(true);
				}
				return returned;
			} catch (InvocationTargetException e) {
				// An exception has been thrown
				// There are two cases here:
				// - this exception was expected (part of business model)
				// - this exception is really unexpected
				// To see it, we iterate on all exceptions that are declared as throwable, and throw target exception when matching
				for (Class<?> exceptionType : proceed.getExceptionTypes()) {
					if (exceptionType.isAssignableFrom(e.getTargetException().getClass())) {
						throw e.getTargetException();
					}
				}
				// Also throw SpecificationsViolationException
				if (SpecificationsViolationException.class.isAssignableFrom(e.getTargetException().getClass())) {
					throw e.getTargetException();
				}
				// If we come here, this means that this exception was unexpected
				// In this case, we wrap this exception in a ModelExecutionException and we throw it
				e.printStackTrace();
				throw new ModelExecutionException(e.getCause());
			}
		}
		// System.out.println("Invoke " + method);
		Initializer initializer = method.getAnnotation(Initializer.class);
		if (initializer != null) {
			internallyInvokeInitializer(getModelEntity().getInitializers(method), args);
			return self;
		}
		if (!initialized && !initializing) {
			throw new UnitializedEntityException(getModelEntity());
		}
		Getter getter = method.getAnnotation(Getter.class);
		if (getter != null) {
			String id = getter.value();
			Object returned = internallyInvokeGetter(id);
			return returned;
		}

		Setter setter = method.getAnnotation(Setter.class);
		if (setter != null) {
			String id = setter.value();
			internallyInvokeSetter(id, args[0], true);
			return null;
		}

		Adder adder = method.getAnnotation(Adder.class);
		if (adder != null) {
			String id = adder.value();
			internallyInvokeAdder(id, args[0], true);
			return null;
		}

		Remover remover = method.getAnnotation(Remover.class);
		if (remover != null) {
			String id = remover.value();
			internallyInvokeRemover(id, args[0], true);
			return null;
		}

		Reindexer reindexer = method.getAnnotation(Reindexer.class);
		if (reindexer != null) {
			String id = reindexer.value();
			internallyInvokeReindexer(id, args[0], (int) args[1], true);
			return null;
		}

		Finder finder = method.getAnnotation(Finder.class);
		if (finder != null) {
			return internallyInvokeFinder(finder, args);
		}

		if (PamelaUtils.methodIsEquivalentTo(method, GET_PROPERTY_CHANGE_SUPPORT)) {
			return getPropertyChangeSuppport();
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_GETTER)) {
			return internallyInvokeGetter((String) args[0] /*getModelEntity().getModelProperty((String) args[0])*/);
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_SETTER)) {
			internallyInvokeSetter((String) args[0] /*getModelEntity().getModelProperty((String) args[0])*/, args[1], false);
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_ADDER)) {
			internallyInvokeAdder((String) args[0] /*getModelEntity().getModelProperty((String) args[0])*/, args[1], false);
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_ADDER_AT_INDEX)) {
			internallyInvokeAdder((String) args[0] /*getModelEntity().getModelProperty((String) args[0])*/, args[1], (int) args[2], false);
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_REMOVER)) {
			internallyInvokeRemover((String) args[0] /*getModelEntity().getModelProperty((String) args[0])*/, args[1], false);
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_FINDER)) {
			internallyInvokeFinder(finder, args);
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_GETTER_ENTITY)) {
			ModelEntity<? super I> e = getModelEntityFromArg((Class<?>) args[1]);
			return internallyInvokeGetter((String) args[0]/*e.getModelProperty((String) args[0])*/);
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_SETTER_ENTITY)) {
			ModelEntity<? super I> e = getModelEntityFromArg((Class<?>) args[2]);
			internallyInvokeSetter((String) args[0] /*e.getModelProperty((String) args[0])*/, args[1], false);
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_ADDER_ENTITY)) {
			ModelEntity<? super I> e = getModelEntityFromArg((Class<?>) args[2]);
			internallyInvokeAdder((String) args[0] /*e.getModelProperty((String) args[0])*/, args[1], false);
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_REMOVER_ENTITY)) {
			ModelEntity<? super I> e = getModelEntityFromArg((Class<?>) args[2]);
			internallyInvokeRemover(e.getModelProperty((String) args[0]), args[1], false);
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_DELETER_ENTITY)) {
			return internallyInvokeDeleter(true);
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_FINDER_ENTITY)) {
			Class<?> class1 = (Class<?>) args[2];
			ModelEntity<? super I> e = getModelEntityFromArg(class1);
			finder = e.getFinder((String) args[0]);
			if (finder != null) {
				return internallyInvokeFinder(finder, args);
			}
			else {
				throw new ModelExecutionException(
						"No such finder defined. Finder '" + args[0] + "' could not be found on entity " + class1.getName());
			}
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_FINDER)) {
			finder = getModelEntity().getFinder((String) args[0]);
			if (finder != null) {
				return internallyInvokeFinder(finder, args);
			}
			else {
				throw new ModelExecutionException("No such finder defined. Finder '" + args[0] + "' could not be found on entity "
						+ getModelEntity().getImplementedInterface().getName());
			}
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, IS_SERIALIZING)) {
			return isSerializing();
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, IS_DESERIALIZING)) {
			return isDeserializing();
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, IS_MODIFIED)) {
			return isModified();
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, SET_MODIFIED)
				|| PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_SET_MODIFIED)) {
			internallyInvokeSetModified((Boolean) args[0]);
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, TO_STRING)) {
			return internallyInvokeToString();
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, DESTROY)) {
			destroy();
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, CLONE_OBJECT)) {
			return cloneObject();
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, EQUALS_OBJECT)) {
			return equalsObject(args[0]);
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, UPDATE_WITH_OBJECT)) {
			return updateWith((I) args[0]);
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, GET_EMBEDDED)) {
			return getDirectEmbeddedObjects();
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, GET_REFERENCED)) {
			return getReferencedObjects();
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, ACCEPT_VISITOR)) {
			return acceptVisitor((PAMELAVisitor) args[0]);
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, ACCEPT_WITH_STRATEGY_VISITOR)) {
			return acceptVisitor((PAMELAVisitor) args[0], (VisitingStrategy) args[1]);
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, IS_DELETED)) {
			return deleted;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, IS_BEING_CLONED)) {
			return beingCloned;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, IS_CREATED_BY_CLONING)) {
			return createdByCloning;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, GET_DELETED_PROPERTY)) {
			return DELETED;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_DELETER)) {
			return internallyInvokeDeleter(false);
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, DELETE_OBJECT)) {
			return internallyInvokeDeleter(true, args);
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_UNDELETER)) {
			return internallyInvokeUndeleter((Boolean) args[0], false);
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, UNDELETE_OBJECT)) {
			return internallyInvokeUndeleter((Boolean) args[0], true);
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, CLONE_OBJECT_WITH_CONTEXT)) {
			return cloneObject(args);
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, HAS_KEY)) {
			ModelProperty<? super I> property = getModelEntity().getModelProperty((String) args[0]);
			return (property != null);
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, OBJECT_FOR_KEY)) {
			ModelProperty<? super I> property = getModelEntity().getModelProperty((String) args[0]);
			if (property != null) {
				return invokeGetter(property);
			}
			else {
				System.err.println("Cannot handle property " + args[0] + " for " + getObject());
				return null;
			}
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, SET_OBJECT_FOR_KEY)) {
			ModelProperty<? super I> property = getModelEntity().getModelProperty((String) args[1]);
			if (property != null) {
				Object newValue = args[0];
				/*Object oldValue = invokeGetter(property);
				if (getModelFactory().getUndoManager() != null) {
					getModelFactory().getUndoManager().addEdit(
							new SetCommand<>(getObject(), getModelEntity(), property, oldValue, newValue, getModelFactory()));
				}*/
				invokeSetter(property, newValue);
				return null;
			}
			else {
				System.err.println("Cannot handle property " + args[0] + " for " + getObject());
				return null;
			}
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, GET_TYPE_FOR_KEY)) {
			ModelProperty<? super I> property = getModelEntity().getModelProperty((String) args[0]);
			if (property != null) {
				return property.getType();
			}
			else {
				System.err.println("Cannot handle property " + args[0] + " for " + getObject());
				return null;
			}
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, ENABLE_ASSERTION_CHECKING)) {
			invokeEnableAssertionChecking();
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, DISABLE_ASSERTION_CHECKING)) {
			invokeDisableAssertionChecking();
			return null;
		}
		ModelProperty<? super I> property = getModelEntity().getPropertyForMethod(method);
		if (property != null) {
			if (PamelaUtils.methodIsEquivalentTo(method, property.getGetterMethod())) {
				return internallyInvokeGetter(property);
			}
			else if (PamelaUtils.methodIsEquivalentTo(method, property.getSetterMethod())) {
				internallyInvokeSetter(property, args[0], true);
				return null;
			}
			else if (PamelaUtils.methodIsEquivalentTo(method, property.getAdderMethod())) {
				internallyInvokeAdder(property, args[0], true);
				return null;
			}
			else if (PamelaUtils.methodIsEquivalentTo(method, property.getRemoverMethod())) {
				internallyInvokeRemover(property, args[0], true);
				return null;
			}

		}
		System.err.println("Cannot handle method " + method + " for " + getObject().getClass() + ". Dumping stack for analysis.");
		Thread.dumpStack();
		return null;
	}

	private PropertyChangeSupport getPropertyChangeSuppport() {
		if (propertyChangeSupport == null) {
			propertyChangeSupport = new PropertyChangeSupport(getObject());
		}
		return propertyChangeSupport;
	}

	private void internallyInvokeInitializer(org.openflexo.pamela.ModelInitializer in, Object[] args) throws ModelDefinitionException {
		initializing = true;
		try {
			List<String> parameters = in.getParameters();
			for (int i = 0; i < parameters.size(); i++) {
				String parameter = parameters.get(i);
				if (parameter != null) {
					internallyInvokeSetter(getModelEntity().getModelProperty(parameter), args[i], true);
				}
			}
		} finally {
			initialized = true;
			initializing = false;
		}
	}

	private @Nonnull ModelEntity<? super I> getModelEntityFromArg(Class<?> class1) throws ModelDefinitionException {
		ModelEntity<?> e = getModelContext().getModelEntity(class1);
		if (e == null) {
			throw new NoSuchEntityException(class1);
		}
		if (!e.isAncestorOf(getModelEntity())) {
			throw new ModelExecutionException(
					((Class<?>) class1).getName() + " is not a super interface of " + getModelEntity().getImplementedInterface().getName());
		}
		// Is e is an ancestor of modelEntity, this means that e is a super interface of the implementedInterface of modelEntity and we can
		// therefore cast e to ModelEntity<? super I>
		return (ModelEntity<? super I>) e;
	}

	private PropertyImplementation<? super I, ?> getPropertyImplementation(ModelProperty<? super I> property)
			throws ModelExecutionException {
		PropertyImplementation<? super I, ?> returned = propertyImplementations.get(property.getPropertyIdentifier());
		if (returned == null) {
			Class<? extends PropertyImplementation<? super I, ?>> implementationClass = null;
			try {
				if (property.getPropertyImplementation() != null) {
					implementationClass = (Class<? extends PropertyImplementation<? super I, ?>>) property.getPropertyImplementation()
							.value();
					Constructor<? extends PropertyImplementation<? super I, ?>> constructor = implementationClass
							.getConstructor(ProxyMethodHandler.class, ModelProperty.class);
					returned = constructor.newInstance(this, property);
				}
				else {
					if (property.getCardinality() == Cardinality.SINGLE) {
						implementationClass = (Class) DefaultSinglePropertyImplementation.class;
						Constructor<? extends PropertyImplementation<? super I, ?>> constructor = implementationClass
								.getConstructor(ProxyMethodHandler.class, ModelProperty.class);
						returned = constructor.newInstance(this, property);
					}
					else if (property.getCardinality() == Cardinality.LIST) {
						implementationClass = (Class) DefaultMultiplePropertyImplementation.class;
						Constructor<? extends PropertyImplementation<? super I, ?>> constructor = implementationClass
								.getConstructor(ProxyMethodHandler.class, ModelProperty.class, Class.class);
						returned = constructor.newInstance(this, property, getModelFactory().getListImplementationClass());
					}
					else {
						throw new ModelExecutionException("Unexpected cardinality for property " + property);
					}
				}
				propertyImplementations.put(property.getPropertyIdentifier(), returned);
				return returned;

			} catch (Exception e) {
				throw new ModelExecutionException(e);
			}
		}
		return returned;
	}

	protected Object internallyInvokeGetter(String propertyIdentifier) throws ModelDefinitionException {
		ModelProperty<? super I> property = getModelEntity().getModelProperty(propertyIdentifier);
		return internallyInvokeGetter(property);
	}

	protected Object internallyInvokeGetter(ModelProperty<? super I> property) throws ModelDefinitionException {
		PropertyImplementation<? super I, ?> propertyImplementation = getPropertyImplementation(property);
		return internallyInvokeGetter(property, propertyImplementation);
	}

	protected void internallyInvokeSetter(String propertyIdentifier, Object value, boolean trackAtomicEdit)
			throws ModelDefinitionException {
		ModelProperty<? super I> property = getModelEntity().getModelProperty(propertyIdentifier);
		internallyInvokeSetter(property, value, trackAtomicEdit);
	}

	protected void internallyInvokeSetter(ModelProperty<? super I> property, Object value, boolean trackAtomicEdit)
			throws ModelDefinitionException {
		PropertyImplementation<? super I, ?> propertyImplementation = getPropertyImplementation(property);
		if (propertyImplementation instanceof SettablePropertyImplementation) {
			internallyInvokeSetter(property, (SettablePropertyImplementation) propertyImplementation, value, trackAtomicEdit);
		}
		else {
			throw new ModelDefinitionException(
					"Property implementation does not support SET protocol: " + property.getPropertyImplementation());
		}
	}

	protected void internallyInvokeAdder(String propertyIdentifier, Object addedValue, boolean trackAtomicEdit)
			throws ModelDefinitionException {
		internallyInvokeAdder(propertyIdentifier, addedValue, -1, trackAtomicEdit);
	}

	protected void internallyInvokeAdder(String propertyIdentifier, Object addedValue, int index, boolean trackAtomicEdit)
			throws ModelDefinitionException {
		ModelProperty<? super I> property = getModelEntity().getModelProperty(propertyIdentifier);
		internallyInvokeAdder(property, addedValue, index, trackAtomicEdit);
	}

	protected void internallyInvokeAdder(ModelProperty<? super I> property, Object addedValue, boolean trackAtomicEdit)
			throws ModelDefinitionException {
		internallyInvokeAdder(property, addedValue, -1, trackAtomicEdit);
	}

	protected void internallyInvokeAdder(ModelProperty<? super I> property, Object addedValue, int index, boolean trackAtomicEdit)
			throws ModelDefinitionException {
		PropertyImplementation<? super I, ?> propertyImplementation = getPropertyImplementation(property);
		if (propertyImplementation instanceof MultiplePropertyImplementation) {
			internallyInvokeAdder(property, (MultiplePropertyImplementation) propertyImplementation, addedValue, index, trackAtomicEdit);
		}
		else {
			throw new ModelDefinitionException(
					"Property implementation does not support ADD protocol: " + property.getPropertyImplementation());
		}
	}

	protected void internallyInvokeRemover(String propertyIdentifier, Object removedValue, boolean trackAtomicEdit)
			throws ModelDefinitionException {
		ModelProperty<? super I> property = getModelEntity().getModelProperty(propertyIdentifier);
		internallyInvokeRemover(property, removedValue, trackAtomicEdit);
	}

	protected void internallyInvokeRemover(ModelProperty<? super I> property, Object removedValue, boolean trackAtomicEdit)
			throws ModelDefinitionException {
		PropertyImplementation<? super I, ?> propertyImplementation = getPropertyImplementation(property);
		if (propertyImplementation instanceof MultiplePropertyImplementation) {
			internallyInvokeRemover(property, (MultiplePropertyImplementation) propertyImplementation, removedValue, trackAtomicEdit);
		}
		else {
			throw new ModelDefinitionException(
					"Property implementation does not support REMOVE protocol: " + property.getPropertyImplementation());
		}
	}

	protected void internallyInvokeReindexer(String propertyIdentifier, Object value, int index, boolean trackAtomicEdit)
			throws ModelDefinitionException {
		ModelProperty<? super I> property = getModelEntity().getModelProperty(propertyIdentifier);
		internallyInvokeReindexer(property, value, index, trackAtomicEdit);
	}

	protected void internallyInvokeReindexer(ModelProperty<? super I> property, Object value, int index, boolean trackAtomicEdit)
			throws ModelDefinitionException {
		PropertyImplementation<? super I, ?> propertyImplementation = getPropertyImplementation(property);
		if (propertyImplementation instanceof ReindexableListPropertyImplementation) {
			internallyInvokeReindexer(property, (ReindexableListPropertyImplementation) propertyImplementation, value, index,
					trackAtomicEdit);
		}
		else {
			throw new ModelDefinitionException(
					"Property implementation does not support REINDEX protocol: " + property.getPropertyImplementation());
		}
	}

	/**
	 * Deletes the current object and all its embedded properties as defined by the {@link Embedded} and {@link ComplexEmbedded}
	 * annotations. Moreover, the provided <code>context</code> represents a list of objects that will also be eventually deleted and which
	 * should be taken into account when computing embedded objects according to the deletion conditions. Invoking this method may result in
	 * deleting indirectly the objects provided by the <code>context</code>, however the invoker should make sure that they have been
	 * actually deleted.
	 *
	 * @param context
	 *            the list of objects that will also be deleted and which should be taken into account when computing embedded objects.
	 * @see Embedded#deletionConditions()
	 * @see ComplexEmbedded#deletionConditions()
	 */
	protected boolean internallyInvokeDeleter(boolean trackAtomicEdit, Object... context) throws ModelDefinitionException {

		// System.out.println("Called internallyInvokeDeleter() for " + getObject());

		if (deleted || deleting) {
			return false;
		}

		deleting = true;
		if (context == null) {
			context = new Object[] { getObject() };
		}
		else {
			context = Arrays.copyOf(context, context.length + 1);
			context[context.length - 1] = getObject();
		}
		// oldValues = new HashMap<>();
		ModelEntity<I> modelEntity = getModelEntity();
		Set<Object> objects = new HashSet<>();
		for (Object o : context) {
			objects.add(o);
		}
		List<Object> embeddedObjects = getModelFactory().getEmbeddedObjects(getObject(), EmbeddingType.DELETION,
				objects.toArray(new Object[objects.size()]));
		objects.addAll(embeddedObjects);

		context = objects.toArray(new Object[objects.size()]);

		// We iterate on all properties conform to PAMELA meta-model
		Iterator<ModelProperty<? super I>> i = modelEntity.getProperties();
		while (i.hasNext()) {
			ModelProperty<? super I> property = i.next();

			if (property.getType().isPrimitive()) {
				// Primitive do not need to be nullified
				// Do nothing
			}
			else {

				PropertyImplementation<? super I, ?> propertyImplementation = getPropertyImplementation(property);

				propertyImplementation.delete(embeddedObjects, context);

				/*
				// We retrieve and store old value for a potential undelete
				Object oldValue = invokeGetter(property);
				
				List<Object> oldValuesList = null;
				if (property.getCardinality() == Cardinality.LIST) {
					oldValuesList = new ArrayList<>((List) oldValue);
				}
				
				oldValues.put(property.getPropertyIdentifier(), oldValue);
				// Otherwise nullify using setter
				if (property.getSetterMethod() != null) {
					invokeSetter(property, null);
				}
				else {
					internallyInvokeSetter(property, null, true);
				}
				
				if (property.getCardinality() == Cardinality.SINGLE) {
					if ((oldValue instanceof DeletableProxyObject) && embeddedObjects.contains(oldValue)) {
						// By the way, this object was embedded, delete it
						((DeletableProxyObject) oldValue).delete(context);
						embeddedObjects.remove(oldValue);
					}
				}
				
				else if (property.getCardinality() == Cardinality.LIST) {
					if (oldValuesList != null) {
						for (Object toBeDeleted : oldValuesList) {
							if ((toBeDeleted instanceof DeletableProxyObject) && embeddedObjects.contains(toBeDeleted)) {
								// By the way, this object was embedded, delete it
								((DeletableProxyObject) toBeDeleted).delete(context);
								embeddedObjects.remove(toBeDeleted);
							}
						}
					}
				}
				 */
			}
		}

		// Are there still embedded objects not deleted ???
		for (Object object : embeddedObjects) {
			if (object instanceof DeletableProxyObject) {
				DeletableProxyObject objectToDelete = (DeletableProxyObject) object;
				if (!objectToDelete.isDeleted()) {
					objectToDelete.delete(context);
					System.err.println("This is weird: this object was embedded but not deleted: " + objectToDelete);
				}
			}
		}

		if (trackAtomicEdit && getUndoManager() != null) {
			getUndoManager().addEdit(new DeleteCommand<>(getObject(), getModelEntity(), getModelFactory()));
		}

		deleted = true;
		deleting = false;

		// System.out.println("DONE internallyInvokeDeleter() for " + getObject());

		// Notify object
		if (getObject() instanceof HasPropertyChangeSupport) {
			HasPropertyChangeSupport object = (HasPropertyChangeSupport) getObject();
			object.getPropertyChangeSupport().firePropertyChange(object.getDeletedProperty(), false, true);
		}

		// Also notify using core PropertyChangeSupport

		// TODO: maybe we have to check that is is not the same PropertyChangeSupport ???
		getPropertyChangeSuppport().firePropertyChange(DELETED, false, true);

		// TODO ASK Syl if we should not remove all the listeners from pcSupport here?!?
		// Did it by default
		for (PropertyChangeListener cl : propertyChangeSupport.getPropertyChangeListeners()) {
			// TODO => notify the listener when it forgot to stop listening
			propertyChangeSupport.removePropertyChangeListener(cl);
		}

		propertyChangeSupport = null;

		return deleted;
	}

	protected boolean internallyInvokeUndeleter(boolean restoreProperties, boolean trackAtomicEdit) throws ModelDefinitionException {

		if (!deleted || deleting) {
			return false;
		}

		undeleting = true;

		if (trackAtomicEdit && getUndoManager() != null) {
			getUndoManager().addEdit(new CreateCommand<>(getObject(), getModelEntity(), getModelFactory()));
		}

		if (restoreProperties) {
			ModelEntity<I> modelEntity = getModelEntity();
			Iterator<ModelProperty<? super I>> i = modelEntity.getProperties();
			while (i.hasNext()) {
				ModelProperty<? super I> property = i.next();
				if (property.getType().isPrimitive()) {
					// No need to restore for primitives
					// Do nothing
				}
				else {

					PropertyImplementation<? super I, ?> propertyImplementation = getPropertyImplementation(property);
					propertyImplementation.undelete();

					// Otherwise nullify using setter
					/*if (property.getSetterMethod() != null) {
						invokeSetter(property, oldValues.get(property.getPropertyIdentifier()));
					}
					else {
						internallyInvokeSetter(property, oldValues.get(property.getPropertyIdentifier()), true);
					}*/
				}
			}
		}

		deleted = false;
		undeleting = false;
		getPropertyChangeSuppport().firePropertyChange(UNDELETED, false, true);
		return deleted;
	}

	/**
	 * Destroy current object<br>
	 * After invoking this, the object won't be accessible and all operation performed on this will be in undetermined state.<br>
	 * To implements deleting/undeleting facilities, use {@link DeletableProxyObject} interface instead
	 */
	public void destroy() {
		/*if (values != null) {
			values.clear();
		}
		values = null;
		if (oldValues != null) {
			oldValues.clear();
		}
		oldValues = null;*/

		if (propertyImplementations != null) {
			propertyImplementations.clear();
		}
		propertyImplementations = null;

		destroyed = true;
	}

	public boolean isDestroyed() {
		return destroyed;
	}

	public Object invokeGetter(ModelProperty<? super I> property) {
		try {
			return property.getGetterMethod().invoke(getObject(), (Object[]) null);
		} catch (IllegalArgumentException e) {
			throw new ModelExecutionException(e);
		} catch (IllegalAccessException e) {
			throw new ModelExecutionException(e);
		} catch (InvocationTargetException e) {
			throw new ModelExecutionException(e);
		}
	}

	public void invokeSetter(ModelProperty<? super I> property, Object value) {

		if (property.getSetterMethod() == null) {
			System.err.println("Inconsistent data: cannot find setter for " + property);
			return;
		}

		try {
			property.getSetterMethod().invoke(getObject(), value);
		} catch (IllegalArgumentException e) {
			throw new ModelExecutionException(e);
		} catch (IllegalAccessException e) {
			throw new ModelExecutionException(e);
		} catch (InvocationTargetException e) {
			throw new ModelExecutionException(e);
		}
	}

	public void invokeUpdater(ModelProperty<? super I> property, Object value) {

		if (property.getUpdaterMethod() == null) {
			System.err.println("Inconsistent data: cannot find updater for " + property);
			return;
		}

		try {
			property.getUpdaterMethod().invoke(getObject(), value);
		} catch (IllegalArgumentException e) {
			throw new ModelExecutionException(e);
		} catch (IllegalAccessException e) {
			throw new ModelExecutionException(e);
		} catch (InvocationTargetException e) {
			throw new ModelExecutionException(e);
		}
	}

	public void invokeAdder(ModelProperty<? super I> property, Object value) {
		try {
			property.getAdderMethod().invoke(getObject(), value);
		} catch (IllegalArgumentException e) {
			throw new ModelExecutionException("Illegal argument exception adder:" + property.getAdderMethod() + " value=" + value, e);
		} catch (IllegalAccessException e) {
			throw new ModelExecutionException(e);
		} catch (InvocationTargetException e) {
			throw new ModelExecutionException(e);
		}
	}

	public void invokeRemover(ModelProperty<? super I> property, Object value) {
		try {
			property.getRemoverMethod().invoke(getObject(), value);
		} catch (IllegalArgumentException e) {
			throw new ModelExecutionException(e);
		} catch (IllegalAccessException e) {
			throw new ModelExecutionException(e);
		} catch (InvocationTargetException e) {
			throw new ModelExecutionException(e);
		}
	}

	public void invokeReindexer(ModelProperty<? super I> property, Object value, int index) {
		try {
			if (property.getReindexerMethod() != null) {
				property.getReindexerMethod().invoke(getObject(), value, index);
			}
			else {
				internallyInvokeReindexer(property, value, index, true);
			}
		} catch (IllegalArgumentException e) {
			throw new ModelExecutionException(e);
		} catch (IllegalAccessException e) {
			throw new ModelExecutionException(e);
		} catch (InvocationTargetException e) {
			throw new ModelExecutionException(e);
		} catch (ModelDefinitionException e) {
			throw new ModelExecutionException(e);
		}
	}

	public void invokeDeleter(Object... context) {
		// TODO manage with deleter
		if (getObject() instanceof DeletableProxyObject) {
			((DeletableProxyObject) getObject()).delete(context);
		}
	}

	public void invokeUndeleter(boolean restoreProperties) {
		// TODO manage with deleter
		if (getObject() instanceof DeletableProxyObject) {
			((DeletableProxyObject) getObject()).undelete(restoreProperties);
		}
	}

	public void invokeDestroy() {
		// TODO manage with deleter
		if (getObject() instanceof AccessibleProxyObject) {
			((AccessibleProxyObject) getObject()).destroy();
		}
	}

	public Object invokeGetter(String propertyIdentifier) throws ModelDefinitionException {
		return invokeGetter(getModelEntity().getModelProperty(propertyIdentifier));
	}

	public void invokeSetter(String propertyIdentifier, Object value) throws ModelDefinitionException {
		invokeSetter(getModelEntity().getModelProperty(propertyIdentifier), value);
	}

	public void invokeAdder(String propertyIdentifier, Object value) throws ModelDefinitionException {
		invokeAdder(getModelEntity().getModelProperty(propertyIdentifier), value);
	}

	public void invokeRemover(String propertyIdentifier, Object value) throws ModelDefinitionException {
		invokeRemover(getModelEntity().getModelProperty(propertyIdentifier), value);
	}

	public void invokeReindexer(String propertyIdentifier, Object value, int index) throws ModelDefinitionException {
		invokeReindexer(getModelEntity().getModelProperty(propertyIdentifier), value, index);
	}

	// TODO: why do we need this ?
	public void invokeSetterForDeserialization(ModelProperty<? super I> property, Object value) throws ModelDefinitionException {
		if (property.getSetterMethod() != null) {
			invokeSetter(property, value);
		}
		else {
			internallyInvokeSetter(property, value, true);
		}
	}

	protected Map<ModelProperty<? super I>, Object> getScheduledSets() {
		return scheduledSets;
	}

	@Deprecated
	private void firePropertyChange(String propertyIdentifier, Object oldValue, Object value) {
		if (getObject() instanceof HasPropertyChangeSupport && !deleting) {
			PropertyChangeSupport propertyChangeSupport = ((HasPropertyChangeSupport) getObject()).getPropertyChangeSupport();
			if (propertyChangeSupport != null) {
				propertyChangeSupport.firePropertyChange(propertyIdentifier, oldValue, value);
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		try {
			if (getModelEntity().getModify() != null && getModelEntity().getModify().synchWithForward()) {
				Object forwarded = internallyInvokeGetter(getModelEntity().getModify().forward());
				if (evt.getSource() == forwarded) {
					if (MODIFIED.equals(evt.getPropertyName())) {
						invokeSetModified((Boolean) evt.getNewValue());
					}
				}
			}
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	protected void invokeSetModified(boolean modified) throws ModelDefinitionException {
		if (getObject() instanceof AccessibleProxyObject) {
			((AccessibleProxyObject) getObject()).setModified(modified);
		}
		else {
			internallyInvokeSetModified(modified);
		}
	}

	public void invokeAdderForDeserialization(ModelProperty<? super I> property, Object value) throws ModelDefinitionException {
		if (property.getAdderMethod() != null) {
			invokeAdder(property, value);
		}
		else {
			internallyInvokeAdder(property, value, true);
		}
	}

	private Object internallyInvokeFinder(@Nonnull Finder finder, Object[] args) throws ModelDefinitionException {
		if (args.length == 0) {
			throw new ModelDefinitionException(
					"Finder " + finder.collection() + " by attribute " + finder.attribute() + " does not declare enough argument!");
		}
		String collectionID = finder.collection();
		ModelProperty<? super I> property = getModelEntity().getModelProperty(collectionID);
		Object collection = invokeGetter(property);
		if (collection == null) {
			return null;
		}
		Object value = args[0];
		String attribute = finder.attribute();
		if (collection instanceof Map<?, ?>) {
			collection = ((Map<?, ?>) collection).values();
		}
		if (collection instanceof Iterable) {
			if (finder.isMultiValued()) {
				List<Object> objects = new ArrayList<>();
				for (Object o : (Iterable<?>) collection) {
					if (isObjectAttributeEquals(o, attribute, value)) {
						objects.add(o);
					}
				}
				return objects;
			}
			else {
				// Prevent ConcurrentModificationException
				if (collection instanceof Collection) {
					for (Object o : new ArrayList<>((Collection<?>) collection)) {
						if (isObjectAttributeEquals(o, attribute, value)) {
							return o;
						}
					}
				}
				else {
					for (Object o : (Iterable<?>) collection) {
						if (isObjectAttributeEquals(o, attribute, value)) {
							return o;
						}
					}
				}
				return null;
			}
		}
		throw new ModelDefinitionException("finder works only on maps and iterable");
	}

	private <T> T internallyInvokeGetter(ModelProperty<? super I> property, PropertyImplementation<? super I, T> propertyImplementation)
			throws ModelDefinitionException {
		return propertyImplementation.get();
	}

	private <T> void internallyInvokeSetter(ModelProperty<? super I> property, SettablePropertyImplementation<I, T> propertyImplementation,
			T value, boolean trackAtomicEdit) throws ModelDefinitionException {
		Object oldValue = invokeGetter(property);
		if (trackAtomicEdit && getUndoManager() != null) {
			if (oldValue != value) {
				getUndoManager().addEdit(new SetCommand<>(getObject(), getModelEntity(), property, oldValue, value, getModelFactory()));
			}
		}
		propertyImplementation.set(value);
	}

	private <T> void internallyInvokeAdder(ModelProperty<? super I> property, MultiplePropertyImplementation<I, T> propertyImplementation,
			T value, int index, boolean trackAtomicEdit) throws ModelDefinitionException {
		// System.out.println("Invoke ADDER "+property.getPropertyIdentifier());
		if (trackAtomicEdit && getUndoManager() != null) {
			getUndoManager().addEdit(new AddCommand<>(getObject(), getModelEntity(), property, value, getModelFactory()));
		}
		propertyImplementation.addTo(value, index);
	}

	private <T> void internallyInvokeRemover(ModelProperty<? super I> property, MultiplePropertyImplementation<I, T> propertyImplementation,
			T value, boolean trackAtomicEdit) throws ModelDefinitionException {
		// System.out.println("Invoke ADDER "+property.getPropertyIdentifier());
		if (trackAtomicEdit && getUndoManager() != null) {
			getUndoManager().addEdit(new RemoveCommand<>(getObject(), getModelEntity(), property, value, getModelFactory()));
		}
		propertyImplementation.removeFrom(value);
	}

	private <T> void internallyInvokeReindexer(ModelProperty<? super I> property,
			ReindexableListPropertyImplementation<I, T> propertyImplementation, T value, int index, boolean trackAtomicEdit)
			throws ModelDefinitionException {
		// System.out.println("Invoke ADDER "+property.getPropertyIdentifier());
		if (trackAtomicEdit && getUndoManager() != null) {
			getUndoManager().addEdit(new RemoveCommand<>(getObject(), getModelEntity(), property, value, getModelFactory()));
			getUndoManager().addEdit(new AddCommand<>(getObject(), getModelEntity(), property, value, getModelFactory()));
		}
		propertyImplementation.reindex(value, index);
	}

	private boolean isObjectAttributeEquals(Object o, String attribute, Object value) throws ModelDefinitionException {
		ProxyMethodHandler<?> handler = getModelFactory().getHandler(o);
		if (handler != null) {
			Object attributeValue = handler.invokeGetter(attribute);
			return isEqual(attributeValue, value);
		}
		else {
			throw new ModelDefinitionException(
					"Found object of type " + o.getClass().getName() + " but is not an instanceof ProxyObject:\n" + o);
		}
	}

	private Object acceptVisitor(PAMELAVisitor pamelaVisitor, VisitingStrategy visitingStrategy) {
		switch (visitingStrategy) {
			case Embedding:
				acceptVisitorEmbeddingStrategy((AccessibleProxyObject) getObject(), pamelaVisitor, new HashSet<Object>());
				break;
			case Exhaustive:
				acceptVisitorExhaustiveStrategy((AccessibleProxyObject) getObject(), pamelaVisitor, new HashSet<Object>());
				break;

			default:
				break;
		}
		return null;
	}

	private static void acceptVisitorEmbeddingStrategy(AccessibleProxyObject object, PAMELAVisitor pamelaVisitor,
			Set<Object> visitedObjects) {
		if (!visitedObjects.contains(object)) {
			visitedObjects.add(object);
			pamelaVisitor.visit(object);
		}

		List<? extends AccessibleProxyObject> directEmbeddedObjects = object.getEmbeddedObjects();
		if (directEmbeddedObjects != null) {
			for (AccessibleProxyObject embeddedObject : directEmbeddedObjects) {
				if (!visitedObjects.contains(embeddedObject)) {
					acceptVisitorEmbeddingStrategy(embeddedObject, pamelaVisitor, visitedObjects);
				}
			}
		}
	}

	private static void acceptVisitorExhaustiveStrategy(AccessibleProxyObject object, PAMELAVisitor pamelaVisitor,
			Set<Object> visitedObjects) {
		if (!visitedObjects.contains(object)) {
			visitedObjects.add(object);
			pamelaVisitor.visit(object);
		}

		List<? extends AccessibleProxyObject> directReferencedObjects = object.getReferencedObjects();
		if (directReferencedObjects != null) {
			for (AccessibleProxyObject referencedObject : directReferencedObjects) {
				if (!visitedObjects.contains(referencedObject)) {
					acceptVisitorExhaustiveStrategy(referencedObject, pamelaVisitor, visitedObjects);
				}
			}
		}
	}

	private Object acceptVisitor(PAMELAVisitor pamelaVisitor) {
		return acceptVisitor(pamelaVisitor, VisitingStrategy.Embedding);
	}

	private List<AccessibleProxyObject> getDirectEmbeddedObjects() {

		List<AccessibleProxyObject> returned = new ArrayList<AccessibleProxyObject>();

		ModelEntity<I> modelEntity = getModelEntity();

		Iterator<ModelProperty<? super I>> properties;
		try {
			properties = modelEntity.getProperties();
		} catch (ModelDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		while (properties.hasNext()) {
			ModelProperty<? super I> p = properties.next();
			if (p.getEmbedded() != null) {
				switch (p.getCardinality()) {
					case SINGLE:
						Object oValue = invokeGetter(p);
						if (oValue instanceof AccessibleProxyObject) {
							returned.add((AccessibleProxyObject) oValue);
						}
						break;
					case LIST:
						List<?> values = (List<?>) invokeGetter(p);
						if (values != null) {
							for (Object o : values) {
								if (o instanceof AccessibleProxyObject) {
									returned.add((AccessibleProxyObject) o);
								}
							}
						}
						break;
					default:
						break;
				}
			}
		}

		return returned;
	}

	private List<AccessibleProxyObject> getReferencedObjects() {

		List<AccessibleProxyObject> returned = new ArrayList<AccessibleProxyObject>();

		ModelEntity<I> modelEntity = getModelEntity();

		Iterator<ModelProperty<? super I>> properties;
		try {
			properties = modelEntity.getProperties();
		} catch (ModelDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		while (properties.hasNext()) {
			ModelProperty<? super I> p = properties.next();
			switch (p.getCardinality()) {
				case SINGLE:
					Object oValue = invokeGetter(p);
					if (oValue instanceof AccessibleProxyObject) {
						returned.add((AccessibleProxyObject) oValue);
					}
					break;
				case LIST:
					List<?> values = (List<?>) invokeGetter(p);
					if (values != null) {
						for (Object o : values) {
							if (o instanceof AccessibleProxyObject) {
								returned.add((AccessibleProxyObject) o);
							}
						}
					}
					break;
				default:
					break;
			}
		}

		return returned;
	}

	static class Compared<O> {

		O o1;
		O o2;
		Boolean equals;

		public Compared(O o1, O o2) {
			this.o1 = o1;
			this.o2 = o2;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((equals == null) ? 0 : equals.hashCode());
			result = prime * result + ((o1 == null) ? 0 : o1.hashCode());
			result = prime * result + ((o2 == null) ? 0 : o2.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Compared other = (Compared) obj;
			if (equals == null) {
				if (other.equals != null)
					return false;
			}
			else if (!equals.equals(other.equals))
				return false;
			if (o1 == null) {
				if (other.o1 != null)
					return false;
			}
			else if (!o1.equals(other.o1))
				return false;
			if (o2 == null) {
				if (other.o2 != null)
					return false;
			}
			else if (!o2.equals(other.o2))
				return false;
			return true;
		}

	}

	public boolean equalsObject(Object obj) {
		return equalsObject(obj, new HashSet<>());
	}

	private boolean equalsObject(Object obj, Set<Compared> seen) {

		Compared returned = new Compared(getObject(), obj);
		seen.add(returned);

		if (getObject() == obj) {
			returned.equals = true;
			return true;
		}
		if (obj == null) {
			returned.equals = false;
			return false;
		}
		ProxyMethodHandler<?> oppositeObjectHandler = getModelFactory().getHandler(obj);
		if (oppositeObjectHandler == null) {
			// Other object is not handled by the same factory
			returned.equals = false;
			return false;
		}
		if (getModelEntity() != oppositeObjectHandler.getModelEntity()) {
			returned.equals = false;
			return false;
		}

		Iterator<ModelProperty<? super I>> properties;
		try {
			properties = getModelEntity().getProperties();
		} catch (ModelDefinitionException e) {
			returned.equals = false;
			return false;
		}
		while (properties.hasNext()) {
			ModelProperty p = properties.next();
			// System.out.println("property " + p + " relevant: " + p.isRelevantForEqualityComputation());
			if (p.isRelevantForEqualityComputation()) {
				switch (p.getCardinality()) {
					case SINGLE:
						Object singleValue = invokeGetter(p);
						Object oppositeValue = oppositeObjectHandler.invokeGetter(p);
						// Special case for properties that are String convertable
						if (p.isStringConvertable()) {
							StringEncoder se = getModelFactory().getStringEncoder();
							try {
								String singleValueAsString = se.toString(singleValue);
								String oppositeValueAsString = se.toString(oppositeValue);
								if ((singleValueAsString == null && oppositeValueAsString != null)
										|| (singleValueAsString != null && !singleValueAsString.equals(oppositeValueAsString))) {
									// System.out.println("Equals fails because of SINGLE serializable property " + p + " value=" +
									// singleValue
									// + " opposite=" + oppositeValue);
									// System.out.println("object1=" + getObject() + " of " + getObject().getClass());
									// System.out.println("object2=" + obj + " of " + obj.getClass());
									returned.equals = false;
									return false;
								}
							} catch (InvalidDataException e) {
								e.printStackTrace();
							}
						}
						else {
							if (seen.contains(new Compared(singleValue, oppositeValue))) {
								// Ignore
							}
							else if (!_isEqual(singleValue, oppositeValue, seen)) {
								// System.out.println("Equals fails because of SINGLE property " + p + " value=" + singleValue + "
								// opposite="
								// + oppositeValue);
								returned.equals = false;
								return false;
							}
						}
						break;
					case LIST:
						List<Object> values = (List) invokeGetter(p);
						List<Object> oppositeValues = (List) oppositeObjectHandler.invokeGetter(p);
						if (!_isEqual(values, oppositeValues, seen)) {
							// System.out.println("values=" + values);
							// System.out.println("oppositeValues=" + oppositeValues);
							// System.out.println("Equals fails because of LIST property difference" + p);
							returned.equals = false;
							return false;
						}
						break;
					default:
						break;
				}
			}
		}
		// System.out.println("ok, equals return true for " + getObject() + " and " + object);
		returned.equals = true;
		return true;
	}

	private boolean _isEqual(Object oldValue, Object newValue, Set<Compared> seen) {
		Compared returned = new Compared(oldValue, newValue);
		seen.add(returned);

		if (oldValue == null) {
			returned.equals = (newValue == null);
			return newValue == null;
		}
		if (oldValue == newValue) {
			returned.equals = true;
			return true;
		}
		if (oldValue instanceof AccessibleProxyObject && newValue instanceof AccessibleProxyObject) {
			ProxyMethodHandler<Object> handler = getModelFactory().getHandler(oldValue);
			boolean returnedValue = handler.equalsObject(newValue, seen);

			returned.equals = returnedValue;
			return returnedValue;
		}
		if (oldValue instanceof List && newValue instanceof List) {
			List<Object> l1 = (List<Object>) oldValue;
			List<Object> l2 = (List<Object>) newValue;
			if (l1.size() != l2.size()) {
				returned.equals = false;
				return false;
			}
			for (int i = 0; i < l1.size(); i++) {
				Object v1 = l1.get(i);
				Object v2 = l2.get(i);
				if (seen.contains(new Compared(v1, v2)))
					continue;

				if (!_isEqual(v1, v2, seen)) {
					returned.equals = false;
					return false;
				}
			}
			return true;
		}
		returned.equals = (oldValue.equals(newValue));
		return oldValue.equals(newValue);

	}

	/**
	 * Called to update current object while comparing it to opposite object, (which must be of right type!), examining each property
	 * values.<br>
	 * Collections are handled while trying to match updated objects with a given strategy<br>
	 * Perform required changes on this object so that at the end of the call, equalsObject(object) shoud return true<br>
	 * Also perform required notifications, so that it is safe to call that method in a deployed environment
	 *
	 * @param obj
	 *            object to update with, which must be of same type
	 * @return boolean indicating if update was successfull
	 */
	public boolean updateWith(I obj) {
		return CompareAndMergeUtils.updateWith(this, obj);
	}

	/**
	 * Clone current object, using meta informations provided by related class All property should be annoted with a @CloningStrategy
	 * annotation which determine the way of handling this property Supplied context is used to determine the closure of objects graph being
	 * constructed during this operation. If a property is marked as @CloningStrategy.CLONE but lead to an object outside scope of cloning
	 * (the closure being computed), then resulting value is nullified. When context is not set, don't compute any closure, and clone all
	 * required objects
	 *
	 * @param context
	 * @return
	 * @throws ModelExecutionException
	 * @throws ModelDefinitionException
	 * @throws CloneNotSupportedException
	 *             when supplied object is not implementing CloneableProxyObject interface
	 */
	protected I cloneObject(Object... context) throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {

		/*System.out.println("Cloning " + getObject());
			for (Object o : context) {
				System.out.println("Context: " + o);
			}*/

		if (context != null && context.length == 1 && context[0].getClass().isArray()) {
			context = (Object[]) context[0];
		}

		// Append this object to supplied context
		if (context != null && context.length > 0) {
			Object[] newContext = new Object[context.length + 1];
			for (int i = 0; i < context.length; i++) {
				newContext[i] = context[i];
			}
			newContext[context.length] = getObject();
			context = newContext;
		}

		if (!(getObject() instanceof CloneableProxyObject)) {
			throw new CloneNotSupportedException();
		}

		Hashtable<CloneableProxyObject, Object> clonedObjects = new Hashtable<>();
		Object returned = performClone(clonedObjects, context);
		// System.out.println("All clones are ready");
		for (CloneableProxyObject o : clonedObjects.keySet()) {
			// System.out.println("Finalizing " + o);
			ProxyMethodHandler<?> clonedObjectHandler = getModelFactory().getHandler(o);
			clonedObjectHandler.finalizeClone(clonedObjects, context);
		}
		return (I) returned;
	}

	/**
	 * Internally used for cloning computation
	 */
	private Object appendToClonedObjects(Hashtable<CloneableProxyObject, Object> clonedObjects,
			CloneableProxyObject objectToCloneOrReference) throws ModelExecutionException, ModelDefinitionException {
		Object returned = clonedObjects.get(objectToCloneOrReference);
		if (returned != null) {
			return returned;
		}
		ProxyMethodHandler<?> clonedValueHandler = getModelFactory().getHandler(objectToCloneOrReference);
		returned = clonedValueHandler.performClone(clonedObjects);
		// System.out.println("CLONING " + objectToCloneOrReference + " clone is " + returned);
		return returned;
	}

	/**
	 * Internally used for cloning computation
	 */
	private Object performClone(Hashtable<CloneableProxyObject, Object> clonedObjects, Object... context)
			throws ModelExecutionException, ModelDefinitionException {
		// System.out.println("******* performClone " + getObject());
		boolean setIsBeingCloned = !beingCloned;
		beingCloned = true;
		Object returned = null;
		try {
			returned = getModelFactory().newInstance(getModelEntity().getImplementedInterface(), true);
			// System.out.println("Perform clone " + getModelEntity());
			ProxyMethodHandler<?> clonedObjectHandler = getModelFactory().getHandler(returned);
			clonedObjectHandler.createdByCloning = true;
			clonedObjectHandler.initialized = true;
			try {

				Iterator<ModelProperty<? super I>> properties = getModelEntity().getPropertiesOrderedForCloning();
				while (properties.hasNext()) {
					ModelProperty p = properties.next();
					switch (p.getCardinality()) {
						case SINGLE:
							Object singleValue = invokeGetter(p);
							switch (p.getCloningStrategy()) {
								case CLONE:
									if (ModelEntity.isModelEntity(p.getType()) && singleValue instanceof CloneableProxyObject) {
										if (!isPartOfContext(singleValue, EmbeddingType.CLOSURE, context)) {
											// Don't do it, outside of context
										}
										else {
											// Unused var Object clonedValue =
											appendToClonedObjects(clonedObjects, (CloneableProxyObject) singleValue);
											// System.out.println("Cloned " + clonedValue + " for " + p);
										}
									}
									else {
										if (singleValue != null) {
											/*if (singleValue instanceof String) {
														clonedObjectHandler.invokeSetter(p, new String((String) singleValue));
													}
													else*/ if (singleValue instanceof DataBinding) {
												clonedObjectHandler.invokeSetter(p, ((DataBinding<?>) singleValue).clone());
											}
											else {
												// TODO: handle primitive types and some basic types (eg. String)
												clonedObjectHandler.invokeSetter(p, singleValue);
											}
										}
										else {
											clonedObjectHandler.invokeSetter(p, null);
										}
									}
									break;
								case REFERENCE:
									clonedObjectHandler.invokeSetter(p, singleValue);
									break;
								case FACTORY:
									break;
								case CUSTOM_CLONE:
									// We have here to invoke custom code (encoded in getStrategyTypeFactory())
									try {
										Object computedValue = BindingEvaluator.evaluateBinding(p.getStrategyTypeFactory(), getObject());
										clonedObjectHandler.invokeSetter(p, computedValue);
									} catch (InvalidKeyValuePropertyException e1) {
										e1.printStackTrace();
									} catch (TypeMismatchException e1) {
										e1.printStackTrace();
									} catch (NullReferenceException e1) {
										e1.printStackTrace();
									} catch (InvocationTargetException e1) {
										e1.printStackTrace();
									}
									break;
								case IGNORE:
									break;
							}
							break;
						case LIST:
							List<?> values = (List<?>) invokeGetter(p);
							if (values != null) {
								List<?> values2 = new ArrayList<>(values);
								for (Object value : values2) {
									switch (p.getCloningStrategy()) {
										case CLONE:
											if (ModelEntity.isModelEntity(p.getType()) && value instanceof CloneableProxyObject) {
												if (!isPartOfContext(value, EmbeddingType.CLOSURE, context)) {
													// Don't do it, outside of context
												}
												else {
													appendToClonedObjects(clonedObjects, (CloneableProxyObject) value);
												}
											} // SGU: removed this code i think it's wrong
											/*else {
													clonedObjectHandler.invokeAdder(p, value);
													}*/
											break;
										case REFERENCE:
											clonedObjectHandler.invokeAdder(p, value);
											break;
										case FACTORY:
											break;
										case IGNORE:
											break;
									}
								}
							}
							break;
						default:
							break;
					}

				}

				clonedObjects.put((CloneableProxyObject) getObject(), returned);
				// System.out.println("Registering " + returned + " as clone of " + getObject());

			} finally {
				clonedObjectHandler.createdByCloning = false;
			}
		} finally {
			if (setIsBeingCloned) {
				beingCloned = false;
			}
		}
		return returned;
	}

	/**
	 * Internally used for cloning computation
	 */
	private Object finalizeClone(Hashtable<CloneableProxyObject, Object> clonedObjects, Object... context)
			throws ModelExecutionException, ModelDefinitionException {
		Object clonedObject = clonedObjects.get(getObject());

		ProxyMethodHandler<?> clonedObjectHandler = getModelFactory().getHandler(clonedObject);
		clonedObjectHandler.createdByCloning = true;
		try {
			Iterator<ModelProperty<? super I>> properties = getModelEntity().getPropertiesOrderedForCloning();

			while (properties.hasNext()) {
				ModelProperty p = properties.next();
				// TODO: cross-check that we should invoke continue
				// In the case of the deletedProperty, it is only normal that there are no setters.
				// We should either prevent this by validating that all properties (that are not deleted properties)
				// have a setter or allow properties to live without a setter.
				switch (p.getCardinality()) {
					case SINGLE:
						if (p.getCloningStrategy() != StrategyType.IGNORE) {
							Object singleValue = invokeGetter(p);
							switch (p.getCloningStrategy()) {
								case CLONE:
									if (getModelFactory().getStringEncoder().isConvertable(p.getType())) {
										Object clonedValue = null;
										try {
											String clonedValueAsString = getModelFactory().getStringEncoder().toString(singleValue);
											clonedValue = getModelFactory().getStringEncoder().fromString(p.getType(), clonedValueAsString);
										} catch (InvalidDataException e) {
											throw new ModelExecutionException(e);
										}
										clonedObjectHandler.invokeSetter(p, clonedValue);
										// clonedObjectHandler.internallyInvokeSetter(p, clonedValue);
									}
									else if (ModelEntity.isModelEntity(p.getType()) && singleValue instanceof CloneableProxyObject) {
										// boolean debug = false;
										/*if (p.getPropertyIdentifier().equals("startShape")) {
												System.out.println("Tiens, pour startShape, singleValue=" + singleValue);
												debug = true;
												}*/
										Object clonedValue = clonedObjects.get(singleValue);
										/*if (debug) {
												System.out.println("clonedValue=" + clonedValue + " singleValue=" + singleValue);
												System.out.println("context=" + context);
												System.out.println("isPartOfContext=" + isPartOfContext(singleValue, EmbeddingType.CLOSURE, context));
												}*/
										if (!isPartOfContext(singleValue, EmbeddingType.CLOSURE, context)) {
											clonedValue = null;
											/*if (debug) {
													System.out.println("mais pas dans le contexte !!!");
													}*/
										}
										clonedObjectHandler.invokeSetter(p, clonedValue);
										// clonedObjectHandler.internallyInvokeSetter(p, clonedValue);
									}
									break;
								case REFERENCE:
									Object referenceValue = singleValue != null ? clonedObjects.get(singleValue) : null;
									if (referenceValue == null) {
										referenceValue = singleValue;
									}
									clonedObjectHandler.invokeSetter(p, referenceValue);
									// clonedObjectHandler.internallyInvokeSetter(p, referenceValue);
									break;
								case CUSTOM_CLONE:
									// clonedObjectHandler.invokeSetter(p, singleValue);
									// clonedObjectHandler.internallyInvokeSetter(p, referenceValue);
									break;
								case FACTORY:
									// We have here to invoke custom code (encoded in getStrategyTypeFactory())
									try {
										Object computedValue = BindingEvaluator.evaluateBinding(p.getStrategyTypeFactory(),
												clonedObject /*getObject()*/);
										clonedObjectHandler.invokeSetter(p, computedValue);
									} catch (InvalidKeyValuePropertyException e1) {
										e1.printStackTrace();
									} catch (TypeMismatchException e1) {
										e1.printStackTrace();
									} catch (NullReferenceException e1) {
										e1.printStackTrace();
									} catch (InvocationTargetException e1) {
										e1.printStackTrace();
									}
									break;
								case IGNORE:
									break;
							}
						}
						break;
					case LIST:
						List<?> values = (List<?>) invokeGetter(p);
						if (values != null) {
							List<?> valuesToClone = new ArrayList<>(values);
							/*System.out.println("Cloning of property " + p);
									System.out.println("Values to clone are: ");
									for (Object value : valuesToClone) {
									System.out.println("* " + value);
									}*/
							for (Object value : valuesToClone) {
								switch (p.getCloningStrategy()) {
									case CLONE:
										if (getModelFactory().getStringEncoder().isConvertable(p.getType())) {
											Object clonedValue = null;
											try {
												String clonedValueAsString = getModelFactory().getStringEncoder().toString(value);
												clonedValue = getModelFactory().getStringEncoder().fromString(p.getType(),
														clonedValueAsString);
											} catch (InvalidDataException e) {
												throw new ModelExecutionException(e);
											}
											List<?> l = (List<?>) clonedObjectHandler.invokeGetter(p);
											clonedObjectHandler.invokeAdder(p, clonedValue);
										}
										else if (ModelEntity.isModelEntity(p.getType()) && value instanceof CloneableProxyObject) {
											Object clonedValue = clonedObjects.get(value);
											if (!isPartOfContext(value, EmbeddingType.CLOSURE, context)) {
												clonedValue = null;
											}
											if (clonedValue != null) {
												clonedObjectHandler.invokeAdder(p, clonedValue);
											}
										}
										break;
									case REFERENCE:
										Object referenceValue = value != null ? clonedObjects.get(value) : null;
										if (referenceValue == null) {
											referenceValue = value;
										}
										clonedObjectHandler.invokeAdder(p, referenceValue);
										break;
									case FACTORY:
										// TODO Not implemented
										break;
									case IGNORE:
										break;
								}

							}
						}
						break;
					default:
						break;
				}

			}
		} finally {
			clonedObjectHandler.createdByCloning = false;
		}

		return clonedObject;
	}

	/**
	 * Internally used for cloning computation This is the method which determine if a value belongs to derived object graph closure
	 */
	private boolean isPartOfContext(Object aValue, EmbeddingType embeddingType, Object... context) {
		if (context == null || context.length == 0) {
			return true;
		}

		for (Object o : context) {
			// Very important: we first have to check if the value is contained in context
			if (aValue == o) {
				return true;
			}
			if (getModelFactory().isEmbedddedIn(o, aValue, embeddingType, context)) {
				return true;
			}
		}

		// System.out.println("Sorry "+aValue+" is not part of context "+context);

		return false;
	}

	/**
	 * Clone several object, using meta informations provided by related class All property should be annoted with a @CloningStrategy
	 * annotation which determine the way of handling this property
	 *
	 * The list of objects is used as the context considered to determine the closure of objects graph being constructed during this
	 * operation. If a property is marked as @CloningStrategy.CLONE but lead to an object outside scope of cloning (the closure being
	 * computed), then resulting value is nullified.
	 *
	 * @param someObjects
	 * @return
	 * @throws ModelExecutionException
	 * @throws ModelDefinitionException
	 * @throws CloneNotSupportedException
	 */
	protected List<Object> cloneObjects(Object... someObjects)
			throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {
		if (someObjects != null && someObjects.length == 1 && someObjects[0].getClass().isArray()) {
			someObjects = (Object[]) someObjects[0];
		}

		for (Object o : someObjects) {
			if (!(o instanceof CloneableProxyObject)) {
				throw new CloneNotSupportedException();
			}
		}

		Hashtable<CloneableProxyObject, Object> clonedObjects = new Hashtable<>();

		for (Object o : someObjects) {
			ProxyMethodHandler<?> clonedObjectHandler = getModelFactory().getHandler(o);
			clonedObjectHandler.performClone(clonedObjects, someObjects);
		}

		for (CloneableProxyObject o : clonedObjects.keySet()) {
			ProxyMethodHandler<?> clonedObjectHandler = getModelFactory().getHandler(o);
			clonedObjectHandler.finalizeClone(clonedObjects, someObjects);
		}

		List<Object> returned = new ArrayList<>();
		for (int i = 0; i < someObjects.length; i++) {
			Object o = someObjects[i];
			returned.add(clonedObjects.get(o));
		}

		return returned;
	}

	/**
	 * Return boolean indicating if supplied clipboard is valid for pasting in object monitored by this method handler<br>
	 *
	 * @param clipboard
	 * @return
	 */
	public static <I> boolean isPastable(Clipboard clipboard, ModelEntity<I> modelEntity) {

		if (clipboard.getTypes().length == 0) {
			// No contents
			return false;
		}

		for (Class<?> type : clipboard.getTypes()) {
			Collection<ModelProperty<? super I>> propertiesAssignableFrom = modelEntity.getPropertiesAssignableFrom(type);
			Collection<ModelProperty<? super I>> pastingPointProperties = Collections2.filter(propertiesAssignableFrom,
					new Predicate<ModelProperty<?>>() {
						@Override
						public boolean apply(ModelProperty<?> arg0) {
							// System.out.println("Property " + arg0);
							// System.out.println("Add PP=" + arg0.getAddPastingPoint());
							// System.out.println("Set PP=" + arg0.getSetPastingPoint());
							return arg0.getAddPastingPoint() != null || arg0.getSetPastingPoint() != null;
						}
					});
			if (pastingPointProperties.size() == 0) {
				// no properties are compatible for pasting type
				// System.out.println("No property declared as pasting point found for " + type + " in " + modelEntity);
				return false;
			}
			else if (pastingPointProperties.size() > 1) {
				// Ambiguous pasting operations: several properties are compatible for pasting type
				// System.out.println("Ambiguous pasting operations: several properties declared as pasting point found for " + type + " in
				// "
				// + modelEntity);
				return true;
			}
		}

		return true;
	}

	/**
	 * Return boolean indicating if supplied clipboard is valid for pasting in object monitored by this method handler<br>
	 *
	 * @param clipboard
	 * @return
	 */
	protected boolean isPastable(Clipboard clipboard) {

		return isPastable(clipboard, getModelEntity());
	}

	/**
	 * Paste supplied clipboard in object monitored by this method handler<br>
	 * Return pasted objects (a single object for a single contents clipboard, and a list of objects for a multiple contents)
	 *
	 * @param clipboard
	 * @return
	 * @throws ModelExecutionException
	 * @throws ModelDefinitionException
	 * @throws CloneNotSupportedException
	 */
	protected Object paste(Clipboard clipboard) throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {

		// System.out.println("PASTING in " + getObject());

		List<Object> returned = null;

		if (!clipboard.isSingleObject()) {
			returned = new ArrayList<>();
		}

		boolean somethingWasPasted = false;
		for (Class<?> type : clipboard.getTypes()) {

			// System.out.println("pasting as " + type);

			Collection<ModelProperty<? super I>> propertiesAssignableFrom = getModelEntity().getPropertiesAssignableFrom(type);
			Collection<ModelProperty<? super I>> pastingPointProperties = Collections2.filter(propertiesAssignableFrom,
					new Predicate<ModelProperty<?>>() {
						@Override
						public boolean apply(ModelProperty<?> arg0) {
							// System.out.println("Property " + arg0);
							// System.out.println("Add PP=" + arg0.getAddPastingPoint());
							// System.out.println("Set PP=" + arg0.getSetPastingPoint());
							return arg0.getAddPastingPoint() != null || arg0.getSetPastingPoint() != null;
						}
					});

			ModelProperty<? super I> pastingProperty;

			if (pastingPointProperties.size() == 0) {
				throw new ClipboardOperationException("Pasting operation: no property is compatible with pasting type " + type);
				// System.out.println("modelEntity=" + getModelEntity());
				// System.out.println("clipboard type=" + type);
				// System.out.println("propertiesAssignableFrom=" + propertiesAssignableFrom);
				// System.out.println("pastingPointProperties=" + pastingPointProperties);

			}
			else if (pastingPointProperties.size() > 1) {

				List<ModelProperty<? super I>> list = new ArrayList<>(pastingPointProperties);
				Collections.sort(list, new Comparator<ModelProperty<? super I>>() {
					@Override
					public int compare(ModelProperty<? super I> o1, ModelProperty<? super I> o2) {
						int p1 = o1.getAddPastingPoint() != null ? o1.getAddPastingPoint().priority() : o1.getSetPastingPoint().priority();
						int p2 = o2.getAddPastingPoint() != null ? o2.getAddPastingPoint().priority() : o2.getSetPastingPoint().priority();
						return p1 - p2;
					}
				});
				// Take the most prioritar
				pastingProperty = list.get(0);
				// throw new ClipboardOperationException(
				// "Ambiguous pasting operations: several properties are compatible for pasting type " + type);
			}
			else {
				pastingProperty = pastingPointProperties.iterator().next();
			}

			Object pastedContents = paste(clipboard, pastingProperty);
			if (clipboard.isSingleObject()) {
				clipboard.consume();
				return pastedContents;
			}
			else if (pastedContents != null) {
				returned.addAll((List) pastedContents);
				somethingWasPasted = true;
			}

		}

		if (!somethingWasPasted) {
			throw new ClipboardOperationException("Cannot paste here: no pasting point found");
		}

		clipboard.consume();
		return returned;
	}

	/**
	 * Paste using supplied clipboard and property, asserting a pasting point could be found<br>
	 * Return pasted objects for supplied property
	 *
	 * @param clipboard
	 * @param modelProperty
	 * @return
	 * @throws ModelExecutionException
	 * @throws ModelDefinitionException
	 * @throws CloneNotSupportedException
	 */
	protected Object paste(Clipboard clipboard, ModelProperty<? super I> modelProperty)
			throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {

		if (modelProperty.getSetPastingPoint() == null && modelProperty.getAddPastingPoint() == null) {
			throw new ClipboardOperationException("Cannot paste here: no pasting point found");
		}
		if (modelProperty.getSetPastingPoint() != null && modelProperty.getAddPastingPoint() != null) {
			throw new ClipboardOperationException(
					"Ambiguous pasting operations: both add and set operations are available for property " + modelProperty);
		}
		// Do it as a SET
		if (modelProperty.getSetPastingPoint() != null) {
			return paste(clipboard, modelProperty, modelProperty.getSetPastingPoint());
		}
		else {
			// Do it as a ADD
			Object returned = paste(clipboard, modelProperty, modelProperty.getAddPastingPoint());
			return returned;
		}
	}

	/**
	 * Paste using supplied clipboard and property, as specified pasting point<br>
	 * Return pasted objects for supplied property
	 *
	 * @param clipboard
	 * @param modelProperty
	 * @param pp
	 * @return
	 * @throws ModelExecutionException
	 * @throws ModelDefinitionException
	 * @throws CloneNotSupportedException
	 */
	protected Object paste(Clipboard clipboard, ModelProperty<? super I> modelProperty, PastingPoint pp)
			throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {
		ModelEntity<?> entity = getModelEntity();
		if (pp == null) {
			throw new ClipboardOperationException("Cannot paste here: no pasting point found");
		}
		// System.out.println("Found pasting point: "+pp);
		if (modelProperty == null) {
			throw new ClipboardOperationException("Cannot paste here: no suitable property found");
			// System.out.println("Found property: "+ppProperty);
		}

		// System.out.println("entity=" + entity);
		// System.out.println("modelProperty=" + modelProperty);
		// System.out.println("entity.hasProperty(modelProperty)=" + entity.hasProperty(modelProperty));

		if (entity.hasProperty(modelProperty) || modelProperty.getModelEntity().isAncestorOf(entity)) {
			if (modelProperty.getSetPastingPoint() == pp) {
				if (!clipboard.isSingleObject()) {
					throw new ClipboardOperationException("Cannot paste here: multiple cardinality clipboard for a SINGLE property");
				}
				Object valueToSet = clipboard.getSingleContents();
				invokeSetter(modelProperty, valueToSet);
				// clipboard.consume();
				return valueToSet;
			}
			else if (modelProperty.getAddPastingPoint() == pp) {
				if (clipboard.isSingleObject()) {
					Object valueToAdd = clipboard.getSingleContents();
					invokeAdder(modelProperty, valueToAdd);
					// clipboard.consume();
					return valueToAdd;
				}
				else {
					List<Object> returned = new ArrayList<>();
					for (Object o : clipboard.getMultipleContents()) {
						if (TypeUtils.isTypeAssignableFrom(modelProperty.getType(), o.getClass())) {
							// System.out.println("PASTE: add " + o + " to " + getObject() + " with " + modelProperty);
							invokeAdder(modelProperty, o);
							returned.add(o);
						}
						else {
							// System.out.println("PASTE: cannot add " + o + " to " + getObject() + " with " + modelProperty);
						}
					}
					// clipboard.consume();
					return returned;
				}
			}
		}

		return null;
	}

	@Override
	public String toString() {
		try {
			return internallyInvokeToString();
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			return super.toString();
		}
	}

	private String internallyInvokeToString() throws ModelDefinitionException {
		StringBuilder sb = new StringBuilder();
		sb.append(getModelEntity().getImplementedInterface().getSimpleName() + "[");
		List<String> variables = new ArrayList<>(propertyImplementations.keySet());
		Collections.sort(variables);
		for (String var : variables) {
			Object obj = propertyImplementations.get(var).get();
			String s = null;
			if (obj != null) {
				if (!(obj instanceof ProxyObject)) {
					s = indent(obj.toString(), var.length() + 1);
				}
				else {
					s = ((ProxyMethodHandler) ((ProxyObject) obj).getHandler()).getModelEntity().getImplementedInterface().getSimpleName();
				}

			}
			sb.append(var).append("=").append(s).append('\n');
		}
		sb.append("]");
		return sb.toString();
	}

	private static String indent(String s, int indent) {
		if (indent > 0 && s != null) {
			String[] split = s.split("\n\r");
			if (split.length > 1) {
				StringBuilder sb = new StringBuilder();
				for (String string : split) {
					// TODO: optimize this
					for (int i = 0; i < indent; i++) {
						sb.append(' ');
					}
					sb.append(string).append('\n');
				}
				return sb.toString();
			}
		}
		return s;
	}

	public boolean isSerializing() {
		return serializing;
	}

	public void setSerializing(boolean serializing, boolean resetModifiedStatus) throws ModelDefinitionException {
		if (this.serializing != serializing) {
			this.serializing = serializing;
			firePropertyChange(SERIALIZING, !serializing, serializing);
			if (resetModifiedStatus && !serializing) {
				internallyInvokeSetModified(false);
			}
		}
	}

	public boolean isDeserializing() {
		return deserializing;
	}

	public void setDeserializing(boolean deserializing) {
		if (this.deserializing != deserializing) {
			this.deserializing = deserializing;
			if (deserializing) {
				// At the begining of the deserialization process, we also need to mark the object as initialized
				initialized = true;
			}
			else {
				modified = false;
			}
			firePropertyChange(DESERIALIZING, !deserializing, deserializing);
		}
	}

	public boolean isModified() {
		return modified;
	}

	private void internallyInvokeSetModified(boolean modified) throws ModelDefinitionException {

		if (modified) {
			if (!isDeserializing() && !isSerializing()) {
				boolean old = this.modified;
				this.modified = modified;
				if (!old) {
					firePropertyChange(MODIFIED, old, modified);
					if (getModelEntity().getModify() != null && getModelEntity().getModify().forward() != null) {
						ModelProperty<? super I> modelProperty = getModelEntity().getModelProperty(getModelEntity().getModify().forward());
						if (modelProperty != null) {
							Object forward = invokeGetter(modelProperty);
							if (forward instanceof ProxyObject) {
								((ProxyMethodHandler<?>) ((ProxyObject) forward).getHandler()).invokeSetModified(modified);
							}
						}
					}
				}
			}
		}
		else if (this.modified != modified) {
			this.modified = modified;
			firePropertyChange(MODIFIED, !modified, modified);
		}
	}

	private boolean enableAssertionChecking = false;

	private void invokeEnableAssertionChecking() {
		this.enableAssertionChecking = true;
	}

	private void invokeDisableAssertionChecking() {
		this.enableAssertionChecking = false;
	}

	public Stack<Method> getAssertionCheckingStack() {
		return assertionCheckingStack;
	}

	private boolean checkOnEntry(Method method, Object[] args) {

		if (!assertionCheckingStack.isEmpty() && assertionCheckingStack.peek() == method) {
			return false;
		}

		assertionCheckingStack.push(method);

		// System.out.println("--------> checkOnEntry " + method);

		checkInvariant();

		JMLMethodDefinition<? super I> jmlMethodDefinition = getModelEntity().getJMLMethodDefinition(method);
		if (jmlMethodDefinition != null) {
			ModelProperty<? super I> property = getModelEntity().getPropertyForMethod(method);
			if (jmlMethodDefinition.getRequires() != null) {
				// System.out.println("Check pre-condition " + jmlMethodDefinition.getRequires().getExpression());
				((JMLRequires) jmlMethodDefinition.getRequires()).check(this, args);
			}
			if (jmlMethodDefinition.getEnsures() != null) {
				// System.out.println("Init post-condition " + jmlMethodDefinition.getEnsures().getExpression());
				Map<String, Object> historyValuesForThisMethod = ((JMLEnsures) jmlMethodDefinition.getEnsures()).checkOnEntry(this, args);
				historyValues.put(method, historyValuesForThisMethod);
			}
		}

		return true;
	}

	private void checkInvariant() {
		if (getModelEntity().getInvariant() != null) {
			getModelEntity().getInvariant().check(this);
		}
	}

	private void checkOnExit(Method method, Object[] args) {

		// System.out.println("<-------- checkOnExit " + method);

		checkInvariant();

		JMLMethodDefinition<? super I> jmlMethodDefinition = getModelEntity().getJMLMethodDefinition(method);
		if (jmlMethodDefinition != null) {
			ModelProperty<? super I> property = getModelEntity().getPropertyForMethod(method);
			if (jmlMethodDefinition.getEnsures() != null) {
				// System.out.println("Check post-condition " + jmlMethodDefinition.getEnsures().getExpression());
				((JMLEnsures) jmlMethodDefinition.getEnsures()).checkOnExit(this, args, historyValues.get(method));
			}
		}

		// checkedMethod = null;

		assertionCheckingStack.pop();

	}

}
