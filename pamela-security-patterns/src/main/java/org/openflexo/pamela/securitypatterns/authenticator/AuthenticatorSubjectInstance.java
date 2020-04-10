package org.openflexo.pamela.securitypatterns.authenticator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticatorSubject;

/**
 * This class wraps all the dynamic authenticator pattern related information extracted from a {@link AuthenticatorSubject} annotated class
 * instance.<br>
 * It has the responsibility of:
 * <ul>
 * <li>Enforcing invariants of the {@link AuthenticatorSubject} annotated class.</li>
 * <li>Enforcing preconditions of the {@link AuthenticatorSubject} annotated class.</li>
 * <li>Enforcing postconditions of the {@link AuthenticatorSubject} annotated class.</li>
 * </ul>
 *
 * @author C. SILVA
 */

@Deprecated
class AuthenticatorSubjectInstance {
	private final Object instance;
	private final AuthenticatorSubjectEntity entity;
	private Object authenticatorInstance;
	private final ArrayList<Object> authInfos;
	private Object defaultIdProof;
	private Object idProof;
	private Object currentIdProof;
	private boolean initializing;
	private boolean checking;

	/**
	 * Constructor of the class.
	 * 
	 * @param instance
	 *            {@link AuthenticatorSubject} annotated class instance.
	 * @param entity
	 *            {@link AuthenticatorSubjectEntity} wrapping this object.
	 */
	AuthenticatorSubjectInstance(Object instance, AuthenticatorSubjectEntity entity) {
		this.instance = instance;
		this.entity = entity;
		this.authInfos = new ArrayList<>();
		this.initializing = false;
		this.checking = false;
	}

	/**
	 * Initializes the relevant instance-specific information. This method is to be called right after the constructor.
	 */
	public void init() {
		try {
			this.initializing = true;
			for (Method m : this.entity.getAuthInfoGetters().values()) {
				Object info = m.invoke(instance);
				this.authInfos.add(info);
			}
			this.authenticatorInstance = entity.getAuthenticatorGetter().invoke(instance);
			this.defaultIdProof = this.entity.getIdProofGetter().invoke(this.instance);
			this.currentIdProof = this.defaultIdProof;
			this.idProof = null;
			this.initializing = false;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * @return the wrapped instance
	 */
	private Object getInstance() {
		return this.instance;
	}

	/**
	 * Method called before all method invoke. It performs the invariant and precondition checks.
	 * 
	 * @param method
	 *            Method which will be invoked
	 */
	void checkBeforeInvoke(Method method) {
		if (!this.initializing && !this.checking) {
			this.checking = true;
			this.checkInvariant();
			this.checkPreconditions(method);
			this.checking = false;
		}
	}

	/**
	 * Method called before after all method invoke. It performs the invariant and postcondition checks.
	 * 
	 * @param method
	 *            Method which will be invoked
	 * @param returnValue
	 *            returnValue of the method
	 */
	void checkAfterInvoke(Method method, Object returnValue) {
		if (!this.initializing && !this.checking) {
			this.checking = true;
			this.checkInvariant();
			this.checkPostcondition(method, returnValue);
			this.checking = false;
		}

	}

	/**
	 * Setter of the valid <code>Proof of Identity</code> of the wrapped instance
	 * 
	 * @param proof
	 *            valid <code>Proof of Identity</code>
	 */
	void setIDProof(Object proof) {
		this.idProof = proof;
	}

	/**
	 * Method checking the <code>Authenticator Subject</code> invariant.
	 */
	private void checkInvariant() {
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
			Object currentProof = this.entity.getIdProofGetter().invoke(this.instance);
			if (!this.currentIdProof.equals(currentProof)) {
				if ((this.idProof == null && !currentProof.equals(this.defaultIdProof))
						|| (this.idProof != null && !currentProof.equals(this.idProof) && !currentProof.equals(this.defaultIdProof))) {
					throw new ModelExecutionException("Subject Invariant Violation: Proof of identity has been forged");
				}
				this.currentIdProof = currentProof;
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method checking the invariant ensuring all <code>Authentication Information</code> does not change throughout runtime.
	 */
	private void checkAuthInfoIsFinal() {
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
		}
	}

	/**
	 * Method checking the invariant ensuring the <code>authenticator</code> does not change throughout runtime.
	 */
	private void checkAuthenticatorIsFinal() {
		try {
			Object currentAuthenticator = this.entity.getAuthenticatorGetter().invoke(this.instance);
			if (!currentAuthenticator.equals(this.authenticatorInstance)) {
				throw new ModelExecutionException("Subject Invariant Violation: Authenticator has changed since initialization");
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Method checking the invariant ensuring uniqueness of the set of <code>Authentication Information</code>.
	 */
	private void checkAuthInfoUniqueness() {
		for (AuthenticatorSubjectInstance otherInstance : this.entity.getInstances().values()) {
			int i;
			for (i = 0; i < authInfos.size(); i++) {
				if (authInfos.get(i) != otherInstance.getAuthInfos().get(i)) {
					break;
				}
			}
			if (i == authInfos.size() && !otherInstance.getInstance().equals(this.instance)) {
				throw new ModelExecutionException("Subject Invariant Violation: Authentication information are not unique");
			}
		}
	}

	/**
	 * @return the set of <code>Authentication Information of the wrapped instance</code>
	 */
	private ArrayList<Object> getAuthInfos() {
		return this.authInfos;
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
		if (this.entity.getAuthenticateMethods().contains(method)) {
			this.checkPostConditionAuthenticate();
		}
	}

	/**
	 * Method checking the postCondition of the <code>Authenticate</code> method.
	 */
	private void checkPostConditionAuthenticate() {
		if (this.idProof == null) {
			throw new ModelExecutionException(String.format("Subject authenticate method postcondition violation (Pattern %s, Class %s)",
					this.entity.getPattern().getID(), this.entity.getBaseClass().getSimpleName()));
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
