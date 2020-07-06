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

import java.util.logging.Level;

import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.model.StringConverterLibrary.Converter;
import org.openflexo.toolbox.StringUtils;

/**
 * An implementation for {@link Level} <-> String {@link Converter}
 * 
 * @author gpolet
 * 
 */
public class LevelConverter extends Converter<Level> {

	public LevelConverter() {
		super(Level.class);
	}

	@Override
	public Level convertFromString(String value, ModelFactory factory) {
		if (StringUtils.isEmpty(value)) {
			return null;
		}
		if (value.equals("SEVERE")) {
			return Level.SEVERE;
		}
		else if (value.equals("WARNING")) {
			return Level.WARNING;
		}
		else if (value.equals("INFO")) {
			return Level.INFO;
		}
		else if (value.equals("CONFIG")) {
			return Level.CONFIG;
		}
		else if (value.equals("FINE")) {
			return Level.FINE;
		}
		else if (value.equals("FINER")) {
			return Level.FINER;
		}
		else if (value.equals("FINEST")) {
			return Level.FINEST;
		}
		return null;

	}

	@Override
	public String convertToString(Level value) {
		return value.getName();
	}
}
