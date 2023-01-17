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

package org.openflexo.pamela.securitypatterns.customauthenticator.model;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.openflexo.pamela.factory.PamelaModel;
import org.openflexo.pamela.securitypatterns.authenticator.AuthenticatorPatternDefinition;
import org.openflexo.pamela.securitypatterns.authenticator.AuthenticatorPatternInstance;

/**
 * A specialization for {@link AuthenticatorPatternDefinition}
 */
public class CustomAuthenticatorPatternInstance extends AuthenticatorPatternInstance<CustomAuthenticator, Subject, String, Integer> {

	public static long TIME_LIMIT = 1000; // 1s

	public CustomAuthenticatorPatternInstance(CustomAuthenticatorPatternDefinition patternDefinition, PamelaModel model, Subject subject)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		super(patternDefinition, model, subject);
	}

	public boolean checkRecentAuthFailCountLessThan3() {
		System.out.println("checkRecentAuthFailCountLessThan3()");
		long currentTime = System.currentTimeMillis();
		List<AuthFailedEvent> events = getEvents(AuthFailedEvent.class);
		// System.out.println("events:" + events);
		if (events.size() >= 3 && (currentTime - events.get(events.size() - 3).getDate()) < TIME_LIMIT) {
			// 3 attempts or more in TIME_LIMIT interval
			return false;
		}
		return true;
	}

	public void generateAuthFailEvent() {
		triggerEvent(new AuthFailedEvent());
		checkRecentAuthFailCountLessThan3();
	}

	@Override
	public void authenticationSuceeded() {
		super.authenticationSuceeded();
		// We may reset AuthFailEvent...
	}
}
