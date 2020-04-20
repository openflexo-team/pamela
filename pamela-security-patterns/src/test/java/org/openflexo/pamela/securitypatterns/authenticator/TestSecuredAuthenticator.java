package org.openflexo.pamela.securitypatterns.authenticator;

import org.junit.Test;
import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.ModelContextLibrary;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.securitypatterns.authenticator.model2.MyAuthenticator;
import org.openflexo.pamela.securitypatterns.authenticator.model2.MySubject;

import junit.framework.TestCase;

public class TestSecuredAuthenticator extends TestCase {

	@Test
	public void testPatternAnalysis() throws Exception {
		ModelContext context = ModelContextLibrary.getCompoundModelContext(MySubject.class, MyAuthenticator.class);
		assertEquals(1, context.getPatternDefinitions(AuthenticatorPatternDefinition.class).size());
		AuthenticatorPatternDefinition patternDefinition = context.getPatternDefinitions(AuthenticatorPatternDefinition.class).get(0);
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

		ModelContext context = ModelContextLibrary.getCompoundModelContext(MySubject.class, MyAuthenticator.class);
		ModelFactory factory = new ModelFactory(context);
		MyAuthenticator manager = factory.newInstance(MyAuthenticator.class);
		MySubject subject = factory.newInstance(MySubject.class, "id1");
		// MySubject subject = factory.newInstance(MySubject.class);
		subject.setAuthInfo("id1");
		subject.setManager(manager);
		manager.addUser(subject.getAuthInfo());
		subject.authenticate();
		assertEquals(subject.getIDProof(), manager.generateFromAuthInfo(subject.getAuthInfo()));
		System.out.println("IDProof=" + subject.getIDProof());
	}

	/*@Test
	public void testRequiresAuthentication() throws Exception {
		MyAuthenticator manager = new MyAuthenticator();
		MySubject subject = new MySubject(manager, "id1");
		manager.addUser(subject.getAuthInfo());
		// We haven't call the authenticate() method, but this method is tagged with "@RequiresAuthentication", thus this call the
		// authenticate() method
		subject.thisMethodRequiresToBeAuthenticated();
		assertFalse(subject.getAuthenticatedMethodHasBeenSuccessfullyCalled());
		assertEquals(subject.getIDProof(), manager.generateFromAuthInfo(subject.getAuthInfo()));
	}*/

	/*@Test
	public void testAuthenticatorInvalidReturn() throws Exception {
		ModelContext context = new ModelContext(Subject.class);
		ModelFactory factory = new ModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, "id");
		subject.setManager(manager);
		subject.authenticate();
		assertEquals(subject.getIDProof(), manager.getDefaultToken());
	}
	
	@Test
	public void testInstanceDiscovery() throws Exception {
		ModelContext context = new ModelContext(Subject.class);
		ModelFactory factory = new ModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		assertNull(context.getPatternInstances(manager));
		Subject subject = factory.newInstance(Subject.class, "id");
		assertNull(context.getPatternInstances(manager));
		assertEquals(1, context.getPatternInstances(subject).size());
		subject.setManager(manager);
		assertEquals(1, context.getPatternInstances(manager).size());
		assertEquals(1, context.getPatternInstances(subject).size());
		assertSame(context.getPatternInstances(manager).iterator().next(), context.getPatternInstances(subject).iterator().next());
	}
	
	@Test
	public void testAuthInfoUniqueness() throws Exception {
		ModelContext context = new ModelContext(Subject.class);
		ModelFactory factory = new ModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, manager, "id");
		subject.getAuthInfo();
		Subject subject2 = factory.newInstance(Subject.class, manager, "id2");
		subject2.getAuthInfo();
		Subject subject3 = factory.newInstance(Subject.class, manager, "id");
		try {
			subject3.getAuthInfo();
			fail();
		} catch (ModelExecutionException e) {
			assertTrue(e.getMessage().contains("Subject Invariant Violation: Authentication information are not unique"));
		}
	}
	
	@Test
	public void testAuthenticatorInvariant() throws Exception {
		ModelContext context = new ModelContext(Subject.class);
		ModelFactory factory = new ModelFactory(context);
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
		ModelContext context = new ModelContext(Subject.class);
		ModelFactory factory = new ModelFactory(context);
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
		ModelContext context = new ModelContext(Subject.class);
		ModelFactory factory = new ModelFactory(context);
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
		ModelContext context = new ModelContext(Subject.class);
		ModelFactory factory = new ModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, manager, "id");
		subject.authenticate();
		assertEquals(subject.getIDProof(), manager.getDefaultToken());
		manager.addUser(subject.getAuthInfo());
		subject.getAuthInfo();
		subject.authenticate();
	}*/
}
