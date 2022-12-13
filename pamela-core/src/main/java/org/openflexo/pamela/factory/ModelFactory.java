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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.openflexo.IObjectGraphFactory;
import org.openflexo.pamela.DeletableProxyObject;
import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.ModelContextLibrary;
import org.openflexo.pamela.annotations.PastingPoint;
import org.openflexo.pamela.exceptions.MissingImplementationException;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelInitializer;
import org.openflexo.pamela.model.ModelProperty;
import org.openflexo.pamela.model.StringConverterLibrary.Converter;
import org.openflexo.pamela.undo.CreateCommand;
import org.openflexo.pamela.xml.XMLSaxDeserializer;
import org.openflexo.pamela.xml.XMLSerializer;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

/**
 * The {@link ModelFactory} is responsible for creating new instances of PAMELA entities.<br>
 * 
 * This class should be considered stateless, regarding to the state of handled instances.<br>
 * 
 * Note that a {@link ModelFactory} might refer to an {@link EditingContext}. When so, new instances are automatically registered in this
 * {@link EditingContext}.
 * 
 * @author sylvain
 * 
 */
public class ModelFactory implements IObjectGraphFactory {

	private Class<?> defaultModelClass = Object.class;
	private Class<? extends List> listImplementationClass = Vector.class;

	private final Map<Class, PAMELAProxyFactory> proxyFactories;
	private final StringEncoder stringEncoder;
	private final ModelContext modelContext;

	private ModelContext extendedContext;

	private EditingContext editingContext;

	// Stores on-the-fly generated classes to proxy the targeted implementation
	// class, but in the right package
	private static Map<Class, Map<Class, Class>> implementationProxyClasses = new HashMap<>();

	public Map<Class, PAMELAProxyFactory> getProxyFactories() {
		return proxyFactories;
	}

	public class PAMELAProxyFactory<I> extends ProxyFactory {
		private final ModelEntity<I> modelEntity;
		private boolean locked = false;
		private boolean overridingSuperClass = false;

		public PAMELAProxyFactory(ModelEntity<I> aModelEntity, ModelContext context) throws ModelDefinitionException {
			super();
			this.modelEntity = aModelEntity;
			setFilter(new MethodFilter() {
				@Override
				public boolean isHandled(Method method) {

					// Abstract methods should be intercepted !
					if (Modifier.isAbstract(method.getModifiers()))
						return true;

					// In related ModelEntity requires it, return true
					if (modelEntity.isMethodToBeIntercepted(method)) {
						return true;
					}

					// The method may also be involved in a pattern
					if (context.isMethodInvolvedInPattern(method)) {
						return true;
					}

					// In all other cases, return false
					return false;
				}
			});
			Class<?> implementingClass = modelEntity.getImplementingClass();

			if (implementingClass == null && modelEntity.isSimplePamelaInstrumentation()) {
				// Special case for a Pamela entity defined for a basic Java class
				implementingClass = modelEntity.getImplementedInterface();
				super.setSuperclass(modelEntity.getImplementedInterface());
			}
			else {
				if (implementingClass == null) {
					implementingClass = defaultModelClass;
				}
				super.setSuperclass(implementingClass);
				Class<?>[] interfaces = { modelEntity.getImplementedInterface() };
				setInterfaces(interfaces);
			}

		}

		public Class<?> getOverridingSuperClass() {
			if (overridingSuperClass) {
				return getSuperclass();
			}
			else {
				return null;
			}
		}

		@Override
		public void setSuperclass(Class clazz) {
			if (getSuperclass() != clazz) {
				if (locked) {
					throw new IllegalStateException(
							"ProxyFactory for " + modelEntity + " is locked. Super-class can no longer be modified.");
				}
			}
			overridingSuperClass = true;
			super.setSuperclass(clazz);
			locked = true;
		}

		/**
		 * Internally used to set a proxy base implementation class in the right package
		 * 
		 * @param clazz
		 */
		private void setProxySuperClass(Class clazz) {
			super.setSuperclass(clazz);
		}

		public ModelFactory getModelFactory() {
			return ModelFactory.this;
		}

		public ModelEntity<I> getModelEntity() {
			return modelEntity;
		}

		public I newInstance(Object... args) throws IllegalArgumentException, NoSuchMethodException, InstantiationException,
				IllegalAccessException, InvocationTargetException, ModelDefinitionException {
			if (modelEntity.isAbstract()) {
				throw new InstantiationException(modelEntity + " is declared as an abstract entity, cannot instantiate it");
			}
			locked = true;
			ProxyMethodHandler<I> handler = new ProxyMethodHandler<>(this, getEditingContext());

			if (args == null) {
				args = new Object[0];
			}

			I returned = null;
			if (modelEntity.isSimplePamelaInstrumentation()) {
				Class<?>[] paramTypesArray = new Class<?>[args.length];
				for (int i = 0; i < args.length; i++) {
					paramTypesArray[i] = args[i].getClass();
				}
				returned = (I) create(paramTypesArray, args, handler);
				handler.setObject(returned);
			}
			else {

				// Java 11 security issue
				// If the base implementation class is not in the same package than the
				// implemented interface, it fails
				// The workaround is to generate (or reuse) on the fly a proxy super classes (in
				// the right package !)
				if (!modelEntity.getImplementedInterface().getPackage().equals(getSuperclass().getPackage())) {
					Class<?> implementationClass = retrieveProxyImplementationClass(modelEntity.getImplementedInterface(), getSuperclass());
					setProxySuperClass(implementationClass);
				}

				returned = (I) create(new Class<?>[0], new Object[0], handler);
				handler.setObject(returned);
				if (args.length > 0 || modelEntity.hasInitializers()) {
					Class<?>[] types = new Class<?>[args.length];
					for (int i = 0; i < args.length; i++) {
						Object o = args[i];
						if (isProxyObject(o)) {
							ModelEntity<?> modelEntity = getModelEntityForInstance(o);
							types[i] = modelEntity.getImplementedInterface();
						}
						else {
							types[i] = o != null ? o.getClass() : null;
						}
					}
					ModelInitializer initializerForArgs = modelEntity.getInitializerForArgs(types);
					if (initializerForArgs != null) {
						handler.initializing = true;
						try {
							initializerForArgs.getInitializingMethod().invoke(returned, args);
						} finally {
							handler.initializing = false;
							handler.initialized = true;
						}
					}
					else {
						if (args.length > 0) {
							StringBuilder sb = new StringBuilder();
							for (Class<?> c : types) {
								if (sb.length() > 0) {
									sb.append(',');
								}
								sb.append(c != null ? c.getName() : "<null>");

							}
							throw new NoSuchMethodException("Could not find any initializer with args " + sb.toString());
						}
					}
				}
			}

			// looks for property to initialize
			for (ModelProperty<? super I> property : modelEntity.getPropertyIterable()) {
				if (property.getInitialize() != null) {
					handler.invokeSetter(property, ModelFactory.this.newInstance(property.getType()));
				}
			}

			objectHasBeenCreated(returned, modelEntity.getImplementedInterface());
			return returned;
		}

		/**
		 * Generate (return when already existant) a base implementation class proxying the declared base implementation class, but in the
		 * right package (the same as the implemented interface)
		 * 
		 * @param implementedInterface
		 * @param superClass
		 * @return
		 */
		private Class retrieveProxyImplementationClass(Class<?> implementedInterface, Class<?> superClass) {

			String packageName = implementedInterface.getPackageName();

			Map<Class, Class> map = implementationProxyClasses.get(superClass);
			if (map == null) {
				map = new HashMap<>();
				implementationProxyClasses.put(superClass, map);
			}

			Class returned = map.get(implementedInterface);
			if (returned == null) {
				ClassPool pool = ClassPool.getDefault();
				CtClass ctClass = pool.makeClass(
						packageName + "." + superClass.getSimpleName() + "_" + implementedInterface.getSimpleName() + "_" + "Proxy");
				try {
					ctClass.setSuperclass(pool.get(superClass.getName()));
				} catch (CannotCompileException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					returned = ctClass.toClass(implementedInterface);
				} catch (CannotCompileException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				map.put(implementedInterface, returned);
			}
			return returned;
		}
	}

	public ModelFactory(Class<?> baseClass) throws ModelDefinitionException {
		this(ModelContextLibrary.getModelContext(baseClass));
	}

	public ModelFactory(ModelContext modelContext) {
		this.modelContext = modelContext;
		proxyFactories = new HashMap<>();
		stringEncoder = new StringEncoder(this);
	}

	public ModelContext getModelContext() {
		return modelContext;
	}

	public ModelContext getExtendedContext() {
		return extendedContext != null ? extendedContext : modelContext;
	}

	public <I> I newInstance(ModelEntity<I> modelEntity) {
		return newInstance(modelEntity, (Object[]) null);
	}

	public <I> I newInstance(ModelEntity<I> modelEntity, Object... args) {
		return newInstance(modelEntity.getImplementedInterface(), args);
	}

	public <I> I newInstance(Class<I> implementedInterface) {
		return newInstance(implementedInterface, (Object[]) null);
	}

	public <I> I newInstance(Class<I> implementedInterface, Object... args) {
		try {
			// this.getModelContext().getPatternContext().enteringConstructor();
			PAMELAProxyFactory<I> proxyFactory = getProxyFactory(implementedInterface, true);
			I returned = proxyFactory.newInstance(args);
			if (getEditingContext() != null) {
				if (getEditingContext().getUndoManager() != null) {
					getEditingContext().getUndoManager().addEdit(new CreateCommand<>(returned, proxyFactory.getModelEntity(), this));
				}
			}
			// this.getModelContext().getPatternContext().leavingConstructor();
			getModelContext().notifiedNewInstance(returned, getModelEntityForInstance(returned));
			return returned;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		} catch (ExceptionInInitializerError e) {
			e.getCause().printStackTrace();
			throw new ModelExecutionException(e);
		}
	}

	public <I> I newInstance(Class<I> implementedInterface, boolean useExtended) {
		return _newInstance(implementedInterface, useExtended, (Object[]) null);
	}

	public <I> I _newInstance(Class<I> implementedInterface, boolean useExtended, Object... args) {
		try {
			PAMELAProxyFactory<I> proxyFactory = getProxyFactory(implementedInterface, true, useExtended);
			I returned = proxyFactory.newInstance(args);
			if (getEditingContext() != null) {
				if (getEditingContext().getUndoManager() != null) {
					getEditingContext().getUndoManager().addEdit(new CreateCommand<>(returned, proxyFactory.getModelEntity(), this));
				}
			}
			return returned;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			throw new ModelExecutionException(e);
		}
	}

	/*
	 * Unused private <I> PAMELAProxyFactory<I> getProxyFactory(Class<I>
	 * implementedInterface) throws ModelDefinitionException { return
	 * getProxyFactory(implementedInterface, true); }
	 */
	private <I> PAMELAProxyFactory<I> getProxyFactory(Class<I> implementedInterface, boolean create) throws ModelDefinitionException {
		return getProxyFactory(implementedInterface, create, false);
	}

	private <I> PAMELAProxyFactory<I> getProxyFactory(Class<I> implementedInterface, boolean create, boolean useExtended)
			throws ModelDefinitionException {
		PAMELAProxyFactory<I> proxyFactory = proxyFactories.get(implementedInterface);
		if (proxyFactory == null) {
			ModelEntity<I> entity;
			if (useExtended) {
				entity = getExtendedContext().getModelEntity(implementedInterface);
			}
			else {
				entity = getModelContext().getModelEntity(implementedInterface);
			}
			if (entity == null) {
				System.out.println("Debug model context");
				Iterator<ModelEntity> it = modelContext.getEntitiesIterator();
				while (it.hasNext()) {
					ModelEntity<?> next = it.next();
					System.out.println("> " + next);
				}
				throw new ModelExecutionException("Unknown entity '" + implementedInterface.getName()
						+ "'! Did you forget to import it or to annotated it with @ModelEntity?");
			}
			else {
				if (create) {
					proxyFactories.put(implementedInterface, proxyFactory = new PAMELAProxyFactory<>(entity, this.getModelContext()));
				}
			}
		}
		return proxyFactory;
	}

	public Class<?> getDefaultModelClass() {
		return defaultModelClass;
	}

	public void setDefaultModelClass(Class<?> defaultModelClass) {
		Class<?> old = defaultModelClass;
		this.defaultModelClass = defaultModelClass;
		for (PAMELAProxyFactory<?> factory : proxyFactories.values()) {
			if (factory.getSuperclass() == old) {
				factory.setSuperclass(defaultModelClass);
			}
		}
	}

	public <I> void setImplementingClassForInterface(Class<? extends I> implementingClass, Class<I> implementedInterface)
			throws ModelDefinitionException {
		try {
			PAMELAProxyFactory<I> proxyFactory = getProxyFactory(implementedInterface, true);
			proxyFactory.setSuperclass(implementingClass);
		} catch (ModelExecutionException e) {
			// OK, we won't add the implementation since the interface is not
			// declared
		}
	}

	public <I> Class<? extends I> getImplementingClassForInterface(Class<I> implementedInterface) throws ModelDefinitionException {
		PAMELAProxyFactory<I> proxyFactory = getProxyFactory(implementedInterface, true);
		if (proxyFactory != null) {
			return (Class) proxyFactory.getSuperclass();
		}
		return null;
	}

	public <I> void setImplementingClassForInterface(Class<? extends I> implementingClass, Class<I> implementedInterface,
			boolean useExtended) throws ModelDefinitionException {
		PAMELAProxyFactory<I> proxyFactory = getProxyFactory(implementedInterface, true, useExtended);
		if (proxyFactory != null) {
			proxyFactory.setSuperclass(implementingClass);
		}
	}

	public <I> void setImplementingClassForEntity(Class<? extends I> implementingClass, ModelEntity<I> entity)
			throws ModelDefinitionException {
		setImplementingClassForInterface(implementingClass, entity.getImplementedInterface());
	}

	<I> void setImplementingClassForEntity(Class<? extends I> implementingClass, ModelEntity<I> entity, boolean useExtended)
			throws ModelDefinitionException {
		setImplementingClassForInterface(implementingClass, entity.getImplementedInterface(), useExtended);
	}

	public Class<? extends List> getListImplementationClass() {
		return listImplementationClass;
	}

	public void setListImplementationClass(Class<? extends List> listImplementationClass) {
		this.listImplementationClass = listImplementationClass;
	}

	public boolean isProxyObject(Object object) {
		return object instanceof ProxyObject;
	}

	public <I> ModelEntity<I> getModelEntityForInstance(I object) {
		ProxyMethodHandler<I> handler = getHandler(object);
		if (handler != null) {
			return handler.getModelEntity();
		}
		return null;
	}

	public <I> ProxyMethodHandler<I> getHandler(I object) {
		if (object instanceof ProxyObject) {

			// Vincent: Check this, handler can be of DelegateImplementation
			// Type( in the case of Edition actions containers)
			// ???
			/*
			 * if(((ProxyObject) object).getHandler() instanceof DelegateImplementation){
			 * return ((DelegateImplementation<I>) ((ProxyObject)
			 * object).getHandler()).getMasterMethodHandler(); }
			 */
			if (((ProxyObject) object).getHandler() instanceof ProxyMethodHandler) {
				return (ProxyMethodHandler<I>) ((ProxyObject) object).getHandler();
			}
		}
		return null;
	}

	public <I> ModelEntity<I> importClass(Class<I> klass) throws ModelDefinitionException {
		ModelEntity<I> modelEntity = modelContext.getModelEntity(klass);
		if (modelEntity == null) {
			extendedContext = new ModelContext(klass, getExtendedContext());
			modelEntity = extendedContext.getModelEntity(klass);
		}
		return modelEntity;
	}

	public StringEncoder getStringEncoder() {
		return stringEncoder;
	}

	public void addConverter(Converter<?> converter) {
		stringEncoder.addConverter(converter);
	}

	public boolean isEmbedddedIn(Object parentObject, Object childObject, EmbeddingType embeddingType) {
		return getEmbeddedObjects(parentObject, embeddingType).contains(childObject);
	}

	public boolean isEmbedddedIn(Object parentObject, Object childObject, EmbeddingType embeddingType, Object... context) {
		return getEmbeddedObjects(parentObject, embeddingType, context).contains(childObject);
	}

	/**
	 * Build and return a List of embedded objects, using meta informations contained in related class All property should be annotated with
	 * a @Embedded annotation which determine the way of handling this property
	 * 
	 * Supplied context is used to determine the closure of objects graph being constructed during this operation.
	 * 
	 * @param root
	 * @return
	 */
	public List<Object> getEmbeddedObjects(Object root, EmbeddingType embeddingType) {
		return getEmbeddedObjects(root, embeddingType, (Object[]) null);
	}

	/**
	 * Build and return a List of embedded objects, using meta informations contained in related class All property should be annotated with
	 * a @Embedded annotation which determine the way of handling this property
	 * 
	 * Supplied context is used to determine the closure of objects graph being constructed during this operation.
	 * 
	 * @param root
	 * @param context
	 * @return
	 */
	public List<Object> getEmbeddedObjects(Object root, EmbeddingType embeddingType, Object... context) {
		if (!isProxyObject(root)) {
			return Collections.emptyList();
		}

		List<Object> derivedObjectsFromContext = new ArrayList<>();
		if (context != null && context.length > 0) {
			for (Object o : context) {
				derivedObjectsFromContext.add(o);
				derivedObjectsFromContext.addAll(getEmbeddedObjects(o, embeddingType));
			}
		}

		List<Object> returned = new ArrayList<>();
		try {
			appendEmbeddedObjects(root, returned, embeddingType);
		} catch (ModelDefinitionException e) {
			throw new ModelExecutionException(e);
		}
		List<Object> discardedObjects = new ArrayList<>();
		for (int i = 0; i < returned.size(); i++) {
			Object o = returned.get(i);
			if (o instanceof ConditionalPresence) {
				boolean allOthersArePresent = true;
				for (Object other : ((ConditionalPresence) o).requiredPresence) {
					if (!returned.contains(other) && !derivedObjectsFromContext.contains(other)) {
						allOthersArePresent = false;
						break;
					}
				}
				if (allOthersArePresent && !returned.contains(((ConditionalPresence) o).object)) {
					// Closure is fine and object is not already present, add
					// object
					returned.set(i, ((ConditionalPresence) o).object);
				}
				else {
					// Discard object
					discardedObjects.add(o);
				}
			}
		}
		for (Object o : discardedObjects) {
			returned.remove(o);
		}
		return returned;
	}

	private class ConditionalPresence {
		private final Object object;
		private final List<Object> requiredPresence;

		public ConditionalPresence(Object object, List<Object> requiredPresence) {
			super();
			this.object = object;
			this.requiredPresence = requiredPresence;
		}
	}

	private void appendEmbedded(ModelProperty p, Object father, List<Object> list, Object child, EmbeddingType embeddingType)
			throws ModelDefinitionException {
		if (!isProxyObject(child)) {
			return;
		}

		if (p.getEmbedded() == null && p.getComplexEmbedded() == null) {
			// this property is not embedded
			return;
		}

		boolean append = false;
		switch (embeddingType) {
			case CLOSURE:
				append = p.getEmbedded() != null && p.getEmbedded().closureConditions().length == 0
						|| p.getComplexEmbedded() != null && p.getComplexEmbedded().closureConditions().length == 0;
				break;
			case DELETION:
				append = p.getEmbedded() != null && p.getEmbedded().deletionConditions().length == 0
						|| p.getComplexEmbedded() != null && p.getComplexEmbedded().deletionConditions().length == 0;
				break;
		}

		if (append) {
			// There is no condition, just append it
			if (!list.contains(child)) {
				// System.out.println("Embedded in "+father+" because of "+p+" : "+child);
				list.add(child);
				appendEmbeddedObjects(child, list, embeddingType);
			}
		}
		else {
			List<Object> requiredPresence = new ArrayList<>();
			if (p.getEmbedded() != null) {
				switch (embeddingType) {
					case CLOSURE:
						for (String c : p.getEmbedded().closureConditions()) {
							ModelEntity<?> closureConditionEntity = getModelEntityForInstance(child);
							ModelProperty closureConditionProperty = closureConditionEntity.getModelProperty(c);
							Object closureConditionRequiredObject = getHandler(child).invokeGetter(closureConditionProperty);
							if (closureConditionRequiredObject != null) {
								requiredPresence.add(closureConditionRequiredObject);
							}
						}
						break;
					case DELETION:
						for (String c : p.getEmbedded().deletionConditions()) {
							ModelEntity<?> deletionConditionEntity = getModelEntityForInstance(child);
							ModelProperty deletionConditionProperty = deletionConditionEntity.getModelProperty(c);
							Object deletionConditionRequiredObject = getHandler(child).invokeGetter(deletionConditionProperty);
							if (deletionConditionRequiredObject != null) {
								requiredPresence.add(deletionConditionRequiredObject);
							}
						}
						break;
				}
				if (requiredPresence.size() > 0) {
					ConditionalPresence conditionalPresence = new ConditionalPresence(child, requiredPresence);
					list.add(conditionalPresence);
				}
				else {
					if (!list.contains(child)) {
						// System.out.println("Embedded in "+father+" because of "+p+" : "+child);
						list.add(child);
						appendEmbeddedObjects(child, list, embeddingType);
					}
				}
			}
			// System.out.println("Embedded in "+father+" : "+child+" conditioned to
			// required presence of "+requiredPresence);
		}
	}

	private void appendEmbeddedObjects(Object father, List<Object> list, EmbeddingType embeddingType) throws ModelDefinitionException {
		ProxyMethodHandler handler = getHandler(father);
		ModelEntity modelEntity = handler.getModelEntity();

		Iterator<ModelProperty<?>> properties = modelEntity.getProperties();
		while (properties.hasNext()) {
			ModelProperty<?> p = properties.next();
			switch (p.getCardinality()) {
				case SINGLE:
					Object oValue = handler.invokeGetter(p);
					appendEmbedded(p, father, list, oValue, embeddingType);
					break;
				case LIST:
					List<?> values = (List<?>) handler.invokeGetter(p);
					if (values != null) {
						for (Object o : values) {
							appendEmbedded(p, father, list, o, embeddingType);
						}
					}
					break;
				default:
					break;
			}
		}
	}

	public Clipboard copy(Object... objects) throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {
		return new Clipboard(this, objects);
	}

	public Clipboard cut(Object... objects) throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {
		Clipboard returned = new Clipboard(this, objects);
		for (Object o : objects) {
			if (o instanceof DeletableProxyObject) {
				((DeletableProxyObject) o).delete(objects);
			}
		}
		return returned;
	}

	/**
	 * Return boolean indicating if supplied clipboard is valid for pasting in object monitored by this method handler<br>
	 * 
	 * @param clipboard
	 * @param context
	 * @return
	 */
	public boolean isPastable(Clipboard clipboard, Object context) throws ClipboardOperationException {
		if (!isProxyObject(context)) {
			throw new ClipboardOperationException("Cannot paste here: context is not valid");
		}

		return getHandler(context).isPastable(clipboard);

	}

	/**
	 * Paste supplied clipboard in context object<br>
	 * Return pasted objects (a single object for a single contents clipboard, and a list of objects for a multiple contents)
	 * 
	 * @param clipboard
	 * @param context
	 * @return
	 * @throws ModelExecutionException
	 * @throws ModelDefinitionException
	 * @throws CloneNotSupportedException
	 */
	public Object paste(Clipboard clipboard, Object context)
			throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {
		if (!isProxyObject(context)) {
			throw new ClipboardOperationException("Cannot paste here: context is not valid: " + context);
		}

		return getHandler(context).paste(clipboard);
	}

	/**
	 * Paste supplied clipboard in context object for supplied property <br>
	 * Return pasted objects (a single object for a single contents clipboard, and a list of objects for a multiple contents)
	 * 
	 * @param clipboard
	 * @param modelProperty
	 * @param context
	 * @return
	 * @throws ModelExecutionException
	 * @throws ModelDefinitionException
	 * @throws CloneNotSupportedException
	 */
	public Object paste(Clipboard clipboard, ModelProperty<?> modelProperty, Object context)
			throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {
		if (!isProxyObject(context)) {
			throw new ClipboardOperationException("Cannot paste here: context is not valid");
		}

		return getHandler(context).paste(clipboard, (ModelProperty) modelProperty);
	}

	/**
	 * Paste supplied clipboard in context object for supplied property at specified pasting point<br>
	 * Return pasted objects (a single object for a single contents clipboard, and a list of objects for a multiple contents)
	 * 
	 * @param clipboard
	 * @param modelProperty
	 * @param pp
	 * @param context
	 * @return
	 * @throws ModelExecutionException
	 * @throws ModelDefinitionException
	 * @throws CloneNotSupportedException
	 */
	public Object paste(Clipboard clipboard, ModelProperty<?> modelProperty, PastingPoint pp, Object context)
			throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {
		if (!isProxyObject(context)) {
			throw new ClipboardOperationException("Cannot paste here: context is not valid");
		}

		return getHandler(context).paste(clipboard, (ModelProperty) modelProperty, pp);
	}

	public String stringRepresentation(Object object) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			serialize(object, baos, SerializationPolicy.PERMISSIVE, false);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return baos.toString();
	}

	public void serialize(Object object, OutputStream os)
			throws IOException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, ModelDefinitionException {
		serialize(object, os, SerializationPolicy.PERMISSIVE, true);
	}

	public void serialize(Object object, OutputStream os, SerializationPolicy policy, boolean resetModifiedStatus)
			throws IOException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, ModelDefinitionException {
		XMLSerializer serializer = new XMLSerializer(this, policy);
		serializer.serializeDocument(object, os, resetModifiedStatus);
	}

	@Override
	public Object deserialize(InputStream is) throws Exception {
		return deserialize(is, DeserializationPolicy.PERMISSIVE);
	}

	public Object deserialize(InputStream is, DeserializationPolicy policy) throws Exception {
		XMLSaxDeserializer deserializer = new XMLSaxDeserializer(this, policy);
		return deserializer.deserializeDocument(is);
	}

	@Override
	public Object deserialize(String input) throws Exception {
		return deserialize(input, DeserializationPolicy.PERMISSIVE);
	}

	public Object deserialize(String input, DeserializationPolicy policy) throws Exception {
		XMLSaxDeserializer deserializer = new XMLSaxDeserializer(this, policy);
		return deserializer.deserializeDocument(input);
	}

	/**
	 * Hook to detect an object creation Default implementation silently returns
	 * 
	 * @param newlyCreatedObject
	 * @param implementedInterface
	 */
	public <I> void objectHasBeenCreated(I newlyCreatedObject, Class<I> implementedInterface) {
	}

	/**
	 * Hook to detect an object deserialization (called just after instance has been created)<br>
	 * Default implementation silently returns
	 * 
	 * @param newlyCreatedObject
	 * @param implementedInterface
	 */
	public <I> void objectIsBeeingDeserialized(I newlyCreatedObject, Class<I> implementedInterface) {
	}

	/**
	 * Hook to detect an object deserialization (called at the end of whole object graph deserialization)<br>
	 * Default implementation silently returns
	 * 
	 * @param newlyCreatedObject
	 * @param implementedInterface
	 */
	public <I> void objectHasBeenDeserialized(I newlyCreatedObject, Class<I> implementedInterface) {
	}

	/**
	 * Return {@link EditingContext} associated with this factory.
	 * 
	 * @return
	 */
	public EditingContext getEditingContext() {
		return editingContext;
	}

	/**
	 * Sets {@link EditingContext} associated with this factory.<br>
	 * When not null, new instances created with this factory are automatically registered in this EditingContext
	 * 
	 * @param editingContext
	 */
	public void setEditingContext(EditingContext editingContext) {
		this.editingContext = editingContext;
	}

	/* @Override */
	@Override
	public final Type getTypeForObject(String typeURI, Object container, String objectName) {
		return (Type) getModelContext().getModelEntity(typeURI);
	}

	@Override
	public void setContext(Object objectGraph) {

	}

	@Override
	public void resetContext() {

	}

	@Override
	public void addToRootNodes(Object anObject) {

	}

	@Override
	public void setContextProperty(String propertyName, Object value) {

	}

	@Override
	public Object getInstanceOf(Type aType, String name) {
		return null;
	}

	@Override
	public boolean objectHasAttributeNamed(Object object, String attrName) {
		return false;
	}

	@Override
	public void addAttributeValueForObject(Object object, String attrName, Object value) {

	}

	@Override
	public void addChildToObject(Object child, Object container) {

	}

	@Override
	public Type getAttributeType(Object currentContainer, String localName) {
		return null;
	}

	/**
	 * Check that this factory contains all required implementation for all non-abstract entities
	 * 
	 * @throws MissingImplementationException
	 *             when an implementation was not found
	 */
	public void checkMethodImplementations() throws ModelDefinitionException, MissingImplementationException {
		ModelContext modelContext = getModelContext();
		MissingImplementationException thrown = null;
		for (Iterator<ModelEntity> it = modelContext.getEntitiesIterator(); it.hasNext();) {
			ModelEntity<?> e = it.next();
			try {
				e.checkMethodImplementations(this);
			} catch (MissingImplementationException ex) {
				System.err.println("MissingImplementationException: " + ex.getMessage());
				thrown = ex;
			}
		}
		if (thrown != null) {
			throw thrown;
		}
	}

}
