/**
 * 
 * Copyright (c) 2014, Openflexo
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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openflexo.model.DeserializationFinalizer;
import org.openflexo.model.DeserializationInitializer;
import org.openflexo.model.ModelContext;
import org.openflexo.model.ModelContext.ModelPropertyXMLTag;
import org.openflexo.model.ModelEntity;
import org.openflexo.model.ModelProperty;
import org.openflexo.model.StringEncoder;
import org.openflexo.model.exceptions.InvalidDataException;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.exceptions.ModelExecutionException;
import org.openflexo.model.exceptions.RestrictiveDeserializationException;
import org.openflexo.model.factory.DeserializationPolicy;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.model.factory.PAMELAConstants;
import org.openflexo.model.factory.ProxyMethodHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLSaxDeserializer extends DefaultHandler {

	public static final String ID = "id";
	public static final String ID_REF = "idref";

	public static final Set<String> INTERNAL_ATTRIBUTE = Stream.of(ID, ID_REF).collect(Collectors.toSet());

	private final ModelFactory factory;
	private final ModelContext context;

	private final Map<String, String> prefixMap = new HashMap<>();

	/**
	 * Stores already serialized objects where value is the serialized object and key is an object coding the unique identifier of the
	 * object
	 */
	private final Map<String, Object> alreadyDeserializedMap = new LinkedHashMap<>();

	/**
	 * Stores an ordered list of deserialized objects in the order they were instantiated during deserialization phase phase
	 */
	private final LinkedList<TransformedObjectInfo> alreadyDeserialized = new LinkedList<>();

	private final DeserializationPolicy policy;

	private String currentConvertibleString = null;

	private LinkedList<TransformedObjectInfo> stack = new LinkedList<>();

	public XMLSaxDeserializer(ModelFactory factory) {
		this(factory, DeserializationPolicy.PERMISSIVE);
	}

	public XMLSaxDeserializer(ModelFactory factory, DeserializationPolicy policy) {
		this.factory = factory;
		this.policy = policy;
		this.context = factory.getModelContext();
	}

	private StringEncoder getStringEncoder() {
		return factory.getStringEncoder();
	}

	public Object deserializeDocument(String xml) throws Exception {
		return deserializeDocument(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
	}

	public Object deserializeDocument(InputStream in) throws Exception {
		alreadyDeserializedMap.clear();

		// prepares buffered stream
		if (!(in instanceof BufferedInputStream && in instanceof ByteArrayInputStream)) {
			in = new BufferedInputStream(in);
		}

		try {
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			SAXParser parser = parserFactory.newSAXParser();
			parser.parse(in, this);

			// We just finished deserialization, call deserialization finalizers now
			for (TransformedObjectInfo info : alreadyDeserialized) {
				Object object = info.getObject();

				ProxyMethodHandler handler = factory.getHandler(object);
				handler.setDeserializing(false);

				finalizeDeserialization(object, info.getModelEntity());
			}

			return alreadyDeserialized.getLast().getObject();
		} catch (SAXException e) {
			if (e.getCause() instanceof Exception) throw (Exception) e.getCause();
			else throw new InvalidDataException(e.getMessage());
		}
	}

	@Override
	public void startDocument() throws SAXException {
		// nothing to do
	}

	@Override
	public void endDocument() throws SAXException {
		// nothing to do
	}


	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		// searches for correct model entity to resolve
		ModelEntity<Object> modelEntity = null;
		ModelProperty<Object> leadingProperty = null;
		if (stack.isEmpty()) {
			modelEntity = (ModelEntity<Object>) factory.getModelContext().getModelEntity(qName);
		} else {
			try {
				TransformedObjectInfo parentInfo = stack.getLast();
				ModelEntity<Object> parentModelEntity = parentInfo.getModelEntity();
				ModelPropertyXMLTag<Object> modelPropertyXMLTag = context.getPropertyForXMLTag(parentModelEntity, factory, qName);
				if (modelPropertyXMLTag != null) {
					modelEntity = (ModelEntity<Object>) modelPropertyXMLTag.getAccessedEntity();
					leadingProperty = modelPropertyXMLTag.getProperty();
				}
				else if (policy == DeserializationPolicy.RESTRICTIVE) {
					throw new RestrictiveDeserializationException("Element with name does not fit any properties within entity " + parentModelEntity);
				}
			} catch (ModelDefinitionException e) {
				throw new SAXException(e);
			}
		}

		if (modelEntity == null) {
			throw new SAXException(new InvalidDataException("Could not find ModelEntity for " +  qName));
		}

		final TransformedObjectInfo info;
		if (getStringEncoder().isConvertable(modelEntity.getImplementedInterface())) {
			// object is convertible from a string, it will only contains a string
			currentConvertibleString = "";
			info = new TransformedObjectInfo(leadingProperty, modelEntity);
		} else {
			// object is constructed using attributes
			// children element will be able to use the stack for containment
			Object object = buildObjectFromAttributes(localName, modelEntity, attributes);
			info = new TransformedObjectInfo(object, leadingProperty, modelEntity);
		}

		// push current state to stack
		stack.addLast(info);

	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		currentConvertibleString = new String(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		TransformedObjectInfo info = stack.removeLast();
		if (info.isConvertible()) {
			// transforms string to object and construct new info
			info = info.convert(factory, currentConvertibleString);
			currentConvertibleString = null;
		}

		// register for finalization
		alreadyDeserialized.add(info);

		// here info contains a constructed object
		// TODO fill objects structures
	}

	private Object buildObjectFromAttributes(String name, ModelEntity<Object> expectedModelEntity, Attributes attributes) throws SAXException {
		String id = attributes.getValue(ID);
		String idref = attributes.getValue(ID_REF);

		if (idref != null) {
			// This seems to be an already de-serialized object
			Object referenceObject = alreadyDeserializedMap.get(idref);
			if (referenceObject == null) {
				// Try to find this object elsewhere in the document
				// NOTE: This should never occur, except if the file was manually edited, or
				// if the file was generated BEFORE development of ordered properties feature

				// TODO: Throw here an error in future release but for backward compatibility we leave it for now.
				// Need to implement backward reference for backward compatibility

				//Element idRefElement = findElementWithId(idrefAttribute.getValue());
				//if (idRefElement != null) {
				//	return buildObjectFromNodeAndModelEntity(idRefElement, expectedModelEntity);
				//}

				throw new SAXException(new InvalidDataException("No reference to object with identifier " + idref));
			}
			return referenceObject;
		}

		// TODO does this really happen ?
		// if it's the case, the serialization has problems
		if (id != null) {
			// does object already exists ?
			Object referenceObject = alreadyDeserializedMap.get(id);
			if (referenceObject != null) {
				// No need to go further: i've got my object
				return referenceObject;
			}
		}

		try {

			// search concrete model entity
			ModelEntity<Object> concreteEntity = expectedModelEntity;
			Class<Object> implementedInterface = null;
			Class<Object> implementingClass = null;

			String entityName = attributes.getValue(PAMELAConstants.Q_MODEL_ENTITY_ATTRIBUTE);
			String className = attributes.getValue(PAMELAConstants.Q_CLASS_ATTRIBUTE);
			if (entityName != null) {
				// ----- Warning -----
				// This next code come from the old deserialization process, I don't fully understand what's done here.
				// I keep it for compatibility, I'll come back there to clean it up later
				// ----- Warning -----
				try {
					implementedInterface = (Class<Object>) Class.forName(entityName);
				} catch (ClassNotFoundException e) {
					// TODO: log something here
				}
				if (entityName != null && policy == DeserializationPolicy.EXTENSIVE) {
					concreteEntity = factory.importClass(implementedInterface);
					if (className != null) {
						try {
							implementingClass = (Class<Object>) Class.forName(className);
							if (implementedInterface.isAssignableFrom(implementingClass)) {
								factory.setImplementingClassForInterface((Class) implementingClass, implementedInterface, policy == DeserializationPolicy.EXTENSIVE);
							}
							else {
								throw new ModelExecutionException(className + " does not implement " + implementedInterface + " for node " + name);
							}
						} catch (ClassNotFoundException e) {
							// TODO: log something here
						}
						}
				}
				if (implementedInterface != null) {
					if (policy == DeserializationPolicy.EXTENSIVE) {
						concreteEntity = factory.getExtendedContext().getModelEntity(implementedInterface);
					}
					else {
						concreteEntity = factory.getModelContext().getModelEntity(implementedInterface);
					}
				}
				if (concreteEntity == null && policy == DeserializationPolicy.RESTRICTIVE) {
					if (entityName != null) {
						throw new RestrictiveDeserializationException("Entity " + entityName + " is not part of this model context");
					}
					else {
						throw new RestrictiveDeserializationException("No entity found for tag " + name);
					}
				}
				// ----- Warning -----
				// End of the strange code
				// ----- Warning -----

			}



			// Creates object instance
			Class<Object> entityClass = concreteEntity.getImplementedInterface();
			Object returned = factory._newInstance(entityClass, policy == DeserializationPolicy.EXTENSIVE);
			initializeDeserialization(returned, expectedModelEntity);

			// registers object
			if (id != null) {
				alreadyDeserializedMap.put(id, returned);
			}

			ProxyMethodHandler<Object> handler = factory.getHandler(returned);
			handler.setDeserializing(true);

			for (int i = 0; i < attributes.getLength(); i++) {
				String attributeNamespace = attributes.getURI(i);
				String attributeName = attributes.getLocalName(i);
				String attributeValue = attributes.getValue(i);

				ModelProperty<Object> property = expectedModelEntity.getPropertyForXMLAttributeName(attributeName);
				if (property == null) {
					if (PAMELAConstants.isPamelaAttribute(attributeNamespace, attributeName)) {
						continue;
					}
					if (INTERNAL_ATTRIBUTE.contains(attributeName)) {
						continue;
					}

					switch (policy) {
						case PERMISSIVE:
							continue;
						case RESTRICTIVE:
							throw new RestrictiveDeserializationException("No attribute found for the attribute named: " + attributeName);
						case EXTENSIVE:
							// TODO: handle extra values
							// break;
							continue; // As long as we don't handlethem, we continue to avoid NPE.
					}
				}
				Object value = getStringEncoder().fromString(property.getType(), attributeValue);
				if (value != null) {
					handler.invokeSetterForDeserialization(property, value);
				}
			}

			return returned;

		} catch (Exception e) {
			throw new SAXException(e);
		}

		/*
		for (Element child : node.getChildren()) {
			ModelPropertyXMLTag<I> modelPropertyXMLTag = factory.getModelContext().getPropertyForXMLTag(expectedModelEntity, factory,
					child.getName());
			ModelProperty<? super I> property = null;
			ModelEntity<?> entity = null;
			if (modelPropertyXMLTag != null) {
				property = modelPropertyXMLTag.getProperty();
				entity = modelPropertyXMLTag.getAccessedEntity();
			}
			else if (policy == DeserializationPolicy.RESTRICTIVE) {
				throw new RestrictiveDeserializationException("Element with name does not fit any properties within entity " + expectedModelEntity);
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
							entity = factory.importClass(implementedInterface);
							if (className != null) {
								try {
									implementingClass = Class.forName(className);
									if (implementedInterface.isAssignableFrom(implementingClass)) {
										factory.setImplementingClassForInterface((Class) implementingClass, implementedInterface,
												policy == DeserializationPolicy.EXTENSIVE);
									}
									else {
										throw new ModelExecutionException(
												className + " does not implement " + implementedInterface + " for node " + child.getName());
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
						entity = factory.getExtendedContext().getModelEntity(implementedInterface);
					}
					else {
						entity = factory.getModelContext().getModelEntity(implementedInterface);
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
					throw new ModelExecutionException(
							"Found property " + property + " but was unable to deserialize the content of node " + child);
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
		*/

	}

	/*
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
	*/

	private void initializeDeserialization(Object object, ModelEntity<Object> modelEntity) throws SAXException {
		factory.objectIsBeeingDeserialized(object, modelEntity.getImplementedInterface());
		try {
			DeserializationInitializer deserializationInitializer = modelEntity.getDeserializationInitializer();
			if (deserializationInitializer != null) {
				Method deserializationInitializerMethod = deserializationInitializer.getDeserializationInitializerMethod();
				if (deserializationInitializerMethod.getParameterTypes().length == 0) {
					deserializationInitializerMethod.invoke(object, new Object[0]);
				}
				else if (deserializationInitializerMethod.getParameterTypes().length == 1) {
					deserializationInitializerMethod.invoke(object, factory);
				}
				else {
					throw new ModelDefinitionException("Wrong number of argument for deserialization initializer " + deserializationInitializerMethod);
				}
			}
		} catch (Exception e) {
			throw new SAXException(e);
		}
	}

	private void finalizeDeserialization(Object object, ModelEntity<Object> modelEntity) throws SAXException {
		try {
			DeserializationFinalizer deserializationFinalizer = modelEntity.getDeserializationFinalizer();
			if (deserializationFinalizer != null) {
				deserializationFinalizer.getDeserializationFinalizerMethod().invoke(object, new Object[0]);
			}
		} catch (Exception e) {
			throw new SAXException(e);
		}
		factory.objectHasBeenDeserialized(object, modelEntity.getImplementedInterface());
	}
}
