/*
 * (c) Copyright 2013-2014 Openflexo
 *
 * This file is part of OpenFlexo.
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
package org.openflexo.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nonnull;

import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;

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

		public ModelProperty<? super I> getProperty() {
			return property;
		}

		public ModelEntity<?> getAccessedEntity() {
			return accessedEntity;
		}
	}

	private Map<Class, ModelEntity> modelEntities;
	private Map<String, ModelEntity> modelEntitiesByXmlTag;
	private final Map<ModelEntity, Map<String, ModelPropertyXMLTag<?>>> modelPropertiesByXmlTag;
	private final Class<?> baseClass;

	public ModelContext(@Nonnull Class<?> baseClass) throws ModelDefinitionException {
		this.baseClass = baseClass;
		modelEntities = new HashMap<Class, ModelEntity>();
		modelEntitiesByXmlTag = new HashMap<String, ModelEntity>();
		modelPropertiesByXmlTag = new HashMap<ModelEntity, Map<String, ModelPropertyXMLTag<?>>>();
		ModelEntity<?> modelEntity = ModelEntityLibrary.importEntity(baseClass);
		appendEntity(modelEntity, new HashSet<ModelEntity<?>>());
		modelEntities = Collections.unmodifiableMap(modelEntities);
		modelEntitiesByXmlTag = Collections.unmodifiableMap(modelEntitiesByXmlTag);
	}

	public ModelContext(Class<?> baseClass, List<ModelContext> contexts) throws ModelDefinitionException {
		this.baseClass = baseClass;
		modelEntities = new HashMap<Class, ModelEntity>();
		modelEntitiesByXmlTag = new HashMap<String, ModelEntity>();
		modelPropertiesByXmlTag = new HashMap<ModelEntity, Map<String, ModelPropertyXMLTag<?>>>();
		for (ModelContext context : contexts) {
			for (Entry<String, ModelEntity> e : context.modelEntitiesByXmlTag.entrySet()) {
				ModelEntity entity = modelEntitiesByXmlTag.put(e.getKey(), e.getValue());
				// TODO: handle properly namespaces. Different namespaces allows to have identical tags
				// See also importModelEntity(Class<T>)
				if (entity != null && !entity.getImplementedInterface().equals(e.getValue().getImplementedInterface())) {
					throw new ModelDefinitionException(entity + " and " + e.getValue()
							+ " declare the same XML tag but not the same implemented interface");
				}
			}
			modelEntities.putAll(context.modelEntities);
		}
		if (baseClass != null) {
			ModelEntity<?> modelEntity = ModelEntityLibrary.importEntity(baseClass);
			appendEntity(modelEntity, new HashSet<ModelEntity<?>>());
		}
		modelEntities = Collections.unmodifiableMap(modelEntities);
		modelEntitiesByXmlTag = Collections.unmodifiableMap(modelEntitiesByXmlTag);
	}

	public ModelContext(Class<?>... baseClasses) throws ModelDefinitionException {
		this(null, makeModelContextList(baseClasses));
	}

	private static List<ModelContext> makeModelContextList(Class<?>... baseClasses) throws ModelDefinitionException {
		List<ModelContext> returned = new ArrayList<ModelContext>();
		for (Class<?> c : baseClasses) {
			returned.add(ModelContextLibrary.getModelContext(c));
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
			throw new ModelDefinitionException("Two entities define the same XMLTag '" + modelEntity.getXMLTag()
					+ "'. Implemented interfaces: " + modelEntity.getImplementedInterface().getName() + " "
					+ put.getImplementedInterface().getName());
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
			modelPropertiesByXmlTag.put(entity, tags = new HashMap<String, ModelContext.ModelPropertyXMLTag<?>>());
			Iterator<ModelProperty<? super I>> i = entity.getProperties();
			while (i.hasNext()) {
				ModelProperty<? super I> property = i.next();
				if (property.getXMLElement() != null) {
					ModelEntity accessedEntity = property.getAccessedEntity();
					if (accessedEntity != null) {
						List<ModelEntity> allDescendantsAndMe = accessedEntity.getAllDescendantsAndMe(this);
						for (ModelEntity accessible : allDescendantsAndMe) {
							ModelPropertyXMLTag<I> tag = new ModelPropertyXMLTag<I>(property, accessible);
							ModelPropertyXMLTag<?> put = tags.put(tag.getTag(), tag);
							if (put != null) {
								throw new ModelDefinitionException("Property " + property
										+ " defines a context which leads to an XMLElement name clash with " + accessible);
							}
						}
						// Fixed issue while commenting following line and replacing it by a call to model-factory specific StringEncoder
						// } else if (StringConverterLibrary.getInstance().hasConverter(property.getType())) {
					} else if (modelFactory.getStringEncoder().isConvertable(property.getType())) {
						ModelPropertyXMLTag<I> tag = new ModelPropertyXMLTag<I>(property);
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
		List<ModelEntity<?>> entities = new ArrayList<ModelEntity<?>>();
		for (Class<?> i : object.getClass().getInterfaces()) {
			appendKnownEntities(entities, i);
		}
		return entities;
	}

	private void appendKnownEntities(List<ModelEntity<?>> entities, Class<?> i) {
		ModelEntity<?> modelEntity = getModelEntity(i);
		if (modelEntity != null && !entities.contains(i)) {
			entities.add(modelEntity);
		} else {
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
			Iterator<ModelProperty> i;
			try {
				i = entity.getProperties();
			} catch (ModelDefinitionException e) {
				e.printStackTrace();
				continue;
			}
			while (i.hasNext()) {
				ModelProperty property = i.next();
				returned.append(property.getPropertyIdentifier()).append(" ").append(property.getCardinality()).append(" type=")
						.append(property.getType().getSimpleName()).append("\n");
			}
		}
		return returned.toString();
	}

}
