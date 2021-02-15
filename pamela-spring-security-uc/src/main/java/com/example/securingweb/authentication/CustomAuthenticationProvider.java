package com.example.securingweb.authentication;

import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.patterns.PropertyParadigmType;
import org.openflexo.pamela.patterns.annotations.Requires;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticationInformation;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.Authenticator;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.RequestAuthentication;
import com.example.securingweb.patterns.TooManyLoginAttemptsException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
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
	@Requires(
			patternID = SessionInfo.PATTERN_ID,
			type = PropertyParadigmType.TemporalLogic,
			property = "assert always auth_fail[*3] & time_limit<3min @ (auth_fail)", exceptionWhenViolated = TooManyLoginAttemptsException.class)

	public Authentication authenticate(Authentication authentication) throws AuthenticationException;

	@RequestAuthentication(patternID = SessionInfo.PATTERN_ID)
	int request(@AuthenticationInformation(patternID = SessionInfo.PATTERN_ID, paramID = USER_NAME) String userName);

	abstract class CustomAuthenticationProviderImpl extends DaoAuthenticationProvider implements CustomAuthenticationProvider {

		@Override
		public Authentication authenticate(Authentication authentication) throws AuthenticationException {

			try {
				System.out.println("On utilise bien le CustomAuthenticationProvider pour " + authentication);
				Thread.dumpStack();
				UsernamePasswordAuthenticationToken returned = (UsernamePasswordAuthenticationToken) super.authenticate(authentication);
				String name = authentication.getName();
				String password = authentication.getCredentials().toString();
				WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
				String userIp = details.getRemoteAddress();
				Authentication authenticationIP = authentication;
				SessionInfo.getCurrentSessionInfo().setUserName(name);
				SessionInfo.getCurrentSessionInfo().setIpAdress(userIp);
				System.out.println("Current session info: " + SessionInfo.getCurrentSessionInfo());
				return returned;
			} catch (AuthenticationException e) {
				throw e;
			} catch (ModelExecutionException e) {
				e.printStackTrace();
				System.out.println("Oulala ca craint");
				// return null;
				throw new SessionAuthenticationException("Cannot open more than one session for a given user");
			}

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

	}

}
