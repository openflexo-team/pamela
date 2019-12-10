package pattern.modelAuthenticator;

import org.openflexo.pamela.annotations.*;
import org.openflexo.pamela.patterns.authenticator.annotations.AuthenticationInformation;
import org.openflexo.pamela.patterns.authenticator.annotations.Authenticator;
import org.openflexo.pamela.patterns.authenticator.annotations.RequestAuthentication;

import java.util.ArrayList;
import java.util.List;

@ModelEntity
@ImplementationClass(IAuthenticator.AuthenticatorImp.class)
@Authenticator(patternID = Subject.PATTERN_ID)
public interface IAuthenticator {
    String LIST = "list";
    String ID = "id";

    @Initializer
    default void init() {setList(new ArrayList<>());}

    @Getter(value = LIST, cardinality = Getter.Cardinality.LIST)
    List<String> getList();
    @Setter(LIST)
    void setList(List<String> val);
    @Adder(LIST)
    void addUser(String val);
    @Remover(LIST)
    void removeUser(String val);

    @RequestAuthentication(patternID = Subject.PATTERN_ID)
    int request(@AuthenticationInformation(patternID = Subject.PATTERN_ID, paramID = ID) String id);

    int generateFromAuthInfo(String id);

    default int getDefaultToken() {return -42;}

    abstract class AuthenticatorImp implements IAuthenticator {
        @Override
        public int request(String id){
            if (this.check(id)){
                return this.generateFromAuthInfo(id);
            }
            return this.getDefaultToken();
        }

        private boolean check(String id){
            for (String userID : this.getList()){
                if (userID.compareTo(id) == 0) return true;
            }
            return false;
        }

        @Override
        public int generateFromAuthInfo(String id){
            return id.hashCode();
        }
    }


}
