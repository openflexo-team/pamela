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

import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * A section within a collaborative document.
 * Demonstrates nested entity synchronization.
 * 
 * @author PAMELA Sync Test
 */
@ModelEntity
@XMLElement(xmlTag = "DocumentSection")
public interface DocumentSection extends AccessibleProxyObject {

	String HEADING = "heading";
	String BODY = "body";
	String ORDER = "order";

	@Getter(value = HEADING, defaultValue = "New Section")
	@XMLAttribute
	String getHeading();

	@Setter(HEADING)
	void setHeading(String heading);

	@Getter(value = BODY, defaultValue = "")
	@XMLAttribute
	String getBody();

	@Setter(BODY)
	void setBody(String body);

	@Getter(value = ORDER, defaultValue = "0")
	@XMLAttribute
	int getOrder();

	@Setter(ORDER)
	void setOrder(int order);

}
