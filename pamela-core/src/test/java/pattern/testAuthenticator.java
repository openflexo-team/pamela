package pattern;

import org.junit.Test;
import org.openflexo.model.AbstractPAMELATest;
import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.patterns.PatternContext;
import org.openflexo.pamela.patterns.authenticator.AuthenticatorPattern;
import org.openflexo.pamela.patterns.authenticator.SubjectEntity;
import pattern.modelAuthenticator.IAuthenticator;
import pattern.modelAuthenticator.Subject;

public class testAuthenticator extends AbstractPAMELATest {

    @Test
    public static void testPatternAnalysis() throws Exception {
        ModelContext context = new ModelContext(Subject.class);
        PatternContext patternContext = context.getPatternContext();
        assertNotNull(patternContext);
        assertNotNull(patternContext.getAuthenticatorPatterns().get(Subject.PATTERN_ID));
        AuthenticatorPattern pattern = patternContext.getAuthenticatorPatterns().get(Subject.PATTERN_ID);
        assertEquals(IAuthenticator.class, pattern.getAuthenticator().getBaseClass());
        assertEquals(IAuthenticator.class.getMethod("request", String.class), pattern.getAuthenticator().getMethod());
        assertTrue(pattern.getSubjects().containsKey(Subject.class) && pattern.getSubjects().size() == 1);
        SubjectEntity subject = pattern.getSubjects().get(Subject.class);
        assertTrue(subject.getArgs().length == 1 && subject.getArgs()[0].equals(Subject.class.getMethod("getAuthInfo")));
        assertTrue(subject.getAuthenticateMethods().size() == 1 && subject.getAuthenticateMethods().get(0).equals(Subject.class.getMethod("authenticate")));
        assertEquals(Subject.class, subject.getBaseClass());
        assertEquals(Subject.class.getMethod("setIdProof", int.class), subject.getIdProofSetter());
    }

    @Test
    public static void testAuthenticateValid() throws Exception{
        ModelContext context = new ModelContext(Subject.class);
        ModelFactory factory = new ModelFactory(context);
        IAuthenticator manager = factory.newInstance(IAuthenticator.class);
        Subject subject = factory.newInstance(Subject.class, manager, "id1");
        subject.setManager(manager);
        manager.addUser(subject.getAuthInfo());
        subject.authenticate();
        assertEquals(subject.getIDProof(), manager.generateFromAuthInfo(subject.getAuthInfo()));
    }

    @Test
    public static void testAuthenticatorInvalidReturn() throws Exception{
        ModelContext context = new ModelContext(Subject.class);
        ModelFactory factory = new ModelFactory(context);
        IAuthenticator manager = factory.newInstance(IAuthenticator.class);
        Subject subject = factory.newInstance(Subject.class, manager, "id");
        subject.setManager(manager);
        subject.authenticate();
        assertEquals(subject.getIDProof(),manager.getDefaultToken());
    }

    @Test
    public static void testInstanceDiscovery() throws Exception {
        //TODO Works with debugger but not with test execution
        /*
        ModelContext context = new ModelContext(Subject.class);
        ModelFactory factory = new ModelFactory(context);
        assertTrue(context.getPatternContext().getKnownInstances().isEmpty());
        IAuthenticator manager = factory.newInstance(IAuthenticator.class);
        assertTrue(context.getPatternContext().getKnownInstances().containsKey(manager));
        Subject subject = factory.newInstance(Subject.class, manager, "id");
        assertTrue(context.getPatternContext().getKnownInstances().containsKey(subject));
        ArrayList<PatternClassWrapper> patternList = context.getPatternContext().getKnownInstances().get(subject);
        assertTrue(patternList.size() == 1 );
        AuthenticatorPattern pattern = (AuthenticatorPattern)patternList.get(0).getPattern();
        assertTrue(pattern.getSubjects().containsKey(patternList.get(0).getKlass()));
        SubjectEntity subjectEntity = pattern.getSubjects().get(patternList.get(0).getKlass());
        assertTrue(subjectEntity.getInstances().size() == 1 && subjectEntity.getInstances().containsKey(subject));
        */
    }

    @Test
    public static void testAuthInfoUniqueness() throws Exception {
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
        }
        catch (ModelExecutionException e){
            e.printStackTrace();
            if (e.getMessage().compareTo("Subject Invariant Violation: Authentication information are not unique") != 0){
                fail();
            }
        }
    }

    @Test
    public static void testAuthenticatorInvariant() throws Exception {
        ModelContext context = new ModelContext(Subject.class);
        ModelFactory factory = new ModelFactory(context);
        IAuthenticator manager = factory.newInstance(IAuthenticator.class);
        Subject subject = factory.newInstance(Subject.class, manager, "id");
        subject.setManager(manager);
        try {
            subject.setManager(factory.newInstance(IAuthenticator.class));
            fail();
        }
        catch (ModelExecutionException e){
            e.printStackTrace();
            if (e.getMessage().compareTo("Subject Invariant Violation: Authenticator has changed since initialization") != 0){
                fail();
            }
        }
    }

    @Test
    public static void testAuthInfoInvariant() throws Exception {
        ModelContext context = new ModelContext(Subject.class);
        ModelFactory factory = new ModelFactory(context);
        IAuthenticator manager = factory.newInstance(IAuthenticator.class);
        Subject subject = factory.newInstance(Subject.class, manager, "id");
        subject.setAuthInfo("id");
        try {
            subject.setAuthInfo(null);
            fail();
        }
        catch (ModelExecutionException e){
            e.printStackTrace();
            if (e.getMessage().compareTo("Subject Invariant Violation: Authentication Information has changed since initialization") != 0){
                fail();
            }
        }
    }

    @Test
    public static void testIdProofForgery() throws Exception {
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
        }
        catch (ModelExecutionException e){
            e.printStackTrace();
            if (e.getMessage().compareTo("Subject Invariant Violation: Proof of identity has been forged") != 0){
                fail();
            }
        }
    }


}
