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
public class Subject extends PropertyChangedSupportDefaultImplementation {
	public static final String PATTERN_ID = "patternID";
	public static final String AUTH_INFO = "auth_info1";
	public static final String MANAGER = "manager";
	public static final String ID_PROOF = "id_proof";

	private String authInfo;
	private int idProof = -1;
	private CustomAuthenticator authenticator;
	private boolean authenticatedMethodHasBeenSuccessfullyCalled = false;

	public Subject(CustomAuthenticator authenticator, String id) {
		setAuthenticator(authenticator);
		setAuthInfo(id);
	}

	public Subject(String id) {
		setAuthInfo(id);
	}

	@AuthenticationInformation(patternID = PATTERN_ID, paramID = CustomAuthenticator.ID)
	public String getAuthInfo() {
		return authInfo;
	}

	public void setAuthInfo(String authInfo) {
		String oldAuthInfo = this.authInfo;
		this.authInfo = authInfo;
		getPropertyChangeSupport().firePropertyChange("authInfo", oldAuthInfo, authInfo);
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
	public void authenticate() {
		setIDProof(getAuthenticator().request(getAuthInfo()));
	}

	@RequiresAuthentication
	public void thisMethodRequiresToBeAuthenticated() {
		System.out.println("perform thisMethodRequiresToBeAuthenticated()");
		authenticatedMethodHasBeenSuccessfullyCalled = true;
	}

	public boolean getAuthenticatedMethodHasBeenSuccessfullyCalled() {
		return authenticatedMethodHasBeenSuccessfullyCalled;
	}

}
