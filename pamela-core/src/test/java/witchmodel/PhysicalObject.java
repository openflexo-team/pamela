/**
 * 
 * Copyright (c) 2015, Openflexo
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

package witchmodel;

import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

import witchmodel.PhysicalObject.PhysicalObjectImpl;

@ModelEntity()
@ImplementationClass(PhysicalObjectImpl.class)
@XMLElement()
public interface PhysicalObject {

	public static final String WIDTH = "width";
	public static final String HEIGHT = "height";
	public static final String LENGTH = "lenght";
	public static final String DENSITY = "density";

	@Getter(value = WIDTH, defaultValue = "0")
	@XMLAttribute(xmlTag = WIDTH)
	public Float getWidth();

	@Setter(WIDTH)
	public void setWidth(Float value);

	@Getter(value = HEIGHT, defaultValue = "0")
	@XMLAttribute(xmlTag = HEIGHT)
	public Float getHeight();

	@Setter(HEIGHT)
	public void setHeight(Float value);

	@Getter(value = LENGTH, defaultValue = "0")
	@XMLAttribute(xmlTag = LENGTH)
	public Float getLength();

	@Setter(LENGTH)
	public void setLength(Float value);

	@Getter(value = DENSITY, defaultValue = "0")
	@XMLAttribute(xmlTag = DENSITY)
	public Float getDensity();

	@Setter(DENSITY)
	public void setDensity(Float value);

	public Float getFullVolume();

	public Float getWeight();

	public abstract class PhysicalObjectImpl implements PhysicalObject {

		public Float getFullVolume() {
			return this.getHeight() * this.getWidth() * this.getLength();
		}

		public Float getWeight() {
			return this.getFullVolume() * this.getDensity() * 1000;
		}
	}

}
