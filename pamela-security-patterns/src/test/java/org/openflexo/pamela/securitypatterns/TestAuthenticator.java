package org.openflexo.pamela.securitypatterns;

import java.util.ArrayList;

import junit.framework.TestCase;
import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.patterns.PatternClassWrapper;
import org.openflexo.pamela.patterns.PatternContext;
import org.openflexo.pamela.securitypatterns.authenticator.AuthenticatorPattern;
import org.openflexo.pamela.securitypatterns.authenticator.AuthenticatorSubjectEntity;
import org.openflexo.pamela.securitypatterns.modelAuthenticator.IAuthenticator;
import org.openflexo.pamela.securitypatterns.modelAuthenticator.Subject;

public class TestAuthenticator extends TestCase {

	public void testPatternAnalysis() throws Exception {
		ModelContext context = new ModelContext(Subject.class);
		PatternContext patternContext = context.getPatternContext();
		assertNotNull(patternContext);
		assertNotNull(patternContext.getPatterns().get(Subject.PATTERN_ID));
		AuthenticatorPattern pattern = (AuthenticatorPattern) patternContext.getPatterns().get(Subject.PATTERN_ID);
		assertEquals(IAuthenticator.class, pattern.getAuthenticator().getBaseClass());
		assertEquals(IAuthenticator.class.getMethod("request", String.class), pattern.getAuthenticator().getRequestMethod());
		assertTrue(pattern.getSubjects().containsKey(Subject.class) && pattern.getSubjects().size() == 1);
		AuthenticatorSubjectEntity subject = pattern.getSubjects().get(Subject.class);
		assertTrue(subject.getArgs().length == 1 && subject.getArgs()[0].equals(Subject.class.getMethod("getAuthInfo")));
		assertTrue(subject.getAuthenticateMethods().size() == 1
				&& subject.getAuthenticateMethods().get(0).equals(Subject.class.getMethod("authenticate")));
		assertEquals(Subject.class, subject.getBaseClass());
		assertEquals(Subject.class.getMethod("setIdProof", int.class), subject.getIdProofSetter());
	}

	public void testAuthenticateValid() throws Exception {
		ModelContext context = new ModelContext(Subject.class);
		ModelFactory factory = new ModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, manager, "id1");
		subject.setManager(manager);
		manager.addUser(subject.getAuthInfo());
		subject.authenticate();
		assertEquals(subject.getIDProof(), manager.generateFromAuthInfo(subject.getAuthInfo()));
	}

	public void testAuthenticatorInvalidReturn() throws Exception {
		ModelContext context = new ModelContext(Subject.class);
		ModelFactory factory = new ModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, manager, "id");
		subject.setManager(manager);
		subject.authenticate();
		assertEquals(subject.getIDProof(), manager.getDefaultToken());
	}

	public void testInstanceDiscovery() throws Exception {
		ModelContext context = new ModelContext(Subject.class);
		ModelFactory factory = new ModelFactory(context);
		assertTrue(context.getPatternContext().getKnownInstances().isEmpty());
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		assertTrue(context.getPatternContext().getKnownInstances().isEmpty());
		manager.getDefaultToken();
		assertTrue(context.getPatternContext().getKnownInstances().containsKey(manager));
		assertEquals(1, context.getPatternContext().getKnownInstances().size());
		Subject subject = factory.newInstance(Subject.class, manager, "id");
		assertEquals(1, context.getPatternContext().getKnownInstances().size());
		subject.getAuthInfo();
		assertTrue(context.getPatternContext().getKnownInstances().containsKey(subject));
		assertEquals(2, context.getPatternContext().getKnownInstances().size());
		ArrayList<PatternClassWrapper> patternList = context.getPatternContext().getKnownInstances().get(subject);
		assertEquals(1, patternList.size());
		AuthenticatorPattern pattern = (AuthenticatorPattern) patternList.get(0).getPattern();
		assertTrue(pattern.getSubjects().containsKey(patternList.get(0).getKlass()));
		AuthenticatorSubjectEntity authenticatorSubjectEntity = pattern.getSubjects().get(patternList.get(0).getKlass());
		assertTrue(authenticatorSubjectEntity.getInstances().size() == 1 && authenticatorSubjectEntity.getInstances().containsKey(subject));
	}

	public void testIndirectInstanceDiscovery() throws Exception {
		ModelContext context = new ModelContext(Subject.class);
		ModelFactory factory = new ModelFactory(context);
		IAuthenticator manager = factory.newInstance(IAuthenticator.class);
		Subject subject = factory.newInstance(Subject.class, manager, "id");
		assertEquals(0, context.getPatternContext().getKnownInstances().size());
		subject.authenticate();
		assertEquals(2, context.getPatternContext().getKnownInstances().size());
		assertTrue(context.getPatternContext().getKnownInstances().containsKey(manager));
		assertTrue(context.getPatternContext().getKnownInstances().containsKey(subject));
	}

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
