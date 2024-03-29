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

package org.openflexo.pamela.converter;

import java.awt.Rectangle;

import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.model.StringConverterLibrary.Converter;

/**
 * An implementation for {@link Rectangle} <-> String {@link Converter}
 * 
 * @author gpolet
 * 
 */
public class AWTRectangleConverter extends Converter<Rectangle> {

	public static final AWTRectangleConverter instance = new AWTRectangleConverter();

	public AWTRectangleConverter() {
		super(Rectangle.class);
	}

	@Override
	public Rectangle convertFromString(String value, PamelaModelFactory factory) {
		if (value == null) {
			return null;
		}
		Rectangle r = new Rectangle();
		int i = value.indexOf("x=");
		if (i > -1 && value.indexOf(',', i) > -1) {
			try {
				r.x = Integer.parseInt(value.substring(i + 2, value.indexOf(',', i)));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		i = value.indexOf("y=");
		if (i > -1 && value.indexOf(',', i) > -1) {
			try {
				r.y = Integer.parseInt(value.substring(i + 2, value.indexOf(',', i)));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		i = value.indexOf("width=");
		if (i > -1 && value.indexOf(',', i) > -1) {
			try {
				r.width = Integer.parseInt(value.substring(i + 6, value.indexOf(',', i)));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		i = value.indexOf("height=");
		if (i > -1 && value.indexOf(']', i) > -1) {
			try {
				r.height = Integer.parseInt(value.substring(i + 7, value.indexOf(']', i)));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return r;
	}

	@Override
	public String convertToString(Rectangle r) {
		return "[x=" + r.x + ",y=" + r.y + ",width=" + r.width + ",height=" + r.height + "]";
	}

}
