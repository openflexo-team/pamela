package com.example.securingweb.authentication;

import java.util.HashMap;
import java.util.Map;

import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticationInformation;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.Authenticator;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.RequestAuthentication;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;

@Component
@ModelEntity
@ImplementationClass(CustomAuthenticationProvider.CustomAuthenticationProviderImpl.class)
@Authenticator(patternID = SessionInfo.PATTERN_ID)
@Imports(@Import(SessionInfo.class))
public interface CustomAuthenticationProvider extends AuthenticationProvider {
	String USER_NAME = "userName";

	public void setPasswordEncoder(PasswordEncoder passwordEncoder);

	public void setUserDetailsService(UserDetailsService userDetailsService);

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException;

	@RequestAuthentication(patternID = SessionInfo.PATTERN_ID)
	UsernamePasswordAuthenticationToken request(
			@AuthenticationInformation(patternID = SessionInfo.PATTERN_ID, paramID = USER_NAME) String userName);

	abstract class CustomAuthenticationProviderImpl extends DaoAuthenticationProvider implements CustomAuthenticationProvider {

		private Map<String, UsernamePasswordAuthenticationToken> tokens = new HashMap<>();

		@Override
		public Authentication authenticate(Authentication authentication) throws AuthenticationException {

			try {
				System.out.println("On utilise bien le CustomAuthenticationProvider pour " + authentication);
				Thread.dumpStack();

				UsernamePasswordAuthenticationToken returned = (UsernamePasswordAuthenticationToken) super.authenticate(authentication);

				SessionInfo sessionInfo = SessionInfo.getCurrentSessionInfo();
				System.out.println("Current session info: " + sessionInfo);

				String name = authentication.getName();
				String password = authentication.getCredentials().toString();
				tokens.put(name, returned);

				sessionInfo.setUserName(name);
				sessionInfo.authenticate();

				return returned;
			} catch (AuthenticationException e) {
				throw e;
			} catch (ModelExecutionException e) {
				e.printStackTrace();
				System.out.println("Oulala ca craint");
				// return null;
				throw new SessionAuthenticationException("Cannot open more than one session for a given user");
			}

			/*try {
				System.out.println("On utilise bien le CustomAuthenticationProvider pour " + authentication);
				Thread.dumpStack();
			
				String name = authentication.getName();
				String password = authentication.getCredentials().toString();
			
				SessionInfo.getCurrentSessionInfo().setUserName(name);
				System.out.println("Current session info: " + SessionInfo.getCurrentSessionInfo());
			
				if (shouldAuthenticateAgainstThirdPartySystem()) {
			
					// use the credentials
					// and authenticate against the third-party system
					return new UsernamePasswordAuthenticationToken(name, password, new ArrayList<>());
				}
				else {
					return null;
				}
			} catch (ModelExecutionException e) {
				e.printStackTrace();
				System.out.println("Oulala ca craint");
				// return null;
				throw new SessionAuthenticationException("Cannot open more than one session for a given user");
			}*/
		}

		/*private boolean shouldAuthenticateAgainstThirdPartySystem() {
			return true;
		}*/

		/*@Override
		public boolean supports(Class<?> authentication) {
			return authentication.equals(UsernamePasswordAuthenticationToken.class);
		}*/

		/*
		 * @Override public int request(String id) { if (this.check(id)) { return
		 * this.generateFromAuthInfo(id); } return this.getDefaultToken(); }
		 * 
		 * private boolean check(String id) { for (String userID : this.getUsers()) { if
		 * (userID.compareTo(id) == 0) return true; } return false; }
		 * 
		 * @Override public int generateFromAuthInfo(String id) { return id.hashCode();
		 * }
		 */

		@Override
		public UsernamePasswordAuthenticationToken request(String userName) {
			System.out.println(">>>>>>>>> On fait un request pour " + userName);
			return tokens.get(userName);
		}
	}

}
