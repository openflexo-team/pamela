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
import java.lang.reflect.Array;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

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
import org.openflexo.pamela.undo.AddCommand;
import org.openflexo.pamela.undo.CreateCommand;
import org.openflexo.pamela.undo.DeleteCommand;
import org.openflexo.pamela.undo.RemoveCommand;
import org.openflexo.pamela.undo.SetCommand;
import org.openflexo.pamela.undo.UndoManager;
import org.openflexo.toolbox.HasPropertyChangeSupport;
import org.openflexo.toolbox.StringUtils;

import com.google.common.base.Defaults;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;

public class ProxyMethodHandler<I> implements MethodHandler, PropertyChangeListener {

	public static final String DELETED = "deleted";
	public static final String UNDELETED = "undeleted";
	public static final String MODIFIED = "modified";
	public static final String DESERIALIZING = "deserializing";
	public static final String SERIALIZING = "serializing";

	private I object;

	private Map<String, Object> values;
	private Map<String, Object> oldValues;

	private boolean destroyed = false;
	private boolean deleted = false;
	private boolean deleting = false;
	private boolean undeleting = false;
	protected boolean initialized = false;
	private boolean serializing = false;
	private boolean deserializing = false;
	private boolean createdByCloning = false;
	private boolean beingCloned = false;
	private boolean modified = false;
	private PropertyChangeSupport propertyChangeSupport;
	protected boolean initializing;

	public static Method PERFORM_SUPER_GETTER;
	public static Method PERFORM_SUPER_SETTER;
	public static Method PERFORM_SUPER_ADDER;
	public static Method PERFORM_SUPER_ADDER_AT_INDEX;
	public static Method PERFORM_SUPER_REMOVER;
	public static Method PERFORM_SUPER_DELETER;
	public static Method PERFORM_SUPER_UNDELETER;
	public static Method PERFORM_SUPER_FINDER;
	public static Method PERFORM_SUPER_GETTER_ENTITY;
	public static Method PERFORM_SUPER_SETTER_ENTITY;
	public static Method PERFORM_SUPER_ADDER_ENTITY;
	public static Method PERFORM_SUPER_REMOVER_ENTITY;
	public static Method PERFORM_SUPER_DELETER_ENTITY;
	public static Method PERFORM_SUPER_FINDER_ENTITY;
	public static Method PERFORM_SUPER_SET_MODIFIED;
	public static Method IS_MODIFIED;
	public static Method SET_MODIFIED;
	public static Method IS_SERIALIZING;
	public static Method IS_DESERIALIZING;
	public static Method TO_STRING;
	public static Method GET_PROPERTY_CHANGE_SUPPORT;
	public static Method GET_DELETED_PROPERTY;
	public static Method CLONE_OBJECT;
	public static Method CLONE_OBJECT_WITH_CONTEXT;
	public static Method IS_CREATED_BY_CLONING;
	public static Method IS_BEING_CLONED;
	public static Method DELETE_OBJECT;
	public static Method UNDELETE_OBJECT;
	public static Method IS_DELETED;
	public static Method EQUALS_OBJECT;
	public static Method UPDATE_WITH_OBJECT;
	public static Method DESTROY;
	public static Method HAS_KEY;
	public static Method OBJECT_FOR_KEY;
	public static Method SET_OBJECT_FOR_KEY;
	public static Method GET_TYPE_FOR_KEY;

	private final PAMELAProxyFactory<I> pamelaProxyFactory;
	private final EditingContext editingContext;

	static {
		try {
			PERFORM_SUPER_GETTER = AccessibleProxyObject.class.getMethod("performSuperGetter", String.class);
			PERFORM_SUPER_SETTER = AccessibleProxyObject.class.getMethod("performSuperSetter", String.class, Object.class);
			PERFORM_SUPER_ADDER = AccessibleProxyObject.class.getMethod("performSuperAdder", String.class, Object.class);
			PERFORM_SUPER_ADDER_AT_INDEX = AccessibleProxyObject.class.getMethod("performSuperAdder", String.class, Object.class,
					Integer.TYPE);
			PERFORM_SUPER_REMOVER = AccessibleProxyObject.class.getMethod("performSuperRemover", String.class, Object.class);
			PERFORM_SUPER_DELETER = DeletableProxyObject.class.getMethod("performSuperDelete",
					Array.newInstance(Object.class, 0).getClass());
			PERFORM_SUPER_UNDELETER = DeletableProxyObject.class.getMethod("performSuperUndelete", Boolean.TYPE);
			PERFORM_SUPER_FINDER = AccessibleProxyObject.class.getMethod("performSuperFinder", String.class, Object.class);
			PERFORM_SUPER_GETTER_ENTITY = AccessibleProxyObject.class.getMethod("performSuperGetter", String.class, Class.class);
			PERFORM_SUPER_SETTER_ENTITY = AccessibleProxyObject.class.getMethod("performSuperSetter", String.class, Object.class,
					Class.class);
			PERFORM_SUPER_ADDER_ENTITY = AccessibleProxyObject.class.getMethod("performSuperAdder", String.class, Object.class,
					Class.class);
			PERFORM_SUPER_REMOVER_ENTITY = AccessibleProxyObject.class.getMethod("performSuperRemover", String.class, Object.class,
					Class.class);
			PERFORM_SUPER_DELETER_ENTITY = DeletableProxyObject.class.getMethod("performSuperDelete", Class.class,
					Array.newInstance(Object.class, 0).getClass());
			PERFORM_SUPER_FINDER_ENTITY = AccessibleProxyObject.class.getMethod("performSuperFinder", String.class, Object.class,
					Class.class);
			IS_SERIALIZING = AccessibleProxyObject.class.getMethod("isSerializing");
			IS_DESERIALIZING = AccessibleProxyObject.class.getMethod("isDeserializing");
			IS_MODIFIED = AccessibleProxyObject.class.getMethod("isModified");
			IS_DELETED = DeletableProxyObject.class.getMethod("isDeleted");
			SET_MODIFIED = AccessibleProxyObject.class.getMethod("setModified", boolean.class);
			PERFORM_SUPER_SET_MODIFIED = AccessibleProxyObject.class.getMethod("performSuperSetModified", boolean.class);
			DELETE_OBJECT = DeletableProxyObject.class.getMethod("delete", Array.newInstance(Object.class, 0).getClass());
			UNDELETE_OBJECT = DeletableProxyObject.class.getMethod("undelete", Boolean.TYPE);
			GET_PROPERTY_CHANGE_SUPPORT = HasPropertyChangeSupport.class.getMethod("getPropertyChangeSupport");
			GET_DELETED_PROPERTY = HasPropertyChangeSupport.class.getMethod("getDeletedProperty");
			TO_STRING = Object.class.getMethod("toString");
			CLONE_OBJECT = CloneableProxyObject.class.getMethod("cloneObject");
			CLONE_OBJECT_WITH_CONTEXT = CloneableProxyObject.class.getMethod("cloneObject", Array.newInstance(Object.class, 0).getClass());
			IS_CREATED_BY_CLONING = CloneableProxyObject.class.getMethod("isCreatedByCloning");
			IS_BEING_CLONED = CloneableProxyObject.class.getMethod("isBeingCloned");
			EQUALS_OBJECT = AccessibleProxyObject.class.getMethod("equalsObject", Object.class);
			UPDATE_WITH_OBJECT = AccessibleProxyObject.class.getMethod("updateWith", Object.class);
			DESTROY = AccessibleProxyObject.class.getMethod("destroy");
			HAS_KEY = KeyValueCoding.class.getMethod("hasKey", String.class);
			OBJECT_FOR_KEY = KeyValueCoding.class.getMethod("objectForKey", String.class);
			SET_OBJECT_FOR_KEY = KeyValueCoding.class.getMethod("setObjectForKey", Object.class, String.class);
			GET_TYPE_FOR_KEY = KeyValueCoding.class.getMethod("getTypeForKey", String.class);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	public ProxyMethodHandler(PAMELAProxyFactory<I> pamelaProxyFactory, EditingContext editingContext) throws ModelDefinitionException {
		this.pamelaProxyFactory = pamelaProxyFactory;
		this.editingContext = editingContext;
		values = new HashMap<>(getModelEntity().getPropertiesSize(), 1.0f);
		initialized = !getModelEntity().hasInitializers();
		initDelegateImplementations();
	}

	private List<DelegateImplementation<? super I>> delegateImplementations;

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
		Object invoke = _invoke(self, method, proceed, args);
		if (method.getReturnType().isPrimitive() && invoke == null) {
			// Avoids an NPE
			invoke = Defaults.defaultValue(method.getReturnType());
		}
		return invoke;
	}

	public Object _invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable {

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
			internallyInvokeSetter(id, args);
			return null;
		}

		Adder adder = method.getAnnotation(Adder.class);
		if (adder != null) {
			String id = adder.value();
			internallyInvokeAdder(id, args);
			return null;
		}

		Remover remover = method.getAnnotation(Remover.class);
		if (remover != null) {
			String id = remover.value();
			internallyInvokerRemover(id, args);
			return null;
		}

		Reindexer reindexer = method.getAnnotation(Reindexer.class);
		if (reindexer != null) {
			String id = reindexer.value();
			internallyInvokerReindexer(id, args);
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
			return internallyInvokeGetter(getModelEntity().getModelProperty((String) args[0]));
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_SETTER)) {
			internallyInvokeSetter(getModelEntity().getModelProperty((String) args[0]), args[1], false);
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_ADDER)) {
			internallyInvokeAdder(getModelEntity().getModelProperty((String) args[0]), args[1], false);
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_ADDER_AT_INDEX)) {
			internallyInvokeAdderAtIndex(getModelEntity().getModelProperty((String) args[0]), args[1], (int) args[2], false);
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_REMOVER)) {
			internallyInvokeRemover(getModelEntity().getModelProperty((String) args[0]), args[1], false);
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_FINDER)) {
			internallyInvokeFinder(finder, args);
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_GETTER_ENTITY)) {
			ModelEntity<? super I> e = getModelEntityFromArg((Class<?>) args[1]);
			return internallyInvokeGetter(e.getModelProperty((String) args[0]));
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_SETTER_ENTITY)) {
			ModelEntity<? super I> e = getModelEntityFromArg((Class<?>) args[2]);
			internallyInvokeSetter(e.getModelProperty((String) args[0]), args[1], false);
			return null;
		}
		else if (PamelaUtils.methodIsEquivalentTo(method, PERFORM_SUPER_ADDER_ENTITY)) {
			ModelEntity<? super I> e = getModelEntityFromArg((Class<?>) args[2]);
			internallyInvokeAdder(e.getModelProperty((String) args[0]), args[1], false);
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
			return updateWith(args[0]);
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

	private Object internallyInvokeGetter(String propertyIdentifier) throws ModelDefinitionException {
		ModelProperty<? super I> property = getModelEntity().getModelProperty(propertyIdentifier);
		return internallyInvokeGetter(property);
	}

	private void internallyInvokeSetter(String propertyIdentifier, Object[] args) throws ModelDefinitionException {
		ModelProperty<? super I> property = getModelEntity().getModelProperty(propertyIdentifier);
		internallyInvokeSetter(property, args[0], true);
	}

	private void internallyInvokeAdder(String propertyIdentifier, Object[] args) throws ModelDefinitionException {
		ModelProperty<? super I> property = getModelEntity().getModelProperty(propertyIdentifier);
		internallyInvokeAdder(property, args[0], true);
	}

	private void internallyInvokerRemover(String id, Object[] args) throws ModelDefinitionException {
		ModelProperty<? super I> property = getModelEntity().getModelProperty(id);
		internallyInvokeRemover(property, args[0], true);
	}

	private void internallyInvokerReindexer(String id, Object[] args) throws ModelDefinitionException {
		ModelProperty<? super I> property = getModelEntity().getModelProperty(id);
		internallyInvokeReindexer(property, args[0], (Integer) args[1], true);
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
	private boolean internallyInvokeDeleter(boolean trackAtomicEdit, Object... context) throws ModelDefinitionException {

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
		oldValues = new HashMap<>();
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

	private boolean internallyInvokeUndeleter(boolean restoreProperties, boolean trackAtomicEdit) throws ModelDefinitionException {

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
					// Otherwise nullify using setter
					if (property.getSetterMethod() != null) {
						invokeSetter(property, oldValues.get(property.getPropertyIdentifier()));
					}
					else {
						internallyInvokeSetter(property, oldValues.get(property.getPropertyIdentifier()), true);
					}
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
		if (values != null) {
			values.clear();
		}
		values = null;
		if (oldValues != null) {
			oldValues.clear();
		}
		oldValues = null;
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

	private Object internallyInvokeGetter(ModelProperty<? super I> property) throws ModelDefinitionException {
		if (property == null) {
			throw new ModelExecutionException("null property while calling getter");
		}
		if (property.getCardinality() == null) {
			throw new ModelExecutionException("Invalid null cardinality for " + property);
		}
		switch (property.getCardinality()) {
			case SINGLE:
				return invokeGetterForSingleCardinality(property);
			case LIST:
				return invokeGetterForListCardinality(property);
			case MAP:
				return invokeGetterForMapCardinality(property);
			default:
				throw new ModelExecutionException("Invalid cardinality: " + property.getCardinality());
		}
	}

	private Object invokeGetterForSingleCardinality(ModelProperty<? super I> property) throws ModelDefinitionException {
		if (property.getGetter() == null) {
			throw new ModelExecutionException("Getter is not defined for property " + property);
		}
		if (property.getReturnedValue() != null) {
			// Simple implementation of ReturnedValue. This can be drastically improved
			String returnedValue = property.getReturnedValue().value();
			StringTokenizer st = new StringTokenizer(returnedValue, ".");
			Object value = this;
			ProxyMethodHandler<?> handler = this;
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				value = handler.invokeGetter(token);
				if (value != null) {
					if (st.hasMoreTokens()) {
						if (!(value instanceof ProxyObject)) {
							throw new ModelExecutionException("Cannot invoke " + st.nextToken() + " on object of type "
									+ value.getClass().getName() + " (caused by returned value: " + returnedValue + ")");
						}
						handler = (ProxyMethodHandler<?>) ((ProxyObject) value).getHandler();
					}
				}
				else {
					return null;
				}
			}
			return value;
		}
		Object returned = null;
		if (values != null) {
			returned = values.get(property.getPropertyIdentifier());
		}
		if (returned != null) {
			return returned;
		}
		else {
			Object defaultValue;
			try {
				defaultValue = property.getDefaultValue(getModelFactory());
			} catch (InvalidDataException e) {
				throw new ModelExecutionException("Invalid default value '" + property.getGetter().defaultValue() + "' for property "
						+ property + " with type " + property.getType(), e);
			}
			if (defaultValue != null) {
				values.put(property.getPropertyIdentifier(), defaultValue);
				return defaultValue;
			}
			if (property.getType().isPrimitive()) {
				throw new ModelExecutionException("No default value defined for primitive property " + property);
			}
			return null;
		}
	}

	private List<?> invokeGetterForListCardinality(ModelProperty<? super I> property) {
		if (property.getGetter() == null) {
			throw new ModelExecutionException("Getter is not defined for property " + property);
		}
		List<?> returned = (List<?>) values.get(property.getPropertyIdentifier());
		if (returned != null) {
			return returned;
		}
		else {
			Class<? extends List> listClass = getModelFactory().getListImplementationClass();
			try {
				returned = listClass.newInstance();
			} catch (InstantiationException e) {
				throw new ModelExecutionException(e);
			} catch (IllegalAccessException e) {
				throw new ModelExecutionException(e);
			}
			if (returned != null) {
				values.put(property.getPropertyIdentifier(), returned);
				return returned;
			}
			return null;
		}
	}

	private Map<?, ?> invokeGetterForMapCardinality(ModelProperty<? super I> property) {
		if (property.getGetter() == null) {
			throw new ModelExecutionException("Getter is not defined for property " + property);
		}
		Map<?, ?> returned = (Map<?, ?>) values.get(property.getPropertyIdentifier());
		if (returned != null) {
			return returned;
		}
		else {
			Class<? extends Map> mapClass = getModelFactory().getMapImplementationClass();
			try {
				returned = mapClass.newInstance();
			} catch (InstantiationException e) {
				throw new ModelExecutionException(e);
			} catch (IllegalAccessException e) {
				throw new ModelExecutionException(e);
			}
			if (returned != null) {
				values.put(property.getPropertyIdentifier(), returned);
				return returned;
			}
			return null;
		}
	}

	public void invokeSetterForDeserialization(ModelProperty<? super I> property, Object value) throws ModelDefinitionException {
		if (property.getSetterMethod() != null) {
			invokeSetter(property, value);
		}
		else {
			internallyInvokeSetter(property, value, true);
		}
	}

	private void internallyInvokeSetter(ModelProperty<? super I> property, Object value, boolean trackAtomicEdit)
			throws ModelDefinitionException {
		Object oldValue = invokeGetter(property);
		if (trackAtomicEdit && getUndoManager() != null) {
			if (oldValue != value) {
				getUndoManager().addEdit(new SetCommand<>(getObject(), getModelEntity(), property, oldValue, value, getModelFactory()));
			}
		}
		switch (property.getCardinality()) {
			case SINGLE:
				invokeSetterForSingleCardinality(property, value);
				break;
			case LIST:
				invokeSetterForListCardinality(property, value);
				break;
			case MAP:
				invokeSetterForMapCardinality(property, value);
				break;
			default:
				throw new ModelExecutionException("Invalid cardinality: " + property.getCardinality());
		}
	}

	/**
	 * This map contains all scheduled set for a given property<br>
	 * We need to retain values beeing set in case of bidirectional inverse properties patterns, to avoid infinite loop
	 */
	private final Map<ModelProperty<? super I>, Object> scheduledSets = new HashMap<>();

	private void invokeSetterForSingleCardinality(ModelProperty<? super I> property, Object value) throws ModelDefinitionException {

		if (scheduledSets.get(property) == value) {
			// This set was already scheduled (we are entering in an infinite loop): break NOW
			return;
		}

		scheduledSets.remove(property);

		// System.out.println("Object " + getModelEntity().getImplementedInterface().getSimpleName() + " set "
		// + property.getPropertyIdentifier() + " with " + value);

		if (property.getSetter() == null && !isDeserializing() && !initializing && !createdByCloning && !deleting && !undeleting) {
			throw new ModelExecutionException("Setter is not defined for property " + property);
		}
		// Object oldValue = invokeGetter(property);
		Object oldValue = internallyInvokeGetter(property);

		// Is it a real change ?
		if (!isEqual(oldValue, value, new HashSet<>())) {
			// System.out.println("Change for " + oldValue + " to " + value);
			boolean hasInverse = property.hasInverseProperty();

			scheduledSets.put(property, value);

			// First handle inverse property for oldValue
			if (hasInverse && oldValue != null) {
				ProxyMethodHandler<Object> oppositeHandler = getModelFactory().getHandler(oldValue);
				if (oppositeHandler == null) {
					// Should not happen
					throw new ModelExecutionException(
							"Opposite entity of " + property + " is of type " + oldValue.getClass().getName() + " is not a ModelEntity.");
				}
				ModelProperty<? super Object> inverseProperty = property.getInverseProperty(oppositeHandler.getModelEntity());
				switch (inverseProperty.getCardinality()) {
					case SINGLE:
						Object oppositeOldValue = oppositeHandler.invokeGetter(inverseProperty);
						if (oppositeOldValue != null) {
							// System.out.println("Object " + inverseProperty.getModelEntity().getImplementedInterface().getSimpleName() +
							// " set "
							// + inverseProperty.getPropertyIdentifier() + " with " + null);
							oppositeHandler.invokeSetter(inverseProperty, null);
						}
						else {
							// No need to reset inverse setter, as it is already set to null
						}
						break;
					case LIST:
						// TODO: what is same object has multiple occurences in the list ???
						List<Object> oppositeListValue = (List<Object>) oppositeHandler.invokeGetter(inverseProperty);
						if (oppositeListValue.contains(getObject())) {
							oppositeHandler.invokeRemover(inverseProperty, getObject());
						}
						else {
							// No need to remove objet from opposite property object was not inside
						}
						break;
					case MAP:
						break;
					default:
						throw new ModelExecutionException("Invalid cardinality: " + inverseProperty.getCardinality());
				}
			}

			// Now do the job, internally
			if (value == null) {
				values.remove(property.getPropertyIdentifier());
			}
			else {
				values.put(property.getPropertyIdentifier(), value);
			}
			firePropertyChange(property.getPropertyIdentifier(), oldValue, value);
			if (getModelEntity().getModify() != null && getModelEntity().getModify().synchWithForward()
					&& property.getPropertyIdentifier().equals(getModelEntity().getModify().forward())) {
				if (oldValue instanceof HasPropertyChangeSupport) {
					((HasPropertyChangeSupport) oldValue).getPropertyChangeSupport().removePropertyChangeListener(MODIFIED, this);
				}
				if (value instanceof HasPropertyChangeSupport) {
					((HasPropertyChangeSupport) value).getPropertyChangeSupport().addPropertyChangeListener(MODIFIED, this);
				}
			}
			// Now handle inverse property for newValue
			if (hasInverse && value != null) {
				ProxyMethodHandler<Object> oppositeHandler = getModelFactory().getHandler(value);
				if (oppositeHandler == null) {
					// Should not happen
					throw new ModelExecutionException(
							"Opposite entity of " + property + " is of type " + value.getClass().getName() + " is not a ModelEntity.");
				}
				ModelProperty<? super Object> inverseProperty = property.getInverseProperty(oppositeHandler.getModelEntity());
				switch (inverseProperty.getCardinality()) {
					case SINGLE:
						Object oppositeOldValue = oppositeHandler.invokeGetter(inverseProperty);
						if (oppositeOldValue != getObject()) {
							// System.out.println("Object " + inverseProperty.getModelEntity().getImplementedInterface().getSimpleName() +
							// " set "
							// + inverseProperty.getPropertyIdentifier() + " with " + getObject());
							oppositeHandler.invokeSetter(inverseProperty, getObject());
						}
						else {
							// No need to set inverse property, because this is already right value
						}
						break;
					case LIST:
						// TODO: what is same object has multiple occurences in the list ???
						List<Object> oppositeListValue = (List<Object>) oppositeHandler.invokeGetter(inverseProperty);
						if (!oppositeListValue.contains(getObject())) {
							oppositeHandler.invokeAdder(inverseProperty, getObject());
						}
						else {
							// No need to add object to inverse property, because this is already inside
						}
						break;
					case MAP:
						break;
					default:
						throw new ModelExecutionException("Invalid cardinality: " + inverseProperty.getCardinality());
				}
			}

			if (property.isSerializable()) {
				invokeSetModified(true);
			}

		}
	}

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

	private void invokeSetModified(boolean modified) throws ModelDefinitionException {
		if (getObject() instanceof AccessibleProxyObject) {
			((AccessibleProxyObject) getObject()).setModified(modified);
		}
		else {
			internallyInvokeSetModified(modified);
		}
	}

	private void invokeSetterForListCardinality(ModelProperty<? super I> property, Object value) {
		if (property.getSetter() == null && !isDeserializing() && !initializing && !createdByCloning && !deleting) {
			throw new ModelExecutionException("Setter is not defined for property " + property);
		}
		if (value != null && !(value instanceof List)) {
			throw new ModelExecutionException("Trying to set a " + value.getClass().getName() + " on property " + property + " but only "
					+ List.class.getName() + " instances or null is allowed");
		}
		List<?> oldValue = (List<?>) invokeGetter(property);
		for (Object o : new ArrayList<>(oldValue)) {
			invokeRemover(property, o);
		}
		if (value != null) {
			for (Object o : (List<?>) value) {
				invokeAdder(property, o);
			}
		}
	}

	private void invokeSetterForMapCardinality(ModelProperty<? super I> property, Object value) {
		if (property.getSetter() == null && !isDeserializing() && !initializing && !createdByCloning && !deleting && !undeleting) {
			throw new ModelExecutionException("Setter is not defined for property " + property);
		}
		// TODO implement this
		throw new UnsupportedOperationException("Setter for MAP: not implemented yet");
	}

	public void invokeAdderForDeserialization(ModelProperty<? super I> property, Object value) throws ModelDefinitionException {
		if (property.getAdderMethod() != null) {
			invokeAdder(property, value);
		}
		else {
			internallyInvokeAdder(property, value, true);
		}
	}

	private void internallyInvokeAdder(ModelProperty<? super I> property, Object value, boolean trackAtomicEdit)
			throws ModelDefinitionException {
		// System.out.println("Invoke ADDER "+property.getPropertyIdentifier());
		if (trackAtomicEdit && getUndoManager() != null) {
			getUndoManager().addEdit(new AddCommand<>(getObject(), getModelEntity(), property, value, getModelFactory()));
		}
		switch (property.getCardinality()) {
			case SINGLE:
				throw new ModelExecutionException(
						"Cannot invoke ADDER on " + property.getPropertyIdentifier() + ": Invalid cardinality SINGLE");
			case LIST:
				invokeAdderForListCardinality(property, value, -1);
				break;
			case MAP:
				invokeAdderForMapCardinality(property, value);
				break;
			default:
				throw new ModelExecutionException("Invalid cardinality: " + property.getCardinality());
		}
	}

	private void internallyInvokeAdderAtIndex(ModelProperty<? super I> property, Object value, int index, boolean trackAtomicEdit)
			throws ModelDefinitionException {
		// System.out.println("Invoke ADDER "+property.getPropertyIdentifier());
		if (trackAtomicEdit && getUndoManager() != null) {
			getUndoManager().addEdit(new AddCommand<>(getObject(), getModelEntity(), property, value, index, getModelFactory()));
		}
		switch (property.getCardinality()) {
			case SINGLE:
				throw new ModelExecutionException(
						"Cannot invoke ADDER on " + property.getPropertyIdentifier() + ": Invalid cardinality SINGLE");
			case LIST:
				invokeAdderForListCardinality(property, value, index);
				break;
			case MAP:
				invokeAdderForMapCardinality(property, value);
				break;
			default:
				throw new ModelExecutionException("Invalid cardinality: " + property.getCardinality());
		}
	}

	private void invokeAdderForListCardinality(ModelProperty<? super I> property, Object value, int index) throws ModelDefinitionException {
		if (property.getAdder() == null && !isDeserializing() && !initializing && !createdByCloning && !deleting && !undeleting) {
			throw new ModelExecutionException("Adder is not defined for property " + property);
		}
		List list = (List) invokeGetter(property);

		if (property.getAllowsMultipleOccurences() || !list.contains(value)) {
			if (index == -1) {
				list.add(value);
			}
			else {
				list.add(index, value);
			}
			firePropertyChange(property.getPropertyIdentifier(), null, value);
			// Handle inverse property for new value
			if (property.hasInverseProperty() && value != null) {
				ProxyMethodHandler<Object> oppositeHandler = getModelFactory().getHandler(value);
				if (oppositeHandler == null) {
					// Should not happen
					throw new ModelExecutionException(
							"Opposite entity of " + property + " is of type " + value.getClass().getName() + " is not a ModelEntity.");
				}
				ModelProperty<? super Object> inverseProperty = property.getInverseProperty(oppositeHandler.getModelEntity());
				switch (inverseProperty.getCardinality()) {
					case SINGLE:
						oppositeHandler.invokeSetter(inverseProperty, getObject());
						break;
					case LIST:
						oppositeHandler.invokeAdder(inverseProperty, getObject());
						break;
					case MAP:
						break;
					default:
						throw new ModelExecutionException("Invalid cardinality: " + inverseProperty.getCardinality());
				}
			}
			if (property.isSerializable()) {
				invokeSetModified(true);
			}
		}
	}

	private void invokeAdderForMapCardinality(ModelProperty<? super I> property, Object value) {
		if (property.getAdder() == null && !isDeserializing() && !initializing && !createdByCloning) {
			throw new ModelExecutionException("Adder is not defined for property " + property);
		}
		// TODO implement this
		throw new UnsupportedOperationException("Adder for MAP: not implemented yet");
	}

	private void internallyInvokeRemover(ModelProperty<? super I> property, Object value, boolean trackAtomicEdit)
			throws ModelDefinitionException {
		// System.out.println("Invoke REMOVER "+property.getPropertyIdentifier());
		if (trackAtomicEdit && getUndoManager() != null) {
			getUndoManager().addEdit(new RemoveCommand<>(getObject(), getModelEntity(), property, value, getModelFactory()));
		}
		switch (property.getCardinality()) {
			case SINGLE:
				throw new ModelExecutionException(
						"Cannot invoke REMOVER on " + property.getPropertyIdentifier() + ": Invalid cardinality SINGLE");
			case LIST:
				invokeRemoverForListCardinality(property, value);
				break;
			case MAP:
				invokeRemoverForMapCardinality(property, value);
				break;
			default:
				throw new ModelExecutionException("Invalid cardinality: " + property.getCardinality());
		}
	}

	private void internallyInvokeReindexer(ModelProperty<? super I> property, Object value, int index, boolean trackAtomicEdit) {
		// System.out.println("Invoke REINDEXER "+property.getPropertyIdentifier());
		if (trackAtomicEdit && getUndoManager() != null) {
			getUndoManager().addEdit(new RemoveCommand<>(getObject(), getModelEntity(), property, value, getModelFactory()));
			getUndoManager().addEdit(new AddCommand<>(getObject(), getModelEntity(), property, value, getModelFactory()));
		}

		List list = (List) invokeGetter(property);
		int oldIndex = list.indexOf(value);

		if (oldIndex > -1) {
			if (oldIndex != index) {
				list.remove(value);
				if (index == -1) {
					list.add(value);
				}
				else {
					list.add(index, value);
				}
				firePropertyChange(property.getPropertyIdentifier(), oldIndex, index);
			}
			else {
				// Index is already correct
			}
		}
		else {
			System.err.println("Inconsistant data: could not find object: " + value);
		}
	}

	private void invokeRemoverForListCardinality(ModelProperty<? super I> property, Object value) throws ModelDefinitionException {
		if (property.getRemover() == null) {
			throw new ModelExecutionException("Remover is not defined for property " + property);
		}
		List<?> list = (List<?>) invokeGetter(property);

		if (list.contains(value)) {
			list.remove(value);
			firePropertyChange(property.getPropertyIdentifier(), value, null);
			// Handle inverse property for new value
			if (property.hasInverseProperty() && value != null) {
				ProxyMethodHandler<Object> oppositeHandler = getModelFactory().getHandler(value);
				if (oppositeHandler == null) {
					// Should not happen
					throw new ModelExecutionException(
							"Opposite entity of " + property + " is of type " + value.getClass().getName() + " is not a ModelEntity.");
				}
				ModelProperty<? super Object> inverseProperty = property.getInverseProperty(oppositeHandler.getModelEntity());
				switch (inverseProperty.getCardinality()) {
					case SINGLE:
						oppositeHandler.invokeSetter(inverseProperty, null);
						break;
					case LIST:
						oppositeHandler.invokeRemover(inverseProperty, getObject());
						break;
					case MAP:
						break;
					default:
						throw new ModelExecutionException("Invalid cardinality: " + inverseProperty.getCardinality());
				}
			}
			if (property.isSerializable()) {
				invokeSetModified(true);
			}
		}
	}

	private void invokeRemoverForMapCardinality(ModelProperty<? super I> property, Object value) {
		if (property.getRemover() == null) {
			throw new ModelExecutionException("Remover is not defined for property " + property);
		}
		// TODO implement this
		throw new UnsupportedOperationException("Remover for MAP: not implemented yet");
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

	private boolean isObjectAttributeEquals(Object o, String attribute, Object value) throws ModelDefinitionException {
		ProxyMethodHandler<?> handler = getModelFactory().getHandler(o);
		if (handler != null) {
			Object attributeValue = handler.invokeGetter(attribute);
			return isEqual(attributeValue, value, new HashSet<>());
		}
		else {
			throw new ModelDefinitionException(
					"Found object of type " + o.getClass().getName() + " but is not an instanceof ProxyObject:\n" + o);
		}
	}

	private static boolean isEqual(Object oldValue, Object newValue, Set<Object> seen) {
		seen.add(oldValue);
		if (oldValue == null) {
			return newValue == null;
		}
		if (oldValue == newValue) {
			return true;
		}
		if (oldValue instanceof AccessibleProxyObject && newValue instanceof AccessibleProxyObject) {
			return ((AccessibleProxyObject) oldValue).equalsObject(newValue);
		}
		if (oldValue instanceof List && newValue instanceof List) {
			List<Object> l1 = (List<Object>) oldValue;
			List<Object> l2 = (List<Object>) newValue;
			if (l1.size() != l2.size()) {
				return false;
			}
			for (int i = 0; i < l1.size(); i++) {
				Object v1 = l1.get(i);
				Object v2 = l2.get(i);
				if (seen.contains(v1))
					continue;

				if (!isEqual(v1, v2, seen)) {
					return false;
				}
			}
			return true;
		}
		return oldValue.equals(newValue);

	}

	/*private Object cloneObject() throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException
		{
		System.out.println("Tiens je clone "+getObject());
	
		if (!(getObject() instanceof CloneableProxyObject)) throw new CloneNotSupportedException();
	
		Hashtable<CloneableProxyObject,Object> clonedObjects = new Hashtable<>();
		Object returned = performClone(clonedObjects);
		for (CloneableProxyObject o : clonedObjects.keySet()) {
			ProxyMethodHandler<?> clonedObjectHandler = getModelFactory().getHandler(o);
			clonedObjectHandler.finalizeClone(clonedObjects);
		}
	
		private Object appendToClonedObjects(Hashtable<CloneableProxyObject,Object> clonedObjects, CloneableProxyObject objectToCloneOrReference) throws ModelExecutionException, ModelDefinitionException
		{
			Object returned = clonedObjects.get(objectToCloneOrReference);
			if (returned != null) return returned;
			ProxyMethodHandler<?> clonedValueHandler = getModelFactory().getHandler(objectToCloneOrReference);
			returned = clonedValueHandler.performClone(clonedObjects);
			System.out.println("for "+objectToCloneOrReference+" clone is "+returned);
			return returned;
		}
	
		private Object performClone(Hashtable<CloneableProxyObject,Object> clonedObjects) throws ModelExecutionException, ModelDefinitionException
		{
			System.out.println("******* performClone "+getObject());
	
			Object returned = null;
			try {
				returned = getModelEntity().newInstance();
			} catch (IllegalArgumentException e) {
				throw new ModelExecutionException(e);
			} catch (NoSuchMethodException e) {
				throw new ModelExecutionException(e);
			} catch (InstantiationException e) {
				throw new ModelExecutionException(e);
			} catch (IllegalAccessException e) {
				throw new ModelExecutionException(e);
			} catch (InvocationTargetException e) {
				throw new ModelExecutionException(e);
			}
			clonedObjects.put((CloneableProxyObject)getObject(),returned);
	
			ProxyMethodHandler<?> clonedObjectHandler = getModelFactory().getHandler(returned);
			Enumeration<ModelProperty<? super I>> properties = getModelEntity().getProperties();
			while(properties.hasMoreElements()) {
				ModelProperty p = properties.nextElement();
				switch (p.getCardinality()) {
				case SINGLE:
					Object singleValue = invokeGetter(p);
					switch (p.getCloningStrategy()) {
					case CLONE:
						if (getModelFactory().isModelEntity(p.getType()) && singleValue instanceof CloneableProxyObject) {
							appendToClonedObjects(clonedObjects, (CloneableProxyObject)singleValue);
						}
						break;
					case REFERENCE:
						break;
					case FACTORY:
						break;
					case IGNORE:
						break;
					}
					break;
				case LIST:
					List values = (List)invokeGetter(p);
					for (Object value : values) {
						switch (p.getCloningStrategy()) {
						case CLONE:
							if (getModelFactory().isModelEntity(p.getType()) && value instanceof CloneableProxyObject) {
								appendToClonedObjects(clonedObjects, (CloneableProxyObject)value);
							}
							break;
						case REFERENCE:
							break;
						case FACTORY:
							break;
						case IGNORE:
							break;
						}
					}
					break;
				default:
					break;
				}
	
			}
	
			return returned;
		}
	
		private Object finalizeClone(Hashtable<CloneableProxyObject,Object> clonedObjects) throws ModelExecutionException, ModelDefinitionException
		{
			Object clonedObject = clonedObjects.get(getObject());
	
			System.out.println("Tiens je finalise le clone pour "+getObject()+" le clone c'est "+clonedObject);
	
		ProxyMethodHandler<?> clonedObjectHandler = getModelFactory().getHandler(clonedObject);
	
			Enumeration<ModelProperty<? super I>> properties = getModelEntity().getProperties();
	
			while(properties.hasMoreElements()) {
				ModelProperty p = properties.nextElement();
				switch (p.getCardinality()) {
				case SINGLE:
					Object singleValue = invokeGetter(p);
					switch (p.getCloningStrategy()) {
					case CLONE:
						if (getModelFactory().getStringEncoder().isConvertable(p.getType())) {
							Object clonedValue = null;
							try {
								String clonedValueAsString = getModelFactory().getStringEncoder().toString(singleValue);
								clonedValue = getModelFactory().getStringEncoder().fromString(p.getType(),clonedValueAsString);
							} catch (InvalidDataException e) {
								throw new ModelExecutionException(e);
							}
							clonedObjectHandler.invokeSetter(p,clonedValue);
						}
						else if (getModelFactory().isModelEntity(p.getType()) && singleValue instanceof CloneableProxyObject) {
							Object clonedValue = clonedObjects.get(singleValue);
							clonedObjectHandler.invokeSetter(p,clonedValue);
						}
						break;
					case REFERENCE:
						Object referenceValue = (singleValue != null ? clonedObjects.get(singleValue) : null);
						if (referenceValue == null) referenceValue = singleValue;
						clonedObjectHandler.invokeSetter(p,referenceValue);
						break;
					case FACTORY:
						// TODO Not implemented
						break;
					case IGNORE:
						break;
					}
					break;
				case LIST:
					List values = (List)invokeGetter(p);
					System.out.println("values:"+values.hashCode()+" "+values);
					List valuesToClone = new ArrayList<>(values);
					for (Object value : valuesToClone) {
						switch (p.getCloningStrategy()) {
						case CLONE:
							if (getModelFactory().getStringEncoder().isConvertable(p.getType())) {
								Object clonedValue = null;
								try {
									String clonedValueAsString = getModelFactory().getStringEncoder().toString(value);
									clonedValue = getModelFactory().getStringEncoder().fromString(p.getType(),clonedValueAsString);
								} catch (InvalidDataException e) {
									throw new ModelExecutionException(e);
								}
								List l = (List)clonedObjectHandler.invokeGetter(p);
								System.out.println("l:"+l.hashCode()+" "+l);
								clonedObjectHandler.invokeAdder(p,clonedValue);
							}
							else if (getModelFactory().isModelEntity(p.getType()) && value instanceof CloneableProxyObject) {
								Object clonedValue = clonedObjects.get(value);
								clonedObjectHandler.invokeAdder(p,clonedValue);
							}
							break;
						case REFERENCE:
							Object referenceValue = (value != null ? clonedObjects.get(value) : null);
							if (referenceValue == null) referenceValue = value;
							clonedObjectHandler.invokeAdder(p,referenceValue);
							break;
						case FACTORY:
							// TODO Not implemented
							break;
						case IGNORE:
							break;
						}
	
					}
					break;
				default:
					break;
				}
	
			}
	
			return clonedObject;
		}*/

	public boolean equalsObject(Object obj) {
		if (getObject() == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		ProxyMethodHandler<?> oppositeObjectHandler = getModelFactory().getHandler(obj);
		if (oppositeObjectHandler == null) {
			// Other object is not handled by the same factory
			return false;
		}
		if (getModelEntity() != oppositeObjectHandler.getModelEntity()) {
			return false;
		}

		Iterator<ModelProperty<? super I>> properties;
		try {
			properties = getModelEntity().getProperties();
		} catch (ModelDefinitionException e) {
			return false;
		}
		while (properties.hasNext()) {
			ModelProperty p = properties.next();
			if (p.isRelevantForEqualityComputation()) {
				switch (p.getCardinality()) {
					case SINGLE:
						Object singleValue = invokeGetter(p);
						Object oppositeValue = oppositeObjectHandler.invokeGetter(p);
						if (!isEqual(singleValue, oppositeValue, new HashSet<>())) {
							// System.out.println("Equals fails because of SINGLE property " + p);
							return false;
						}
						break;
					case LIST:
						List<Object> values = (List) invokeGetter(p);
						List<Object> oppositeValues = (List) oppositeObjectHandler.invokeGetter(p);
						if (!isEqual(values, oppositeValues, new HashSet<>())) {
							// System.out.println("Equals fails because of LIST property " + p);
							return false;
						}
						break;
					default:
						break;
				}
			}
		}
		// System.out.println("ok, equals return true for " + getObject() + " and " + object);
		return true;
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
	public boolean updateWith(Object obj) {

		// System.out.println("updateWith between " + getObject() + " and " + obj);

		if (getObject() == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		ProxyMethodHandler<?> oppositeObjectHandler = getModelFactory().getHandler(obj);
		if (oppositeObjectHandler == null) {
			// Other object is not handled by the same factory
			return false;
		}
		if (getModelEntity() != oppositeObjectHandler.getModelEntity()) {
			return false;
		}

		// System.out.println("Distance: " + getDistance(obj));

		Iterator<ModelProperty<? super I>> properties;
		try {
			properties = getModelEntity().getProperties();
		} catch (ModelDefinitionException e) {
			return false;
		}
		while (properties.hasNext()) {
			ModelProperty p = properties.next();

			if (p.isSerializable()) {
				// System.out.println("[" + Thread.currentThread().getName() + "] Propriete " + p.getPropertyIdentifier());

				switch (p.getCardinality()) {
					case SINGLE:
						Object singleValue = invokeGetter(p);
						Object oppositeValue = oppositeObjectHandler.invokeGetter(p);
						// System.out.println("[" + Thread.currentThread().getName() + "] Ici-1 avec " + p.getPropertyIdentifier());
						if (!isEqual(singleValue, oppositeValue, new HashSet<>())) {
							if (p.getAccessedEntity() != null && singleValue instanceof AccessibleProxyObject) {
								// System.out
								// .println("[" + Thread.currentThread().getName() + "] Ici-3 avec " + p.getPropertyIdentifier());
								((AccessibleProxyObject) singleValue).updateWith(oppositeValue);
							}
							else {
								// System.out
								// .println("[" + Thread.currentThread().getName() + "] Ici-4 avec " + p.getPropertyIdentifier());
								invokeSetter(p, oppositeValue);
							}
						}
						break;
					case LIST:
						Map<Object, Integer> reindex = new LinkedHashMap<>();
						List<Object> values = invokeGetterForListCardinality(p);
						List<Object> oppositeValues = oppositeObjectHandler.invokeGetterForListCardinality(p);
						ListMatching matching = match(values, oppositeValues);
						// System.out.println("For property " + p.getPropertyIdentifier() + " matching=" + matching);
						for (Matched m : matching.matchedList) {
							// System.out.println("match " + m.idx1 + " with " + m.idx2);
							Object o1 = values.get(m.idx1);
							Object o2 = oppositeValues.get(m.idx2);
							if (o1 instanceof AccessibleProxyObject) {
								((AccessibleProxyObject) o1).updateWith(o2);
							}
							// Store desired index
							reindex.put(o1, m.idx2);
						}
						// Do it in reverse order to avoid IndexOutOfBoundException !!!
						for (int i = matching.removed.size() - 1; i >= 0; i--) {
							Removed r = matching.removed.get(i);
							Object removedObject = values.get(r.removedIndex);
							invokeRemover(p, removedObject);
						}
						for (Added a : matching.added) {
							Object addedObject = oppositeValues.get(a.originalIndex);
							invokeAdder(p, addedObject);
							// Store desired index
							reindex.put(addedObject, a.insertedIndex);
						}
						// Now handle eventual reindexing of property values
						for (Object o : reindex.keySet()) {
							int idx = reindex.get(o);
							if (values.indexOf(o) != idx) {
								// System.out.println("Moving " + values.indexOf(o) + " to " + idx);
								invokeReindexer(p, o, idx);
							}
						}

						break;
					case MAP:
						System.err.println("Not implemented: MAP support for updateWith()");
						break;
					default:
						break;
				}
			}
		}

		// System.out.println("ok, equals return true for " + getObject() + " and " + object);
		return true;
	}

	/**
	 * Compute the distance (double value between 0.0 and 1.0) between this object and an opposite object (which must be of right type!) If
	 * two objects are equals, return 0. If two objects are totally differents, return 1.
	 * 
	 * @param object
	 * @return
	 */
	public double getDistance(Object obj) {
		if (getObject() == obj) {
			return 0.0;
		}
		if (obj == null) {
			return 1.0;
		}
		ProxyMethodHandler oppositeObjectHandler = getModelFactory().getHandler(obj);
		if (oppositeObjectHandler == null) {
			// Other object is not handled by the same factory
			return 1.0;
		}
		if (getModelEntity() != oppositeObjectHandler.getModelEntity()) {
			return 1.0;
		}

		Iterator<ModelProperty<? super I>> properties;
		try {
			properties = getModelEntity().getProperties();
		} catch (ModelDefinitionException e) {
			return 1.0;
		}

		double distance = 0.0;
		double totalPonderation = 0.0;

		while (properties.hasNext()) {
			ModelProperty p = properties.next();

			if (p.isSerializable()) {

				double propertyPonderation = 1.0;

				switch (p.getCardinality()) {
					case SINGLE:
						Object singleValue = invokeGetter(p);
						Object oppositeValue = oppositeObjectHandler.invokeGetter(p);
						propertyPonderation = getPropertyPonderation(p);
						if (singleValue != null || oppositeValue != null) {
							totalPonderation += propertyPonderation;
							if (!isEqual(singleValue, oppositeValue, new HashSet<>())) {
								double valueDistance = getDistanceBetweenValues(singleValue, oppositeValue);
								distance = distance + valueDistance * propertyPonderation;
								// System.out.println("Property " + p.getPropertyIdentifier() + " distance=" + valueDistance + "
								// ponderation="
								// + propertyPonderation);
							}
							else {
								// System.out.println(
								// "Property " + p.getPropertyIdentifier() + " distance=0.0" + " ponderation=" + propertyPonderation);
							}
						}
						else {
							// null values are ignored and not taken under account
						}
						break;
					case LIST:
						List<Object> values = invokeGetterForListCardinality(p);
						List<Object> oppositeValues = oppositeObjectHandler.invokeGetterForListCardinality(p);
						propertyPonderation = Math.max(values != null ? values.size() : 0,
								oppositeValues != null ? oppositeValues.size() : 0);
						if ((values != null && values.size() > 0) || (oppositeValues != null && oppositeValues.size() > 0)) {
							totalPonderation += propertyPonderation;
							if (!isEqual(values, oppositeValues, new HashSet<>())) {
								double valueDistance = getDistanceBetweenListValues(values, oppositeValues);
								distance = distance + valueDistance * propertyPonderation;
								// System.out.println("Property " + p.getPropertyIdentifier() + " distance=" + valueDistance + "
								// ponderation="
								// + propertyPonderation);
							}
							else {
								// System.out.println(
								// "Property " + p.getPropertyIdentifier() + " distance=0.0" + " ponderation=" + propertyPonderation);
							}
						}
						else {
							// null values are ignored and not taken under account
						}
						break;
					case MAP:
						System.err.println("Not implemented: MAP support for getDistance()");
						break;
					default:
						break;
				}
			}
		}

		if (totalPonderation > 0) {
			return distance / totalPonderation;
		}

		return 0.0;
	}

	private static double getPropertyPonderation(ModelProperty<?> p) {
		double propertyPonderation = 1.0;
		if (TypeUtils.isPrimitive(p.getType()) || p.getType().equals(String.class) || p.isStringConvertable()) {
			propertyPonderation = 1.0;
		}
		if (p.getAccessedEntity() != null) {
			propertyPonderation = p.getAccessedEntity().getPropertiesSize();
		}
		// System.out.println("Ponderation for property: " + p.getPropertyIdentifier() + " " + propertyPonderation);
		return propertyPonderation;
	}

	public double getDistanceBetweenValues(Object v1, Object v2) {
		if (v1 == null) {
			return (v2 == null ? 0.0 : 1.0);
		}
		if (v2 == null) {
			return (v1 == null ? 0.0 : 1.0);
		}
		if (v1 == v2) {
			return 0;
		}
		if (v1.equals(v2)) {
			return 0;
		}
		if (TypeUtils.isPrimitive(v1.getClass()) || v1 instanceof String) {
			String s1 = v1.toString();
			String s2 = v2.toString();
			return (double) StringUtils.levenshteinDistance(s1, s2) / (double) Math.max(s1.length(), s2.length());
		}
		if (v1 instanceof AccessibleProxyObject && v2 instanceof AccessibleProxyObject) {
			ProxyMethodHandler<?> handler = getModelFactory().getHandler(v1);
			return handler.getDistance(v2);
		}
		return 1.0;
	}

	private double getDistanceBetweenListValues(List<Object> l1, List<Object> l2) {
		if (l1 == null) {
			return (l2 == null ? 0.0 : 1.0);
		}
		if (l2 == null) {
			return (l1 == null ? 0.0 : 1.0);
		}
		if (l1 == l2) {
			return 0;
		}
		if (l1.equals(l2)) {
			return 0;
		}
		ListMatching matching = match(l1, l2);
		// System.out.println("Matching=" + matching);
		double total = matching.added.size() + matching.removed.size() + matching.matchedList.size();
		double score = matching.added.size() + matching.removed.size();
		for (Matched m : matching.matchedList) {
			Object o1 = l1.get(m.idx1);
			Object o2 = l2.get(m.idx2);
			score += getDistanceBetweenValues(o1, o2);
		}
		return score / total;
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
				invokeSetter(modelProperty, clipboard.getSingleContents());
				return clipboard.getSingleContents();
			}
			else if (modelProperty.getAddPastingPoint() == pp) {
				if (clipboard.isSingleObject()) {
					invokeAdder(modelProperty, clipboard.getSingleContents());
					return clipboard.getSingleContents();
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
					return returned;
				}
			}
		}

		return null;
	}

	@Override
	public String toString() {
		return internallyInvokeToString();
	}

	private String internallyInvokeToString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getModelEntity().getImplementedInterface().getSimpleName() + "[");
		List<String> variables = new ArrayList<>(values.keySet());
		Collections.sort(variables);
		for (String var : variables) {
			Object obj = values.get(var);
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

	static class Matched {
		int idx1 = -1;
		int idx2 = -1;

		public Matched(int idx1, int idx2) {
			super();
			this.idx1 = idx1;
			this.idx2 = idx2;
		}

		@Override
		public String toString() {
			return "Matched(" + idx1 + "," + idx2 + ")";
		}
	}

	static class Added {
		// Index of object in new list
		int originalIndex = -1;
		int insertedIndex = -1;

		public Added(int originalIndex, int insertedIndex) {
			super();
			this.originalIndex = originalIndex;
			this.insertedIndex = insertedIndex;
		}

		@Override
		public String toString() {
			return "Added(" + originalIndex + "," + insertedIndex + ")";
		}
	}

	static class Removed {
		// Index of object in initial list
		int removedIndex = -1;

		public Removed(int removedIndex) {
			super();
			this.removedIndex = removedIndex;
		}

		@Override
		public String toString() {
			return "Removed(" + removedIndex + ")";
		}
	}

	static class ListMatching {
		List<Removed> removed = new ArrayList<>();
		List<Added> added = new ArrayList<>();
		List<Matched> matchedList = new ArrayList<>();

		public Matched getMatchedForList2Index(int index) {
			for (Matched m : matchedList) {
				if (m.idx2 == index) {
					return m;
				}
			}
			return null;
		}

		public Matched getMatchedForList1Index(int index) {
			for (Matched m : matchedList) {
				if (m.idx1 == index) {
					return m;
				}
			}
			return null;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("Matched:" + matchedList);
			sb.append(" Added:" + added);
			sb.append(" Removed:" + removed);
			return sb.toString();
		}
	}

	/**
	 * Compute optimal matching between two lists of objects
	 * 
	 * @param l1
	 * @param l2
	 * @return
	 */
	private ListMatching match(List<Object> l1, List<Object> l2) {
		ListMatching returned = bruteForceMatch(l1, l2);
		return returned;
	}

	/**
	 * A functional algorithm, but not really optimal
	 * 
	 * @param l1
	 * @param l2
	 * @return
	 */
	private ListMatching bruteForceMatch(List<Object> l1, List<Object> l2) {
		ListMatching returned = new ListMatching();

		List<Object> list1 = new ArrayList<>(l1);
		List<Object> list2 = new ArrayList<>(l2);

		while (list1.size() > 0 && list2.size() > 0) {
			Matched matched = getBestMatch(list1, list2);
			if (matched == null) {
				break;
			}
			Object o1 = list1.get(matched.idx1);
			Object o2 = list2.get(matched.idx2);
			matched.idx1 = l1.indexOf(list1.get(matched.idx1));
			matched.idx2 = l2.indexOf(list2.get(matched.idx2));
			// System.out.println(
			// "Matched " + getModelFactory().stringRepresentation(o1) + " and " + getModelFactory().stringRepresentation(o2));
			returned.matchedList.add(matched);
			list1.remove(o1);
			list2.remove(o2);
		}

		if (list1.size() > 0) {
			// Removed
			for (int i = 0; i < list1.size(); i++) {
				returned.removed.add(new Removed(l1.indexOf(list1.get(i))));
			}
		}

		if (list2.size() > 0) {
			// Added
			for (int i = 0; i < list2.size(); i++) {
				int insertionIndex = -1;
				int originalIndex = l2.indexOf(list2.get(i));
				int current = originalIndex - 1;
				while (insertionIndex == -1 && current >= 0) {
					Matched m = returned.getMatchedForList2Index(current);
					if (m != null) {
						insertionIndex = m.idx1 + 1;
					}
					current--;
				}
				if (insertionIndex == -1) {
					insertionIndex = 0;
				}
				returned.added.add(new Added(l2.indexOf(list2.get(i)), insertionIndex));
			}
		}

		return returned;
	}

	/**
	 * Retrieve best match between the two lists.<br>
	 * Best match is represented by a couple of objects (one in each list) of exactely same type, whose distance is the minimal found.<br>
	 * A minimal distance is required as a threshold (here 0.7)
	 * 
	 * @param l1
	 * @param l2
	 * @return
	 */
	private Matched getBestMatch(List<Object> l1, List<Object> l2) {
		Matched returned = null;
		double bestDistance = 0.7; // Double.POSITIVE_INFINITY;
		int m1 = 0, m2 = 0;
		for (int i = 0; i < l1.size(); i++) {
			Object o1 = l1.get(i);
			for (int j = 0; j < l2.size(); j++) {
				Object o2 = l2.get(j);
				if (o1 instanceof AccessibleProxyObject && o2 instanceof AccessibleProxyObject) {
					ProxyMethodHandler<?> h1 = getModelFactory().getHandler(o1);
					ProxyMethodHandler<?> h2 = getModelFactory().getHandler(o1);
					if (h1.getModelEntity() == h2.getModelEntity()) {
						// Matching is possible only for exact same type
						double d = getDistanceBetweenValues(o1, o2);
						if (d < bestDistance) {
							returned = new Matched(i, j);
							bestDistance = d;
							m1 = i;
							m2 = j;
						}
					}
				}
			}
		}
		return returned;
	}

	/**
	 * Stupid implementation, do not use it in production
	 * 
	 * @param l1
	 * @param l2
	 * @return
	 */
	private ListMatching stupidMatch(List<Object> l1, List<Object> l2) {
		System.out.println("On matche les deux listes " + l1 + " et " + l2);
		ListMatching returned = new ListMatching();
		if (l1.size() <= l2.size()) {
			for (int i = 0; i < l1.size(); i++) {
				returned.matchedList.add(new Matched(i, i));
			}
			for (int i = l1.size(); i < l2.size(); i++) {
				returned.added.add(new Added(i, i));
			}
		}
		else if (l1.size() > l2.size()) {
			for (int i = 0; i < l1.size(); i++) {
				returned.matchedList.add(new Matched(i, i));
			}
			for (int i = l2.size(); i < l1.size(); i++) {
				returned.removed.add(new Removed(i));
			}
		}
		System.out.println("Return " + returned);
		return returned;
	}

}