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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openflexo.model.ModelContext;
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
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XML deserializer using a SAX Parser
 */
public class XMLSaxDeserializer extends DefaultHandler {

	public static final String ID = "id";
	public static final String ID_REF = "idref";
	public static final String CLASS_NAME = "className";

	public static final Set<String> IGNORED_ATTRIBUTES = Stream
			.of(ID, ID_REF, CLASS_NAME, "xmlns:p", PAMELAConstants.Q_MODEL_ENTITY_ATTRIBUTE, PAMELAConstants.Q_CLASS_ATTRIBUTE)
			.collect(Collectors.toSet());

	@FunctionalInterface
	private interface Resolver {
		void resolve(TransformedObjectInfo resolved) throws SAXException;
	}

	private final ModelFactory factory;
	private final ModelContext context;

	/**
	 * Stores already serialized objects where value is the serialized object and key is an object coding the unique identifier of the
	 * object
	 */
	private final HashMap<String, Object> objectsWithId = new HashMap<>();

	private final List<TransformedObjectInfo> allObjects = new LinkedList<>();

	/**
	 * Stores lambda to resolve forward references
	 */
	private final Map<String, List<Resolver>> forwardReferences = new HashMap<>();

	private final DeserializationPolicy policy;

	private StringBuilder currentConvertibleString = new StringBuilder();

	private LinkedList<TransformedObjectInfo> stack = new LinkedList<>();

	private TransformedObjectInfo rootInfo = null;

	public XMLSaxDeserializer(ModelFactory factory) {
		this(factory, DeserializationPolicy.PERMISSIVE);
	}

	public XMLSaxDeserializer(ModelFactory factory, DeserializationPolicy policy) {
		this.factory = factory;
		this.policy = policy;
		this.context = factory.getModelContext();
	}

	public Object deserializeDocument(String xml) throws Exception {
		return deserializeDocument(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
	}

	public Object deserializeDocument(InputStream in) throws Exception {
		objectsWithId.clear();

		// prepares buffered stream
		if (!(in instanceof BufferedInputStream && in instanceof ByteArrayInputStream)) {
			in = new BufferedInputStream(in);
		}

		try {
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			SAXParser parser = parserFactory.newSAXParser();
			parser.parse(in, this);

			// Close deserializing mode
			for (TransformedObjectInfo info : allObjects) {
				info.finalizeDeserialization();
			}

			// checks for pending references
			if (forwardReferences.size() > 0) {
				throw new InvalidDataException("Unresolved references to objects with identifiers " + forwardReferences.keySet());
			}

			return rootInfo.getObject();
		} catch (SAXException e) {
			if (e.getCause() instanceof Exception)
				throw (Exception) e.getCause();
			else
				throw new InvalidDataException(e.getMessage());
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

	private TransformedObjectInfo constructMetaInformations(String qName) throws SAXException {
		ModelEntity<Object> modelEntity = null;
		ModelProperty<Object> leadingProperty = null;
		Object parent = null;
		if (stackEmpty()) {
			modelEntity = (ModelEntity<Object>) factory.getModelContext().getModelEntity(qName);
		}
		else {
			try {
				TransformedObjectInfo parentInfo = peekInfo();
				if (parentInfo != null) {
					parent = parentInfo.getObject();
					if (parentInfo != null) {
						ModelEntity<Object> parentModelEntity = parentInfo.getModelEntity();
						ModelPropertyXMLTag<Object> modelPropertyXMLTag = context.getPropertyForXMLTag(parentModelEntity, factory, qName);
						if (modelPropertyXMLTag != null) {
							modelEntity = (ModelEntity<Object>) modelPropertyXMLTag.getAccessedEntity();
							leadingProperty = modelPropertyXMLTag.getProperty();
						}
						else if (policy == DeserializationPolicy.RESTRICTIVE) {
							throw new RestrictiveDeserializationException(
									"Element with name does not fit any properties within entity " + parentModelEntity);
						}
					}
				}
			} catch (ModelDefinitionException e) {
				throw new SAXException(e);
			}
		}
		return leadingProperty != null || modelEntity != null ? new TransformedObjectInfo(factory, parent, leadingProperty, modelEntity)
				: null;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		final TransformedObjectInfo info = constructMetaInformations(qName);
		String id = attributes.getValue(ID);

		if (info != null) {
			// if modelEntity is convertible from start, a
			if (!info.isConvertible()) {
				String idref = attributes.getValue(ID_REF);
				if (idref != null) {
					// objects is a reference
					Object referenceObject = objectsWithId.get(idref);
					if (referenceObject != null) {
						info.setObject(referenceObject);
					}
					else {
						// it needs to be resolved later
						List<Resolver> forwards = forwardReferences.computeIfAbsent(idref, (v) -> new ArrayList<>());
						forwards.add((target) -> {
							info.setObject(target.getObject());
							connectObject(info);
						});
					}
				}
				else {
					// object is constructed using attributes
					if (buildObjectFromAttributes(localName, id, info, attributes)) {
						register(id, info);
					}
				}
			}

		}
		else if (policy == DeserializationPolicy.RESTRICTIVE) {
			throw new SAXException(new InvalidDataException("Could not find ModelEntity for " + qName));
		}
		// push current state to stack
		pushInfo(info);

	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String text = new String(ch, start, length).trim();
		currentConvertibleString.append(text);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		TransformedObjectInfo info = popInfo();
		// info may be null if current object is a reference
		if (info != null) {
			if (info.isConvertible()) {
				// transforms string to object and construct new info
				info.setFromString(currentConvertibleString.toString());
				info.initializeDeserialization();
			}

			if (info.isResolved()) {
				connectObject(info);
			}
		}

		currentConvertibleString = new StringBuilder();
	}

	private void connectObject(TransformedObjectInfo info) throws SAXException {
		// don't set a null object
		if (info.getObject() == null)
			return;

		// adds object to its parent if needed
		ModelProperty<Object> property = info.getLeadingProperty();
		if (property != null) {
			try {
				ProxyMethodHandler parent = factory.getHandler(info.getParent());

				if (parent != null) {
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
				}
				else {
					System.err.println("Cound not find parent for " + info + " object=" + info.getObject() + " parent=" + info.getParent());
				}
			} catch (ModelDefinitionException e) {
				throw new SAXException(e);
			}
		}
	}

	/**
	 * Constructs an object from it's attributes
	 * 
	 * @return true if an object was built, false other wise
	 */
	private boolean buildObjectFromAttributes(String name, String id, TransformedObjectInfo info, Attributes attributes)
			throws SAXException {
		// if it's the case, the serialization has problems
		if (id != null && objectsWithId.containsKey(id)) {
			// No need to go further: i've got my object
			return false;
		}

		try {
			// search concrete model entity
			ModelEntity<Object> concreteEntity = info.getModelEntity();
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
					throw new InvalidDataException("Class not found " + e.getMessage());
				}
			}
			else if (entityName != null) {
				try {
					implementedInterface = (Class<Object>) Class.forName(entityName);
				} catch (ClassNotFoundException e) {
					throw new InvalidDataException("Class not found " + e.getMessage());
				}
				if (entityName != null && policy == DeserializationPolicy.EXTENSIVE) {
					concreteEntity = factory.importClass(implementedInterface);
					if (className != null) {
						try {
							implementingClass = (Class<Object>) Class.forName(className);
							if (implementedInterface.isAssignableFrom(implementingClass)) {
								factory.setImplementingClassForInterface((Class) implementingClass, implementedInterface,
										policy == DeserializationPolicy.EXTENSIVE);
							}
							else {
								throw new ModelExecutionException(
										className + " does not implement " + implementedInterface + " for node " + name);
							}
						} catch (ClassNotFoundException e) {
							throw new InvalidDataException("Class not found " + e.getMessage());
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
			Object returned = null;
			try {
				returned = factory._newInstance(entityClass, policy == DeserializationPolicy.EXTENSIVE);
			} catch (NullPointerException e) {
				System.err.println("!!! Unexpected exception " + e);
				System.err.println("!!! while deserializing " + name + " id=" + id + " attributes=" + attributes);
				System.err.println("!!! entityClass="+entityClass);
			}
			info.setObject(returned);
			info.initializeDeserialization();

			for (int i = 0; i < attributes.getLength(); i++) {
				String attributeName = attributes.getQName(i);
				ModelProperty<Object> property = concreteEntity.getPropertyForXMLAttributeName(attributeName);

				if (property == null && IGNORED_ATTRIBUTES.contains(attributeName)) {
					continue;
				}

				if (property == null) {
					if (policy == DeserializationPolicy.RESTRICTIVE) {
						throw new RestrictiveDeserializationException("No attribute found for the attribute named: " + attributeName);
					}
					else {
						continue;
					}
				}

				// transforms child
				TransformedObjectInfo childInfo = new TransformedObjectInfo(factory, returned, property, null);
				childInfo.setFromString(attributes.getValue(i));
				info.initializeDeserialization();
				connectObject(childInfo);
				allObjects.add(childInfo);
			}

			return true;
		} catch (Exception e) {
			if (e instanceof SAXException)
				throw (SAXException) e;
			throw new SAXException(e);
		}
	}

	private void register(String id, TransformedObjectInfo info) throws SAXException {
		allObjects.add(info);
		if (id != null) {
			objectsWithId.put(id, info.getObject());

			// resolves forward references if any
			List<Resolver> forwards = forwardReferences.remove(id);
			if (forwards != null) {
				for (Resolver forward : forwards) {
					forward.resolve(info);
				}
			}
		}
	}

	private boolean stackEmpty() {
		return stack.isEmpty();
	}

	private void pushInfo(TransformedObjectInfo info) {
		if (stack.isEmpty())
			rootInfo = info;
		stack.push(info);
	}

	private TransformedObjectInfo peekInfo() {
		return stack.peek();
	}

	private TransformedObjectInfo popInfo() {
		return stack.pop();
	}
}
