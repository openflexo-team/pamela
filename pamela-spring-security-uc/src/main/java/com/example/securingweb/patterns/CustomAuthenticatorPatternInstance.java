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

package com.example.securingweb.patterns;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.openflexo.pamela.patterns.PreconditionViolationException;
import org.openflexo.pamela.patterns.PropertyViolationException;
import org.openflexo.pamela.patterns.annotations.Requires;
import org.openflexo.pamela.securitypatterns.authenticator.AuthenticatorPatternDefinition;
import org.openflexo.pamela.securitypatterns.authenticator.AuthenticatorPatternInstance;

import com.example.securingweb.authentication.SessionInfo;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * A specialization for {@link AuthenticatorPatternDefinition}
 */
public class CustomAuthenticatorPatternInstance<A, S, AI, PI> extends AuthenticatorPatternInstance<A, S, AI, PI> {

	String key;
	private final int MAX_ATTEMPT = 3;
	LoadingCache<String, Integer> attemptsCache;
	boolean isBlocked;

	public CustomAuthenticatorPatternInstance(CustomAuthenticatorPatternDefinition patternDefinition, S subject) {
		super(patternDefinition, subject);
		key = ((SessionInfo) subject).getIpAdress();
		attemptsCache = CacheBuilder.newBuilder().expireAfterWrite(3, TimeUnit.MINUTES).build(new CacheLoader<String, Integer>() {
			@Override
			public Integer load(String key) {
				return 0;
			}

		});
	}

	@Override
	public void invokePrecondition(Requires precondition, Method method) throws PropertyViolationException {
		super.invokePrecondition(precondition, method);

		if (precondition.property().equals("assert always auth_fail[*3] & time_limit<3min @ (auth_fail)")) {
			System.out.println("J'appelle la methode " + method + " pour " + method.getDeclaringClass());

			Thread.dumpStack();

			System.out.printf("LA CLE VAUT " + key + "\n");

			if (attemptsCache.getUnchecked(key) >= MAX_ATTEMPT) {
				isBlocked = true;
				throw new PreconditionViolationException(precondition);
			}

			else {
				int attempts = 0;
				attempts = attemptsCache.getUnchecked(key);
				attempts++;
				System.out.printf("La tentative num�ro " + attempts + " a �chou�\n");
				attemptsCache.put(key, attempts);
				System.out.println("L'authentification a �chou�, la valeur dans le cache augmente de 1 \n");
			}

		}

		/*		else if (precondition.property().equals("assert always not(a)[*0:10];a")) {
					
				    private final int MAX_ATTEMPT = 10;
				    private LoadingCache<String, Integer> attemptsCache;
		
				    attemptsCache = CacheBuilder.newBuilder().
				    expireAfterWrite(1, TimeUnit.DAYS).build(new CacheLoader<String, Integer>() {
				            public Integer load(String key) {
				                return 0;
				            }
				    
				    });
				        
		
				    public void loginSucceeded(String key) {
				        attemptsCache.invalidate(key);
				    }
		
				    public void loginFailed(String key) {
				        int attempts = 0;
				        try {
				            attempts = attemptsCache.get(key);
				        } catch (ExecutionException e) {
				            attempts = 0;
				        }
				        attempts++;
				        attemptsCache.put(key, attempts);
				    }
		
				    public boolean isBlocked(String key) {
				        try {
				            return attemptsCache.get(key) >= MAX_ATTEMPT;
				        } catch (ExecutionException e) {
				            return false;
				     }
				  }
					
				}*/

	}

	@Override
	public void authenticationSuceeded() {
		// TODO Auto-generated method stub
		super.authenticationSuceeded();
		attemptsCache.invalidate(key);
		System.out.println("L'authentification a r�ussi, le cache est r�initialis� \n");

	}
}
