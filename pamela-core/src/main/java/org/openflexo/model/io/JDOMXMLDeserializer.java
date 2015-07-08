/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.model.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.openflexo.model.ModelContext.ModelPropertyXMLTag;
import org.openflexo.model.ModelEntity;
import org.openflexo.model.ModelProperty;
import org.openflexo.model.exceptions.InvalidDataException;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.exceptions.ModelExecutionException;
import org.openflexo.model.exceptions.RestrictiveDeserializationException;
import org.openflexo.model.factory.DeserializationPolicy;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.model.factory.PAMELAConstants;
import org.openflexo.model.factory.ProxyMethodHandler;

public class JDOMXMLDeserializer extends AbstractModelDeserializer implements ModelDeserializer {

	private Map<String, Element> index;

	public JDOMXMLDeserializer(ModelFactory factory) {
		this(factory, DeserializationPolicy.PERMISSIVE);
	}

	public JDOMXMLDeserializer(ModelFactory factory, DeserializationPolicy policy) {
		super(factory);
		this.policy = policy;
	}

	@Override
	public <I> Object deserializeDocument(InputStream in) throws IOException, InvalidDataException, ModelDefinitionException {
		alreadyDeserializedMap.clear();
		alreadyDeserialized.clear();

		try {
			Document dataDocument = parseXMLData(in);
			Element rootElement = dataDocument.getRootElement();
			return buildObjectFromNode(rootElement);
		} catch (JDOMException e) {
			throw new InvalidDataException("JDOM Exception: " + e.getLocalizedMessage());
		}
	}

	@Override
	public <I> Object deserializeDocument(String xml) throws IOException, InvalidDataException, ModelDefinitionException {
		alreadyDeserializedMap.clear();
		alreadyDeserialized.clear();
		Document dataDocument;
		try {
			dataDocument = parseXMLData(xml);
			Element rootElement = dataDocument.getRootElement();
			return buildObjectFromNode(rootElement);
		} catch (JDOMException e) {
			throw new InvalidDataException("JDOM Exception: " + e.getLocalizedMessage());
		}
	}

	private Object buildObjectFromNode(Element node) throws InvalidDataException, ModelDefinitionException {
		ModelEntity<?> modelEntity = modelFactory.getModelContext().getModelEntity(node.getName());
		Object object = buildObjectFromNodeAndModelEntity(node, modelEntity);
		for (ProxyMethodHandler<?> handler : deserializingHandlers) {
			handler.setDeserializing(false);
		}

		// We just finished deserialization, call deserialization finalizers now
		for (DeserializedObject o : alreadyDeserialized) {
			finalizeDeserialization(o.object, o.modelEntity);
		}
		return object;
	}

	private <I> Object buildObjectFromNodeAndModelEntity(Element node, ModelEntity<I> modelEntity) throws InvalidDataException,
			ModelDefinitionException {
		Object currentDeserializedReference = null;
		Attribute idAttribute = node.getAttribute(ID);
		Attribute idrefAttribute = node.getAttribute(ID_REF);
		Attribute classNameAttribute = node.getAttribute(CLASS_NAME);
		if (idrefAttribute != null) {
			// This seems to be an already deserialized object
			Object reference;
			reference = idrefAttribute.getValue();
			Object referenceObject = alreadyDeserializedMap.get(reference);
			if (referenceObject == null) {
				// Try to find this object elsewhere in the document
				// NOTE: This should never occur, except if the file was
				// manually edited, or
				// if the file was generated BEFORE development of ordered
				// properties feature

				// TODO: Throw here an error in future release but for backward compatibility we leave it for now.
				Element idRefElement = findElementWithId(idrefAttribute.getValue());
				if (idRefElement != null) {
					return buildObjectFromNodeAndModelEntity(idRefElement, modelEntity);
				}
				throw new InvalidDataException("No reference to object with identifier " + reference);
			}
			else {
				// No need to go further: i've got my object
				// Debugging.debug ("Stopping decoding: object found as a
				// reference "+reference+" "+referenceObject);
				return referenceObject;
			}
		}
		if (idAttribute != null) {
			currentDeserializedReference = idAttribute.getValue();
			Object referenceObject = alreadyDeserializedMap.get(currentDeserializedReference);
			if (referenceObject != null) {
				// No need to go further: i've got my object
				return referenceObject;
			}
		}

		// I need to rebuild it

		I returned;
		String text = node.getText();
		if (text != null && getStringEncoder() != null && getStringEncoder().isConvertable(modelEntity.getImplementedInterface())) {
			// GPO: I am not sure this is still useful.
			try {
				returned = getStringEncoder().fromString(modelEntity.getImplementedInterface(), text);
			} catch (InvalidDataException e) {
				throw new ModelExecutionException(e);
			}
		}
		else {
			Class<I> entityClass = modelEntity.getImplementedInterface();
			// TODO: This little hook should disappear (backward compatibility with XMLCoDe where for some classes, class name was also
			// serialized)
			if (classNameAttribute != null) {
				try {
					entityClass = (Class<I>) Class.forName(classNameAttribute.getValue());
					// System.out.println("Switch from " + modelEntity.getImplementedInterface() + " to " + entityClass);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			returned = modelFactory._newInstance(entityClass, policy == DeserializationPolicy.EXTENSIVE);
			initializeDeserialization(returned, modelEntity);
		}

		if (currentDeserializedReference != null) {
			alreadyDeserializedMap.put(currentDeserializedReference, returned);
		}

		alreadyDeserialized.add(new DeserializedObject<I>(returned, modelEntity));

		ProxyMethodHandler<I> handler = modelFactory.getHandler(returned);
		deserializingHandlers.add(handler);
		handler.setDeserializing(true);
		for (Attribute attribute : node.getAttributes()) {
			ModelProperty<? super I> property = modelEntity.getPropertyForXMLAttributeName(attribute.getName());
			if (property == null) {
				if (attribute.getNamespace().equals(PAMELAConstants.NAMESPACE)
						&& (attribute.getName().equals(PAMELAConstants.CLASS_ATTRIBUTE) || attribute.getName().equals(
								PAMELAConstants.MODEL_ENTITY_ATTRIBUTE))) {
					continue;
				}
				if (attribute.getName().equals(ID) || attribute.getName().equals(ID_REF)) {
					continue;
				}
				switch (policy) {
					case PERMISSIVE:
						continue;
					case RESTRICTIVE:
						throw new RestrictiveDeserializationException("No attribute found for the attribute named: " + attribute.getName());
					case EXTENSIVE:
						// TODO: handle extra values
						// break;
						continue; // As long as we don't handlethem, we continue to avoid NPE.
				}
			}
			Object value = getStringEncoder().fromString(property.getType(), attribute.getValue());
			if (value != null) {
				handler.invokeSetterForDeserialization(property, value);
			}

		}
		for (Element child : node.getChildren()) {
			ModelPropertyXMLTag<I> modelPropertyXMLTag = modelFactory.getModelContext().getPropertyForXMLTag(modelEntity, modelFactory,
					child.getName());
			ModelProperty<? super I> property = null;
			ModelEntity<?> entity = null;
			if (modelPropertyXMLTag != null) {
				property = modelPropertyXMLTag.getProperty();
				entity = modelPropertyXMLTag.getAccessedEntity();
			}
			else if (policy == DeserializationPolicy.RESTRICTIVE) {
				throw new RestrictiveDeserializationException("Element with name does not fit any properties within entity " + modelEntity);
			}
			Class<?> implementedInterface = null;
			Class<?> implementingClass = null;
			String entityName = child.getAttributeValue(PAMELAConstants.MODEL_ENTITY_ATTRIBUTE, PAMELAConstants.NAMESPACE);
			String className = child.getAttributeValue(PAMELAConstants.CLASS_ATTRIBUTE, PAMELAConstants.NAMESPACE);
			if (entityName != null) {
				try {
					implementedInterface = Class.forName(entityName);
				} catch (ClassNotFoundException e) {
					// TODO: log something here
				}
				switch (policy) {
					case PERMISSIVE:
						break;
					case RESTRICTIVE:
						break;
					case EXTENSIVE:
						if (entityName != null) {
							entity = modelFactory.importClass(implementedInterface);
							if (className != null) {
								try {
									implementingClass = Class.forName(className);
									if (implementedInterface.isAssignableFrom(implementingClass)) {
										modelFactory.setImplementingClassForInterface((Class) implementingClass, implementedInterface,
												policy == DeserializationPolicy.EXTENSIVE);
									}
									else {
										throw new ModelExecutionException(className + " does not implement " + implementedInterface
												+ " for node " + child.getName());
									}
								} catch (ClassNotFoundException e) {
									// TODO: log something here
								}
							}
						}
						break;
				}
				if (implementedInterface != null) {
					if (policy == DeserializationPolicy.EXTENSIVE) {
						entity = modelFactory.getExtendedContext().getModelEntity(implementedInterface);
					}
					else {
						entity = modelFactory.getModelContext().getModelEntity(implementedInterface);
					}
				}
				if (entity == null && policy == DeserializationPolicy.RESTRICTIVE) {
					if (entityName != null) {
						throw new RestrictiveDeserializationException("Entity " + entityName + " is not part of this model context");
					}
					else {
						throw new RestrictiveDeserializationException("No entity found for tag " + child.getName());
					}
				}
			}
			if (property != null) {
				Object value = null;
				if (entity != null && !getStringEncoder().isConvertable(property.getType())) {
					value = buildObjectFromNodeAndModelEntity(child, entity);
				}
				else if (getStringEncoder().isConvertable(property.getType())) {
					value = getStringEncoder().fromString(property.getType(), child.getText());
				}
				else {
					// Should not happen
					throw new ModelExecutionException("Found property " + property + " but was unable to deserialize the content of node "
							+ child);
				}
				switch (property.getCardinality()) {
					case SINGLE:
						handler.invokeSetterForDeserialization(property, value);
						break;
					case LIST:
						handler.invokeAdderForDeserialization(property, value);
						break;
					case MAP:
						throw new UnsupportedOperationException("Cannot deserialize maps for now");
					default:
						break;
				}
			}

		}

		return returned;
	}

	private class MatchingElement {
		private final Element element;
		private final ModelEntity<?> modelEntity;

		private MatchingElement(Element element, ModelEntity<?> modelEntity) {
			super();
			this.element = element;
			this.modelEntity = modelEntity;
		}

		@Override
		public String toString() {
			return element.toString() + "/" + modelEntity;
		}
	}

	protected Document parseXMLData(InputStream xmlStream) throws IOException, JDOMException {
		SAXBuilder parser = new SAXBuilder();
		Document reply = parser.build(xmlStream);
		makeIndex(reply);
		return reply;
	}

	protected Document parseXMLData(String xml) throws IOException, JDOMException {
		SAXBuilder parser = new SAXBuilder();
		Document reply = parser.build(new StringReader(xml));
		makeIndex(reply);
		return reply;
	}

	static private class ElementWithIDFilter extends ElementFilter {

		public ElementWithIDFilter() {
			super();
		}

		@Override
		public Element filter(Object arg0) {
			Element element = super.filter(arg0);
			if (element != null && element.getAttributeValue("id") != null) {
				return element;
			}
			return null;
		}

	}

	public Document makeIndex(Document doc) {
		index = new Hashtable<String, Element>();
		Iterator<Element> it = doc.getDescendants(new ElementWithIDFilter());
		Element e = null;
		while (it.hasNext()) {
			e = it.next();
			index.put(e.getAttributeValue("id"), e);
		}
		return doc;
	}

	private Element findElementWithId(String id) {
		return index.get(id);
	}

}
