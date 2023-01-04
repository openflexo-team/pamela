package org.openflexo.pamela.securitypatterns.authenticator;

import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.factory.PamelaModel;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.securitypatterns.authenticator.model.IAuthenticator;
import org.openflexo.pamela.securitypatterns.authenticator.model.Subject;

import junit.framework.TestCase;

public class TestAuthenticator extends TestCase {

	@Test
	public void testPatternAnalysis() throws Exception {
		PamelaMetaModel context = new PamelaMetaModel(Subject.class);
		assertEquals(1, context.getPatternDefinitions(AuthenticatorPatternDefinition.class).size());
		AuthenticatorPatternDefinition patternDefinition = context.getPatternDefinitions(AuthenticatorPatternDefinition.class).get(0);
		assertEquals(Subject.PATTERN_ID, patternDefinition.getIdentifier());

		assertEquals(IAuthenticator.class, patternDefinition.authenticatorModelEntity.getImplementedInterface());
		assertEquals(IAuthenticator.class.getMethod("request", String.class), patternDefinition.requestAuthentificationMethod);

		assertEquals(Subject.class, patternDefinition.subjectModelEntity.getImplementedInterface());
		assertEquals(Subject.class.getMethod("getAuthInfo"), patternDefinition.authentificationInfoMethods.get(0));
		assertEquals(Subject.class.getMethod("setIdProof", int.class), patternDefinition.proofOfIdentitySetterMethod);
		assertEquals(Subject.class.getMethod("getManager"), patternDefinition.authenticatorGetterMethod);
		assertEquals(Subject.class.getMethod("authenticate"), patternDefinition.authenticateMethod);

	}

	@Test
	public void testAuthenticateValid() throws Exception {
		PamelaMetaModel context = new PamelaMetaModel(Subject.class);
		PamelaModelFactory factory = new PamelaModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, "id1");
		subject.setManager(manager);
		manager.addUser(subject.getAuthInfo());
		subject.authenticate();
		assertEquals(subject.getIDProof(), manager.generateFromAuthInfo(subject.getAuthInfo()));
		System.out.println("IDProof=" + subject.getIDProof());
	}

	@Test
	public void testRequiresAuthentication() throws Exception {
		PamelaMetaModel context = new PamelaMetaModel(Subject.class);
		PamelaModelFactory factory = new PamelaModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, "id1");
		subject.setManager(manager);
		manager.addUser(subject.getAuthInfo());
		// We haven't call the authenticate() method, but this method is tagged with "@RequiresAuthentication", thus this call the
		// authenticate() method
		subject.thisMethodRequiresToBeAuthenticated();
		assertEquals(subject.getIDProof(), manager.generateFromAuthInfo(subject.getAuthInfo()));
	}

	@Test
	public void testAuthenticatorInvalidReturn() throws Exception {
		PamelaMetaModel context = new PamelaMetaModel(Subject.class);
		PamelaModelFactory factory = new PamelaModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, "id");
		subject.setManager(manager);
		subject.authenticate();
		assertEquals(subject.getIDProof(), manager.getDefaultToken());
	}

	@Test
	public void testInstanceDiscovery() throws Exception {
		PamelaMetaModel metaModel = new PamelaMetaModel(Subject.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		PamelaModel model = factory.getModel();
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		assertNull(model.getPatternInstances(manager));
		Subject subject = factory.newInstance(Subject.class, "id");
		assertNull(model.getPatternInstances(manager));
		assertEquals(1, model.getPatternInstances(subject).size());
		subject.setManager(manager);
		assertEquals(1, model.getPatternInstances(manager).size());
		assertEquals(1, model.getPatternInstances(subject).size());
		assertSame(model.getPatternInstances(manager).iterator().next(), model.getPatternInstances(subject).iterator().next());
	}

	@Test
	public void testAuthInfoUniqueness() throws Exception {
		PamelaMetaModel context = new PamelaMetaModel(Subject.class);
		PamelaModelFactory factory = new PamelaModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, manager, "id");
		Subject subject2 = factory.newInstance(Subject.class, manager, "id2");
		try {
			Subject subject3 = factory.newInstance(Subject.class, manager, "id");
			fail();
		} catch (ModelExecutionException e) {
			assertTrue(e.getMessage().contains("Subject Invariant Violation: Authentication information are not unique"));
		}
	}

	@Test
	public void testAuthenticatorInvariant() throws Exception {
		PamelaMetaModel context = new PamelaMetaModel(Subject.class);
		PamelaModelFactory factory = new PamelaModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, manager, "id");
		subject.setManager(manager);
		try {
			subject.setManager(factory.newInstance(IAuthenticator.class));
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
		PamelaMetaModel context = new PamelaMetaModel(Subject.class);
		PamelaModelFactory factory = new PamelaModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, manager, "id");
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
		PamelaMetaModel context = new PamelaMetaModel(Subject.class);
		PamelaModelFactory factory = new PamelaModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, manager, "id");
		subject.setIdProof(-1);
		subject.authenticate();
		subject.setIdProof(subject.getIDProof());
		try {
			subject.setIdProof(subject.getIDProof() + 1);
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
		PamelaMetaModel context = new PamelaMetaModel(Subject.class);
		PamelaModelFactory factory = new PamelaModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, manager, "id");
		subject.authenticate();
		assertEquals(subject.getIDProof(), manager.getDefaultToken());
		manager.addUser(subject.getAuthInfo());
		subject.getAuthInfo();
		subject.authenticate();
	}

	@Test
	public void testCoucou() throws Exception {
		PamelaMetaModel context = PamelaMetaModelLibrary.retrieveMetaModel(Subject.class);
		PamelaModelFactory factory = new PamelaModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, "id");
		subject.setManager(manager);
		// TODO: write a test
		manager.aMethodGuardedWithAPrecondition();
	}

}
