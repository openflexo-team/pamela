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

package org.openflexo.pamela.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.openflexo.connie.binding.javareflect.ReflectionUtils;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.CloneableProxyObject;
import org.openflexo.pamela.DeletableProxyObject;
import org.openflexo.pamela.KeyValueCoding;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.SpecifiableProxyObject;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.Finder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Implementation;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.annotations.Modify;
import org.openflexo.pamela.annotations.Reindexer;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.StringConverter;
import org.openflexo.pamela.annotations.Updater;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.annotations.jml.Invariant;
import org.openflexo.pamela.exceptions.MissingImplementationException;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.exceptions.PropertyClashException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.factory.PamelaUtils;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.jml.JMLInvariant;
import org.openflexo.pamela.jml.JMLMethodDefinition;
import org.openflexo.pamela.model.StringConverterLibrary.Converter;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * A {@link ModelEntity} represents a concept in a PAMELA meta-model<br>
 * 
 * A {@link ModelEntity} is reified in Java using an interface or class, annotated with a
 * {@link org.openflexo.pamela.annotations.ModelEntity} annotation
 * 
 * @author guillaume, sylvain
 * 
 * @param <I>
 *            java type addressed by this entity
 */
public class ModelEntity<I> {

	/**
	 * The implemented interface corresponding to this model entity
	 */
	private final Class<I> implementedInterface;

	/**
	 * The model entity annotation describing this entity
	 */
	private final org.openflexo.pamela.annotations.ModelEntity entityAnnotation;

	/**
	 * The implementationClass associated with this model entity
	 */
	private final ImplementationClass implementationClass;

	/**
	 * The {@link XMLElement} annotation, if any
	 */
	private XMLElement xmlElement;

	/**
	 * The properties of this entity. The key is the identifier of the property
	 */
	private final Map<String, ModelProperty<? super I>> properties;

	/**
	 * The properties of this entity. The key is the identifier of the property
	 */
	private final Map<ModelMethod, ModelProperty<? super I>> propertyMethods;

	/**
	 * The properties of this entity. The key is the xml attribute name of the property
	 */
	private Map<String, ModelProperty<? super I>> modelPropertiesByXMLAttributeName;

	/**
	 * The xmlTag of this entity, if any
	 */
	private String xmlTag;

	/**
	 * The modify annotation of this entity, if any
	 */
	private Modify modify;

	/**
	 * Whether this entity is an abstract entity. Abstract entities cannot be instantiated.
	 */
	private final boolean isAbstract;

	/**
	 * The default implementing class of this entity. The class can be abstract. This value may be null.
	 */
	private Class<?> implementingClass;

	/**
	 * The list of super interfaces of this entity. This may be null.
	 */
	private List<Class<? super I>> superImplementedInterfaces;

	/**
	 * The complete list of all the super entities of this entity.
	 */
	private List<ModelEntity<? super I>> allSuperEntities;

	/**
	 * The list of super entities (matching the list of super interfaces). This may be null
	 */
	private List<ModelEntity<? super I>> directSuperEntities;

	/**
	 * The initializers of this entity.
	 */
	private final Map<Method, ModelInitializer> initializers;

	DeserializationInitializer deserializationInitializer = null;
	DeserializationFinalizer deserializationFinalizer = null;

	private boolean initialized;

	public final Map<String, ModelProperty<I>> declaredModelProperties;

	private Set<ModelEntity<?>> embeddedEntities;

	private final Map<Class<I>, Set<Method>> delegateImplementations;

	ModelEntity(@Nonnull Class<I> implementedInterface) throws ModelDefinitionException {

		super(/*implementedInterface.getName()*/);

		this.implementedInterface = implementedInterface;
		declaredModelProperties = new HashMap<>();
		properties = new LinkedHashMap<>();
		propertyMethods = new HashMap<>();
		initializers = new HashMap<>();
		embeddedEntities = new HashSet<>();
		entityAnnotation = implementedInterface.getAnnotation(org.openflexo.pamela.annotations.ModelEntity.class);
		implementationClass = implementedInterface.getAnnotation(ImplementationClass.class);
		// xmlElement = implementedInterface.getAnnotation(XMLElement.class);
		modify = implementedInterface.getAnnotation(Modify.class);
		isAbstract = entityAnnotation.isAbstract();
		// We resolve here the model super interface
		// The corresponding model entity MUST be resolved later
		for (Class<?> i : implementedInterface.getInterfaces()) {
			if (i.isAnnotationPresent(org.openflexo.pamela.annotations.ModelEntity.class)) {
				if (superImplementedInterfaces == null) {
					superImplementedInterfaces = new ArrayList<>();
				}
				superImplementedInterfaces.add((Class<? super I>) i);
			}
		}
		for (Field field : getImplementedInterface().getDeclaredFields()) {
			StringConverter converter = field.getAnnotation(StringConverter.class);
			if (converter != null) {
				try {
					StringConverterLibrary.getInstance().addConverter((Converter<?>) field.get(null));
				} catch (IllegalArgumentException e) {
					// This should not happen since interfaces can only have static fields
					// and we pass 'null'
					throw new ModelDefinitionException("Field " + field + " is not static! Cannot use it as string converter.");
				} catch (IllegalAccessException e) {
					throw new ModelDefinitionException("Illegal access to field " + field);
				} catch (ClassCastException e) {
					throw new ModelDefinitionException("Field " + field.getName() + " is annotated with " + StringConverter.class.getName()
							+ " but the value of the field is not an instance of " + Converter.class.getName());
				}
			}
		}

		// We scan already all the declared properties but we do not resolve their type. We do not resolve inherited properties either.
		for (Method m : getImplementedInterface().getDeclaredMethods()) {
			String propertyIdentifier = getPropertyIdentifier(m);
			// Sylvain: i commented following condition, as if a Pamela method overrides an interface where parent method
			// was not annotated, property was ignored. But i dont't understand the reason of this condition
			// Guillaume, could you please check this ?
			if (propertyIdentifier == null /*|| !declaredModelProperties.containsKey(propertyIdentifier)*/) {
				List<Method> overridenMethods = ReflectionUtils.getOverridenMethods(m);
				for (Method override : overridenMethods) {
					propertyIdentifier = getPropertyIdentifier(override);
					if (propertyIdentifier != null) {
						break;
					}
				}
			}

			if (propertyIdentifier != null && !declaredModelProperties.containsKey(propertyIdentifier)) {
				// The next line creates the property
				ModelProperty<I> property = ModelProperty.getModelProperty(propertyIdentifier, this);
				declaredModelProperties.put(propertyIdentifier, property);
			}
			org.openflexo.pamela.annotations.Initializer initializer = m.getAnnotation(org.openflexo.pamela.annotations.Initializer.class);
			if (initializer != null) {
				initializers.put(m, new ModelInitializer(initializer, m));
			}

			org.openflexo.pamela.annotations.DeserializationFinalizer deserializationFinalizer = m
					.getAnnotation(org.openflexo.pamela.annotations.DeserializationFinalizer.class);
			if (deserializationFinalizer != null) {
				if (this.deserializationFinalizer == null) {
					this.deserializationFinalizer = new DeserializationFinalizer(deserializationFinalizer, m);
				}
				else {
					throw new ModelDefinitionException(
							"Duplicated deserialization finalizer found for entity " + getImplementedInterface());
				}
			}

			org.openflexo.pamela.annotations.DeserializationInitializer deserializationInitializer = m
					.getAnnotation(org.openflexo.pamela.annotations.DeserializationInitializer.class);
			if (deserializationInitializer != null) {
				if (this.deserializationInitializer == null) {
					this.deserializationInitializer = new DeserializationInitializer(deserializationInitializer, m);
				}
				else {
					throw new ModelDefinitionException(
							"Duplicated deserialization initializer found for entity " + getImplementedInterface());
				}
			}

			// Register JML annotations if class is implementing SpecifiableProxyObject
			if (SpecifiableProxyObject.class.isAssignableFrom(getImplementedInterface())) {
				registerJMLAnnotations(m);
			}
		}

		// Register JML annotations if class is implementing SpecifiableProxyObject
		if (SpecifiableProxyObject.class.isAssignableFrom(getImplementedInterface())) {
			registerJMLAnnotations();
		}

		// Init delegate implementations
		delegateImplementations = new HashMap<>();
		for (Class<?> c : getImplementedInterface().getDeclaredClasses()) {
			if (c.getAnnotation(Implementation.class) != null) {
				if (getImplementedInterface().isAssignableFrom(c)) {
					Class<I> candidateImplementation = (Class<I>) c;
					// System.out.println("Found implementation " + candidateImplementation + " for " + getImplementedInterface());
					Set<Method> implementedMethods = new HashSet<Method>() {
						// We override here the add method to avoid to have duplicated method declaration with different return types
						@Override
						public boolean add(Method m) {
							for (Method m2 : this) {
								if (PamelaUtils.methodIsEquivalentTo(m, m2)) {
									if (TypeUtils.isTypeAssignableFrom(m.getGenericReturnType(), m2.getGenericReturnType())) {
										// m2 is the most specialized method, we can skip the adding of m
										return false;
									}
									else if (TypeUtils.isTypeAssignableFrom(m2.getGenericReturnType(), m.getGenericReturnType())) {
										// m is the most specialized method
										// We remove the previously defined (but less generic) m2, and add more generic m method
										remove(m2);
										return super.add(m);
									}
								}
							}
							return super.add(m);
						}
					};
					for (Method m : candidateImplementation.getDeclaredMethods()) {
						implementedMethods.add(m);
					}
					delegateImplementations.put(candidateImplementation, implementedMethods);
				}
				else {
					throw new ModelDefinitionException("Found candidate implementation " + c + " for entity " + getImplementedInterface()
							+ " which does not implement " + getImplementedInterface());
				}
			}
		}

	}

	public void finalizeImport() throws ModelDefinitionException {
		for (ModelProperty<? super I> property : properties.values()) {
			property.finalizeImport();
		}
	}

	private static String getPropertyIdentifier(Method m) {
		String propertyIdentifier = null;
		Getter aGetter = m.getAnnotation(Getter.class);
		if (aGetter != null) {
			propertyIdentifier = aGetter.value();
		}
		else {
			Setter aSetter = m.getAnnotation(Setter.class);
			if (aSetter != null) {
				propertyIdentifier = aSetter.value();
			}
			else {
				Adder anAdder = m.getAnnotation(Adder.class);
				if (anAdder != null) {
					propertyIdentifier = anAdder.value();
				}
				else {
					Remover aRemover = m.getAnnotation(Remover.class);
					if (aRemover != null) {
						propertyIdentifier = aRemover.value();
					}
				}
			}
		}
		return propertyIdentifier;
	}

	void init() throws ModelDefinitionException {

		// System.out.println("Init " + getImplementedInterface() + " with direct super entities " + getDirectSuperEntities());

		// We now resolve our inherited entities and properties
		if (getDirectSuperEntities() != null) {
			embeddedEntities.addAll(getDirectSuperEntities());
		}
		for (ModelProperty<? super I> property : declaredModelProperties.values()) {
			if (property.getType() != null && !StringConverterLibrary.getInstance().hasConverter(property.getType())
					&& !property.getType().isEnum() && !property.isStringConvertable() && !property.ignoreType()) {
				try {
					embeddedEntities.add(ModelEntityLibrary.get(property.getType(), true));
				} catch (ModelDefinitionException e) {
					throw new ModelDefinitionException("Could not retrieve model entity for property " + property + " and entity " + this,
							e);
				}
			}
		}

		// We also resolve our imports
		Imports imports = implementedInterface.getAnnotation(Imports.class);
		if (imports != null) {
			for (Import imp : imports.value()) {
				embeddedEntities.add(ModelEntityLibrary.get(imp.value(), true));
			}
		}

		embeddedEntities = Collections.unmodifiableSet(embeddedEntities);

		checkImplementationsClash();

		// System.out.println("For " + getImplementedInterface() + " embeddedEntities=" + embeddedEntities);
	}

	public Map<Class<I>, Set<Method>> getDelegateImplementations() {
		return delegateImplementations;
	}

	private void checkImplementationsClash() throws ModelDefinitionException {

		// System.out.println("checkImplementationsClash() for " + getImplementedInterface());
		// System.out.println("embeddedEntities=" + embeddedEntities);
		// System.out.println("getDirectSuperEntities()=" + getDirectSuperEntities());

		if (getDirectSuperEntities() != null) {
			Set<Method> implementedMethods = new HashSet<>();
			for (ModelEntity<? super I> parentEntity : getDirectSuperEntities()) {
				for (Class<? super I> implClass : parentEntity.delegateImplementations.keySet()) {
					for (Method m : parentEntity.delegateImplementations.get(implClass)) {
						for (Method m2 : implementedMethods) {
							if (PamelaUtils.methodIsEquivalentTo(m, m2) && !isToBeExcludedFromImplementationClashChecking(m)) {
								// We are in the case of implementation clash
								// We must now check if this clash was property handled
								boolean localImplementationWasFound = false;
								for (Class<?> localImplClass : delegateImplementations.keySet()) {
									for (Method m3 : delegateImplementations.get(localImplClass)) {
										if (PamelaUtils.methodIsEquivalentTo(m, m3)) {
											// A local implementation was found
											localImplementationWasFound = true;
											break;
										}
									}
									if (!localImplementationWasFound) {
										break;
									}
								}
								if (!localImplementationWasFound) {
									throw new ModelDefinitionException(
											"Multiple inheritance implementation clash with method " + m + " defined in " + implClass
													+ " and " + m2.getDeclaringClass() + ". Please disambiguate method.");
								}
							}
						}
						implementedMethods.add(m);
						// System.out.println("Consider implementation method " + m + " in " + getImplementedInterface());
					}
				}
			}
		}
	}

	/**
	 * Hook used to prevent multiple inheritance clash in JACOCO context
	 * 
	 * @param m
	 * @return
	 */
	private static boolean isToBeExcludedFromImplementationClashChecking(Method m) {
		return m.getName().contains("jacocoInit");
	}

	void mergeProperties() throws ModelDefinitionException {
		if (initialized) {
			return;
		}
		properties.putAll(declaredModelProperties);

		// Resolve inherited properties (we only scan direct parent properties, since themselves will scan for their inherited parents)
		if (getDirectSuperEntities() != null) {
			for (ModelEntity<? super I> parentEntity : getDirectSuperEntities()) {
				parentEntity.mergeProperties();
				for (ModelProperty<? super I> property : parentEntity.properties.values()) {
					createMergedProperty(property.getPropertyIdentifier(), true);
				}
			}
		}

		// Validate properties now (they should all have a getter and a return type, etc...
		for (ModelProperty<? super I> p : properties.values()) {
			p.validate();
		}
		initialized = true;
		for (ModelProperty<? super I> p : properties.values()) {
			propertyMethods.put(new ModelMethod(p.getGetterMethod()), p);
			if (p.getSetterMethod() != null) {
				propertyMethods.put(new ModelMethod(p.getSetterMethod()), p);
			}
			if (p.getAdderMethod() != null) {
				propertyMethods.put(new ModelMethod(p.getAdderMethod()), p);
			}
			if (p.getRemoverMethod() != null) {
				propertyMethods.put(new ModelMethod(p.getRemoverMethod()), p);
			}
		}

		// TODO: maybe it would be better to be closer to what constructors do, ie, if there are super-initializer,
		// And none of them are without arguments, then this entity should define an initializer with the same
		// method signature (this is to enforce the developer to be aware of what the parameters do):
		// FlexoModelObject.init(String flexoID) vs AbstractNode.init(String nodeName)-->same signature but the semantics of the parameter
		// is different
		// Validate initializers

		for (ModelInitializer i : initializers.values()) {
			for (String s : i.getParameters()) {
				if (s == null) {
					continue;
				}
				ModelProperty<? super I> modelProperty = getModelProperty(s);
				if (modelProperty == null) {
					throw new ModelDefinitionException("Initializer " + i.getInitializingMethod().toGenericString()
							+ " declares a parameter " + s + " but this entity has no such declared property");
				}
			}
		}
	}

	public Set<ModelEntity<?>> getEmbeddedEntities() {
		return embeddedEntities;
	}

	public Class<?> getImplementingClass() throws ModelDefinitionException {
		if (implementingClass != null) {
			return implementingClass;
		}
		if (implementationClass != null) {
			return implementingClass = implementationClass.value();
			// This may be not required under some circumstances
			/*if (implementedInterface.isAssignableFrom(implementationClass.value())) {
				return implementingClass = implementationClass.value();
			}
			else {
				throw new ModelDefinitionException("Class " + implementationClass.value().getName()
						+ " is declared as an implementation class of " + this + " but does not extend " + implementedInterface.getName());
			}*/
		}
		else {
			if (getDirectSuperEntities() != null) {
				for (ModelEntity<? super I> e : getDirectSuperEntities()) {
					Class<?> klass = e.getImplementingClass();
					if (klass != null) {
						if (implementingClass == null) {
							implementingClass = klass;
							// System.out.println("Found " + implementingClass + " for " + e.getImplementedInterface());
						}
						else if (implementingClass != klass) {
							throw new ModelDefinitionException(
									"Ambiguous implementing klass for entity '" + this + "'. Found more than one valid super klass: "
											+ implementingClass.getName() + " and " + klass.getName());
						}
					}
				}
			}
		}
		return implementingClass;
	}

	public boolean singleInheritance() {
		return superImplementedInterfaces != null && superImplementedInterfaces.size() == 1;
	}

	public boolean multipleInheritance() {
		return superImplementedInterfaces != null && superImplementedInterfaces.size() > 1;
	}

	public List<ModelEntity<? super I>> getDirectSuperEntities() throws ModelDefinitionException {
		if (directSuperEntities == null && superImplementedInterfaces != null) {
			directSuperEntities = new ArrayList<>(superImplementedInterfaces.size());
			for (Class<? super I> superInterface : superImplementedInterfaces) {
				ModelEntity<? super I> superEntity = ModelEntityLibrary.get(superInterface, true);
				directSuperEntities.add(superEntity);
			}
		}
		return directSuperEntities;
	}

	/**
	 * Returns a list of all the (direct & indirect) super entities of this entity.
	 * 
	 * @return all the (direct & indirect) super entities of this entity.
	 * @throws ModelDefinitionException
	 */
	public List<ModelEntity<? super I>> getAllSuperEntities() throws ModelDefinitionException {
		if (allSuperEntities == null && superImplementedInterfaces != null) {
			allSuperEntities = new ArrayList<>();
			// 1. We add the direct ancestors of this entity
			allSuperEntities.addAll(getDirectSuperEntities());
			// 2. We add the indirect ancestors of this entity to have a topologically sorted array.
			for (ModelEntity<? super I> superEntity : new ArrayList<>(allSuperEntities)) {
				allSuperEntities.addAll(superEntity.getAllSuperEntities());
			}
		}
		return allSuperEntities;
	}

	public boolean hasProperty(String propertyIdentifier) {
		return properties.containsKey(propertyIdentifier);
	}

	public boolean hasProperty(ModelProperty<?> modelProperty) {
		return properties.containsValue(modelProperty);
	}

	public ModelProperty<? super I> getPropertyForMethod(Method method) {
		return propertyMethods.get(new ModelMethod(method));
	}

	public ModelProperty<? super I> getPropertyForXMLAttributeName(String name) {
		if (modelPropertiesByXMLAttributeName == null) {
			synchronized (this) {
				if (modelPropertiesByXMLAttributeName == null) {
					modelPropertiesByXMLAttributeName = new HashMap<>();
					for (ModelProperty<? super I> property : properties.values()) {
						if (property.getXMLTag() != null) {
							modelPropertiesByXMLAttributeName.put(property.getXMLTag(), property);
						}
					}
					modelPropertiesByXMLAttributeName = Collections.unmodifiableMap(modelPropertiesByXMLAttributeName);
				}
			}
		}
		return modelPropertiesByXMLAttributeName.get(name);
	}

	/**
	 * Returns whether the implemented interface associated with <code>this</code> model entity has a method annotated with a {@link Getter}
	 * with its value set to the provided <code>propertyIdentifier</code>.
	 * 
	 * @param propertyIdentifier
	 * @return
	 */
	private boolean declaresProperty(String propertyIdentifier) {
		return declaredModelProperties.containsKey(propertyIdentifier);
	}

	/**
	 * Returns the {@link ModelProperty} with the identifier <code>propertyIdentifier</code>.
	 * 
	 * @param propertyIdentifier
	 *            the identifier of the property
	 * @return the property with the identifier <code>propertyIdentifier</code>.
	 * @throws ModelDefinitionException
	 */
	public ModelProperty<? super I> getModelProperty(String propertyIdentifier) throws ModelDefinitionException {
		return properties.get(propertyIdentifier);
	}

	/**
	 * Creates the {@link ModelProperty} with the identifier <code>propertyIdentifier</code>.
	 * 
	 * @param propertyIdentifier
	 *            the identifier of the property
	 * @param create
	 *            whether the property should be create or not, if not found
	 * @return the property with the identifier <code>propertyIdentifier</code>.
	 * @throws ModelDefinitionException
	 */
	void createMergedProperty(String propertyIdentifier, boolean create) throws ModelDefinitionException {
		ModelProperty<? super I> returned = buildModelProperty(propertyIdentifier);
		properties.put(propertyIdentifier, returned);
	}

	/**
	 * Builds the {@link ModelProperty} with identifier <code>propertyIdentifier</code>, if it is declared at least once in the hierarchy
	 * (i.e., at least one method is annotated with the {@link Getter} annotation and the given identifier, <code>propertyIdentifier</code>
	 * ). In case of inheritance, the property is combined with all its ancestors. In case of multiple inheritance of the same property,
	 * conflicts are resolved to the possible extent. In case of contradiction, a {@link PropertyClashException} is thrown.
	 * 
	 * @param propertyIdentifier
	 *            the identifier of the property
	 * @return the new, possibly combined, property.
	 * @throws ModelDefinitionException
	 *             in case of an inconsistency in the model of a clash of property inheritance.
	 */
	private ModelProperty<? super I> buildModelProperty(String propertyIdentifier) throws ModelDefinitionException {
		ModelProperty<I> property = ModelProperty.getModelProperty(propertyIdentifier, this);
		if (singleInheritance() || multipleInheritance()) {
			ModelProperty<? super I> parentProperty = buildModelPropertyUsingParentProperties(propertyIdentifier, property);
			return combine(property, parentProperty);
		}
		return property;
	}

	/**
	 * Returns a model property with the identifier <code>propertyIdentifier</code> which is a combination of all the model properties with
	 * the identifier <code>propertyIdentifier</code> of the parent entities. This method may return <code>null</code> in case amongst all
	 * parents, non of them declare a property with identifier <code>propertyIdentifier</code>.
	 * 
	 * @param propertyIdentifier
	 *            the identifier of the property
	 * @param property
	 *            the model property with the identifier defined for <code>this</code> {@link ModelEntity}.
	 * @return
	 * @throws ModelDefinitionException
	 */
	private ModelProperty<? super I> buildModelPropertyUsingParentProperties(String propertyIdentifier, ModelProperty<I> property)
			throws ModelDefinitionException {
		ModelProperty<? super I> returned = null;
		for (ModelEntity<? super I> parent : getDirectSuperEntities()) {
			if (!parent.hasProperty(propertyIdentifier)) {
				continue;
			}
			if (returned == null) {
				returned = parent.getModelProperty(propertyIdentifier);
			}
			else {
				returned = combineAsAncestors(parent.getModelProperty(propertyIdentifier), returned, property);
			}
		}
		return returned;
	}

	/**
	 * Returns a combined property which is the merge of the property <code>property</code> and its parent property
	 * <code>parentProperty</code>. In case of conflicts, the behaviour defined by <code>property</code> superseeds the one defined by
	 * <code>parentProperty</code>
	 * 
	 * @param property
	 *            the property to merge
	 * @param parentProperty
	 *            the parent property to merge
	 * @return a combined/merged property
	 * @throws ModelDefinitionException
	 */
	private ModelProperty<? super I> combine(ModelProperty<I> property, ModelProperty<? super I> parentProperty)
			throws ModelDefinitionException {
		return property.combineWith(parentProperty, property);
	}

	private ModelProperty<? super I> combineAsAncestors(ModelProperty<? super I> property1, ModelProperty<? super I> property2,
			ModelProperty<I> declaredProperty) throws ModelDefinitionException {
		if (property1 == null) {
			return property2;
		}
		if (property2 == null) {
			return property1;
		}
		checkForContradictions(property1, property2, declaredProperty);
		return property1.combineWith(property2, declaredProperty);
	}

	private void checkForContradictions(ModelProperty<? super I> property1, ModelProperty<? super I> property2,
			ModelProperty<I> declaredProperty) throws PropertyClashException {
		String contradiction = property1.contradicts(property2, declaredProperty);
		if (contradiction != null) {
			throw new PropertyClashException("Property '" + property1.getPropertyIdentifier() + "' contradiction between entity '"
					+ property1.getModelEntity() + "' and entity '" + property2.getModelEntity() + "'.\nReason:" + contradiction);
		}
	}

	protected boolean declaresModelProperty(String propertyIdentifier) {
		for (Method m : getImplementedInterface().getDeclaredMethods()) {
			Getter aGetter = m.getAnnotation(Getter.class);
			if (aGetter != null && aGetter.value().equals(propertyIdentifier)) {
				return true;
			}
			Setter aSetter = m.getAnnotation(Setter.class);
			if (aSetter != null && aSetter.value().equals(propertyIdentifier)) {
				return true;
			}
			Adder anAdder = m.getAnnotation(Adder.class);
			if (anAdder != null && anAdder.value().equals(propertyIdentifier)) {
				return true;
			}
			Remover aRemover = m.getAnnotation(Remover.class);
			if (aRemover != null && aRemover.value().equals(propertyIdentifier)) {
				return true;
			}
			Reindexer aReindexer = m.getAnnotation(Reindexer.class);
			if (aReindexer != null && aReindexer.value().equals(propertyIdentifier)) {
				return true;
			}
		}
		return false;
	}

	final public Class<I> getImplementedInterface() {
		return implementedInterface;
	}

	public boolean isSimplePamelaInstrumentation() {
		return !getImplementedInterface().isInterface();
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public ImplementationClass getImplementationClass() {
		return implementationClass;
	}

	private boolean xmlElementHasBeenRetrieved = false;

	/**
	 * Return {@link XMLElement} by combining all super entities declarations
	 * 
	 * @return
	 */
	public XMLElement getXMLElement() {

		if (!xmlElementHasBeenRetrieved) {

			xmlElementHasBeenRetrieved = true;

			xmlElement = implementedInterface.getAnnotation(XMLElement.class);

			String xmlTag = XMLElement.DEFAULT_XML_TAG;
			String context = XMLElement.NO_CONTEXT;
			String namespace = XMLElement.NO_NAME_SPACE;
			String idFactory = XMLElement.NO_ID_FACTORY;
			boolean primary = false;

			if (xmlElement != null) {
				xmlTag = xmlElement.xmlTag();
				if (xmlTag == null || xmlTag.equals(XMLElement.DEFAULT_XML_TAG)) {
					xmlTag = getImplementedInterface().getSimpleName();
				}
			}

			try {
				if (getDirectSuperEntities() != null) {
					for (ModelEntity<?> superEntity : getDirectSuperEntities()) {
						if (superEntity.getXMLElement() != null) {
							if (superEntity.getXMLElement() != null) {
								if (xmlElement == null) {
									xmlElement = superEntity.getXMLElement();
								}
								else {
									if (!superEntity.getXMLElement().context().equals(XMLElement.NO_CONTEXT)) {
										context = superEntity.getXMLElement().context();
									}
									else {
										context = xmlElement.context();
									}
									if (!superEntity.getXMLElement().namespace().equals(XMLElement.NO_NAME_SPACE)) {
										namespace = superEntity.getXMLElement().namespace();
									}
									else {
										namespace = xmlElement.namespace();
									}
									if (!superEntity.getXMLElement().idFactory().equals(XMLElement.NO_ID_FACTORY)) {
										idFactory = superEntity.getXMLElement().idFactory();
									}
									else {
										idFactory = xmlElement.idFactory();
									}
									primary |= superEntity.getXMLElement().primary();
									primary |= xmlElement.primary();
									xmlElement = new XMLElement.XMLElementImpl(xmlTag, context, namespace, primary, idFactory);
								}
							}
						}
					}
				}
			} catch (ModelDefinitionException e) {
				e.printStackTrace();
			}

		}

		return xmlElement;
	}

	public String getXMLTag() {
		if (xmlTag == null) {
			if (getXMLElement() != null) {
				xmlTag = getXMLElement().xmlTag();
			}
			if (xmlTag == null || xmlTag.equals(XMLElement.DEFAULT_XML_TAG)) {
				xmlTag = getImplementedInterface().getSimpleName();
			}
		}
		return xmlTag;
	}

	/**
	 * Return an iterator for {@link ModelProperty} objects<br>
	 * Note that order is absolutely not guaranteed
	 * 
	 * @return
	 * @throws ModelDefinitionException
	 */
	public Iterator<ModelProperty<? super I>> getProperties() throws ModelDefinitionException {
		return properties.values().iterator();
	}

	public Iterable<ModelProperty<? super I>> getPropertyIterable() {
		return Collections.unmodifiableCollection(properties.values());
	}

	public Collection<ModelProperty<I>> getDeclaredProperties() {
		return declaredModelProperties.values();
	}

	/**
	 * Return an iterator for {@link ModelProperty} objects<br>
	 * Order respect {@link CloningStrategy#cloneAfterProperty()} annotation
	 * 
	 * @return
	 * @throws ModelDefinitionException
	 */
	public Iterator<ModelProperty<? super I>> getPropertiesOrderedForCloning() throws ModelDefinitionException {
		ArrayList<ModelProperty<? super I>> returned = new ArrayList<>();
		for (ModelProperty<? super I> p : properties.values()) {
			appendProperty(p, returned);
		}
		return returned.iterator();
	}

	private void appendProperty(ModelProperty<? super I> p, List<ModelProperty<? super I>> list) throws ModelDefinitionException {
		if (p.getCloneAfterProperty() != null) {
			appendProperty((ModelProperty<? super I>) p.getCloneAfterProperty(), list);
			if (!list.contains(p)) {
				list.add(p);
			}
		}
		else {
			if (!list.contains(p)) {
				list.add(0, p);
			}
		}
	}

	public int getPropertiesSize() {
		return properties.size();
	}

	@Override
	public String toString() {
		return "ModelEntity[" + getImplementedInterface().getSimpleName() + "]";
	}

	public List<ModelEntity> getAllDescendantsAndMe(PamelaMetaModel pamelaMetaModel) throws ModelDefinitionException {
		List<ModelEntity> returned = getAllDescendants(pamelaMetaModel);
		returned.add(this);
		return returned;
	}

	public List<ModelEntity> getAllDescendants(PamelaMetaModel pamelaMetaModel) throws ModelDefinitionException {
		List<ModelEntity> returned = new ArrayList<>();
		Iterator<ModelEntity> i = pamelaMetaModel.getEntities();
		while (i.hasNext()) {
			ModelEntity<?> entity = i.next();
			if (isAncestorOf(entity)) {
				returned.add(entity);
			}
		}
		return returned;
	}

	public boolean isAncestorOf(ModelEntity<?> entity) throws ModelDefinitionException {
		if (entity == null) {
			return false;
		}
		if (entity.getDirectSuperEntities() != null) {
			for (ModelEntity<?> e : entity.getDirectSuperEntities()) {
				if (e == this) {
					return true;
				}
				else if (isAncestorOf(e)) {
					return true;
				}
			}
		}
		return false;
	}

	private Boolean hasInitializers;

	public boolean hasInitializers() throws ModelDefinitionException {
		if (hasInitializers == null) {
			if (initializers.size() > 0) {
				return hasInitializers = true;
			}
			else if (getDirectSuperEntities() != null) {
				for (ModelEntity<?> e : getDirectSuperEntities()) {
					if (e.hasInitializers()) {
						return hasInitializers = true;
					}
				}
			}
			return hasInitializers = false;
		}
		return hasInitializers;
	}

	public ModelInitializer getInitializers(Method m) throws ModelDefinitionException {
		if (m.getDeclaringClass() != implementedInterface) {
			ModelEntity<?> e = ModelEntityLibrary.get(m.getDeclaringClass());
			if (e == null) {
				throw new ModelExecutionException("Could not find initializer for method " + m.toGenericString() + ". Make sure that "
						+ m.getDeclaringClass().getName() + " is annotated with ModelEntity and has been imported.");
			}
			return e.getInitializers(m);
		}
		return initializers.get(m);
	}

	public ModelInitializer getInitializerForArgs(Object[] args) throws ModelDefinitionException {
		Class<?>[] types = new Class[args.length];
		for (int i = 0; i < args.length; i++) {
			types[i] = args[i].getClass();
		}
		return getInitializerForArgs(types);
	}

	public ModelInitializer getInitializerForArgs(Class<?>[] types) throws ModelDefinitionException {
		List<ModelInitializer> list = getPossibleInitializers(types);
		if (list.size() == 0) {
			if (initializers.size() > 0) {
				return null;
			}
			ModelInitializer found = null;
			if (getDirectSuperEntities() != null) {
				for (ModelEntity<? super I> e : getDirectSuperEntities()) {
					ModelInitializer initializer = e.getInitializerForArgs(types);
					if (found == null) {
						found = initializer;
					}
					else {
						throw new ModelDefinitionException("Initializer clash: " + found.getInitializingMethod().toGenericString()
								+ " cannot be distinguished with " + initializer.getInitializingMethod().toGenericString()
								+ ". Please override initializer in " + getImplementedInterface());
					}

				}
			}
			return found;
		}
		return list.get(0);
	}

	public List<ModelInitializer> getPossibleInitializers(Class<?>[] types) {
		List<ModelInitializer> list = new ArrayList<>();
		for (ModelInitializer init : initializers.values()) {
			int i = 0;
			Class<?>[] parameterTypes = init.getInitializingMethod().getParameterTypes();
			boolean ok = parameterTypes.length == types.length;
			if (ok) {
				for (Class<?> c : parameterTypes) {
					if (types[i] != null && !c.isAssignableFrom(types[i])) {
						ok = false;
						break;
					}
					i++;
				}
				if (ok) {
					list.add(init);
				}
			}
		}
		return list;
	}

	/**
	 * Return the first found deserialization initializer in the class hierarchy<br>
	 * TODO: manage multiple inheritance issues
	 * 
	 * @return
	 * @throws ModelDefinitionException
	 */
	// TODO: manage multiple inheritance issues
	public DeserializationInitializer getDeserializationInitializer() throws ModelDefinitionException {
		if (deserializationInitializer == null) {
			if (getDirectSuperEntities() != null) {
				for (ModelEntity<?> e : getDirectSuperEntities()) {
					if (e.getDeserializationInitializer() != null) {
						return e.getDeserializationInitializer();
					}
				}
			}
		}
		return deserializationInitializer;
	}

	/**
	 * Return the first found deserialization finalizer in the class hierarchy<br>
	 * TODO: manage multiple inheritance issues
	 * 
	 * @return
	 * @throws ModelDefinitionException
	 */
	// TODO: manage multiple inheritance issues
	public DeserializationFinalizer getDeserializationFinalizer() throws ModelDefinitionException {
		if (deserializationFinalizer == null) {
			if (getDirectSuperEntities() != null) {
				for (ModelEntity<?> e : getDirectSuperEntities()) {
					if (e.getDeserializationFinalizer() != null) {
						return e.getDeserializationFinalizer();
					}
				}
			}
		}
		return deserializationFinalizer;
	}

	/**
	 * Returns the list of model properties of this model entity which are of the type provided by <code>type</code> or any of its
	 * compatible type (ie, a super-type of <code>type</code>).
	 * 
	 * @param type
	 *            the type used for model properties lookup
	 * @return a list of model properties to which an instance of <code>type</code> can be assigned or added
	 */
	public Collection<ModelProperty<? super I>> getPropertiesAssignableFrom(Class<?> type) {
		Collection<ModelProperty<? super I>> ppProperties = new ArrayList<>();
		for (ModelProperty<? super I> p : properties.values()) {
			if (TypeUtils.isTypeAssignableFrom(p.getType(), type)) {
				ppProperties.add(p);
			}
		}
		return ppProperties;
	}

	public Modify getModify() throws ModelDefinitionException {
		if (modify != null) {
			return modify;
		}
		else {
			if (getDirectSuperEntities() != null) {
				for (ModelEntity<? super I> e : getDirectSuperEntities()) {
					if (e.getModify() != null) {
						if (modify == null) {
							modify = e.getModify();
						}
						else {
							throw new ModelDefinitionException("Duplicated modify annotation on " + this
									+ ". Please add modify annotation on " + implementedInterface.getName());
						}
					}
				}
			}
		}
		return null;
	}

	public Finder getFinder(String string) {
		return null;
	}

	/**
	 * Check that this entity with supplied factory contains all required implementation<br>
	 * If entity is abstract simply return
	 * 
	 * @throws MissingImplementationException
	 *             when an implementation was not found
	 */
	public void checkMethodImplementations(PamelaModelFactory factory) throws ModelDefinitionException, MissingImplementationException {
		// Abstract entities are allowed not to provide all implementations
		if (isAbstract()) {
			return;
		}

		MissingImplementationException thrown = null;
		for (Method method : getNotOverridenMethods()) {
			if (!checkMethodImplementation(method, factory)) {
				System.err.println("NOT FOUND: " + method + " in " + getImplementedInterface());
				if (thrown == null) {
					thrown = new MissingImplementationException(this, method, factory);
				}
			}
		}
		if (thrown != null) {
			throw thrown;
		}
	}

	/**
	 * Return the list of all methods beeing implemented by entity addressed by implemented interface<br>
	 * Methods beeing shadowed by overriden methods are excluded from results
	 * 
	 * @return
	 */
	public List<Method> getNotOverridenMethods() {
		List<Method> returned = new ArrayList<>();
		for (Method m1 : getImplementedInterface().getMethods()) {
			boolean isOverriden = false;
			// Method overridingMethod = null;
			for (Method m2 : getImplementedInterface().getMethods()) {
				if (!m1.equals(m2) && PamelaUtils.methodOverrides(m2, m1, getImplementedInterface())) {
					isOverriden = true;
					// overridingMethod = m2;
				}

			}
			if (!isOverriden) {
				returned.add(m1);
			} /*else {
				System.out.println("Dismiss " + m1 + " because overriden by " + overridingMethod);
				}*/
		}
		return returned;
	}

	/**
	 * Check that this entity provides an implementation for supplied method, given a {@link PamelaModelFactory}
	 * 
	 * @return true if an implementation was found
	 * @throws ModelDefinitionException
	 */
	private boolean checkMethodImplementation(Method method, PamelaModelFactory factory) throws ModelDefinitionException {

		// Abstract entities are allowed not to provide all implementations
		ModelProperty<?> property = getPropertyForMethod(method);
		if (property != null) {
			return true;
		}
		else {
			if (method.isDefault()) {
				return true;
			}
			if (method.getAnnotation(Getter.class) != null) {
				return true;
			}
			if (method.getAnnotation(Setter.class) != null) {
				return true;
			}
			if (method.getAnnotation(Updater.class) != null) {
				return true;
			}
			if (method.getAnnotation(Finder.class) != null) {
				return true;
			}
			if (method.getAnnotation(Initializer.class) != null) {
				return true;
			}
			if (method.getAnnotation(Reindexer.class) != null) {
				return true;
			}

			// This has not been recognized as a property
			if (HasPropertyChangeSupport.class.isAssignableFrom(getImplementedInterface())) {
				if (PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.GET_PROPERTY_CHANGE_SUPPORT)) {
					if (PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.GET_PROPERTY_CHANGE_SUPPORT)) {
						return true;
					}
				}
			}
			if (AccessibleProxyObject.class.isAssignableFrom(getImplementedInterface())) {
				if (PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.PERFORM_SUPER_GETTER)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.PERFORM_SUPER_SETTER)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.PERFORM_SUPER_ADDER)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.PERFORM_SUPER_ADDER_AT_INDEX)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.PERFORM_SUPER_REMOVER)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.PERFORM_SUPER_FINDER)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.PERFORM_SUPER_GETTER_ENTITY)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.PERFORM_SUPER_SETTER_ENTITY)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.PERFORM_SUPER_ADDER_ENTITY)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.PERFORM_SUPER_REMOVER_ENTITY)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.PERFORM_SUPER_DELETER_ENTITY)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.PERFORM_SUPER_FINDER_ENTITY)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.PERFORM_SUPER_INITIALIZER)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.IS_SERIALIZING)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.IS_DESERIALIZING)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.IS_MODIFIED)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.SET_MODIFIED)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.PERFORM_SUPER_SET_MODIFIED)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.DESTROY)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.UPDATE_WITH_OBJECT)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.EQUALS_OBJECT)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.EQUALS_OBJECT_USING_FILTER)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.ACCEPT_VISITOR)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.ACCEPT_WITH_STRATEGY_VISITOR)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.GET_EMBEDDED)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.GET_REFERENCED)) {
					return true;
				}
			}
			if (CloneableProxyObject.class.isAssignableFrom(getImplementedInterface())) {
				if (PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.CLONE_OBJECT)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.CLONE_OBJECT_WITH_CONTEXT)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.IS_BEING_CLONED)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.IS_CREATED_BY_CLONING)) {
					return true;
				}
			}
			if (DeletableProxyObject.class.isAssignableFrom(getImplementedInterface())) {
				if (PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.IS_DELETED)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.GET_DELETED_PROPERTY)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.PERFORM_SUPER_DELETER)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.DELETE_OBJECT)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.PERFORM_SUPER_UNDELETER)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.UNDELETE_OBJECT)) {
					return true;
				}
			}
			if (DeletableProxyObject.class.isAssignableFrom(getImplementedInterface())) {
				if (PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.IS_DELETED)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.GET_DELETED_PROPERTY)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.PERFORM_SUPER_DELETER)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.DELETE_OBJECT)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.PERFORM_SUPER_UNDELETER)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.UNDELETE_OBJECT)) {
					return true;
				}
			}
			if (KeyValueCoding.class.isAssignableFrom(getImplementedInterface())) {
				if (PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.HAS_KEY)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.OBJECT_FOR_KEY)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.SET_OBJECT_FOR_KEY)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.GET_TYPE_FOR_KEY)) {
					return true;
				}
			}
			if (SpecifiableProxyObject.class.isAssignableFrom(getImplementedInterface())) {
				if (PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.ENABLE_ASSERTION_CHECKING)
						|| PamelaUtils.methodIsEquivalentTo(method, ProxyMethodHandler.DISABLE_ASSERTION_CHECKING)) {
					return true;
				}
			}

			// Look up in base implementation class
			Class<?> implementingClassForInterface = factory.getImplementingClassForInterface(getImplementedInterface());
			if (implementingClassForInterface != null) {
				try {
					Method m = implementingClassForInterface.getMethod(method.getName(), method.getParameterTypes());
					if (m != null && !Modifier.isAbstract(m.getModifiers())) {
						// We have found a non-abtract method which implements searched API method
						return true;
					}
				} catch (SecurityException e) {
				} catch (NoSuchMethodException e) {
				}
			}
		}

		// Look up in delegate implementation class
		return checkMethodImplementationInDelegateImplementations(method, factory);
	}

	/**
	 * Check that this entity provides an implementation for supplied method, given a {@link PamelaModelFactory}
	 * 
	 * @return true if an implementation was found
	 * @throws ModelDefinitionException
	 */
	private boolean checkMethodImplementationInDelegateImplementations(Method method, PamelaModelFactory factory)
			throws ModelDefinitionException {
		// Look up in delegate implementation class
		if (getDelegateImplementations().size() > 0) {
			for (Class<? super I> delegateImplementationClass : getDelegateImplementations().keySet()) {
				try {
					Method m = delegateImplementationClass.getMethod(method.getName(), method.getParameterTypes());
					if (m != null && !Modifier.isAbstract(m.getModifiers())) {
						// We have found a non-abtract method which implements searched API method
						return true;
					}
				} catch (SecurityException e) {
				} catch (NoSuchMethodException e) {
				}
			}
		}
		// May be in parent entitities ?
		if (getDirectSuperEntities() != null) {
			for (ModelEntity<? super I> parentEntity : getDirectSuperEntities()) {
				if (parentEntity.checkMethodImplementationInDelegateImplementations(method, factory)) {
					return true;
				}
			}
		}

		return false;

	}

	private Map<String, JMLMethodDefinition> jmlMethods = new HashMap<>();
	private JMLInvariant<I> invariant;

	private void registerJMLAnnotations() {
		if (SpecifiableProxyObject.class.isAssignableFrom(getImplementedInterface())) {
			if (getImplementedInterface().isAnnotationPresent(Invariant.class)) {
				invariant = new JMLInvariant<>(getImplementedInterface().getAnnotation(Invariant.class), this);
			}
		}
	}

	private JMLMethodDefinition<I> registerJMLAnnotations(Method method) {
		if (JMLMethodDefinition.hasJMLAnnotations(method)) {
			JMLMethodDefinition<I> returned = new JMLMethodDefinition<>(method, this);
			jmlMethods.put(returned.getSignature(), returned);
			return returned;
		}
		return null;
	}

	public JMLMethodDefinition<? super I> getJMLMethodDefinition(Method method) {
		JMLMethodDefinition<? super I> returned = jmlMethods.get(PamelaUtils.getSignature(method, getImplementedInterface(), true));
		if (returned == null) {
			try {
				if (getDirectSuperEntities() != null) {
					for (ModelEntity<? super I> superEntity : getDirectSuperEntities()) {
						returned = superEntity.getJMLMethodDefinition(method);
						if (returned != null) {
							return returned;
						}
					}
				}
			} catch (ModelDefinitionException e) {
				e.printStackTrace();
			}
		}
		return returned;
	}

	public JMLInvariant<I> getInvariant() {
		return invariant;
	}

	public static boolean isModelEntity(Class<?> type) {
		return type.isAnnotationPresent(org.openflexo.pamela.annotations.ModelEntity.class);
	}
}
