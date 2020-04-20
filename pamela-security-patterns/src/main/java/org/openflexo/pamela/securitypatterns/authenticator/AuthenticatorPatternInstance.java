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

package org.openflexo.pamela.securitypatterns.authenticator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openflexo.pamela.PamelaUtils;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.patterns.PatternInstance;
import org.openflexo.pamela.patterns.ReturnWrapper;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.Authenticator;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.RequiresAuthentication;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Represent an instance of a given {@link AuthenticatorPatternDefinition}
 * 
 * It has the responsibility of:
 * <ul>
 * <li>Maintaining state variables of the pattern instance</li>
 * <li>Enforcing invariants of the {@link Authenticator} annotated class.</li>
 * <li>Enforcing preconditions of the {@link Authenticator} annotated class.</li>
 * <li>Enforcing postconditions of the {@link Authenticator} annotated class.</li>
 * </ul>
 *
 * @author Caine Silva, Sylvain Guerin
 */
public class AuthenticatorPatternInstance<A, S, AI, PI> extends PatternInstance<AuthenticatorPatternDefinition>
		implements PropertyChangeListener {

	private final S subject;
	private A authenticator;
	private AI authInfo;
	private PI proofOfIdentity;
	private PI defaultIdProof;

	private boolean isAuthenticating = false;
	private boolean isChecking = false;
	private boolean isAuthenticated = false;

	public AuthenticatorPatternInstance(AuthenticatorPatternDefinition patternDefinition, S subject) {
		super(patternDefinition);
		this.subject = subject;
		registerStakeHolder(subject, AuthenticatorPatternDefinition.SUBJECT_ROLE);
		try {
			authInfo = retrieveAuthentificationInformation();
			defaultIdProof = retrieveProofOfIdentity();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (subject instanceof HasPropertyChangeSupport) {
			((HasPropertyChangeSupport) subject).getPropertyChangeSupport().addPropertyChangeListener(this);
		}
		checkAuthenticator();
	}

	public boolean isValid() {
		// Perform here required checks
		return subject != null && authenticator != null;
	}

	public S getSubject() {
		return subject;
	}

	public A getAuthenticator() {
		return authenticator;
	}

	private A retrieveAuthenticator() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return (A) getPatternDefinition().authenticatorGetterMethod.invoke(subject);
	}

	private void checkAuthenticator() {
		A retrievedAuthenticator;
		try {
			retrievedAuthenticator = retrieveAuthenticator();
			if (authenticator == null && retrievedAuthenticator != null) {
				authenticator = retrievedAuthenticator;
				registerStakeHolder(authenticator, AuthenticatorPatternDefinition.AUTHENTICATOR_ROLE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == subject) {
			// System.out.println("propertyChange from subject " + evt.getPropertyName() + " evt=" + evt);
			checkAuthenticator();
		}
	}

	/**
	 * Performs the authentication of the given instance.
	 * 
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * 
	 */
	void performAuthentication() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		System.out.println("performAuthentication() !!!");

		isAuthenticating = true;
		try {
			proofOfIdentity = (PI) getPatternDefinition().requestAuthentificationMethod.invoke(authenticator,
					retrieveAuthentificationInformation());
			System.out.println("proofOfIdentity=" + proofOfIdentity);
			setProofOfIdentity(proofOfIdentity);
			if (proofOfIdentity != null) {
				isAuthenticated = true;
			}
		} finally {
			isAuthenticating = false;
		}
	}

	public boolean isAuthenticated() {
		return isAuthenticated;
	}

	public AI retrieveAuthentificationInformation() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return (AI) getPatternDefinition().authentificationInfoMethod.invoke(subject);
	}

	public PI retrieveProofOfIdentity() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return (PI) getPatternDefinition().proofOfIdentityGetterMethod.invoke(getSubject());
	}

	public void setProofOfIdentity(PI proofOfIdentity) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		getPatternDefinition().proofOfIdentitySetterMethod.invoke(subject, proofOfIdentity);
	}

	/**
	 * Method called before every method of interest is about to be invoked. Performs the execution, if relevant.
	 * 
	 * @param instance
	 *            Object on which the method is called
	 * @param method
	 *            Called method
	 * @param args
	 * @return a {@link ReturnWrapper} wrapping true if the execution of the invoke should go one after the call, false if not.
	 * @throws InvocationTargetException
	 *             if an error occurred when internally invoking a method
	 * @throws IllegalAccessException
	 *             if an error occurred when internally invoking a method
	 * @throws NoSuchMethodException
	 *             if an error occurred when internally invoking a method
	 */
	@Override
	public ReturnWrapper processMethodBeforeInvoke(Object instance, Method method, Object[] args)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {

		if (instance != getSubject()) {
			// We are only interested to the method calls on the subject
			return new ReturnWrapper(true, null);
		}

		if (isChecking) {
			// Avoid stack overflow
			return new ReturnWrapper(true, null);
		}

		checkBeforeInvoke(instance, method, args);

		if (PamelaUtils.methodIsEquivalentTo(method, getPatternDefinition().authenticateMethod)) {
			if (isValid()) {
				performAuthentication();
				return new ReturnWrapper(false, null);
			}
		}

		Method apiMethod = getPatternDefinition().subjectModelEntity.getImplementedInterface().getMethod(method.getName(),
				method.getParameterTypes());

		if (method.getAnnotation(RequiresAuthentication.class) != null || (apiMethod.getAnnotation(RequiresAuthentication.class) != null)) {
			if (isValid() && !isAuthenticated()) {
				performAuthentication();
			}
			if (!isAuthenticated()) {
				throw new ModelExecutionException("This method requires prior authentification before execution");
			}
		}
		return new ReturnWrapper(true, null);
	}

	@Override
	public void processMethodAfterInvoke(Object instance, Method method, Object returnValue, Object[] args)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		if (instance != getSubject()) {
			// We are only interested to the method calls on the subject
			return;
		}

		if (isChecking) {
			// Avoid stack overflow
			return;
		}

		checkAfterInvoke(instance, method, returnValue, args);
	}

	/**
	 * Method called before all method invoke. It performs the invariant and precondition checks.
	 * 
	 * @param method
	 *            Method which will be invoked
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	void checkBeforeInvoke(Object instance, Method method, Object[] args)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		System.out.println(" ------- checkBeforeInvoke " + method);
		if (isValid()) {
			isChecking = true;
			try {
				checkInvariant();
				checkPreconditions(method);
			} finally {
				isChecking = false;
			}
		}
	}

	/**
	 * Method called before after all method invoke. It performs the invariant and postcondition checks.
	 * 
	 * @param method
	 *            Method which will be invoked
	 * @param returnValue
	 *            returnValue of the method
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	void checkAfterInvoke(Object instance, Method method, Object returnValue, Object[] args)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (isValid()) {
			isChecking = true;
			try {
				this.checkInvariant();
				this.checkPostcondition(method, returnValue);
			} finally {
				isChecking = false;
			}
		}

	}

	/**
	 * Method checking the <code>Authenticator Subject</code> invariant.
	 * 
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private void checkInvariant() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		System.out.println("checkInvariant() for " + this);
		this.checkAuthInfoUniqueness();
		this.checkAuthenticatorIsFinal();
		this.checkAuthInfoIsFinal();
		this.checkIdProofIsValid();
	}

	/**
	 * Method checking the invariant preventing <code>Proof of Identity</code> forgery.
	 */
	private void checkIdProofIsValid() {
		try {
			if (isAuthenticated() && !isAuthenticating) {
				PI currentProof = retrieveProofOfIdentity();
				if (!proofOfIdentity.equals(currentProof)) {
					if ((proofOfIdentity == null && !currentProof.equals(defaultIdProof)) || (proofOfIdentity != null
							&& !currentProof.equals(proofOfIdentity) && !currentProof.equals(this.defaultIdProof))) {
						throw new ModelExecutionException("Subject Invariant Violation: Proof of identity has been forged");
					}
				}
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method checking the invariant ensuring all <code>Authentication Information</code> does not change throughout runtime.
	 */
	private void checkAuthInfoIsFinal() {
		try {
			AI currentAuthInfo = retrieveAuthentificationInformation();
			if (currentAuthInfo != authInfo) {
				throw new ModelExecutionException(
						"Subject Invariant Violation: Authentication Information has changed since initialization");
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// TODO: multiple AuthInfo
		/*
		int i = 0;
		try {
			for (Method getter : this.entity.getAuthInfoGetters().values()) {
				Object currentAuthInfo = getter.invoke(this.instance);
				if (!currentAuthInfo.equals(this.authInfos.get(i))) {
					break;
				}
				i++;
			}
			if (i != this.authInfos.size()) {
				throw new ModelExecutionException(
						"Subject Invariant Violation: Authentication Information has changed since initialization");
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}*/
	}

	/**
	 * Method checking the invariant ensuring the <code>authenticator</code> does not change throughout runtime.
	 */
	private void checkAuthenticatorIsFinal() {
		try {
			A currentAuthenticator = retrieveAuthenticator();
			if (currentAuthenticator != authenticator) {
				throw new ModelExecutionException("Subject Invariant Violation: Authenticator has changed since initialization");
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Method checking the invariant ensuring uniqueness of the set of <code>Authentication Information</code>.
	 * 
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private void checkAuthInfoUniqueness() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		AI currentAuthInfo = retrieveAuthentificationInformation();
		for (PatternInstance<AuthenticatorPatternDefinition> pi : getModelContext().getPatternInstances(getPatternDefinition())) {
			AuthenticatorPatternInstance otherInstance = (AuthenticatorPatternInstance) pi;
			if (otherInstance != this) {
				if (currentAuthInfo.equals(otherInstance.retrieveAuthentificationInformation())) {
					throw new ModelExecutionException("Subject Invariant Violation: Authentication information are not unique");
				}
			}
		}
		// TODO: multiple AuthInfo
		/*for (AuthenticatorSubjectInstance otherInstance : this.entity.getInstances().values()) {
			int i;
			for (i = 0; i < authInfos.size(); i++) {
				if (authInfos.get(i) != otherInstance.getAuthInfos().get(i)) {
					break;
				}
			}
			if (i == authInfos.size() && !otherInstance.getInstance().equals(this.instance)) {
				throw new ModelExecutionException("Subject Invariant Violation: Authentication information are not unique");
			}
		}*/
	}

	/**
	 * Method checking the postconditions, if any, after the <code>method</code> invoke
	 * 
	 * @param method
	 *            Just-invoked method
	 * @param returnValue
	 *            Return Value of the invoked method
	 */
	private void checkPostcondition(Method method, Object returnValue) {
		if (PamelaUtils.methodIsEquivalentTo(method, getPatternDefinition().authenticateMethod)) {
			if (proofOfIdentity == null) {
				throw new ModelExecutionException(String.format(
						"Subject authenticate method postcondition violation (Pattern %s, Class %s)",
						getPatternDefinition().getIdentifier(), getPatternDefinition().subjectModelEntity.getImplementedInterface()));
			}

		}
	}

	/**
	 * Method checking the preconditions, if any, before the <code>method</code> invoke
	 * 
	 * @param method
	 *            Method to be invoked after check
	 */
	private void checkPreconditions(Method method) {

	}

}
