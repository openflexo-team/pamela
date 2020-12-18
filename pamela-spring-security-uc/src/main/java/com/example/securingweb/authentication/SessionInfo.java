package com.example.securingweb.authentication;

import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;
import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpSession;

import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticateMethod;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticationInformation;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticatorGetter;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticatorSubject;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.ProofOfIdentitySetter;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Scope(value = SCOPE_SESSION, proxyMode = TARGET_CLASS)
@ModelEntity
@ImplementationClass(SessionInfo.SessionInfoImpl.class)
@AuthenticatorSubject(patternID = SessionInfo.PATTERN_ID)
public interface SessionInfo {

	String SESSION_INFO = "SESSION_INFO";
	String PATTERN_ID = "AuthenticatorPattern";
	String USER_NAME = "username";
	String AUTHENTICATION_PROVIDER = "authenticationProvider";
	String ID_PROOF = "idProof";

	@Getter(value = USER_NAME)
	@AuthenticationInformation(patternID = PATTERN_ID, paramID = CustomAuthenticationProvider.USER_NAME)
	String getUserName();

	@Setter(USER_NAME)
	void setUserName(String val);

	@Getter(value = ID_PROOF, ignoreType = true)
	UsernamePasswordAuthenticationToken getIDProof();

	@Setter(ID_PROOF)
	@ProofOfIdentitySetter(patternID = PATTERN_ID)
	void setIdProof(UsernamePasswordAuthenticationToken value);

	@Getter(AUTHENTICATION_PROVIDER)
	@AuthenticatorGetter(patternID = PATTERN_ID)
	CustomAuthenticationProvider getAuthenticationProvider();

	@Setter(AUTHENTICATION_PROVIDER)
	void setAuthenticationProvider(CustomAuthenticationProvider val);

	@AuthenticateMethod(patternID = PATTERN_ID)
	void authenticate();

	@Override
	public String toString();

	abstract class SessionInfoImpl implements SessionInfo {

		private final String created;

		public SessionInfoImpl() {
			this.created = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
			System.out.println("On cree une nouvelle SessionInfo ");
			// Thread.dumpStack();
		}

		@Override
		public String toString() {
			return "SessionInfo userName=" + getUserName() + " (created on " + created + ")";
		}

	}

	public static SessionInfo getCurrentSessionInfo() {
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpSession session = attr.getRequest().getSession(true);
		return (SessionInfo) session.getAttribute(SessionInfo.SESSION_INFO);
	}

}
