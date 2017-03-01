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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
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
	public static final String CLASS_NAME = "className";

	public static final Set<String> IGNORED_ATTRIBUTES = Stream.of(
			ID, ID_REF, CLASS_NAME, "xmlns:p", PAMELAConstants.Q_MODEL_ENTITY_ATTRIBUTE, PAMELAConstants.Q_CLASS_ATTRIBUTE
		).collect(Collectors.toSet());

	private final ModelFactory factory;
	private final ModelContext context;

	/**
	 * Stores already serialized objects where value is the serialized object and key is an object coding the unique identifier of the
	 * object
	 */
	private final Map<String, Object> alreadyDeserializedMap = new LinkedHashMap<>();

	/**
	 * Stores lambda to resolve forward references
	 */
	private final Map<String, List<Consumer<Object>>> forwardReferences = new HashMap<>();

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

			// checks for pending references
			if (forwardReferences.size() > 0) {
				throw new InvalidDataException("Unresolved references to objects with identifiers " + forwardReferences.keySet());
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
				if (parentInfo != null) {
					ModelEntity<Object> parentModelEntity = parentInfo.getModelEntity();
					ModelPropertyXMLTag<Object> modelPropertyXMLTag = context.getPropertyForXMLTag(parentModelEntity, factory, qName);
					if (modelPropertyXMLTag != null) {
						modelEntity = (ModelEntity<Object>) modelPropertyXMLTag.getAccessedEntity();
						leadingProperty = modelPropertyXMLTag.getProperty();
					}
					else if (policy == DeserializationPolicy.RESTRICTIVE) {
						throw new RestrictiveDeserializationException("Element with name does not fit any properties within entity " + parentModelEntity);
					}
				}
			} catch (ModelDefinitionException e) {
				throw new SAXException(e);
			}
		}

		final TransformedObjectInfo info;
		if (modelEntity != null ) {
			if (getStringEncoder().isConvertable(modelEntity.getImplementedInterface())) {
				// object is convertible from a string, it will only contains a string
				currentConvertibleString = "";
				info = new TransformedObjectInfo(leadingProperty, modelEntity);
			}
			else {
				String idref = attributes.getValue(ID_REF);
				if (idref != null) {
					// objects is a reference
					Object referenceObject = alreadyDeserializedMap.get(idref);
					if (referenceObject == null) {
						// it needs to be resolved later
						final ModelEntity<Object> finalModelEntity = modelEntity;
						final ModelProperty<Object> finalModelProperty = leadingProperty;
						List<Consumer<Object>> forwards = forwardReferences.getOrDefault(idref, new ArrayList<>());
						forwards.add((target) -> {
							try {
								connectObject(new TransformedObjectInfo(target, finalModelProperty, finalModelEntity));
							} catch (SAXException e) {
								// TODO add lambda with SAXException
								e.printStackTrace();
							}
						});
						info = null;
					}
					else {
						info = new TransformedObjectInfo(referenceObject, leadingProperty, modelEntity);
					}
				}
				else {
					// object is constructed using attributes
					Object object = buildObjectFromAttributes(localName, modelEntity, attributes);
					info = new TransformedObjectInfo(object, leadingProperty, modelEntity);
				}
			}
		} else if (policy != DeserializationPolicy.RESTRICTIVE) {
			// ignores the element
			info = null;
		} else {
			throw new SAXException(new InvalidDataException("Could not find ModelEntity for " +  qName));
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
		// info may be null if current object is a reference
		if (info != null) {
			if (info.isConvertible()) {
				// transforms string to object and construct new info
				info = info.convert(factory, currentConvertibleString);
				currentConvertibleString = null;
			}

			// register for finalization
			alreadyDeserialized.add(info);
			connectObject(info);
		}

	}

	private void connectObject(TransformedObjectInfo info) throws SAXException {
		// adds object to its parent if needed
		ModelProperty<Object> property = info.getLeadingProperty();
		if (property != null) {
			try {
				ProxyMethodHandler parent = factory.getHandler(stack.getLast().getObject());
				switch (property.getCardinality()) {
					case SINGLE:
						parent.invokeSetterForDeserialization(property, info.getObject());
						break;
					case LIST:
						parent.invokeAdderForDeserialization(property, info.getObject());
						break;
					case MAP:
						throw new UnsupportedOperationException("Cannot deserialize maps for now");
					default:
						break;
				}
			} catch (ModelDefinitionException e) {
				throw new SAXException(e);
			}
		}
	}

	private Object buildObjectFromAttributes(String name, ModelEntity<Object> expectedModelEntity, Attributes attributes) throws SAXException {

		// TODO does this really happen ?
		String id = attributes.getValue(ID);
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
			String className = attributes.getValue(CLASS_NAME);
			if (className == null) {
				className = attributes.getValue(PAMELAConstants.Q_CLASS_ATTRIBUTE);
			}

			// ----- Warning -----
			// This next code come from the old deserialization process, I don't fully understand what's done here.
			// I keep it for compatibility, I'll come back there to clean it up later
			// ----- Warning -----
			if (className != null) {
				try {
					implementedInterface = (Class<Object>) Class.forName(className);
				} catch (ClassNotFoundException e) {
					throw new InvalidDataException("Class not found "+ e.getMessage());
				}
			}
			else if (entityName != null) {
				try {
					implementedInterface = (Class<Object>) Class.forName(entityName);
				} catch (ClassNotFoundException e) {
					throw new InvalidDataException("Class not found "+ e.getMessage());
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
							throw new InvalidDataException("Class not found "+ e.getMessage());
						}
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


			// Creates object instance
			Class<Object> entityClass = concreteEntity.getImplementedInterface();
			Object returned = factory._newInstance(entityClass, policy == DeserializationPolicy.EXTENSIVE);
			initializeDeserialization(returned, concreteEntity);

			// registers object
			if (id != null) {
				register(id, returned);
			}

			ProxyMethodHandler<Object> handler = factory.getHandler(returned);
			handler.setDeserializing(true);

			for (int i = 0; i < attributes.getLength(); i++) {
				String attributeName = attributes.getQName(i);
				String attributeValue = attributes.getValue(i);

				ModelProperty<Object> property = concreteEntity.getPropertyForXMLAttributeName(attributeName);
				if (property == null) {
					if (IGNORED_ATTRIBUTES.contains(attributeName)) {
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
							continue; // As long as we don't handle them, we continue to avoid NPE.
					}
				}
				Object value = getStringEncoder().fromString(property.getType(), attributeValue);
				if (value != null) {
					handler.invokeSetterForDeserialization(property, value);
				}
			}

			return returned;

		} catch (Exception e) {
			if (e instanceof SAXException) throw (SAXException) e;
			throw new SAXException(e);
		}
	}

	private void register(String id, Object object) {
		alreadyDeserializedMap.put(id, object);

		// resolves forward references if any
		List<Consumer<Object>> forwards = forwardReferences.remove(id);
		if (forwards != null) {
			for (Consumer<Object> forward : forwards) {
				forward.accept(object);
			}
		}

	}

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
