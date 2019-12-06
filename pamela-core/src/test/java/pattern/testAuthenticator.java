package pattern;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.model.AbstractPAMELATest;
import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.patterns.PatternContext;
import pattern.modelAuthenticator.IAuthenticator;
import pattern.modelAuthenticator.Subject;

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
        // TODO Add getters to inspect code
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
