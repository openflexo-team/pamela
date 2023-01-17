package org.openflexo.pamela.securitypatterns.customauthenticator;

import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.factory.PamelaModel;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.patterns.PostconditionViolationException;
import org.openflexo.pamela.securitypatterns.authenticator.AuthenticatorPatternDefinition;
import org.openflexo.pamela.securitypatterns.customauthenticator.model.CustomAuthenticator;
import org.openflexo.pamela.securitypatterns.customauthenticator.model.CustomAuthenticatorPatternInstance;
import org.openflexo.pamela.securitypatterns.customauthenticator.model.Subject;

import junit.framework.TestCase;

public class TestCustomAuthenticator extends TestCase {

	@Test
	public void testPatternAnalysis() throws Exception {
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(Subject.class, CustomAuthenticator.class);
		assertEquals(1, metaModel.getPatternDefinitions(AuthenticatorPatternDefinition.class).size());
		AuthenticatorPatternDefinition patternDefinition = metaModel.getPatternDefinitions(AuthenticatorPatternDefinition.class).get(0);
		assertEquals(Subject.PATTERN_ID, patternDefinition.getIdentifier());

		assertEquals(CustomAuthenticator.class, patternDefinition.authenticatorModelEntity.getImplementedInterface());
		assertEquals(CustomAuthenticator.class.getMethod("request", String.class, String.class),
				patternDefinition.requestAuthentificationMethod);

		assertEquals(Subject.class, patternDefinition.subjectModelEntity.getImplementedInterface());
		assertEquals(2, patternDefinition.authentificationInfoMethods.size());
		assertTrue(patternDefinition.authentificationInfoMethods.contains(Subject.class.getMethod("getLogin")));
		assertTrue(patternDefinition.authentificationInfoMethods.contains(Subject.class.getMethod("getPassword")));
		assertEquals(1, patternDefinition.authentificationInfoUniqueKeyMethods.size());
		assertEquals(Subject.class.getMethod("getLogin"), patternDefinition.authentificationInfoUniqueKeyMethods.get(0));
		assertEquals(Subject.class.getMethod("setIDProof", int.class), patternDefinition.proofOfIdentitySetterMethod);
		assertEquals(Subject.class.getMethod("getAuthenticator"), patternDefinition.authenticatorGetterMethod);
		assertEquals(Subject.class.getMethod("authenticate"), patternDefinition.authenticateMethod);

	}

	@Test
	public void testAuthenticateValid() throws Exception {
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(Subject.class, CustomAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		CustomAuthenticator authenticator = factory.newInstance(CustomAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, "id1", "passwd1");
		subject.setAuthenticator(authenticator);
		authenticator.addUser(subject.getLogin(), subject.getPassword());
		subject.authenticate();
		assertEquals(subject.getIDProof(), authenticator.generateFromAuthInfo(subject.getLogin(), subject.getPassword()));
		System.out.println("IDProof=" + subject.getIDProof());
	}

	@Test
	public void testAuthenticateInvalid() throws Exception {
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(Subject.class, CustomAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		CustomAuthenticator authenticator = factory.newInstance(CustomAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, "id1", "passwd1");
		subject.setAuthenticator(authenticator);
		authenticator.addUser(subject.getLogin(), "anOtherPassword");
		subject.authenticate();
		assertNotEquals(subject.getIDProof(), authenticator.generateFromAuthInfo(subject.getLogin(), subject.getPassword()));
		System.out.println("IDProof=" + subject.getIDProof());
	}

	@Test
	public void testRequiresAuthentication() throws Exception {
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(Subject.class, CustomAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		CustomAuthenticator authenticator = factory.newInstance(CustomAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, "id1", "passwd1");
		subject.setAuthenticator(authenticator);
		authenticator.addUser(subject.getLogin(), subject.getPassword());
		// We haven't call the authenticate() method, but this method is tagged with "@RequiresAuthentication", thus this call the
		// authenticate() method
		subject.thisMethodRequiresToBeAuthenticated();
		assertEquals(subject.getIDProof(), authenticator.generateFromAuthInfo(subject.getLogin(), subject.getPassword()));
	}

	@Test
	public void testAuthenticatorInvalidReturn() throws Exception {
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(Subject.class, CustomAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		CustomAuthenticator authenticator = factory.newInstance(CustomAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, "id", "passwd");
		subject.setAuthenticator(authenticator);
		subject.authenticate();
		assertEquals(subject.getIDProof(), authenticator.getDefaultToken());
	}

	@Test
	public void testInstanceDiscovery() throws Exception {
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(Subject.class, CustomAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		PamelaModel model = factory.getModel();
		CustomAuthenticator authenticator = factory.newInstance(CustomAuthenticator.class);
		assertNull(model.getPatternInstances(authenticator));
		Subject subject = factory.newInstance(Subject.class, "id", "passwd");
		assertNull(model.getPatternInstances(authenticator));
		assertEquals(1, model.getPatternInstances(subject).size());
		subject.setAuthenticator(authenticator);
		assertEquals(1, model.getPatternInstances(authenticator).size());
		assertEquals(1, model.getPatternInstances(subject).size());
		assertSame(model.getPatternInstances(authenticator).iterator().next(), model.getPatternInstances(subject).iterator().next());
	}

	@Test
	public void testAuthInfoUniqueness() throws Exception {
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(Subject.class, CustomAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		CustomAuthenticator authenticator = factory.newInstance(CustomAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, authenticator, "id", "passwd");
		Subject subject2 = factory.newInstance(Subject.class, authenticator, "id2", "passwd2");
		try {
			Subject subject3 = factory.newInstance(Subject.class, authenticator, "id", "passwd");
			fail();
		} catch (ModelExecutionException e) {
			System.out.println("Donc " + e.getMessage());
			assertTrue(e.getMessage().contains("Subject Invariant Violation: Authentication information are not unique"));
		} catch (Exception e2) {
			System.out.println("Ben alors ??? avec " + e2);
			fail();

		}
	}

	@Test
	public void testAuthenticatorInvariant() throws Exception {
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(Subject.class, CustomAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		CustomAuthenticator authenticator = factory.newInstance(CustomAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, authenticator, "id", "passwd");
		subject.setAuthenticator(authenticator);
		try {
			subject.setAuthenticator(factory.newInstance(CustomAuthenticator.class));
			fail();
		} catch (ModelExecutionException e) {
			e.printStackTrace();
			if (e.getMessage().compareTo("Subject Invariant Violation: Authenticator has changed since initialization") != 0) {
				fail();
			}
		}
	}

	@Test
	public void testAuthInfoInvariant() throws Exception {
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(Subject.class, CustomAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		CustomAuthenticator authenticator = factory.newInstance(CustomAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, authenticator, "id", "passwd");
		subject.setLogin("id");
		try {
			subject.setLogin(null);
			fail();
		} catch (ModelExecutionException e) {
			e.printStackTrace();
			if (e.getMessage().compareTo("Subject Invariant Violation: Authentication Information has changed since initialization") != 0) {
				fail();
			}
		}
	}

	@Test
	public void testIdProofForgery() throws Exception {
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(Subject.class, CustomAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		CustomAuthenticator authenticator = factory.newInstance(CustomAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, authenticator, "id", "passwd");
		subject.setIDProof(-1);
		subject.authenticate();
		subject.setIDProof(subject.getIDProof());
		try {
			subject.setIDProof(subject.getIDProof() + 1);
			fail();
		} catch (ModelExecutionException e) {
			e.printStackTrace();
			if (e.getMessage().compareTo("Subject Invariant Violation: Proof of identity has been forged") != 0) {
				fail();
			}
		}
	}

	@Test
	public void testInvariantValidityWithDynamicPrivilegeRules() throws Exception {
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(Subject.class, CustomAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		CustomAuthenticator authenticator = factory.newInstance(CustomAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, authenticator, "id", "passwd");
		subject.authenticate();
		assertEquals(subject.getIDProof(), authenticator.getDefaultToken());
		authenticator.addUser(subject.getLogin(), subject.getPassword());
		subject.getLogin();
		subject.authenticate();
	}

	@Test
	public void testCoucou() throws Exception {
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(Subject.class, CustomAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		CustomAuthenticator authenticator = factory.newInstance(CustomAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, "id", "passwd");
		subject.setAuthenticator(authenticator);
		// TODO: write a test
		authenticator.aMethodGuardedWithAPrecondition();
	}

	@Test
	public void testMultipleInvalidAuthentication() throws Exception {
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(Subject.class, CustomAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		CustomAuthenticator authenticator = factory.newInstance(CustomAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, "id1", "passwd1");
		subject.setAuthenticator(authenticator);
		authenticator.addUser(subject.getLogin(), "anOtherPassword");
		subject.authenticate();
		subject.authenticate();
		try {
			subject.authenticate();
			fail("This request should raise a ModelExecutionException(PostconditionViolationException)");
		} catch (ModelExecutionException e) {
			if (e.getCause() instanceof PostconditionViolationException) {
				// Success
			}
			else {
				e.printStackTrace();
				fail("This request should raise a PostconditionViolationException, not a " + e);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("This request should raise a ModelExecutionException, not a " + e.getCause().getClass());
		}

		// We wait
		Thread.sleep(CustomAuthenticatorPatternInstance.TIME_LIMIT + 100);
		// This should be still possible, but password still invalid
		subject.authenticate();
		System.out.println("IDProof=" + subject.getIDProof());
		assertNotEquals(subject.getIDProof(), authenticator.generateFromAuthInfo(subject.getLogin(), subject.getPassword()));

		// Set right password and re-authenticate
		subject.setPassword("anOtherPassword");
		subject.authenticate();
		System.out.println("IDProof=" + subject.getIDProof());
		assertEquals(subject.getIDProof(), authenticator.generateFromAuthInfo(subject.getLogin(), subject.getPassword()));

	}

}
