package org.openflexo.pamela.securitypatterns.authenticator;

import org.junit.Test;
import org.openflexo.pamela.securitypatterns.authenticator.model2.MyAuthenticator;
import org.openflexo.pamela.securitypatterns.authenticator.model2.MySubject;

import junit.framework.TestCase;

public class TestUnsecuredAuthenticator extends TestCase {

	// Functional test
	@Test
	public void testAuthenticateValid() throws Exception {
		MyAuthenticator manager = new MyAuthenticator();
		MySubject subject = new MySubject(manager, "id1");
		manager.addUser(subject.getAuthInfo());
		subject.authenticate();
		assertEquals(subject.getIDProof(), manager.generateFromAuthInfo(subject.getAuthInfo()));
		System.out.println("IDProof=" + subject.getIDProof());
	}

	@Test
	public void testRequiresAuthentication() throws Exception {
		MyAuthenticator manager = new MyAuthenticator();
		MySubject subject = new MySubject(manager, "id1");
		manager.addUser(subject.getAuthInfo());
		// We haven't call the authenticate() method, but this method is tagged with "@RequiresAuthentication", thus this call the
		// authenticate() method
		subject.thisMethodRequiresToBeAuthenticated();
		// We should NOT be able to call this, but we did it
		assertTrue(subject.getAuthenticatedMethodHasBeenSuccessfullyCalled());
	}

	@Test
	public void testAuthenticatorInvalidReturn() throws Exception {
		MyAuthenticator manager = new MyAuthenticator();
		MySubject subject = new MySubject(manager, "id1");
		subject.authenticate();
		// We are not authenticated: this is the default token
		assertEquals(subject.getIDProof(), manager.getDefaultToken());
	}

	@Test
	public void testAuthInfoUniqueness() throws Exception {
		MyAuthenticator manager = new MyAuthenticator();
		MySubject subject = new MySubject(manager, "id");
		subject.getAuthInfo();
		MySubject subject2 = new MySubject(manager, "id2");
		subject2.getAuthInfo();
		MySubject subject3 = new MySubject(manager, "id");
		// This should raise an exception, because this authInfo is not unique
		// Unsecure code
		subject3.getAuthInfo();
	}

	@Test
	public void testAuthenticatorInvariant() throws Exception {
		MyAuthenticator manager = new MyAuthenticator();
		MySubject subject = new MySubject(manager, "id");
		manager.addUser(subject.getAuthInfo());
		subject.setManager(manager);
		// This should raise an exception, because this authInfo is not unique
		// Unsecure code
		subject.setManager(new MyAuthenticator());
	}

	@Test
	public void testAuthInfoInvariant() throws Exception {
		MyAuthenticator manager = new MyAuthenticator();
		MySubject subject = new MySubject(manager, "id");
		subject.setManager(manager);
		subject.setAuthInfo("id");
		// This should raise an exception, because this authInfo is not unique
		// Unsecure code
		subject.setAuthInfo("id2");
	}

	@Test
	public void testIdProofForgery() throws Exception {
		MyAuthenticator manager = new MyAuthenticator();
		MySubject subject = new MySubject(manager, "id");
		subject.setIDProof(-1);
		subject.authenticate();
		subject.setIDProof(subject.getIDProof());
		// This should raise an exception, because this authInfo is not unique
		// Unsecure code
		subject.setIDProof(subject.getIDProof() + 1);
	}

}
