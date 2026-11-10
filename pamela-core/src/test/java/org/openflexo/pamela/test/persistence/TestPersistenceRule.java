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


package org.openflexo.pamela.test.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;

/**
 * The persistence rule of PAMELA-F-3: a property is persistent when it is neither derived nor transient, whatever the way the model is
 * saved. Until {@link ModelProperty#PERSISTENCE_RULE} becomes the default, {@link ModelProperty#isPersistent()} still answers the former
 * XML criterion; the assertions that depend on the mode say so.
 */
public class TestPersistenceRule {

	private static PamelaModelFactory factory() throws ModelDefinitionException {
		return new PamelaModelFactory(PamelaMetaModelLibrary.retrieveMetaModel(Document.class));
	}

	private static ModelProperty<?> property(String identifier) throws ModelDefinitionException {
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(Document.class);
		ModelEntity<Document> entity = metaModel.getModelEntity(Document.class);
		return entity.getModelProperty(identifier);
	}

	@Test
	public void testRule() throws ModelDefinitionException {
		assertTrue(property(Document.NAME).isPersistentByRule());
		assertTrue(property(Document.NOTE).isPersistentByRule());
		assertFalse(property(Document.CACHE).isPersistentByRule());
		assertFalse(property(Document.LENGTH).isPersistentByRule());
		assertTrue(property(Document.CACHE).isTransient());
		assertFalse(property(Document.NOTE).isTransient());
	}

	@Test
	public void testIsPersistentFollowsTheMode() throws ModelDefinitionException {
		assertTrue(property(Document.NAME).isPersistent());
		assertEquals(ModelProperty.PERSISTENCE_RULE, property(Document.NOTE).isPersistent());
		assertFalse(property(Document.CACHE).isPersistent());
		assertFalse(property(Document.LENGTH).isPersistent());
	}

	@Test
	public void testModifiedStatus() throws ModelDefinitionException {
		Document document = factory().newInstance(Document.class);
		document.setModified(false);
		document.setCache("computed");
		assertFalse("Setting a transient property marks the object modified", document.isModified());

		document.setNote("a note");
		assertEquals("Setting a persistent property without XML mapping", ModelProperty.PERSISTENCE_RULE, document.isModified());

		document.setModified(false);
		document.setName("doc");
		assertTrue("Setting an XML-mapped property does not mark the object modified", document.isModified());
	}

	@Test
	public void testTransientIsIgnoredForEquality() throws ModelDefinitionException {
		PamelaModelFactory factory = factory();
		Document d1 = factory.newInstance(Document.class);
		d1.setName("doc");
		d1.setNote("note");
		d1.setCache("one");
		Document d2 = factory.newInstance(Document.class);
		d2.setName("doc");
		d2.setNote("note");
		d2.setCache("two");
		assertTrue("Two documents differing only by a transient property", d1.equalsObject(d2));
		d2.setNote("another note");
		assertFalse("Two documents differing by a persistent property", d1.equalsObject(d2));
	}

	@Test
	public void testTransientIsNotCloned() throws ModelDefinitionException {
		Document document = factory().newInstance(Document.class);
		document.setName("doc");
		document.setNote("note");
		document.setCache("computed");
		Document clone = (Document) document.cloneObject();
		assertEquals("doc", clone.getName());
		assertEquals("note", clone.getNote());
		assertNull("A transient property is cloned", clone.getCache());
	}

	@Test
	public void testTransientCannotCarryAnXMLMapping() {
		try {
			new PamelaModelFactory(PamelaMetaModelLibrary.retrieveMetaModel(TransientWithXMLMapping.class));
			fail("A transient property with an XML mapping was accepted");
		} catch (ModelDefinitionException e) {
			assertTrue(e.getMessage(), e.getMessage().contains("transient"));
		}
	}
}
