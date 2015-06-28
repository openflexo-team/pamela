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
import java.util.HashMap;
import java.util.Map;

import org.openflexo.model.ModelEntity;
import org.openflexo.model.exceptions.InvalidDataException;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.DeserializationPolicy;
import org.openflexo.model.factory.ModelFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class JSonModelDeserializer extends AbstractModelDeserializer implements ModelDeserializer {

	private JsonFactory jsonFactory;

	public JSonModelDeserializer(ModelFactory aModelFactory) {
		super(aModelFactory);
		jsonFactory = new JsonFactory();
	}

	@Override
	public Object deserializeDocument(String input) throws IOException, InvalidDataException, ModelDefinitionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <I> Object deserializeDocument(InputStream is) throws IOException, InvalidDataException, ModelDefinitionException {

		JsonParser jp = jsonFactory.createParser(is);
		JsonToken token = null;
		Map<String, Object> properties = new HashMap<String, Object>();

		ModelEntity<I> modelEntity = null;
		String currentName = null;
		I returned = null;

		// Verify that we start with an Object
		token = jp.nextToken();
		if (token != JsonToken.START_OBJECT) {
			throw new InvalidDataException("Invalid JSon data: no start object!");
		}
		else {
			while (!jp.isClosed()) {
				token = jp.nextToken();
				if (token != null) {
					switch (token) {
						case END_OBJECT:
							// end of an object
							System.out.println("End of an object named: " + currentName + " having (" + properties.toString() + ")");
							properties.clear();
							modelEntity = null;
							currentName = null;
							break;

						case START_OBJECT:
							// start of an object, looking for relative ModelEntity
							System.out.println(" Nouvel Objet: " + currentName);
							modelEntity = (ModelEntity<I>) modelFactory.getModelContext().getModelEntity(currentName);

							Class<I> entityClass = modelEntity.getImplementedInterface();

							returned = modelFactory._newInstance(entityClass, policy == DeserializationPolicy.EXTENSIVE);
							initializeDeserialization(returned, modelEntity);

							if (modelEntity != null) {
								System.out.println("Found Entity named: " + modelEntity.getTypeName());
							}
							else {
								throw new ModelDefinitionException("Unknown model entity: " + currentName);
							}
							break;

						case FIELD_NAME:
							currentName = jp.getCurrentName();
							break;

						case VALUE_STRING:
							if (modelEntity != null) {
								// this is a property
								System.out.println(" Nouvel Propriété: " + currentName + " -- " + jp.getText());
								properties.put(jp.getCurrentName(), jp.getText());
							}
							else {
								// this is irrelevant for now
								System.out.println("Irrelevant data");
							}
							break;

						default:
							System.out.println(" Nouvo Token: " + token.asString() + " -- " + currentName);
							break;
					}
				}
				else {
					jp.close();
				}

			}

		}

		return returned;
	}
}
