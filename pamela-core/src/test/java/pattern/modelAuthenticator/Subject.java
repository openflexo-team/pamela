package pattern.modelAuthenticator;

import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.factory.AccessibleProxyObject;
import org.openflexo.pamela.patterns.authenticator.annotations.AuthenticateMethod;
import org.openflexo.pamela.patterns.authenticator.annotations.AuthenticationInformation;
import org.openflexo.pamela.patterns.authenticator.annotations.AuthenticatorSubject;
import org.openflexo.pamela.patterns.authenticator.annotations.ProofOfIdentity;

@ModelEntity
@AuthenticatorSubject(patternID = Subject.PATTERN_ID)
public interface Subject extends AccessibleProxyObject {
    String PATTERN_ID = "patternID";
    String AUTH_INFO = "auth_info1";
    String AUTH_INFO_BIS = "auth_info2";
    String MANAGER = "manager";
    String ID_PROOF = "id_proof";

    default void init(IAuthenticator manager, String id) {setManager(manager);setAuthInfo(id);}

    @Getter(value = AUTH_INFO, defaultValue = AUTH_INFO)
    @AuthenticationInformation(patternID = PATTERN_ID, paramID = IAuthenticator.ID)
    String getAuthInfo();
    @Setter(AUTH_INFO)
    void setAuthInfo(String val);

    @Getter(value = ID_PROOF, defaultValue = "-1")
    int getIDProof();
    @Setter(ID_PROOF)
    @ProofOfIdentity(patternID = PATTERN_ID)
    void setIdProof(int val);

    @Getter(MANAGER)
    IAuthenticator getManager();
    @Setter(MANAGER)
    void setManager(IAuthenticator val);

    @AuthenticateMethod(patternID = PATTERN_ID, authenticator = "getManager")
    void authenticate();

}
