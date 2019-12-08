package pattern;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.model.AbstractPAMELATest;
import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.patterns.PatternClassWrapper;
import org.openflexo.pamela.patterns.PatternContext;
import org.openflexo.pamela.patterns.authenticator.AuthenticatorPattern;
import org.openflexo.pamela.patterns.authenticator.SubjectEntity;
import org.openflexo.pamela.patterns.authenticator.SubjectInstance;
import org.openflexo.pamela.patterns.authenticator.annotations.AuthenticateMethod;
import org.openflexo.pamela.patterns.authenticator.annotations.Authenticator;
import pattern.modelAuthenticator.IAuthenticator;
import pattern.modelAuthenticator.Subject;

import java.lang.reflect.Method;
import java.util.ArrayList;

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
        manager.init();
        Subject subject = factory.newInstance(Subject.class);
        subject.init(manager, "id1");
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
        Subject subject = factory.newInstance(Subject.class);
        manager.init();
        subject.init(manager, "id");
        subject.setManager(manager);
        subject.authenticate();
        assertEquals(subject.getIDProof(),manager.getDefaultToken());
    }

    @Test
    public static void testSetterNoEffect() throws Exception {
        ModelContext context = new ModelContext(Subject.class);
        ModelFactory factory = new ModelFactory(context);
        IAuthenticator manager = factory.newInstance(IAuthenticator.class);
        Subject subject = factory.newInstance(Subject.class);
        subject.setManager(manager);
        manager.addUser(subject.getAuthInfo());
        subject.authenticate();
        subject.setIdProof(-1);
        assertEquals(subject.getIDProof(), manager.generateFromAuthInfo(subject.getAuthInfo()));
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
        Subject subject = factory.newInstance(Subject.class);
        assertTrue(context.getPatternContext().getKnownInstances().containsKey(subject));
        ArrayList<PatternClassWrapper> patternList = context.getPatternContext().getKnownInstances().get(subject);
        assertTrue(patternList.size() == 1 );
        AuthenticatorPattern pattern = (AuthenticatorPattern)patternList.get(0).getPattern();
        assertTrue(pattern.getSubjects().containsKey(patternList.get(0).getKlass()));
        SubjectEntity subjectEntity = pattern.getSubjects().get(patternList.get(0).getKlass());
        assertTrue(subjectEntity.getInstances().size() == 1 && subjectEntity.getInstances().containsKey(subject));
        */
    }
}
