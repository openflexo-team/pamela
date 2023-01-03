package com.example.securingweb.authentication;

import java.util.HashMap;
import java.util.Map;

import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.patterns.annotations.RaiseEventOnException;
import org.openflexo.pamela.patterns.annotations.Requires;
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

import com.example.securingweb.patterns.AuthFailedEvent;

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
			// type = PropertyParadigmType.TemporalLogic,
			// property = "assert always auth_fail[*3] & time_limit<3min @ (auth_fail)"
			property = "patternInstance.checkAuthFailCount()"
	/*,exceptionWhenViolated = TooManyLoginAttemptsException.class*/)
	// @Requires(patternID = SessionInfo.PATTERN_ID, type = PropertyParadigmType.TemporalLogic, property = "assert always not(a)[*0:10];a")
	// Another idea :
	// event e1,e2,e3
	// {
	// event e = generateEvent();
	// if (e1 != null) then assert (e.time - e1.time < 1h);
	// if (e1 == null) e1 <- e; else e1 <- e2;
	// if (e2 == null) e2 <- e; else e2 <- e3;
	// if (e3 == null) e3 <- e;
	// }
	@RaiseEventOnException(patternID = SessionInfo.PATTERN_ID, onException = AuthenticationException.class, generateEvent = AuthFailedEvent.class)
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
