package com.example.securingweb.authentication;

import java.util.HashMap;
import java.util.Map;

import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.patterns.annotations.Ensures;
import org.openflexo.pamela.patterns.annotations.OnException;
import org.openflexo.pamela.patterns.annotations.OnException.OnExceptionStategy;
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
	@OnException(
			patternID = SessionInfo.PATTERN_ID,
			onException = AuthenticationException.class,
			perform = "patternInstance.generateAuthFailEvent()",
			strategy = OnExceptionStategy.HandleAndRethrowException)
	@Ensures(patternID = SessionInfo.PATTERN_ID, property = "patternInstance.checkRecentAuthFailCountLessThan3()")
	public Authentication authenticate(Authentication authentication) throws AuthenticationException;

	/**
	 * This method retrieve proof of identity given supplied authentication information
	 * 
	 * @param userName
	 * @return
	 */
	@RequestAuthentication(patternID = SessionInfo.PATTERN_ID)
	UsernamePasswordAuthenticationToken request(
			@AuthenticationInformation(patternID = SessionInfo.PATTERN_ID, paramID = USER_NAME) String userName);

	abstract class CustomAuthenticationProviderImpl extends DaoAuthenticationProvider implements CustomAuthenticationProvider {

		private Map<String, UsernamePasswordAuthenticationToken> tokens = new HashMap<>();

		/**
		 * Implementation is here trivial as we use map filled by {@link #authenticate(Authentication)} method
		 */
		@Override
		public UsernamePasswordAuthenticationToken request(String userName) {
			return tokens.get(userName);
		}

		@Override
		public Authentication authenticate(Authentication authentication) throws AuthenticationException {

			try {
				System.out.println("authenticate(Authentication) called for " + authentication);
				UsernamePasswordAuthenticationToken returned = (UsernamePasswordAuthenticationToken) super.authenticate(authentication);
				String name = authentication.getName();
				// String password = authentication.getCredentials().toString();
				WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
				String userIp = details.getRemoteAddress();
				SessionInfo sessionInfo = SessionInfo.getCurrentSessionInfo();
				sessionInfo.setUserName(name);
				sessionInfo.setIpAdress(userIp);
				tokens.put(name, returned);
				System.out.println("Current session info: " + sessionInfo);
				sessionInfo.authenticate();
				// Ensure that we are now in authenticated context
				sessionInfo.checkSecure();
				return returned;
			} catch (AuthenticationException e) {
				throw e;
			} catch (ModelExecutionException e) {
				e.printStackTrace();
				throw new SessionAuthenticationException("Exception during authentication: " + e.getMessage());
			}

		}

	}

}
