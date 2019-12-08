package pattern;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.model.AbstractPAMELATest;
import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.patterns.PatternContext;
import org.openflexo.pamela.patterns.authenticator.AuthenticatorPattern;
import org.openflexo.pamela.patterns.authenticator.SubjectEntity;
import org.openflexo.pamela.patterns.authenticator.annotations.AuthenticateMethod;
import pattern.modelAuthenticator.IAuthenticator;
import pattern.modelAuthenticator.Subject;

import java.lang.reflect.Method;

public class testAuthenticator extends AbstractPAMELATest {
    ModelFactory factory;

    @Override
    @BeforeClass
    public void setUp() throws Exception{
        factory = new ModelFactory(new ModelContext(Subject.class));
    }

    @Test
    public void testPatternAnalysis() throws Exception {
        PatternContext patternContext = this.factory.getModelContext().getPatternContext();
        assertNotNull(patternContext);
        assertNotNull(patternContext.getAuthenticatorPatterns().get(Subject.PATTERN_ID));
        AuthenticatorPattern pattern = patternContext.getAuthenticatorPatterns().get(Subject.PATTERN_ID);
        assertEquals(IAuthenticator.class, pattern.getAuthenticator().getBaseClass());
        assertEquals(IAuthenticator.class.getMethod("request", String.class), pattern.getAuthenticator().getMethod());
        assertTrue(pattern.getSubjects().containsKey(Subject.class) && pattern.getSubjects().size() == 1);
        SubjectEntity subject = pattern.getSubjects().get(Subject.class);
        assertTrue(subject.getArgs().length == 1 && subject.getArgs()[0].equals(Subject.class.getMethod("getAuthInfo")));
        assertEquals(Subject.class.getMethod("authenticate" ).getAnnotation(AuthenticateMethod.class).authenticator(), subject.getAuthenticateMethods().get(Subject.class.getMethod("authenticate" )));
        assertEquals(Subject.class, subject.getBaseClass());
        assertEquals(Subject.class.getMethod("setIdProof", int.class), subject.getIdProofSetter());
    }

    @Test
    public void testAuthenticateValid() throws Exception{
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
    public void testAuthenticatorInvalidReturn() throws Exception{
        IAuthenticator manager = factory.newInstance(IAuthenticator.class);
        Subject subject = factory.newInstance(Subject.class);
        manager.init();
        subject.init(manager, "id");
        subject.setManager(manager);
        subject.authenticate();
        assertEquals(subject.getIDProof(),manager.getDefaultToken());
    }

    @Test
    public void testSetterNoEffect() throws Exception {
        IAuthenticator manager = factory.newInstance(IAuthenticator.class);
        Subject subject = factory.newInstance(Subject.class);
        subject.setManager(manager);
        manager.addUser(subject.getAuthInfo());
        subject.authenticate();
        subject.setIdProof(-1);
        assertEquals(subject.getIDProof(), manager.generateFromAuthInfo(subject.getAuthInfo()));
    }
}
