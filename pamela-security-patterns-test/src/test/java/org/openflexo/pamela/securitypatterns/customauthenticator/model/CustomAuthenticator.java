package org.openflexo.pamela.securitypatterns.customauthenticator.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.patterns.annotations.Ensures;
import org.openflexo.pamela.patterns.annotations.OnException;
import org.openflexo.pamela.patterns.annotations.OnException.OnExceptionStategy;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticationInformation;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.Authenticator;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.RequestAuthentication;

@ModelEntity
@Authenticator(patternID = Subject.PATTERN_ID)
public class CustomAuthenticator {

	private Map<String, String> users;

	public CustomAuthenticator() {
		users = new HashMap<>();
	}

	public Map<String, String> getUsers() {
		return users;
	}

	public void addUser(String login, String password) {
		users.put(login, password);
	}

	public void removeUser(String login) {
		users.remove(login);
	}

	@RequestAuthentication(patternID = Subject.PATTERN_ID)
	public int request(@AuthenticationInformation(patternID = Subject.PATTERN_ID, paramID = Subject.LOGIN) String login,
			@AuthenticationInformation(patternID = Subject.PATTERN_ID, paramID = Subject.PASSWORD) String password)
			throws UnknownLoginException, InvalidPasswordException {

		try {
			checkLoginExists(login);
			checkPasswordMatches(login, password);
			return this.generateFromAuthInfo(login, password);
		} catch (UnknownLoginException e) {
			// Unknown user
			e.printStackTrace();
		} catch (InvalidPasswordException e) {
			// Invalid login/password couple
			e.printStackTrace();
		}
		return this.getDefaultToken();
	}

	@Ensures(patternID = Subject.PATTERN_ID, property = "checkRecentAuthFailCountLessThan3()")
	@OnException(
			patternID = Subject.PATTERN_ID,
			onException = UnknownLoginException.class,
			perform = "generateAuthFailEvent()",
			strategy = OnExceptionStategy.HandleAndRethrowException)
	public void checkLoginExists(String login) throws UnknownLoginException {
		if (getUsers().get(login) == null) {
			throw new UnknownLoginException("Unknown user: " + login);
		}
	}

	@Ensures(patternID = Subject.PATTERN_ID, property = "checkRecentAuthFailCountLessThan3()")
	@OnException(
			patternID = Subject.PATTERN_ID,
			onException = InvalidPasswordException.class,
			perform = "generateAuthFailEvent()",
			strategy = OnExceptionStategy.HandleAndRethrowException)
	public void checkPasswordMatches(String login, String password) throws InvalidPasswordException {
		if (getUsers().get(login) != null && !getUsers().get(login).equals(password)) {
			throw new InvalidPasswordException("Invalid password for user: " + login);
		}
	}

	public int generateFromAuthInfo(String login, String password) throws UnknownLoginException {
		// System.out.println("----------> Hop pour " + login + "/" + password);
		return Objects.hash(login, password);
	}

	public int getDefaultToken() {
		return -42;
	}

	// @Requires(patternID = Subject.PATTERN_ID, property = "prout")
	public void aMethodGuardedWithAPrecondition() {
		System.out.println("aMethodGuardedWithAPrecondition()");
	}

}
