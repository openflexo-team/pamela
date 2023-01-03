package org.openflexo.pamela.securitypatterns.customauthenticator;

import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.factory.PamelaModel;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.securitypatterns.authenticator.AuthenticatorPatternDefinition;
import org.openflexo.pamela.securitypatterns.customauthenticator.model.CustomAuthenticator;
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
		assertEquals(CustomAuthenticator.class.getMethod("request", String.class), patternDefinition.requestAuthentificationMethod);

		assertEquals(Subject.class, patternDefinition.subjectModelEntity.getImplementedInterface());
		assertEquals(Subject.class.getMethod("getAuthInfo"), patternDefinition.authentificationInfoMethod);
		assertEquals(Subject.class.getMethod("setIDProof", int.class), patternDefinition.proofOfIdentitySetterMethod);
		assertEquals(Subject.class.getMethod("getAuthenticator"), patternDefinition.authenticatorGetterMethod);
		assertEquals(Subject.class.getMethod("authenticate"), patternDefinition.authenticateMethod);

	}

	@Test
	public void testAuthenticateValid() throws Exception {
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(Subject.class, CustomAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		CustomAuthenticator authenticator = factory.newInstance(CustomAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, "id1");
		subject.setAuthenticator(authenticator);
		authenticator.addUser(subject.getAuthInfo());
		subject.authenticate();
		assertEquals(subject.getIDProof(), authenticator.generateFromAuthInfo(subject.getAuthInfo()));
		System.out.println("IDProof=" + subject.getIDProof());
	}

	@Test
	public void testRequiresAuthentication() throws Exception {
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(Subject.class, CustomAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		CustomAuthenticator authenticator = factory.newInstance(CustomAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, "id1");
		subject.setAuthenticator(authenticator);
		authenticator.addUser(subject.getAuthInfo());
		// We haven't call the authenticate() method, but this method is tagged with "@RequiresAuthentication", thus this call the
		// authenticate() method
		subject.thisMethodRequiresToBeAuthenticated();
		assertEquals(subject.getIDProof(), authenticator.generateFromAuthInfo(subject.getAuthInfo()));
	}

	@Test
	public void testAuthenticatorInvalidReturn() throws Exception {
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(Subject.class, CustomAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		CustomAuthenticator authenticator = factory.newInstance(CustomAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, "id");
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
		Subject subject = factory.newInstance(Subject.class, "id");
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
		Subject subject = factory.newInstance(Subject.class, authenticator, "id");
		Subject subject2 = factory.newInstance(Subject.class, authenticator, "id2");
		try {
			Subject subject3 = factory.newInstance(Subject.class, authenticator, "id");
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
		Subject subject = factory.newInstance(Subject.class, authenticator, "id");
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
		Subject subject = factory.newInstance(Subject.class, authenticator, "id");
		subject.setAuthInfo("id");
		try {
			subject.setAuthInfo(null);
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
		Subject subject = factory.newInstance(Subject.class, authenticator, "id");
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
		Subject subject = factory.newInstance(Subject.class, authenticator, "id");
		subject.authenticate();
		assertEquals(subject.getIDProof(), authenticator.getDefaultToken());
		authenticator.addUser(subject.getAuthInfo());
		subject.getAuthInfo();
		subject.authenticate();
	}

	@Test
	public void testCoucou() throws Exception {
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(Subject.class, CustomAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		CustomAuthenticator authenticator = factory.newInstance(CustomAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, "id");
		subject.setAuthenticator(authenticator);
		// TODO: write a test
		authenticator.aMethodGuardedWithAPrecondition();
	}

}
