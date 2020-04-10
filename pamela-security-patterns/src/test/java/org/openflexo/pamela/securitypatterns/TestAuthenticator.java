package org.openflexo.pamela.securitypatterns;

import org.junit.Test;
import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.securitypatterns.authenticator.AuthenticatorPatternDefinition;
import org.openflexo.pamela.securitypatterns.modelAuthenticator.IAuthenticator;
import org.openflexo.pamela.securitypatterns.modelAuthenticator.Subject;

import junit.framework.TestCase;

public class TestAuthenticator extends TestCase {

	@Test
	public void testPatternAnalysis() throws Exception {
		ModelContext context = new ModelContext(Subject.class);
		assertEquals(1, context.getPatternDefinitions(AuthenticatorPatternDefinition.class).size());
		AuthenticatorPatternDefinition patternDefinition = context.getPatternDefinitions(AuthenticatorPatternDefinition.class).get(0);
		assertEquals(Subject.PATTERN_ID, patternDefinition.getIdentifier());

		assertEquals(IAuthenticator.class, patternDefinition.authenticatorModelEntity.getImplementedInterface());
		assertEquals(IAuthenticator.class.getMethod("request", String.class), patternDefinition.requestAuthentificationMethod);

		assertEquals(Subject.class, patternDefinition.subjectModelEntity.getImplementedInterface());
		assertEquals(Subject.class.getMethod("getAuthInfo"), patternDefinition.authentificationInfoMethod);
		assertEquals(Subject.class.getMethod("setIdProof", int.class), patternDefinition.proofOfIdentitySetterMethod);
		assertEquals(Subject.class.getMethod("getManager"), patternDefinition.authenticatorGetterMethod);
		assertEquals(Subject.class.getMethod("authenticate"), patternDefinition.authenticateMethod);

	}

	@Test
	public void testAuthenticateValid() throws Exception {
		ModelContext context = new ModelContext(Subject.class);
		ModelFactory factory = new ModelFactory(context);
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
		ModelContext context = new ModelContext(Subject.class);
		ModelFactory factory = new ModelFactory(context);
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
		Subject subject2 = factory.newInstance(Subject.class, manager, "id2");
		Subject subject3 = factory.newInstance(Subject.class, manager, "id");
		subject.getAuthInfo();
		subject2.getAuthInfo();
		try {
			subject3.getAuthInfo();
			fail();
		} catch (ModelExecutionException e) {
			e.printStackTrace();
			if (e.getMessage().compareTo("Subject Invariant Violation: Authentication information are not unique") != 0) {
				fail();
			}
		}
	}

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

	public void testInvariantValidityWithDynamicPrivilegeRules() throws Exception {
		ModelContext context = new ModelContext(Subject.class);
		ModelFactory factory = new ModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, manager, "id");
		subject.authenticate();
		manager.addUser(subject.getAuthInfo());
		subject.getAuthInfo();
		subject.authenticate();
	}
}
