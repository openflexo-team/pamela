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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

import javax.annotation.Nonnull;

import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.CloneableProxyObject;
import org.openflexo.pamela.DeletableProxyObject;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.Finder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.annotations.Reindexer;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.Updater;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.exceptions.NoSuchEntityException;
import org.openflexo.pamela.exceptions.UnitializedEntityException;
import org.openflexo.pamela.factory.PamelaModelFactory.PAMELAProxyFactory;
import org.openflexo.pamela.jml.SpecificationsViolationException;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;
import org.openflexo.pamela.model.PamelaVisitor;
import org.openflexo.pamela.model.PamelaVisitor.VisitingStrategy;
import org.openflexo.pamela.model.property.DefaultMultiplePropertyImplementation;
import org.openflexo.pamela.model.property.DefaultSinglePropertyImplementation;
import org.openflexo.pamela.model.property.MultiplePropertyImplementation;
import org.openflexo.pamela.model.property.PropertyImplementation;
import org.openflexo.pamela.model.property.ReindexableListPropertyImplementation;
import org.openflexo.pamela.model.property.SettablePropertyImplementation;
import org.openflexo.pamela.patterns.ExecutionMonitor;
import org.openflexo.pamela.patterns.PatternInstance;
import org.openflexo.pamela.patterns.PostconditionViolationException;
import org.openflexo.pamela.patterns.PreconditionViolationException;
import org.openflexo.pamela.patterns.PropertyViolationException;
import org.openflexo.pamela.patterns.ReturnWrapper;
import org.openflexo.pamela.patterns.annotations.Ensures;
import org.openflexo.pamela.patterns.annotations.Requires;
import org.openflexo.pamela.undo.AddCommand;
import org.openflexo.pamela.undo.RemoveCommand;
import org.openflexo.pamela.undo.SetCommand;
import org.openflexo.pamela.undo.UndoManager;

import com.google.common.base.Defaults;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;

/**
 * Invocation handler in the core of PAMELA: main class for PAMELA interpreter<br>
 *
 * This is the class where method call dispatching is performed. Specialized concerns are
 * delegated to dedicated handler classes:
 * <ul>
 * <li>{@link JMLAssertionChecker} — pre/post-conditions and invariants</li>
 * <li>{@link NotificationHandler} — PropertyChange, modified, serializing state</li>
 * <li>{@link ObjectLifecycleHandler} — delete, undelete, destroy</li>
 * <li>{@link CloningHandler} — object cloning</li>
 * <li>{@link ObjectGraphHandler} — equality, visitor, embedded objects</li>
 * <li>{@link ClipboardHandler} — copy/paste operations</li>
 * </ul>
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
	 * We need to retain values being set in case of bidirectional inverse properties patterns, to avoid infinite loop
	 */
	private final Map<ModelProperty<? super I>, Object> scheduledSets = new HashMap<>();

	protected boolean initialized = false;
	protected boolean initializing;

	private Map<String, PropertyImplementation<? super I, ?>> propertyImplementations;
	private List<DelegateImplementation<? super I>> delegateImplementations;

	private final PAMELAProxyFactory<I> pamelaProxyFactory;
	private final EditingContext editingContext;

	// --- Specialized handlers ---
	private final JMLAssertionChecker<I> jmlChecker;
	private final NotificationHandler<I> notificationHandler;
	private final ObjectLifecycleHandler<I> lifecycleHandler;
	private final CloningHandler<I> cloningHandler;
	private final ObjectGraphHandler<I> objectGraphHandler;
	private final ClipboardHandler<I> clipboardHandler;

	public ProxyMethodHandler(PAMELAProxyFactory<I> pamelaProxyFactory, EditingContext editingContext) throws ModelDefinitionException {
		this.pamelaProxyFactory = pamelaProxyFactory;
		this.editingContext = editingContext;
		propertyImplementations = new HashMap<>(getModelEntity().getPropertiesSize(), 1.0f);
		initialized = !getModelEntity().hasInitializers();
		initDelegateImplementations();

		jmlChecker = new JMLAssertionChecker<>(this);
		notificationHandler = new NotificationHandler<>(this);
		lifecycleHandler = new ObjectLifecycleHandler<>(this);
		cloningHandler = new CloningHandler<>(this);
		objectGraphHandler = new ObjectGraphHandler<>(this);
		clipboardHandler = new ClipboardHandler<>(this);
	}

	// =========================================================================
	// Handler accessors (used by sub-handlers to call each other)
	// =========================================================================

	public JMLAssertionChecker<I> getJmlChecker() {
		return jmlChecker;
	}

	public NotificationHandler<I> getNotificationHandler() {
		return notificationHandler;
	}

	public ObjectLifecycleHandler<I> getLifecycleHandler() {
		return lifecycleHandler;
	}

	public CloningHandler<I> getCloningHandler() {
		return cloningHandler;
	}

	public ObjectGraphHandler<I> getObjectGraphHandler() {
		return objectGraphHandler;
	}

	public ClipboardHandler<I> getClipboardHandler() {
		return clipboardHandler;
	}

	// =========================================================================
	// Delegate implementations (multiple inheritance)
	// =========================================================================

	private void initDelegateImplementations() throws ModelDefinitionException {
		delegateImplementations = new ArrayList<>();
		initDelegateImplementations(getModelEntity());
	}

	private void initDelegateImplementations(ModelEntity<? super I> entity) throws ModelDefinitionException {
		if (entity.getDelegateImplementations().size() > 0) {
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

	// =========================================================================
	// Core accessors
	// =========================================================================

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

	public PamelaModelFactory getModelFactory() {
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

	private PamelaMetaModel getModelContext() {
		return getModelFactory().getModelContext();
	}

	// =========================================================================
	// Lifecycle state (delegated)
	// =========================================================================

	public boolean isDeleting() {
		return lifecycleHandler.isDeleting();
	}

	public boolean isUndeleting() {
		return lifecycleHandler.isUndeleting();
	}

	public boolean isInitializing() {
		return initializing;
	}

	public boolean isCreatedByCloning() {
		return cloningHandler.isCreatedByCloning();
	}

	public boolean isDestroyed() {
		return lifecycleHandler.isDestroyed();
	}

	/** Called by ObjectLifecycleHandler.destroy() */
	void clearPropertyImplementations() {
		if (propertyImplementations != null) {
			propertyImplementations.clear();
		}
		propertyImplementations = null;
	}

	// =========================================================================
	// Main dispatch (invoke)
	// =========================================================================

	@Override
	public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable {
		boolean assertionChecking = false;
		boolean keepGoing = true;
		Object invoke = null;

		if (jmlChecker.isEnabled()) {
			assertionChecking = jmlChecker.checkOnEntry(method, args);
		}

		for (ExecutionMonitor monitor : getModelFactory().getModelContext().getExecutionMonitors()) {
			monitor.enteringMethod(self, method, args);
		}

		Set<PatternInstance<?>> patternInstances = getModelFactory().getModelContext().getPatternInstances(self);
		if (patternInstances != null) {
			for (PatternInstance<?> patternInstance : patternInstances) {
				// TODO: Perf issue : implement a cache here
				List<Requires> preconditions = patternInstance.getPatternDefinition().getPreconditions(method);
				if (preconditions != null) {
					System.out.println("Invoking preconditions for " + method + " in pattern instance : " + patternInstance);
					for (Requires precondition : preconditions) {
						try {
							patternInstance.invokePrecondition(precondition, method);
						} catch (PropertyViolationException e) {
							if (!precondition.exceptionWhenViolated().equals(PreconditionViolationException.class)) {
								// A specific exception should be thrown
								Constructor<? extends Exception> c = precondition.exceptionWhenViolated().getConstructor(String.class,
										Throwable.class);
								Exception thrownException = c.newInstance("Violated property " + precondition.property(), e);
								throw thrownException;
							}
							else {
								throw e;
							}
						}
					}
				}
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

		if (keepGoing) {
			invoke = _invoke(self, method, proceed, args);
			if (method.getReturnType().isPrimitive() && invoke == null) {
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
				// TODO: Perf issue : implement a cache here
				List<Ensures> postconditions = patternInstance.getPatternDefinition().getPostconditions(method);
				if (postconditions != null) {
					System.out.println("Invoking postconditions for " + method + " in pattern instance : " + patternInstance);
					for (Ensures postcondition : postconditions) {
						try {
							patternInstance.invokePostcondition(postcondition, method);
						} catch (PropertyViolationException e) {
							if (!postcondition.exceptionWhenViolated().equals(PostconditionViolationException.class)) {
								// A specific exception should be thrown
								Constructor<? extends Exception> c = postcondition.exceptionWhenViolated().getConstructor(String.class,
										Throwable.class);
								Exception thrownException = c.newInstance("Violated property " + postcondition.property(), e);
								throw thrownException;
							}
							else {
								throw e;
							}
						}
					}
				}
			}
		}

		for (ExecutionMonitor monitor : getModelFactory().getModelContext().getExecutionMonitors()) {
			monitor.leavingMethod(self, method, args, invoke);
		}

		if (jmlChecker.isEnabled() && assertionChecking) {
			jmlChecker.checkOnExit(method, args);
		}

		return invoke;
	}

	private Object _invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable {

		// First check delegate implementations (partial implementations via @Implementation)
		for (DelegateImplementation<? super I> delegateImplementation : delegateImplementations) {
			if (delegateImplementation.handleMethod(method)) {
				return delegateImplementation.invoke(self, method, proceed, args);
			}
		}

		if (proceed != null) {
			ModelProperty<? super I> property = getModelEntity().getPropertyForMethod(method);
			boolean callSetModifiedAtTheEnd = false;
			if (property != null) {
				if (PamelaUtils.methodIsEquivalentTo(method, property.getSetterMethod())
						|| PamelaUtils.methodIsEquivalentTo(method, property.getUpdaterMethod())) {
					Object oldValue = invokeGetter(property);
					if (getUndoManager() != null && oldValue != args[0]) {
						getUndoManager().addEdit(
								new SetCommand<>(getObject(), getModelEntity(), property, oldValue, args[0], getModelFactory()));
					}
					if (property.isSerializable()) {
						callSetModifiedAtTheEnd = true;
					}
				}
				if (PamelaUtils.methodIsEquivalentTo(method, property.getAdderMethod())) {
					if (getUndoManager() != null) {
						getUndoManager().addEdit(new AddCommand<>(getObject(), getModelEntity(), property, args[0], getModelFactory()));
					}
					if (property.isSerializable()) {
						callSetModifiedAtTheEnd = true;
					}
				}
				if (PamelaUtils.methodIsEquivalentTo(method, property.getRemoverMethod())) {
					if (getUndoManager() != null) {
						getUndoManager().addEdit(new RemoveCommand<>(getObject(), getModelEntity(), property, args[0], getModelFactory()));
					}
					if (property.isSerializable()) {
						callSetModifiedAtTheEnd = true;
					}
				}
			}
			try {
				Object returned = proceed.invoke(self, args);
				if (callSetModifiedAtTheEnd) {
					invokeSetModified(true);
				}
				return returned;
			} catch (InvocationTargetException e) {
				for (Class<?> exceptionType : proceed.getExceptionTypes()) {
					if (exceptionType.isAssignableFrom(e.getTargetException().getClass())) {
						throw e.getTargetException();
					}
				}
				if (SpecificationsViolationException.class.isAssignableFrom(e.getTargetException().getClass())) {
					throw e.getTargetException();
				}
				e.printStackTrace();
				throw new ModelExecutionException(e.getCause());
			}
		}

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
			return internallyInvokeGetter(getter.value());
		}

		Setter setter = method.getAnnotation(Setter.class);
		if (setter != null) {
			internallyInvokeSetter(setter.value(), args[0], true);
			return null;
		}

		Updater updater = method.getAnnotation(Updater.class);
		if (updater != null) {
			internallyInvokeUpdater(updater.value(), args[0], true);
			return null;
		}

		Adder adder = method.getAnnotation(Adder.class);
		if (adder != null) {
			internallyInvokeAdder(adder.value(), args[0], true);
			return null;
		}

		Remover remover = method.getAnnotation(Remover.class);
		if (remover != null) {
			internallyInvokeRemover(remover.value(), args[0], true);
			return null;
		}

		Reindexer reindexer = method.getAnnotation(Reindexer.class);
		if (reindexer != null) {
			internallyInvokeReindexer(reindexer.value(), args[0], (int) args[1], true);
			return null;
		}

		Finder finder = method.getAnnotation(Finder.class);
		if (finder != null) {
			return internallyInvokeFinder(finder, args);
		}

		if (PamelaUtils.methodIsEquivalentTo(method, GET_PROPERTY_CHANGE_SUPPORT)) {
			return notificationHandler.getPropertyChangeSupport();
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_GETTER)) {
			return internallyInvokeGetter((String) args[0]);
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_SETTER)) {
			internallyInvokeSetter((String) args[0], args[1], false);
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_ADDER)) {
			internallyInvokeAdder((String) args[0], args[1], false);
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_ADDER_AT_INDEX)) {
			internallyInvokeAdder((String) args[0], args[1], (int) args[2], false);
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_REMOVER)) {
			internallyInvokeRemover((String) args[0], args[1], false);
			return null;
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
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_GETTER_ENTITY)) {
			return internallyInvokeGetter((String) args[0]);
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_SETTER_ENTITY)) {
			internallyInvokeSetter((String) args[0], args[1], false);
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_ADDER_ENTITY)) {
			internallyInvokeAdder((String) args[0], args[1], false);
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
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_INITIALIZER)) {
			Object[] developpedArgs = (Object[]) args[0];
			internallyInvokeInitializer(getModelEntity().getInitializerForArgs(developpedArgs), developpedArgs);
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, IS_SERIALIZING)) {
			return notificationHandler.isSerializing();
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, IS_DESERIALIZING)) {
			return notificationHandler.isDeserializing();
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, IS_MODIFIED)) {
			return notificationHandler.isModified();
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, SET_MODIFIED)
				|| PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_SET_MODIFIED)) {
			notificationHandler.setModified((Boolean) args[0]);
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, TO_STRING)) {
			return internallyInvokeToString();
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, DESTROY)) {
			lifecycleHandler.destroy();
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, CLONE_OBJECT)) {
			return cloningHandler.cloneObject();
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, EQUALS_OBJECT)) {
			return objectGraphHandler.equalsObject(args[0]);
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, EQUALS_OBJECT_USING_FILTER)) {
			return objectGraphHandler.equalsObject(args[0], (Function) args[1]);
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, UPDATE_WITH_OBJECT)) {
			return objectGraphHandler.updateWith((I) args[0]);
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, GET_EMBEDDED)) {
			return objectGraphHandler.getDirectEmbeddedObjects();
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, GET_EMBEDDED_VALIDABLE)) {
			return objectGraphHandler.getDirectEmbeddedObjects();
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, GET_REFERENCED)) {
			return objectGraphHandler.getReferencedObjects();
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, ACCEPT_VISITOR)) {
			return objectGraphHandler.acceptVisitor((PamelaVisitor) args[0]);
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, ACCEPT_WITH_STRATEGY_VISITOR)) {
			return objectGraphHandler.acceptVisitor((PamelaVisitor) args[0], (VisitingStrategy) args[1]);
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, IS_DELETED)) {
			return lifecycleHandler.isDeleted();
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, IS_BEING_CLONED)) {
			return cloningHandler.isBeingCloned();
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, IS_CREATED_BY_CLONING)) {
			return cloningHandler.isCreatedByCloning();
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
			return cloningHandler.cloneObject(args);
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, HAS_KEY)) {
			return getModelEntity().getModelProperty((String) args[0]) != null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, OBJECT_FOR_KEY)) {
			ModelProperty<? super I> property = getModelEntity().getModelProperty((String) args[0]);
			if (property != null) {
				return invokeGetter(property);
			}
			System.err.println("Cannot handle property " + args[0] + " for " + getObject());
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, SET_OBJECT_FOR_KEY)) {
			ModelProperty<? super I> property = getModelEntity().getModelProperty((String) args[1]);
			if (property != null) {
				invokeSetter(property, args[0]);
				return null;
			}
			System.err.println("Cannot handle property " + args[0] + " for " + getObject());
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, GET_TYPE_FOR_KEY)) {
			ModelProperty<? super I> property = getModelEntity().getModelProperty((String) args[0]);
			if (property != null) {
				return property.getType();
			}
			System.err.println("Cannot handle property " + args[0] + " for " + getObject());
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, ENABLE_ASSERTION_CHECKING)) {
			jmlChecker.enable();
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, DISABLE_ASSERTION_CHECKING)) {
			jmlChecker.disable();
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
			else if (PamelaUtils.methodIsEquivalentTo(method, property.getUpdaterMethod())) {
				internallyInvokeUpdater(property, args[0], true);
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

	// =========================================================================
	// Initializer
	// =========================================================================

	private void internallyInvokeInitializer(org.openflexo.pamela.model.ModelInitializer in, Object[] args)
			throws ModelDefinitionException {
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

	// =========================================================================
	// Delete / Undelete (thin wrappers over ObjectLifecycleHandler)
	// =========================================================================

	protected boolean internallyInvokeDeleter(boolean trackAtomicEdit, Object... context) throws ModelDefinitionException {
		return lifecycleHandler.delete(trackAtomicEdit, context);
	}

	protected boolean internallyInvokeUndeleter(boolean restoreProperties, boolean trackAtomicEdit) throws ModelDefinitionException {
		return lifecycleHandler.undelete(restoreProperties, trackAtomicEdit);
	}

	public void destroy() {
		lifecycleHandler.destroy();
	}

	// =========================================================================
	// Property implementation management
	// =========================================================================

	public PropertyImplementation<? super I, ?> getPropertyImplementation(ModelProperty<? super I> property)
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

	// =========================================================================
	// Getter / Setter / Adder / Remover / Reindexer (internal)
	// =========================================================================

	protected Object internallyInvokeGetter(String propertyIdentifier) throws ModelDefinitionException {
		ModelProperty<? super I> property = getModelEntity().getModelProperty(propertyIdentifier);
		return internallyInvokeGetter(property);
	}

	protected Object internallyInvokeGetter(ModelProperty<? super I> property) throws ModelDefinitionException {
		PropertyImplementation<? super I, ?> propertyImplementation = getPropertyImplementation(property);
		return internallyInvokeGetter(property, propertyImplementation);
	}

	private <T> T internallyInvokeGetter(ModelProperty<? super I> property, PropertyImplementation<? super I, T> propertyImplementation)
			throws ModelDefinitionException {
		return propertyImplementation.get();
	}

	protected void internallyInvokeSetter(String propertyIdentifier, Object value, boolean trackAtomicEdit)
			throws ModelDefinitionException {
		ModelProperty<? super I> property = getModelEntity().getModelProperty(propertyIdentifier);
		internallyInvokeSetter(property, value, trackAtomicEdit);
	}

	public void internallyInvokeSetter(ModelProperty<? super I> property, Object value, boolean trackAtomicEdit)
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

	private <T> void internallyInvokeSetter(ModelProperty<? super I> property, SettablePropertyImplementation<I, T> propertyImplementation,
			T value, boolean trackAtomicEdit) throws ModelDefinitionException {
		Object oldValue = invokeGetter(property);
		if (trackAtomicEdit && getUndoManager() != null && oldValue != value) {
			getUndoManager().addEdit(new SetCommand<>(getObject(), getModelEntity(), property, oldValue, value, getModelFactory()));
		}
		propertyImplementation.set(value);
	}

	protected void internallyInvokeUpdater(String propertyIdentifier, Object value, boolean trackAtomicEdit)
			throws ModelDefinitionException {
		ModelProperty<? super I> property = getModelEntity().getModelProperty(propertyIdentifier);
		internallyInvokeUpdater(property, value, trackAtomicEdit);
	}

	public void internallyInvokeUpdater(ModelProperty<? super I> property, Object value, boolean trackAtomicEdit)
			throws ModelDefinitionException {
		PropertyImplementation<? super I, ?> propertyImplementation = getPropertyImplementation(property);
		if (propertyImplementation instanceof SettablePropertyImplementation) {
			internallyInvokeUpdater(property, (SettablePropertyImplementation) propertyImplementation, value, trackAtomicEdit);
		}
		else {
			throw new ModelDefinitionException(
					"Property implementation does not support SET protocol: " + property.getPropertyImplementation());
		}
	}

	private <T> void internallyInvokeUpdater(ModelProperty<? super I> property, SettablePropertyImplementation<I, T> propertyImplementation,
			T value, boolean trackAtomicEdit) throws ModelDefinitionException {
		if (trackAtomicEdit && getUndoManager() != null) {
			Object oldValue = invokeGetter(property);
			if (oldValue != value) {
				getUndoManager().addEdit(new SetCommand<>(getObject(), getModelEntity(), property, oldValue, value, getModelFactory()));
			}
		}
		propertyImplementation.update(value);
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

	private <T> void internallyInvokeAdder(ModelProperty<? super I> property, MultiplePropertyImplementation<I, T> propertyImplementation,
			T value, int index, boolean trackAtomicEdit) throws ModelDefinitionException {
		if (trackAtomicEdit && getUndoManager() != null) {
			getUndoManager().addEdit(new AddCommand<>(getObject(), getModelEntity(), property, value, getModelFactory()));
		}
		propertyImplementation.addTo(value, index);
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

	private <T> void internallyInvokeRemover(ModelProperty<? super I> property,
			MultiplePropertyImplementation<I, T> propertyImplementation, T value, boolean trackAtomicEdit) throws ModelDefinitionException {
		if (trackAtomicEdit && getUndoManager() != null) {
			getUndoManager().addEdit(new RemoveCommand<>(getObject(), getModelEntity(), property, value, getModelFactory()));
		}
		propertyImplementation.removeFrom(value);
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

	private <T> void internallyInvokeReindexer(ModelProperty<? super I> property,
			ReindexableListPropertyImplementation<I, T> propertyImplementation, T value, int index, boolean trackAtomicEdit)
			throws ModelDefinitionException {
		if (trackAtomicEdit && getUndoManager() != null) {
			getUndoManager().addEdit(new RemoveCommand<>(getObject(), getModelEntity(), property, value, getModelFactory()));
			getUndoManager().addEdit(new AddCommand<>(getObject(), getModelEntity(), property, value, getModelFactory()));
		}
		propertyImplementation.reindex(value, index);
	}

	// =========================================================================
	// Public invoke* (called via reflection from property methods)
	// =========================================================================

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
			if (property.getCardinality() == Cardinality.LIST) {
				// TODO: do it with adder/remover/reindexer methods
				System.err.println("TODO: do it with adder/remover/reindexer methods");
			}
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

	public void invokeAdderForDeserialization(ModelProperty<? super I> property, Object value) throws ModelDefinitionException {
		if (property.getAdderMethod() != null) {
			invokeAdder(property, value);
		}
		else {
			internallyInvokeAdder(property, value, true);
		}
	}

	public Map<ModelProperty<? super I>, Object> getScheduledSets() {
		return scheduledSets;
	}

	// =========================================================================
	// Serializing / Deserializing / Modified (delegated to NotificationHandler)
	// =========================================================================

	public boolean isSerializing() {
		return notificationHandler.isSerializing();
	}

	public void setSerializing(boolean serializing, boolean resetModifiedStatus) throws ModelDefinitionException {
		notificationHandler.setSerializing(serializing, resetModifiedStatus);
	}

	public boolean isDeserializing() {
		return notificationHandler.isDeserializing();
	}

	public void setDeserializing(boolean deserializing) {
		notificationHandler.setDeserializing(deserializing);
	}

	public boolean isModified() {
		return notificationHandler.isModified();
	}

	@Deprecated
	public void invokeSetModified(boolean modified) throws ModelDefinitionException {
		if (getObject() instanceof AccessibleProxyObject) {
			((AccessibleProxyObject) getObject()).setModified(modified);
		}
		else {
			notificationHandler.setModified(modified);
		}
	}

	// =========================================================================
	// PropertyChangeListener (delegated to NotificationHandler)
	// =========================================================================

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		notificationHandler.propertyChange(evt);
	}

	// =========================================================================
	// Assertion checking (delegated to JMLAssertionChecker)
	// =========================================================================

	public Stack<Method> getAssertionCheckingStack() {
		return jmlChecker.getAssertionCheckingStack();
	}

	// =========================================================================
	// Clipboard (delegated to ClipboardHandler)
	// =========================================================================

	public static <I> boolean isPastable(Clipboard clipboard, ModelEntity<I> modelEntity) {
		return ClipboardHandler.isPastable(clipboard, modelEntity);
	}

	protected boolean isPastable(Clipboard clipboard) {
		return clipboardHandler.isPastable(clipboard);
	}

	protected Object paste(Clipboard clipboard) throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {
		return clipboardHandler.paste(clipboard);
	}

	protected Object paste(Clipboard clipboard, ModelProperty<? super I> modelProperty)
			throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {
		return clipboardHandler.paste(clipboard, modelProperty);
	}

	protected Object paste(Clipboard clipboard, ModelProperty<? super I> modelProperty,
			org.openflexo.pamela.annotations.PastingPoint pp)
			throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {
		return clipboardHandler.paste(clipboard, modelProperty, pp);
	}

	// =========================================================================
	// Cloning (delegated to CloningHandler)
	// =========================================================================

	protected I cloneObject(Object... context) throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {
		return cloningHandler.cloneObject(context);
	}

	protected List<Object> cloneObjects(Object... someObjects)
			throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {
		return cloningHandler.cloneObjects(someObjects);
	}

	// =========================================================================
	// Equals / updateWith / visitor (delegated to ObjectGraphHandler)
	// =========================================================================

	public boolean equalsObject(Object obj) {
		return objectGraphHandler.equalsObject(obj);
	}

	public boolean equalsObject(Object obj, Function<ModelProperty, Boolean> considerProperty) {
		return objectGraphHandler.equalsObject(obj, considerProperty);
	}

	public boolean updateWith(I obj) {
		return objectGraphHandler.updateWith(obj);
	}

	// =========================================================================
	// Finder
	// =========================================================================

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
		if (collection instanceof java.util.Map<?, ?>) {
			collection = ((java.util.Map<?, ?>) collection).values();
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

	private boolean isObjectAttributeEquals(Object o, String attribute, Object value) throws ModelDefinitionException {
		ProxyMethodHandler<?> h = getModelFactory().getHandler(o);
		if (h != null) {
			Object attributeValue = h.invokeGetter(attribute);
			return isEqual(attributeValue, value);
		}
		else {
			throw new ModelDefinitionException(
					"Found object of type " + o.getClass().getName() + " but is not an instanceof ProxyObject:\n" + o);
		}
	}

	// =========================================================================
	// toString
	// =========================================================================

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
		sb.append(getModelEntity().getImplementedInterface().getSimpleName()).append("[");
		List<String> variables = new ArrayList<>(propertyImplementations.keySet());
		java.util.Collections.sort(variables);
		for (String var : variables) {
			Object obj = propertyImplementations.get(var).get();
			String s = null;
			if (obj != null) {
				if (!(obj instanceof ProxyObject)) {
					s = indent(obj.toString(), var.length() + 1);
				}
				else {
					s = ((ProxyMethodHandler) ((ProxyObject) obj).getHandler()).getModelEntity().getImplementedInterface()
							.getSimpleName();
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

	// =========================================================================
	// Helpers
	// =========================================================================

	private @Nonnull ModelEntity<? super I> getModelEntityFromArg(Class<?> class1) throws ModelDefinitionException {
		ModelEntity<?> e = getModelContext().getModelEntity(class1);
		if (e == null) {
			throw new NoSuchEntityException(class1);
		}
		if (!e.isAncestorOf(getModelEntity())) {
			throw new ModelExecutionException(
					((Class<?>) class1).getName() + " is not a super interface of " + getModelEntity().getImplementedInterface().getName());
		}
		return (ModelEntity<? super I>) e;
	}
}
