/**
 * Copyright (c) 2013-2015, Openflexo
 * 
 * This file is part of Pamela-core, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 */

package org.openflexo.pamela.test.sync;

import java.util.List;

import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * A simple PAMELA model entity representing a collaborative document.
 * This entity is designed to be synchronized across multiple replicas
 * using the RabbitMQ-based sync infrastructure.
 * 
 * @author PAMELA Sync Test
 */
@ModelEntity
@XMLElement(xmlTag = "CollaborativeDocument")
public interface CollaborativeDocument extends AccessibleProxyObject {

	// Property keys
	String TITLE = "title";
	String CONTENT = "content";
	String AUTHOR = "author";
	String VERSION = "version";
	String TAGS = "tags";
	String SECTIONS = "sections";

	// ========== TITLE ==========
	
	@Getter(value = TITLE, defaultValue = "Untitled")
	@XMLAttribute
	String getTitle();

	@Setter(TITLE)
	void setTitle(String title);

	// ========== CONTENT ==========
	
	@Getter(value = CONTENT, defaultValue = "")
	@XMLAttribute
	String getContent();

	@Setter(CONTENT)
	void setContent(String content);

	// ========== AUTHOR ==========
	
	@Getter(value = AUTHOR, defaultValue = "Anonymous")
	@XMLAttribute
	String getAuthor();

	@Setter(AUTHOR)
	void setAuthor(String author);

	// ========== VERSION ==========
	
	@Getter(value = VERSION, defaultValue = "1")
	@XMLAttribute
	int getVersion();

	@Setter(VERSION)
	void setVersion(int version);

	// ========== TAGS (multi-valued) ==========
	
	@Getter(value = TAGS, cardinality = Cardinality.LIST)
	List<String> getTags();

	@Setter(TAGS)
	void setTags(List<String> tags);

	@Adder(TAGS)
	void addToTags(String tag);

	@Remover(TAGS)
	void removeFromTags(String tag);

	// ========== SECTIONS (embedded entities) ==========
	
	@Getter(value = SECTIONS, cardinality = Cardinality.LIST)
	@XMLElement
	List<DocumentSection> getSections();

	@Setter(SECTIONS)
	void setSections(List<DocumentSection> sections);

	@Adder(SECTIONS)
	void addToSections(DocumentSection section);

	@Remover(SECTIONS)
	void removeFromSections(DocumentSection section);

}
