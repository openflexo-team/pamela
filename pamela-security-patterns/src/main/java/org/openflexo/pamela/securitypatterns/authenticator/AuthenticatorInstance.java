package org.openflexo.pamela.securitypatterns.authenticator;

import java.lang.reflect.Method;

import org.openflexo.pamela.securitypatterns.authenticator.annotations.Authenticator;

/**
 * This class wraps all the dynamic authenticator pattern related information extracted from a {@link Authenticator} annotated class
 * instance.<br>
 * It has the responsibility of:
 * <ul>
 * <li>Enforcing invariants of the {@link Authenticator} annotated class.</li>
 * <li>Enforcing preconditions of the {@link Authenticator} annotated class.</li>
 * <li>Enforcing postconditions of the {@link Authenticator} annotated class.</li>
 * </ul>
 *
 * @author C. SILVA
 */

@Deprecated
class AuthenticatorInstance {
	private final Object instance;
	private final AuthenticatorEntity entity;
	private boolean initializing;

	/**
	 * Constructor of the class
	 * 
	 * @param instance
	 *            {@link Authenticator} annotated class instance.
	 * @param entity
	 *            {@link AuthenticatorEntity} wrapping this object.
	 */
	AuthenticatorInstance(Object instance, AuthenticatorEntity entity) {
		this.entity = entity;
		this.instance = instance;
		this.initializing = false;
	}

	/**
	 * Initializes the relevant instance-specific information. This method is to be called right after the constructor.
	 */
	public void init() {
		this.initializing = true;
		this.initializing = false;
	}

	/**
	 * Method called before all method invoke. It performs the invariant and precondition checks.
	 * 
	 * @param method
	 *            Method which will be invoked
	 */
	void checkBeforeInvoke(Method method) {
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
	}
}
