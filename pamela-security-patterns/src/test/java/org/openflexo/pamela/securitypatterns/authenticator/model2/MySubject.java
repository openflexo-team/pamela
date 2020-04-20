package org.openflexo.pamela.securitypatterns.authenticator.model2;

import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticateMethod;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticationInformation;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticatorGetter;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticatorSubject;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.ProofOfIdentityGetter;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.ProofOfIdentitySetter;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.RequiresAuthentication;

@ModelEntity
@AuthenticatorSubject(patternID = MySubject.PATTERN_ID)
public class MySubject {
	public static final String PATTERN_ID = "patternID";
	public static final String AUTH_INFO = "auth_info1";
	public static final String MANAGER = "manager";
	public static final String ID_PROOF = "id_proof";

	private String authInfo;
	private int idProof = -1;
	private MyAuthenticator manager;
	private boolean authenticatedMethodHasBeenSuccessfullyCalled = false;

	public MySubject(MyAuthenticator authenticator, String id) {
		setManager(authenticator);
		setAuthInfo(id);
	}

	public MySubject(String id) {
		setAuthInfo(id);
	}

	@AuthenticationInformation(patternID = PATTERN_ID, paramID = MyAuthenticator.ID)
	public String getAuthInfo() {
		return authInfo;
	}

	public void setAuthInfo(String authInfo) {
		this.authInfo = authInfo;
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
	public MyAuthenticator getManager() {
		return manager;
	}

	public void setManager(MyAuthenticator manager) {
		this.manager = manager;
	}

	@AuthenticateMethod(patternID = PATTERN_ID)
	public void authenticate() {
		setIDProof(getManager().request(getAuthInfo()));
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
