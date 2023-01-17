package org.openflexo.pamela.securitypatterns.customauthenticator.model;

import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticateMethod;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticationInformation;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticatorGetter;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticatorSubject;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.ProofOfIdentityGetter;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.ProofOfIdentitySetter;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.RequiresAuthentication;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;

@ModelEntity
@AuthenticatorSubject(patternID = Subject.PATTERN_ID)
public abstract class Subject extends PropertyChangedSupportDefaultImplementation {

	public static final String PATTERN_ID = "patternID";
	public static final String LOGIN = "login";
	public static final String PASSWORD = "password";

	private String login;
	private String password;
	private int idProof = -1;
	private CustomAuthenticator authenticator;
	private boolean authenticatedMethodHasBeenSuccessfullyCalled = false;

	public Subject(CustomAuthenticator authenticator, String login, String password) {
		setAuthenticator(authenticator);
		setLogin(login);
		setPassword(password);
	}

	public Subject(String login, String password) {
		setLogin(login);
		setPassword(password);
	}

	@AuthenticationInformation(patternID = PATTERN_ID, paramID = LOGIN, isUniqueKey = true, index = 0)
	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		String oldLogin = this.login;
		this.login = login;
		getPropertyChangeSupport().firePropertyChange("login", oldLogin, login);
	}

	@AuthenticationInformation(patternID = PATTERN_ID, paramID = PASSWORD, isUniqueKey = false, index = 1)
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		String oldPassword = this.password;
		this.password = password;
		getPropertyChangeSupport().firePropertyChange("password", oldPassword, password);
	}

	@ProofOfIdentityGetter(patternID = PATTERN_ID)
	public int getIDProof() {
		return idProof;
	}

	@ProofOfIdentitySetter(patternID = PATTERN_ID)
	public void setIDProof(int idProof) {
		this.idProof = idProof;
	}

	@AuthenticatorGetter(patternID = PATTERN_ID)
	public CustomAuthenticator getAuthenticator() {
		return authenticator;
	}

	public void setAuthenticator(CustomAuthenticator authenticator) {
		CustomAuthenticator oldAuthenticator = this.authenticator;
		this.authenticator = authenticator;
		getPropertyChangeSupport().firePropertyChange("authenticator", oldAuthenticator, authenticator);
	}

	@AuthenticateMethod(patternID = PATTERN_ID)
	public abstract void authenticate();
	// Implementation is not necessary !
	// {
	// setIDProof(getAuthenticator().request(getLogin(), getPassword()));
	// }

	@RequiresAuthentication
	public void thisMethodRequiresToBeAuthenticated() {
		System.out.println("perform thisMethodRequiresToBeAuthenticated()");
		authenticatedMethodHasBeenSuccessfullyCalled = true;
	}

	public boolean getAuthenticatedMethodHasBeenSuccessfullyCalled() {
		return authenticatedMethodHasBeenSuccessfullyCalled;
	}

}
