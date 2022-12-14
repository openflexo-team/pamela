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

package org.openflexo.pamela.test.tests4;

import org.openflexo.pamela.factory.PamelaModelFactory;

public abstract class MyContainerImpl implements MyContainer {

	private MyContents lecontenu = null;
	private PamelaModelFactory factory = null;
	private String contentURI = null;

	public void setFactory(PamelaModelFactory fact) {
		factory = fact;
	}

	@Override
	public String getContentURI() {
		if (lecontenu != null) {
			contentURI = new String("Content://" + lecontenu.toString());
			// If you uncomment this => infinite loop
			// setContentURI("Content://" + lecontenu.toString());
		}
		return contentURI;
	}

	@Override
	public void setContentURI(String anURI) {
		System.out.println("JE positionne l'URI " + anURI);
		contentURI = anURI;
	}

	@Override
	public String getContents() {
		if (lecontenu != null) {
			System.out.println("Getting something + " + lecontenu.getValue());
			return lecontenu.getValue();
		}
		else if (contentURI != null) {

			String anURi = getContentURI();
			System.out.println("Getting from URI: " + anURi);
			// If you uncomment this => infinite loop!
			// setContents(anURi.substring(10));
			lecontenu = MyContentsImpl.fromString(factory, anURi.substring(10));
			return lecontenu.getValue();
		}
		else {
			System.out.println("Getting Nothing ");
			return null;
		}
	}

	@Override
	public void setContents(String someContents) {
		if (getContents() == null) {
			System.out.println("Setting something (creation): " + someContents);
			lecontenu = MyContentsImpl.fromString(factory, someContents);
		}
		else {
			System.out.println("Setting something : " + someContents);
			lecontenu.setValue(someContents);
		}
		setContentURI("Content://" + lecontenu.getValue());
	}
}
