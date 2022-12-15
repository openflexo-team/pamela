package org.openflexo.pamela.securitypatterns.authenticator;

import java.beans.PropertyChangeEvent;

import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.factory.PamelaModel;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.securitypatterns.authenticator.model2.MyAuthenticator;
import org.openflexo.pamela.securitypatterns.authenticator.model2.MySubject;

import junit.framework.TestCase;

public class TestSecuredAuthenticator extends TestCase {

	@Test
	public void testPatternAnalysis() throws Exception {
		PamelaMetaModelLibrary.clearCache();
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(MySubject.class, MyAuthenticator.class);
		assertEquals(1, metaModel.getPatternDefinitions(AuthenticatorPatternDefinition.class).size());
		AuthenticatorPatternDefinition patternDefinition = metaModel.getPatternDefinitions(AuthenticatorPatternDefinition.class).get(0);
		assertEquals(MySubject.PATTERN_ID, patternDefinition.getIdentifier());

		assertEquals(MyAuthenticator.class, patternDefinition.authenticatorModelEntity.getImplementedInterface());
		assertEquals(MyAuthenticator.class.getMethod("request", String.class), patternDefinition.requestAuthentificationMethod);

		assertEquals(MySubject.class, patternDefinition.subjectModelEntity.getImplementedInterface());
		assertEquals(MySubject.class.getMethod("getAuthInfo"), patternDefinition.authentificationInfoMethod);
		assertEquals(MySubject.class.getMethod("setIDProof", int.class), patternDefinition.proofOfIdentitySetterMethod);
		assertEquals(MySubject.class.getMethod("getManager"), patternDefinition.authenticatorGetterMethod);
		assertEquals(MySubject.class.getMethod("authenticate"), patternDefinition.authenticateMethod);

	}

	// Functional test
	@Test
	public void testAuthenticateValid() throws Exception {
		PamelaMetaModelLibrary.clearCache();
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(MySubject.class, MyAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		MyAuthenticator manager = factory.newInstance(MyAuthenticator.class);
		MySubject subject = factory.newInstance(MySubject.class, "id1");
		subject.setManager(manager);
		manager.addUser(subject.getAuthInfo());
		subject.authenticate();
		assertEquals(subject.getIDProof(), manager.generateFromAuthInfo(subject.getAuthInfo()));
		System.out.println("IDProof=" + subject.getIDProof());
	}

	@Test
	public void testRequiresAuthentication() throws Exception {
		PamelaMetaModelLibrary.clearCache();
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(MySubject.class, MyAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		MyAuthenticator manager = factory.newInstance(MyAuthenticator.class);
		MySubject subject = factory.newInstance(MySubject.class, "id1");
		manager.addUser(subject.getAuthInfo());

		// We haven't call the authenticate() method, but this method is tagged with "@RequiresAuthentication", thus this call the
		// authenticate() method
		try {
			subject.thisMethodRequiresToBeAuthenticated();
			fail();
		} catch (ModelExecutionException e) {
			// as expected
		}

		subject.setManager(manager);
		subject.thisMethodRequiresToBeAuthenticated();

		assertTrue(subject.getAuthenticatedMethodHasBeenSuccessfullyCalled());
		assertEquals(subject.getIDProof(), manager.generateFromAuthInfo(subject.getAuthInfo()));
	}

	@Test
	public void testAuthenticatorInvalidReturn() throws Exception {
		PamelaMetaModelLibrary.clearCache();
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(MySubject.class, MyAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		MyAuthenticator manager = factory.newInstance(MyAuthenticator.class);
		MySubject subject = factory.newInstance(MySubject.class, "id1");
		subject.setManager(manager);
		subject.authenticate();
		// We are not authenticated: this is the default token
		assertEquals(subject.getIDProof(), manager.getDefaultToken());
	}

	@Test
	public void testInstanceDiscovery() throws Exception {
		PamelaMetaModelLibrary.clearCache();
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(MySubject.class, MyAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		PamelaModel model = factory.getModel();
		MyAuthenticator manager = factory.newInstance(MyAuthenticator.class);
		assertNull(model.getPatternInstances(manager));
		MySubject subject = factory.newInstance(MySubject.class, "id");
		assertNull(model.getPatternInstances(manager));
		assertEquals(1, model.getPatternInstances(subject).size());
		subject.setManager(manager);
		assertNull(model.getPatternInstances(manager));
		((AuthenticatorPatternInstance) model.getPatternInstances(subject).iterator().next())
				.propertyChange(new PropertyChangeEvent(subject, "manager", null, manager));
		assertEquals(1, model.getPatternInstances(manager).size());
		assertEquals(1, model.getPatternInstances(subject).size());
		assertSame(model.getPatternInstances(manager).iterator().next(), model.getPatternInstances(subject).iterator().next());
	}

	@Test
	public void testAuthInfoUniqueness() throws Exception {
		PamelaMetaModelLibrary.clearCache();
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(MySubject.class, MyAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		MyAuthenticator manager = factory.newInstance(MyAuthenticator.class);
		MySubject subject = factory.newInstance(MySubject.class, "id");
		subject.setManager(manager);
		subject.getAuthInfo();
		MySubject subject2 = factory.newInstance(MySubject.class, "id2");
		subject2.setManager(manager);
		subject2.getAuthInfo();
		MySubject subject3 = factory.newInstance(MySubject.class, "id");
		subject3.setManager(manager);
		try {
			subject3.getAuthInfo();
			fail();
		} catch (ModelExecutionException e) {
			assertTrue(e.getMessage().contains("Subject Invariant Violation: Authentication information are not unique"));
		}
	}

	@Test
	public void testAuthenticatorInvariant() throws Exception {
		PamelaMetaModelLibrary.clearCache();
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(MySubject.class, MyAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		MyAuthenticator manager = factory.newInstance(MyAuthenticator.class);
		MySubject subject = factory.newInstance(MySubject.class, "id");
		subject.setManager(manager);
		subject.authenticate();
		try {
			subject.setManager(factory.newInstance(MyAuthenticator.class));
			subject.authenticate();
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
		PamelaMetaModelLibrary.clearCache();
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(MySubject.class, MyAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		MyAuthenticator manager = factory.newInstance(MyAuthenticator.class);
		MySubject subject = factory.newInstance(MySubject.class, "id");
		subject.setManager(manager);
		subject.setAuthInfo("id");
		try {
			subject.setAuthInfo("id2");
			subject.authenticate();
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
		PamelaMetaModelLibrary.clearCache();
		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(MySubject.class, MyAuthenticator.class);
		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		MyAuthenticator manager = factory.newInstance(MyAuthenticator.class);
		MySubject subject = factory.newInstance(MySubject.class, "id");
		subject.setManager(manager);
		subject.setAuthInfo("id");
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

}
