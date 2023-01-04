/**
 * 
 * Copyright (c) 2013-2020, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of pamela-security-patterns, a component of the software infrastructure 
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

package org.openflexo.pamela.securitypatterns.authenticator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openflexo.pamela.annotations.Getter;

/**
 * This annotation is used in the definition of an <code>Authenticator Pattern</code>. <br>
 * This annotation is to be put on a {@link Getter} method in a {@link AuthenticatorSubject} annotated class and on the corresponding
 * parameter of the {@link RequestAuthentication} annotated method of the associated {@link Authenticator} class. <br>
 * The associated field is supposed to be initialized in constructor and must not change throughout runtime. <br>
 * A class can have multiple {@link AuthenticationInformation} annotations. The set of all of these fields is supposed to be unique. (i.e.
 * two different instances must have a different set of <code>Authentication Information</code>).
 *
 * @author Caine Silva, Sylvain Guerin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD, ElementType.PARAMETER })
public @interface AuthenticationInformation {
	/**
	 * @return The unique identifier of the associated Authenticator Pattern instance.
	 */
	String patternID();

	/**
	 * @return The identifier allowing the pattern to link the {@link AuthenticationInformation} getter with the
	 *         {@link RequestAuthentication} parameter.
	 */
	String paramID();

	/**
	 * @return true if this AuthenticationInformation should be unique regarding to AuthenticationInformation unicity
	 */
	boolean isUniqueKey() default true;

	/**
	 * @return index of this AuthenticationInformation in the ordered list of all AuthenticationInformations
	 * @return
	 */
	int index() default -1;
}
