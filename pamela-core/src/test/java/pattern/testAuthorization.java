package pattern;

import org.openflexo.model.AbstractPAMELATest;
import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.ModelContextLibrary;
import org.openflexo.pamela.patterns.PatternContext;
import org.openflexo.pamela.patterns.authorization.AuthorizationPattern;
import org.openflexo.pamela.patterns.authorization.AuthorizationSubjectEntity;
import pattern.modelAuthorization.*;

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
}
