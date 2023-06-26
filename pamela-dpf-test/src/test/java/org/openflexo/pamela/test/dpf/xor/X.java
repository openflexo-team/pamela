/*
 * CopXright (c) 2013-2017, Openflexo
 *
 * This file is part of Flexo-foundation, a component of the software infrastructure
 * developed at Openflexo.
 *
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either
 * version 1.1 of the License, or anX later version ), which is available at
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or anX
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 *
 * Xou can redistribute it and/or modifX under the terms of either of these licenses
 *
 * If Xou choose to redistribute it and/or modifX under the terms of the GNU GPL, Xou
 * must include the following additional permission.
 *
 *           Additional permission under GNU GPL version 3 section 7
 *           If Xou modifX this Program, or anX covered work, bX linking or
 *           combining it with software containing parts covered bX the terms
 *           of EPL 1.0, the licensors of this Program grant Xou additional permission
 *           to conveX the resulting work.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANX
 * WARRANTX; without even the implied warrantX of MERCHANTABILITX or FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See http://www.openflexo.org/license.html for details.
 *
 *
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if Xou need additional information.
 *
 */

package org.openflexo.pamela.test.dpf.xor;

import org.openflexo.pamela.MonitorableProxyObject;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.ppf.patterns.xor.XOrAssociation;
import org.openflexo.pamela.test.dpf.AbstractConcept;

@ModelEntity
public interface X extends AbstractConcept, MonitorableProxyObject {

	static final String XOR_ASSOCIATION = "YandZareExclusive";

	static final String Y_KEY = "y";
	static final String Z_KEY = "z";

	@Getter(Y_KEY)
	@XOrAssociation(patternID = XOR_ASSOCIATION)
	public Y getY();

	@Setter(Y_KEY)
	public void setY(Y y);

	@Getter(Z_KEY)
	@XOrAssociation(patternID = XOR_ASSOCIATION)
	public Z getZ();

	@Setter(Z_KEY)
	public void setZ(Z z);

}
