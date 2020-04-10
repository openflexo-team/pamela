package org.openflexo.pamela.securitypatterns.authenticator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openflexo.pamela.PamelaUtils;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.patterns.PatternInstance;
import org.openflexo.pamela.patterns.ReturnWrapper;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.RequiresAuthentication;
import org.openflexo.toolbox.HasPropertyChangeSupport;

public class AuthenticatorPatternInstance<A, S, AI, PI> extends PatternInstance<AuthenticatorPatternDefinition>
		implements PropertyChangeListener {

	private final S subject;
	private A authenticator;
	private boolean isAuthenticated = false;

	public AuthenticatorPatternInstance(AuthenticatorPatternDefinition patternDefinition, S subject) {
		super(patternDefinition);
		this.subject = subject;
		registerStakeHolder(subject, AuthenticatorPatternDefinition.SUBJECT_ROLE);
		if (subject instanceof HasPropertyChangeSupport) {
			((HasPropertyChangeSupport) subject).getPropertyChangeSupport().addPropertyChangeListener(this);
		}
		checkAuthenticator();
	}

	public boolean isValid() {
		// Perform here required checks
		if (subject == null || authenticator == null) {
			return false;
		}
		return true;
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
		System.out.println("method: " + getPatternDefinition().requestAuthentificationMethod);
		System.out.println("AuthInfo=" + getAuthentificationInformation());

		PI proofOfIdentity = (PI) getPatternDefinition().requestAuthentificationMethod.invoke(authenticator,
				getAuthentificationInformation());
		System.out.println("proofOfIdentity=" + proofOfIdentity);
		setProofOfIdentity(proofOfIdentity);
		if (proofOfIdentity != null) {
			isAuthenticated = true;
		}
	}

	public boolean isAuthenticated() {
		return isAuthenticated;
	}

	public AI getAuthentificationInformation() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return (AI) getPatternDefinition().authentificationInfoMethod.invoke(subject);
	}

	public void setProofOfIdentity(PI proofOfIdentity) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		getPatternDefinition().proofOfIdentitySetterMethod.invoke(subject, proofOfIdentity);
	}

	/**
	 * Method called before every <code>baseClass</code> method invoke. Performs the execution, if relevant.
	 * 
	 * @param instance
	 *            Object on which the method is called
	 * @param method
	 *            Called method
	 * @param klass
	 *            Pattern-related class of identified im the class tree of <code>instance</code>
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
	public ReturnWrapper processMethodBeforeInvoke(Object instance, Method method /*, Class klass*/, Object[] args)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		/*Method superMethod = klass.getMethod(method.getName(), method.getParameterTypes());
		if (this.authenticateMethods.contains(method)) {
			pattern.performAuthentication(instance, this.idProofSetter, this.args, this.authenticatorGetter, this);
			return new ReturnWrapper(!Modifier.isAbstract(method.getModifiers()), null);
		}
		else if (this.authenticateMethods.contains(superMethod)) {
			pattern.performAuthentication(instance, this.idProofSetter, this.args, this.authenticatorGetter, this);
			return new ReturnWrapper(!Modifier.isAbstract(method.getModifiers()), null);
		}
		return new ReturnWrapper(true, null);*/

		if (instance != getSubject()) {
			// We are only interested to the method calls on the subject
			return new ReturnWrapper(true, null);
		}

		/*if (PamelaUtils.methodIsEquivalentTo(method, getPatternDefinition().requestAuthentificationMethod)
				|| PamelaUtils.methodIsEquivalentTo(method, getPatternDefinition().authentificationInfoMethod)
				|| PamelaUtils.methodIsEquivalentTo(method, getPatternDefinition().proofOfIdentitySetterMethod)
				|| PamelaUtils.methodIsEquivalentTo(method, getPatternDefinition().authenticatorGetterMethod)
				|| PamelaUtils.methodIsEquivalentTo(method, getPatternDefinition().authenticateMethod)) {
			return null;
		}*/

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

		// System.out.println("On execute la methode " + method);

		/*if (isValid()) {
			performAuthentication();
		}*/

		// System.out.println("On appelle la methode " + method + " pour le pattern Authenticator");

		return new ReturnWrapper(true, null);
	}

}
