package pattern;

import org.openflexo.model.AbstractPAMELATest;
import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.ModelContextLibrary;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.patterns.PatternClassWrapper;
import org.openflexo.pamela.patterns.PatternContext;
import org.openflexo.pamela.patterns.authorization.*;
import pattern.modelAuthorization.*;

import java.util.ArrayList;

public class testAuthorization extends AbstractPAMELATest {

    public void testPatternAnalysis() throws Exception {
        ModelContext context = new ModelContext(ModelContextLibrary.getCompoundModelContext(Subject.class, Resource.class));
        PatternContext patternContext = context.getPatternContext();
        assertNotNull(patternContext);
        assertNotNull(patternContext.getPatterns().get(PermissionChecker.PATTERN));
        AuthorizationPattern pattern = (AuthorizationPattern) patternContext.getPatterns().get(PermissionChecker.PATTERN);
        assertEquals(PermissionChecker.class, pattern.getCheckerEntity().getBaseClass());
        assertTrue(pattern.getSubjects().containsKey(Subject.class) && pattern.getSubjects().size() == 1);
        assertTrue(pattern.getResources().containsKey(Resource.class) && pattern.getResources().size() == 1);
        AuthorizationSubjectEntity subject = pattern.getSubjects().get(Subject.class);
        assertTrue(subject.isLinked());
    }

    public void testInstanceDiscovery() throws Exception {
        ModelContext context = new ModelContext(ModelContextLibrary.getCompoundModelContext(Subject.class, Resource.class));
        ModelFactory factory = new ModelFactory(context);

        Subject subject = factory.newInstance(Subject.class,42,"id");
        assertTrue(context.getPatternContext().getKnownInstances().containsKey(subject));
        ArrayList<PatternClassWrapper> wrappers = context.getPatternContext().getKnownInstances().get(subject);
        assertEquals(1,wrappers.size());
        assertTrue(wrappers.get(0).getPattern() instanceof AuthorizationPattern);
        AuthorizationPattern pattern = (AuthorizationPattern) wrappers.get(0).getPattern();
        assertTrue(pattern.getSubjects().containsKey(wrappers.get(0).getKlass()));
        AuthorizationSubjectEntity subjectEntity = pattern.getSubjects().get(wrappers.get(0).getKlass());
        assertTrue(subjectEntity.getInstances().containsKey(subject));
        AuthorizationSubjectInstance instance = subjectEntity.getInstances().get(subject);
        assertEquals(instance.getIds().size(),2);
        assertTrue(instance.getIds().contains("id"));
        assertTrue(instance.getIds().contains(42));

        PermissionChecker checker = factory.newInstance(PermissionChecker.class);
        checker.toString();
        assertTrue(context.getPatternContext().getKnownInstances().containsKey(checker));
        wrappers = context.getPatternContext().getKnownInstances().get(checker);
        assertEquals(1,wrappers.size());
        assertTrue(wrappers.get(0).getPattern() instanceof AuthorizationPattern);
        pattern = (AuthorizationPattern) wrappers.get(0).getPattern();
        PermissionCheckerEntity checkerEntity = pattern.getCheckerEntity();
        assertTrue(checkerEntity.getInstances().containsKey(checker));

        Resource resource = factory.newInstance(Resource.class,"iii",3.14,checker);
        assertTrue(context.getPatternContext().getKnownInstances().containsKey(resource));
        wrappers = context.getPatternContext().getKnownInstances().get(resource);
        assertEquals(1,wrappers.size());
        assertTrue(wrappers.get(0).getPattern() instanceof AuthorizationPattern);
        pattern = (AuthorizationPattern) wrappers.get(0).getPattern();
        assertTrue(pattern.getResources().containsKey(wrappers.get(0).getKlass()));
        AuthorizationResourceEntity resourceEntity = pattern.getResources().get(wrappers.get(0).getKlass());
        assertTrue(resourceEntity.getInstances().containsKey(resource));
        AuthorizationResourceInstance rinstance = resourceEntity.getInstances().get(resource);
        assertEquals(rinstance.getIds().size(),1);
        assertTrue(rinstance.getIds().containsKey(PermissionChecker.RESOURCEID));
        assertTrue(rinstance.getIds().get(PermissionChecker.RESOURCEID).equals("iii"));
    }
}
