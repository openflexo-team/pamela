package org.openflexo.pamela.securitypatterns.authenticator.model2;

import java.util.ArrayList;
import java.util.List;

import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticationInformation;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.Authenticator;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.RequestAuthentication;

@ModelEntity
@Authenticator(patternID = MySubject.PATTERN_ID)
public class MyAuthenticator {

	public static final String ID = "id";

	private List<String> users;

	public MyAuthenticator() {
		users = new ArrayList<String>();
	}

	public List<String> getUsers() {
		return users;
	}

	public void addUser(String val) {
		users.add(val);
	}

	public void removeUser(String val) {
		users.remove(val);
	}

	@RequestAuthentication(patternID = MySubject.PATTERN_ID)
	public int request(@AuthenticationInformation(patternID = MySubject.PATTERN_ID, paramID = ID) String id) {
		if (this.check(id)) {
			return this.generateFromAuthInfo(id);
		}
		return this.getDefaultToken();
	}

	private boolean check(String id) {
		for (String userID : getUsers()) {
			if (userID.compareTo(id) == 0)
				return true;
		}
		return false;
	}

	public int generateFromAuthInfo(String id) {
		return id.hashCode();
	}

	public int getDefaultToken() {
		return -42;
	}

}
