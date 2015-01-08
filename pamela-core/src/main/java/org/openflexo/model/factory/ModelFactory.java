/*
 * (c) Copyright 2012-2014 Openflexo
 *
 * This file is part of Openflexo Software Infrastructure.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.openflexo.model.factory;

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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import org.jdom2.JDOMException;
import org.openflexo.IObjectGraphFactory;
import org.openflexo.model.ModelContext;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.ModelEntity;
import org.openflexo.model.ModelInitializer;
import org.openflexo.model.ModelProperty;
import org.openflexo.model.StringConverterLibrary.Converter;
import org.openflexo.model.StringEncoder;
import org.openflexo.model.annotations.PastingPoint;
import org.openflexo.model.exceptions.InvalidDataException;
import org.openflexo.model.exceptions.MissingImplementationException;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.exceptions.ModelExecutionException;
import org.openflexo.model.io.XMLDeserializer;
import org.openflexo.model.io.XMLSerializer;
import org.openflexo.model.undo.CreateCommand;

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
	private Class<? extends Map> mapImplementationClass = Hashtable.class;

	private final Map<Class, PAMELAProxyFactory> proxyFactories;
	private final StringEncoder stringEncoder;
	private final ModelContext modelContext;

	private ModelContext extendedContext;

	private EditingContext editingContext;

	public Map<Class, PAMELAProxyFactory> getProxyFactories() {
		return proxyFactories;
	}

	public class PAMELAProxyFactory<I> extends ProxyFactory {
		private final ModelEntity<I> modelEntity;
		private boolean locked = false;
		private boolean overridingSuperClass = false;

		public PAMELAProxyFactory(ModelEntity<I> aModelEntity) throws ModelDefinitionException {
			super();
			this.modelEntity = aModelEntity;
			setFilter(new MethodFilter() {
				@Override
				public boolean isHandled(Method method) {

					if (Modifier.isAbstract(method.getModifiers()))
						return true;
					if (method.getName().equals("toString")) {
						return true;
					}
					// TODO perf issue ??? Check this !
					if (modelEntity.getPropertyForMethod(method) != null) {
						return true;
					}
					return false;
					// Old code
					/*return Modifier.isAbstract(method.getModifiers()) || method.getName().equals("toString")
							&& method.getParameterTypes().length == 0 && method.getDeclaringClass() == Object.class;*/
				}
			});
			Class<?> implementingClass = modelEntity.getImplementingClass();
			if (implementingClass == null) {
				implementingClass = defaultModelClass;
			}
			super.setSuperclass(implementingClass);
			Class<?>[] interfaces = { modelEntity.getImplementedInterface() };
			setInterfaces(interfaces);

		}

		public Class<?> getOverridingSuperClass() {
			if (overridingSuperClass) {
				return getSuperclass();
			} else {
				return null;
			}
		}

		@Override
		public void setSuperclass(Class clazz) {
			if (getSuperclass() != clazz) {
				if (locked) {
					throw new IllegalStateException("ProxyFactory for " + modelEntity
							+ " is locked. Super-class can no longer be modified.");
				}
			}
			overridingSuperClass = true;
			super.setSuperclass(clazz);
			locked = true;
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
			ProxyMethodHandler<I> handler = new ProxyMethodHandler<I>(this, getEditingContext());
			I returned = (I) create(new Class<?>[0], new Object[0], handler);
			handler.setObject(returned);
			if (args == null) {
				args = new Object[0];
			}
			if (args.length > 0 || modelEntity.hasInitializers()) {
				Class<?>[] types = new Class<?>[args.length];
				for (int i = 0; i < args.length; i++) {
					Object o = args[i];
					if (isProxyObject(o)) {
						ModelEntity<?> modelEntity = getModelEntityForInstance(o);
						types[i] = modelEntity.getImplementedInterface();
					} else {
						types[i] = o != null ? o.getClass() : null;
					}
				}
				ModelInitializer initializerForArgs = modelEntity.getInitializerForArgs(types);
				if (initializerForArgs != null) {
					initializerForArgs.getInitializingMethod().invoke(returned, args);
				} else {
					if (args.length > 0) {
						StringBuilder sb = new StringBuilder();
						for (Class<?> c : types) {
							if (sb.length() > 0) {
								sb.append(',');
							}
							sb.append(c.getName());

						}
						throw new NoSuchMethodException("Could not find any initializer with args " + sb.toString());
					}
				}
			}
			objectHasBeenCreated(returned, modelEntity.getImplementedInterface());
			return returned;
		}
	}

	public ModelFactory(Class<?> baseClass) throws ModelDefinitionException {
		this(ModelContextLibrary.getModelContext(baseClass));
	}

	public ModelFactory(ModelContext modelContext) {
		this.modelContext = modelContext;
		proxyFactories = new HashMap<Class, PAMELAProxyFactory>();
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
			PAMELAProxyFactory<I> proxyFactory = getProxyFactory(implementedInterface, true);
			I returned = proxyFactory.newInstance(args);
			if (getEditingContext() != null) {
				getEditingContext().register(returned);
				if (getEditingContext().getUndoManager() != null) {
					getEditingContext().getUndoManager().addEdit(new CreateCommand<I>(returned, proxyFactory.getModelEntity(), this));
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

	public <I> I newInstance(Class<I> implementedInterface, boolean useExtended) {
		return _newInstance(implementedInterface, useExtended, (Object[]) null);
	}

	public <I> I _newInstance(Class<I> implementedInterface, boolean useExtended, Object... args) {
		try {
			PAMELAProxyFactory<I> proxyFactory = getProxyFactory(implementedInterface, true, useExtended);
			I returned = proxyFactory.newInstance(args);
			if (getEditingContext() != null) {
				getEditingContext().register(returned);
				if (getEditingContext().getUndoManager() != null) {
					getEditingContext().getUndoManager().addEdit(new CreateCommand<I>(returned, proxyFactory.getModelEntity(), this));
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

	private <I> PAMELAProxyFactory<I> getProxyFactory(Class<I> implementedInterface) throws ModelDefinitionException {
		return getProxyFactory(implementedInterface, true);
	}

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
			} else {
				entity = getModelContext().getModelEntity(implementedInterface);
			}
			if (entity == null) {
				System.out.println("Debug model context");
				Iterator<ModelEntity> it = modelContext.getEntities();
				while (it.hasNext()) {
					ModelEntity<?> next = it.next();
					System.out.println("> " + next);
				}
				throw new ModelExecutionException("Unknown entity '" + implementedInterface.getName()
						+ "'! Did you forget to import it or to annotated it with @ModelEntity?");
			} else {
				if (create) {
					proxyFactories.put(implementedInterface, proxyFactory = new PAMELAProxyFactory<I>(entity));
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
			return proxyFactory.getSuperclass();
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

	public Class<? extends Map> getMapImplementationClass() {
		return mapImplementationClass;
	}

	public void setMapImplementationClass(Class<? extends Map> mapImplementationClass) {
		this.mapImplementationClass = mapImplementationClass;
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
			/*if(((ProxyObject) object).getHandler() instanceof DelegateImplementation){
				return ((DelegateImplementation<I>) ((ProxyObject) object).getHandler()).getMasterMethodHandler();
			}*/
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
	 * @param context
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

		List<Object> derivedObjectsFromContext = new ArrayList<Object>();
		if (context != null && context.length > 0) {
			for (Object o : context) {
				derivedObjectsFromContext.add(o);
				derivedObjectsFromContext.addAll(getEmbeddedObjects(o, embeddingType));
			}
		}

		List<Object> returned = new ArrayList<Object>();
		try {
			appendEmbeddedObjects(root, returned, embeddingType);
		} catch (ModelDefinitionException e) {
			throw new ModelExecutionException(e);
		}
		List<Object> discardedObjects = new ArrayList<Object>();
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
				} else {
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
			append = p.getEmbedded() != null && p.getEmbedded().closureConditions().length == 0 || p.getComplexEmbedded() != null
					&& p.getComplexEmbedded().closureConditions().length == 0;
			break;
		case DELETION:
			append = p.getEmbedded() != null && p.getEmbedded().deletionConditions().length == 0 || p.getComplexEmbedded() != null
					&& p.getComplexEmbedded().deletionConditions().length == 0;
			break;
		}

		if (append) {
			// There is no condition, just append it
			if (!list.contains(child)) {
				// System.out.println("Embedded in "+father+" because of "+p+" : "+child);
				list.add(child);
				appendEmbeddedObjects(child, list, embeddingType);
			}
		} else {
			List<Object> requiredPresence = new ArrayList<Object>();
			if (p.getEmbedded() != null) {
				switch (embeddingType) {
				case CLOSURE:
					for (String c : p.getEmbedded().closureConditions()) {
						ModelEntity closureConditionEntity = getModelEntityForInstance(child);
						ModelProperty closureConditionProperty = closureConditionEntity.getModelProperty(c);
						Object closureConditionRequiredObject = getHandler(child).invokeGetter(closureConditionProperty);
						if (closureConditionRequiredObject != null) {
							requiredPresence.add(closureConditionRequiredObject);
						}
					}
					break;
				case DELETION:
					for (String c : p.getEmbedded().deletionConditions()) {
						ModelEntity deletionConditionEntity = getModelEntityForInstance(child);
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
				} else {
					if (!list.contains(child)) {
						// System.out.println("Embedded in "+father+" because of "+p+" : "+child);
						list.add(child);
						appendEmbeddedObjects(child, list, embeddingType);
					}
				}
			}
			// System.out.println("Embedded in "+father+" : "+child+" conditioned to required presence of "+requiredPresence);
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
	public Object paste(Clipboard clipboard, Object context) throws ModelExecutionException, ModelDefinitionException,
			CloneNotSupportedException {
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
	public Object paste(Clipboard clipboard, ModelProperty<?> modelProperty, Object context) throws ModelExecutionException,
			ModelDefinitionException, CloneNotSupportedException {
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

	public void serialize(Object object, OutputStream os) throws IOException, IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, ModelDefinitionException {
		serialize(object, os, SerializationPolicy.PERMISSIVE, true);
	}

	public void serialize(Object object, OutputStream os, SerializationPolicy policy, boolean resetModifiedStatus) throws IOException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException, ModelDefinitionException {
		XMLSerializer serializer = new XMLSerializer(this, policy);
		serializer.serializeDocument(object, os, resetModifiedStatus);
	}

	@Override
	public Object deserialize(InputStream is) throws Exception, IOException {
		return deserialize(is, DeserializationPolicy.PERMISSIVE);
	}

	public Object deserialize(InputStream is, DeserializationPolicy policy) throws IOException, JDOMException, InvalidDataException,
			ModelDefinitionException {
		XMLDeserializer deserializer = new XMLDeserializer(this, policy);
		return deserializer.deserializeDocument(is);
	}

	@Override
	public Object deserialize(String input) throws Exception, IOException {
		return deserialize(input, DeserializationPolicy.PERMISSIVE);
	}

	public Object deserialize(String input, DeserializationPolicy policy) throws IOException, JDOMException, InvalidDataException,
			ModelDefinitionException {
		XMLDeserializer deserializer = new XMLDeserializer(this, policy);
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
		// TODO Auto-generated method stub

	}

	@Override
	public void resetContext() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addToRootNodes(Object anObject) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setContextProperty(String propertyName, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getInstanceOf(Type aType, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean objectHasAttributeNamed(Object object, String attrName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addAttributeValueForObject(Object object, String attrName, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addChildToObject(Object child, Object container) {
		// TODO Auto-generated method stub

	}

	@Override
	public Type getAttributeType(Object currentContainer, String localName) {
		// TODO Auto-generated method stub
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
		for (Iterator<ModelEntity> it = modelContext.getEntities(); it.hasNext();) {
			ModelEntity e = it.next();
			e.checkMethodImplementations(this);
		}

	}

}
