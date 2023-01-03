package org.openflexo.pamela.securitypatterns.authenticator.model;

import java.util.ArrayList;
import java.util.List;

import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.patterns.annotations.Requires;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticationInformation;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.Authenticator;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.RequestAuthentication;

@ModelEntity
@ImplementationClass(IAuthenticator.AuthenticatorImp.class)
@Authenticator(patternID = Subject.PATTERN_ID)
public interface IAuthenticator {
	String USERS = "users";
	String ID = "id";

	@Initializer
	default void init() {
		setUsers(new ArrayList<>());
	}

	@Getter(value = USERS, cardinality = Getter.Cardinality.LIST)
	List<String> getUsers();

	@Setter(USERS)
	void setUsers(List<String> users);

	@Adder(USERS)
	void addUser(String val);

	@Remover(USERS)
	void removeUser(String val);

	@Requires(patternID = Subject.PATTERN_ID, /*type = PropertyParadigmType.Java,*/ property = "prout")
	public void aMethodGuardedWithAPrecondition();

	@RequestAuthentication(patternID = Subject.PATTERN_ID)
	int request(@AuthenticationInformation(patternID = Subject.PATTERN_ID, paramID = ID) String id);

	int generateFromAuthInfo(String id);

	default int getDefaultToken() {
		return -42;
	}

	abstract class AuthenticatorImp implements IAuthenticator {
		@Override
		public int request(String id) {
			if (this.check(id)) {
				return this.generateFromAuthInfo(id);
			}
			return this.getDefaultToken();
		}

		private boolean check(String id) {
			for (String userID : this.getUsers()) {
				if (userID.compareTo(id) == 0)
					return true;
			}
			return false;
		}

		@Override
		public int generateFromAuthInfo(String id) {
			return id.hashCode();
		}

		@Override
		public void aMethodGuardedWithAPrecondition() {
			System.out.println("aMethodGuardedWithAPrecondition");
		}

	}

}
