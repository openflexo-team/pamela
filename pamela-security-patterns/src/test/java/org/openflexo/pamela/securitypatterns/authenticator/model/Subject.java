package org.openflexo.pamela.securitypatterns.authenticator.model;

import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.factory.AccessibleProxyObject;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticateMethod;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticationInformation;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticatorGetter;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticatorSubject;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.ProofOfIdentitySetter;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.RequiresAuthentication;

@ModelEntity
@ImplementationClass(Subject.SubjectImp.class)
@AuthenticatorSubject(patternID = Subject.PATTERN_ID)
public interface Subject extends AccessibleProxyObject {
	String PATTERN_ID = "patternID";
	String AUTH_INFO = "auth_info1";
	String MANAGER = "manager";
	String ID_PROOF = "id_proof";

	@Initializer
	default void init(IAuthenticator authenticator, String id) {
		setManager(authenticator);
		setAuthInfo(id);
	}

	@Initializer
	default void init(String id) {
		setAuthInfo(id);
	}

	@Getter(value = AUTH_INFO, defaultValue = AUTH_INFO)
	@AuthenticationInformation(patternID = PATTERN_ID, paramID = IAuthenticator.ID)
	String getAuthInfo();

	@Setter(AUTH_INFO)
	void setAuthInfo(String val);

	@Getter(value = ID_PROOF, defaultValue = "-1")
	int getIDProof();

	@Setter(ID_PROOF)
	@ProofOfIdentitySetter(patternID = PATTERN_ID)
	void setIdProof(int val);

	@Getter(MANAGER)
	@AuthenticatorGetter(patternID = PATTERN_ID)
	IAuthenticator getManager();

	@Setter(MANAGER)
	void setManager(IAuthenticator val);

	@AuthenticateMethod(patternID = PATTERN_ID)
	void authenticate();

	@RequiresAuthentication
	public void thisMethodRequiresToBeAuthenticated();

	abstract class SubjectImp implements Subject {

		@Override
		public void thisMethodRequiresToBeAuthenticated() {
			System.out.println("I need to be authenticated to execute this");
		}
	}
}
