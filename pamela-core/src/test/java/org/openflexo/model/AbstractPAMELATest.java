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

package org.openflexo.model;

import junit.framework.TestCase;

import org.flexo.model.AbstractNode;
import org.flexo.model.TestModelObject;
import org.flexo.model.FlexoProcess;
import org.flexo.model.StartNode;
import org.flexo.model.TokenEdge;
import org.flexo.model.WKFObject;
import org.openflexo.model.exceptions.ModelDefinitionException;

public abstract class AbstractPAMELATest extends TestCase {

	/**
	 * Little hack to access the library clear() method. This is only for testing purposes.
	 */
	protected void clearModelEntityLibrary() {
		ModelEntityLibrary.clear();
	}

	protected void validateBasicModelContext(ModelContext modelContext) throws ModelDefinitionException {
		ModelEntity<TestModelObject> modelObjectEntity = modelContext.getModelEntity(TestModelObject.class);
		ModelEntity<FlexoProcess> processEntity = modelContext.getModelEntity(FlexoProcess.class);
		ModelEntity<AbstractNode> abstractNodeEntity = modelContext.getModelEntity(AbstractNode.class);
		ModelEntity<StartNode> startNodeEntity = modelContext.getModelEntity(StartNode.class);
		ModelEntity<TokenEdge> tokenEdgeEntity = modelContext.getModelEntity(TokenEdge.class);
		ModelEntity<WKFObject> wkfObjectEntity = modelContext.getModelEntity(WKFObject.class);

		assertNotNull(processEntity);
		assertNotNull(abstractNodeEntity);
		assertNotNull(startNodeEntity);
		assertNotNull(tokenEdgeEntity);
		assertNotNull(wkfObjectEntity);

		ModelProperty<? super FlexoProcess> nodesProperty = processEntity.getModelProperty(FlexoProcess.NODES);
		assertNotNull(nodesProperty);
		ModelProperty<? super FlexoProcess> fooProperty = processEntity.getModelProperty(FlexoProcess.FOO);
		assertNotNull(fooProperty);
		assertNotNull(modelObjectEntity.getModelProperty(TestModelObject.FLEXO_ID));
		assertNotNull(processEntity.getModelProperty(TestModelObject.FLEXO_ID));
		assertNotNull(wkfObjectEntity.getModelProperty(TestModelObject.FLEXO_ID));
		assertNotNull(wkfObjectEntity.getModelProperty(TestModelObject.FLEXO_ID).getSetter());

		ModelProperty<? super WKFObject> wkfObjectProcessProperty = wkfObjectEntity.getModelProperty(WKFObject.PROCESS);
		assertNotNull(wkfObjectProcessProperty);
		assertNull(wkfObjectProcessProperty.getInverseProperty(processEntity));
		assertNotNull(wkfObjectProcessProperty.getSetter());
		ModelProperty<? super AbstractNode> abstractNodeProcessProperty = abstractNodeEntity.getModelProperty(WKFObject.PROCESS);
		assertNotNull(abstractNodeProcessProperty);
		assertNotNull(abstractNodeProcessProperty.getInverseProperty(processEntity));
		assertNotNull(abstractNodeProcessProperty.getSetter());
		assertTrue(modelObjectEntity.getAllDescendants(modelContext).contains(processEntity));
	}
}
