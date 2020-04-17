/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo.pamela;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.Nonnull;

import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.patterns.AbstractPatternFactory;
import org.openflexo.pamela.patterns.DeclarePatterns;
import org.openflexo.pamela.patterns.PatternDefinition;
import org.openflexo.pamela.patterns.PatternInstance;
import org.openflexo.pamela.patterns.PatternLibrary;
import org.openflexo.toolbox.StringUtils;

public class ModelContext {

	public static class ModelPropertyXMLTag<I> {
		private final String tag;
		private final ModelProperty<? super I> property;
		private final ModelEntity<?> accessedEntity;

		public ModelPropertyXMLTag(ModelProperty<? super I> property) {
			super();
			this.property = property;
			this.accessedEntity = null;
			this.tag = property.getXMLContext() + property.getXMLElement().xmlTag();
		}

		public ModelPropertyXMLTag(ModelProperty<? super I> property, ModelEntity<?> accessedEntity) {
			super();
			this.property = property;
			this.accessedEntity = accessedEntity;
			this.tag = property.getXMLContext() + accessedEntity.getXMLTag();
		}

		public String getTag() {
			return tag;
		}

		public List<String> getDeprecatedTags() {
			List<String> returned = new ArrayList<>();
			if (accessedEntity.getXMLElement() != null && StringUtils.isNotEmpty(accessedEntity.getXMLElement().deprecatedXMLTags())) {
				StringTokenizer st = new StringTokenizer(accessedEntity.getXMLElement().deprecatedXMLTags(), ",");
				while (st.hasMoreTokens()) {
					String nextTag = st.nextToken();
					returned.add(property.getXMLContext() + nextTag);
				}
			}
			if (getProperty().getXMLElement() != null && StringUtils.isNotEmpty(getProperty().getXMLElement().deprecatedContext())) {
				returned.add(getProperty().getXMLElement().deprecatedContext() + accessedEntity.getXMLTag());
			}
			if (returned.size() == 0) {
				return null;
			}
			return returned;
		}

		public ModelProperty<? super I> getProperty() {
			return property;
		}

		public ModelEntity<?> getAccessedEntity() {
			return accessedEntity;
		}

		@Override
		public String toString() {
			return "ModelPropertyXMLTag" + getAccessedEntity() + getProperty() + "/tag=" + getTag();
		}
	}

	private Map<Class, ModelEntity> modelEntities;
	private Map<String, ModelEntity<?>> modelEntitiesByXmlTag;
	private final Map<ModelEntity<?>, Map<String, ModelPropertyXMLTag<?>>> modelPropertiesByXmlTag;
	private final Class<?> baseClass;

	public ModelContext(@Nonnull Class<?> baseClass, boolean isFinalModel) throws ModelDefinitionException {
		this.baseClass = baseClass;
		modelEntities = new HashMap<>();
		modelEntitiesByXmlTag = new HashMap<>();
		modelPropertiesByXmlTag = new HashMap<>();
		ModelEntity<?> modelEntity = ModelEntityLibrary.importEntity(baseClass);
		appendEntity(modelEntity, new HashSet<>());
		modelEntities = Collections.unmodifiableMap(modelEntities);
		modelEntitiesByXmlTag = Collections.unmodifiableMap(modelEntitiesByXmlTag);
		if (isFinalModel){
			discoverPatterns();
		}
	}

	/**
	 * Browse the {@link ModelEntity} code to identify {@link PatternDefinition}
	 * 
	 * @throws ModelDefinitionException
	 */
	private void discoverPatterns() throws ModelDefinitionException {
		/*this.patternContext = new PatternContext(this);
		for (ModelEntity<?> modelEntity : modelEntities.values()) {
			modelEntity.finalizeImport();
			this.patternContext.attachClass(modelEntity.getImplementedInterface());
		}*/

		ServiceLoader<PatternLibrary> loader = ServiceLoader.load(PatternLibrary.class);

		for (PatternLibrary patternLibrary : loader) {
			// System.out.println("Found PatternLibrary: " + patternLibrary);
			DeclarePatterns declarePatterns = patternLibrary.getClass().getAnnotation(DeclarePatterns.class);
			for (Class<? extends AbstractPatternFactory<?>> factoryClass : declarePatterns.value()) {
				// System.out.println("Analysing pattern: " + factoryClass);
				try {
					Constructor<? extends AbstractPatternFactory<?>> constructor = factoryClass.getConstructor(ModelContext.class);
					AbstractPatternFactory<?> patternFactory = constructor.newInstance(this);
					patternFactories.add(patternFactory);
					for (ModelEntity<?> modelEntity : modelEntities.values()) {
						patternFactory.discoverEntity(modelEntity);
					}
					// System.out.println("patternDefinitions= " + patternFactory.getPatternDefinitions());
					for (PatternDefinition patternDefinition : patternFactory.getPatternDefinitions().values()) {
						patternDefinition.finalizeDefinition();
					}
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public <P extends PatternDefinition> List<P> getPatternDefinitions(Class<P> patternDefinitionClass) {
		List<P> returned = new ArrayList<>();
		for (AbstractPatternFactory<?> patternFactory : patternFactories) {
			for (PatternDefinition patternDefinition : patternFactory.getPatternDefinitions().values()) {
				if (patternDefinitionClass.isAssignableFrom(patternDefinition.getClass())) {
					returned.add((P) patternDefinition);
				}
			}
		}
		return returned;
	}

	private List<AbstractPatternFactory<?>> patternFactories = new ArrayList<>();
	private Map<Object, Set<PatternInstance<?>>> patternInstances = new HashMap<>();
	private Map<PatternDefinition, Set<PatternInstance<?>>> registeredPatternInstances = new HashMap<>();

	public <I> void notifiedNewInstance(I newInstance, ModelEntity<I> modelEntity) {
		for (AbstractPatternFactory<?> patternFactory : patternFactories) {
			for (PatternDefinition patternDefinition : patternFactory.getPatternDefinitions().values()) {
				patternDefinition.notifiedNewInstance(newInstance, modelEntity);
			}
		}
	}

	public boolean isMethodInvolvedInPattern(Method method) {
		for (AbstractPatternFactory<?> patternFactory : patternFactories) {
			for (PatternDefinition patternDefinition : patternFactory.getPatternDefinitions().values()) {
				if (patternDefinition.isMethodInvolvedInPattern(method)) {
					return true;
				}
			}
		}
		return false;

	}

	public <P extends PatternDefinition> void registerPatternInstance(PatternInstance<P> patternInstance) {
		P definition = patternInstance.getPatternDefinition();
		Set<PatternInstance<?>> s = registeredPatternInstances.get(definition);
		if (s == null) {
			s = new HashSet<>();
			registeredPatternInstances.put(definition, s);
		}
		System.out.println("Registering " + patternInstance);
		s.add(patternInstance);
	}

	public void registerStakeHolderForPatternInstance(Object stakeHolder, String role, PatternInstance<?> patternInstance) {
		Set<PatternInstance<?>> s = patternInstances.get(stakeHolder);
		if (s == null) {
			s = new HashSet<>();
			patternInstances.put(stakeHolder, s);
		}
		System.out.println("Registering " + stakeHolder + " as " + role + " for pattern instance " + patternInstance);
		s.add(patternInstance);
	}

	public Set<PatternInstance<?>> getPatternInstances(Object stakeholder) {
		return patternInstances.get(stakeholder);
	}

	public <P extends PatternDefinition> Set<PatternInstance<P>> getPatternInstances(P patternDefinition) {
		return (Set) registeredPatternInstances.get(patternDefinition);
	}

	public ModelContext(Class<?> baseClass, List<ModelContext> contexts) throws ModelDefinitionException {
		this.baseClass = baseClass;
		modelEntities = new HashMap<>();
		modelEntitiesByXmlTag = new HashMap<>();
		modelPropertiesByXmlTag = new HashMap<>();
		for (ModelContext context : contexts) {
			for (Entry<String, ModelEntity<?>> e : context.modelEntitiesByXmlTag.entrySet()) {
				ModelEntity<?> entity = modelEntitiesByXmlTag.put(e.getKey(), e.getValue());
				// TODO: handle properly namespaces. Different namespaces allows to have identical tags
				// See also importModelEntity(Class<T>)
				if (entity != null && !entity.getImplementedInterface().equals(e.getValue().getImplementedInterface())) {
					throw new ModelDefinitionException(
							entity + " and " + e.getValue() + " declare the same XML tag but not the same implemented interface");
				}
			}
			modelEntities.putAll(context.modelEntities);
		}
		if (baseClass != null) {
			ModelEntity<?> modelEntity = ModelEntityLibrary.importEntity(baseClass);
			appendEntity(modelEntity, new HashSet<>());
		}
		modelEntities = Collections.unmodifiableMap(modelEntities);
		modelEntitiesByXmlTag = Collections.unmodifiableMap(modelEntitiesByXmlTag);
		discoverPatterns();
	}

	public ModelContext(List<Class<?>> baseClasses) throws ModelDefinitionException {
		this(null, makeModelContextList(baseClasses));
	}

	private static List<ModelContext> makeModelContextList(List<Class<?>> baseClasses) throws ModelDefinitionException {
		List<ModelContext> returned = new ArrayList<>();
		for (Class<?> c : baseClasses) {
			returned.add(ModelContextLibrary.getModelContext(c, false));
		}
		return returned;
	}

	public ModelContext(ModelContext... contexts) throws ModelDefinitionException {
		this(null, contexts);
	}

	public ModelContext(Class<?> baseClass, ModelContext... contexts) throws ModelDefinitionException {
		this(baseClass, Arrays.asList(contexts));
	}

	private void appendEntity(ModelEntity<?> modelEntity, Set<ModelEntity<?>> visited) throws ModelDefinitionException {
		visited.add(modelEntity);
		modelEntities.put(modelEntity.getImplementedInterface(), modelEntity);
		ModelEntity<?> put = modelEntitiesByXmlTag.put(modelEntity.getXMLTag(), modelEntity);
		if (put != null && put != modelEntity) {
			throw new ModelDefinitionException(
					"Two entities define the same XMLTag '" + modelEntity.getXMLTag() + "'. Implemented interfaces: "
							+ modelEntity.getImplementedInterface().getName() + " " + put.getImplementedInterface().getName());
		}
		if (!modelEntity.isAbstract()) {
			if (modelEntity.getXMLElement() != null && StringUtils.isNotEmpty(modelEntity.getXMLElement().deprecatedXMLTags())) {
				StringTokenizer st = new StringTokenizer(modelEntity.getXMLElement().deprecatedXMLTags(), ",");
				while (st.hasMoreTokens()) {
					String deprecatedTag = st.nextToken();
					ModelEntity<?> put2 = modelEntitiesByXmlTag.put(deprecatedTag, modelEntity);
					if (put2 != null && put2 != modelEntity) {
						throw new ModelDefinitionException(
								"Two entities define the same XMLTag '" + deprecatedTag + "'. Implemented interfaces: "
										+ modelEntity.getImplementedInterface().getName() + " " + put2.getImplementedInterface().getName());
					}
				}
			}
		}
		for (ModelEntity<?> e : modelEntity.getEmbeddedEntities()) {
			if (!visited.contains(e)) {
				appendEntity(e, visited);
			}
		}
	}

	public Class<?> getBaseClass() {
		return baseClass;
	}

	public ModelEntity<?> getModelEntity(String xmlElementName) {
		return modelEntitiesByXmlTag.get(xmlElementName);
	}

	public Iterator<ModelEntity> getEntities() {
		return modelEntities.values().iterator();
	}

	public int getEntityCount() {
		return modelEntities.size();
	}

	public <I> ModelEntity<I> getModelEntity(Class<I> implementedInterface) {
		return modelEntities.get(implementedInterface);
	}

	/**
	 * Return the appropriate {@link ModelPropertyXMLTag} matching searched XML Tag, supplied as parameter (xmlTag).<br>
	 * Note that required parameters include the entity which gives the context where such XML tag is to be looked-up, and the
	 * {@link ModelFactory} to use.<br>
	 * TODO: Also note that this implementation is not safe when using multiple factories, because caching is performed whithout taking
	 * modelFactory under account.
	 * 
	 * @param entity
	 * @param modelFactory
	 * @param xmlTag
	 * @return
	 * @throws ModelDefinitionException
	 */
	public <I> ModelPropertyXMLTag<I> getPropertyForXMLTag(ModelEntity<I> entity, ModelFactory modelFactory, String xmlTag)
			throws ModelDefinitionException {

		Map<String, ModelPropertyXMLTag<?>> tags = modelPropertiesByXmlTag.get(entity);
		if (tags == null) {
			modelPropertiesByXmlTag.put(entity, tags = new HashMap<>());
			Iterator<ModelProperty<? super I>> i = entity.getProperties();
			while (i.hasNext()) {
				ModelProperty<? super I> property = i.next();
				if (property.getXMLElement() != null) {
					ModelEntity<?> accessedEntity = property.getAccessedEntity();
					if (accessedEntity != null) {
						List<ModelEntity> allDescendantsAndMe = accessedEntity.getAllDescendantsAndMe(this);
						for (ModelEntity<?> accessible : allDescendantsAndMe) {
							ModelPropertyXMLTag<I> tag = new ModelPropertyXMLTag<>(property, accessible);
							ModelPropertyXMLTag<?> put = tags.put(tag.getTag(), tag);
							if (put != null) {
								throw new ModelDefinitionException("Property " + property
										+ " defines a context which leads to an XMLElement name clash with " + accessible);
							}
							if (!accessible.isAbstract()) {
								if (tag.getDeprecatedTags() != null) {
									for (String deprecatedTag : tag.getDeprecatedTags()) {
										ModelPropertyXMLTag<?> put2 = tags.put(deprecatedTag, tag);
										if (put2 != null) {
											throw new ModelDefinitionException("Property " + property
													+ " defines a context which leads to an XMLElement name clash with " + accessible);
										}
									}
								}
							}
						}
						// Fixed issue while commenting following line and replacing it by a call to model-factory specific StringEncoder
						// } else if (StringConverterLibrary.getInstance().hasConverter(property.getType())) {
					}
					else if (modelFactory.getStringEncoder().isConvertable(property.getType())) {
						ModelPropertyXMLTag<I> tag = new ModelPropertyXMLTag<>(property);
						ModelPropertyXMLTag<?> put = tags.put(tag.getTag(), tag);
						if (put != null) {
							throw new ModelDefinitionException("Property " + property
									+ " defines a context which leads to an XMLElement name clash with " + property.getType().getName());
						}
					}
				}
			}
		}
		return (ModelPropertyXMLTag<I>) tags.get(xmlTag);
	}

	public List<ModelEntity<?>> getUpperEntities(Object object) {
		List<ModelEntity<?>> entities = new ArrayList<>();
		for (Class<?> i : object.getClass().getInterfaces()) {
			appendKnownEntities(entities, i);
		}
		return entities;
	}

	private void appendKnownEntities(List<ModelEntity<?>> entities, Class<?> i) {
		ModelEntity<?> modelEntity = getModelEntity(i);
		if (modelEntity != null && !entities.contains(i)) {
			entities.add(modelEntity);
		}
		else {
			for (Class<?> j : i.getInterfaces()) {
				appendKnownEntities(entities, j);
			}
		}
	}

	public String debug() {
		StringBuffer returned = new StringBuffer();
		returned.append("*************** ModelContext ****************\n");
		returned.append("Entities number: " + modelEntities.size() + "\n");
		for (ModelEntity entity : modelEntities.values()) {
			returned.append("------------------- ").append(entity.getImplementedInterface().getSimpleName())
					.append(" -------------------\n");
			Iterator<ModelProperty<?>> i;
			try {
				i = entity.getProperties();
			} catch (ModelDefinitionException e) {
				e.printStackTrace();
				continue;
			}
			while (i.hasNext()) {
				ModelProperty<?> property = i.next();
				returned.append(property.getPropertyIdentifier()).append(" ").append(property.getCardinality()).append(" type=")
						.append(property.getType().getSimpleName()).append("\n");
			}
		}
		return returned.toString();
	}

}
