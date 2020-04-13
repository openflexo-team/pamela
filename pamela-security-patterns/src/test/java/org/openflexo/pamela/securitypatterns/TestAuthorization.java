package org.openflexo.pamela.securitypatterns;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.TestCase;
import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.ModelContextLibrary;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.patterns.PatternClassWrapper;
import org.openflexo.pamela.patterns.PatternContext;
import org.openflexo.pamela.securitypatterns.authorization.*;
import org.openflexo.pamela.securitypatterns.modelAuthorization.PermissionChecker;
import org.openflexo.pamela.securitypatterns.modelAuthorization.Resource;
import org.openflexo.pamela.securitypatterns.modelAuthorization.Subject;

public class TestAuthorization extends TestCase {

	public void testPatternAnalysis() throws Exception {
		ModelContext context = new ModelContext(ModelContextLibrary.getCompoundModelContext(Subject.class, Resource.class));
		assertEquals(1, context.getPatternDefinitions(AuthorizationPatternDefinition.class).size());
		AuthorizationPatternDefinition pattern = context.getPatternDefinitions(AuthorizationPatternDefinition.class).get(0);
		assertEquals(PermissionChecker.PATTERN,pattern.getIdentifier());
		assertTrue(pattern.isValid());

		assertNotNull(pattern.getSubject());
		assertEquals(Subject.class,pattern.getSubject().getImplementedInterface());
		assertEquals(Subject.class.getMethod("getID"),pattern.getSubjectIdGetters().get(PermissionChecker.SUBJECTID));
		assertEquals(Subject.class.getMethod("getStringID"), pattern.getSubjectIdGetters().get(PermissionChecker.SUBJECTSTRINGID));
		assertEquals(2, pattern.getSubjectAccessMethods().size());
		AuthorizationPatternDefinition.SubjectAccessMethodWrapper wrapper = pattern.getSubjectAccessMethods().get(Subject.class.getMethod("getResource", String.class));
		assertNotNull(wrapper);
		assertEquals("get", wrapper.getMethodID());
		assertNotNull(wrapper.getRealIndexes());
		assertEquals(wrapper.getRealIndexes().size(),0);
		assertNotNull(wrapper.getParamMapping());
		assertEquals(1, wrapper.getParamMapping().size());
		assertEquals((Integer)0, wrapper.getParamMapping().get(PermissionChecker.RESOURCEID));
		wrapper = pattern.getSubjectAccessMethods().get(Subject.class.getMethod("setResource", String.class, double.class));
		assertNotNull(wrapper);
		assertEquals("set", wrapper.getMethodID());
		assertNotNull(wrapper.getRealIndexes());
		assertEquals(wrapper.getRealIndexes().size(),1);
		assertEquals((Integer)1, wrapper.getRealIndexes().get(0));
		assertNotNull(wrapper.getParamMapping());
		assertEquals(1, wrapper.getParamMapping().size());
		assertEquals((Integer)0, wrapper.getParamMapping().get(PermissionChecker.RESOURCEID));


		assertNotNull(pattern.getResource());
		assertEquals(Resource.class,pattern.getResource().getImplementedInterface());
		assertEquals(2, pattern.getResourceAccessMethods().size());
		assertTrue(pattern.getResourceAccessMethods().containsKey("get"));
		assertEquals(Resource.class.getMethod("getR1"),pattern.getResourceAccessMethods().get("get"));
		assertTrue(pattern.getResourceAccessMethods().containsKey("set"));
		assertEquals(Resource.class.getMethod("setR1", double.class),pattern.getResourceAccessMethods().get("set"));
		assertEquals(1, pattern.getResourceIdGetters().size());
		assertTrue(pattern.getResourceIdGetters().containsKey(PermissionChecker.RESOURCEID));
		assertEquals(Resource.class.getMethod("getID"),pattern.getResourceIdGetters().get(PermissionChecker.RESOURCEID));
		assertEquals(Resource.class.getMethod("getChecker" ), pattern.getCheckerGetter());

		assertNotNull(pattern.getChecker());
		assertEquals(PermissionChecker.class, pattern.getChecker().getImplementedInterface());
		assertEquals(2,pattern.getSubjectIdParameters().size());
		assertTrue(pattern.getSubjectIdParameters().containsKey(PermissionChecker.SUBJECTID));
		assertEquals((Integer)1, pattern.getSubjectIdParameters().get(PermissionChecker.SUBJECTID));
		assertTrue(pattern.getSubjectIdParameters().containsKey(PermissionChecker.SUBJECTSTRINGID));
		assertEquals((Integer)0, pattern.getSubjectIdParameters().get(PermissionChecker.SUBJECTSTRINGID));
		assertEquals(1,pattern.getResourceIdParameters().size());
		assertTrue(pattern.getResourceIdParameters().containsKey(PermissionChecker.RESOURCEID));
		assertEquals((Integer) 2, pattern.getResourceIdParameters().get(PermissionChecker.RESOURCEID));
		assertEquals(3, pattern.getMethodIdIndex());
		assertEquals(PermissionChecker.class.getMethod("check", String.class, int.class, String.class, String.class), pattern.getCheckMethod());
	}

	public void testInstanceDiscovery() throws Exception {
		ModelContext context = new ModelContext(ModelContextLibrary.getCompoundModelContext(Subject.class, Resource.class));
		ModelFactory factory = new ModelFactory(context);

		Subject subject = factory.newInstance(Subject.class, 42, "id");
		assertTrue(context.getPatternContext().getKnownInstances().containsKey(subject));
		ArrayList<PatternClassWrapper> wrappers = context.getPatternContext().getKnownInstances().get(subject);
		assertEquals(1, wrappers.size());
		assertTrue(wrappers.get(0).getPattern() instanceof AuthorizationPattern);
		AuthorizationPattern pattern = (AuthorizationPattern) wrappers.get(0).getPattern();
		assertTrue(pattern.getSubjects().containsKey(wrappers.get(0).getKlass()));
		AuthorizationSubjectEntity subjectEntity = pattern.getSubjects().get(wrappers.get(0).getKlass());
		assertTrue(subjectEntity.getInstances().containsKey(subject));
		AuthorizationSubjectInstance instance = subjectEntity.getInstances().get(subject);
		assertEquals(instance.getIds().size(), 2);
		assertTrue(instance.getIds().contains("id"));
		assertTrue(instance.getIds().contains(42));

		PermissionChecker checker = factory.newInstance(PermissionChecker.class);
		checker.toString();
		assertTrue(context.getPatternContext().getKnownInstances().containsKey(checker));
		wrappers = context.getPatternContext().getKnownInstances().get(checker);
		assertEquals(1, wrappers.size());
		assertTrue(wrappers.get(0).getPattern() instanceof AuthorizationPattern);
		pattern = (AuthorizationPattern) wrappers.get(0).getPattern();
		PermissionCheckerEntity checkerEntity = pattern.getCheckerEntity();
		assertTrue(checkerEntity.getInstances().containsKey(checker));

		Resource resource = factory.newInstance(Resource.class, "iii", 3.14, checker);
		assertTrue(context.getPatternContext().getKnownInstances().containsKey(resource));
		wrappers = context.getPatternContext().getKnownInstances().get(resource);
		assertEquals(1, wrappers.size());
		assertTrue(wrappers.get(0).getPattern() instanceof AuthorizationPattern);
		pattern = (AuthorizationPattern) wrappers.get(0).getPattern();
		assertTrue(pattern.getResources().containsKey(wrappers.get(0).getKlass()));
		AuthorizationResourceEntity resourceEntity = pattern.getResources().get(wrappers.get(0).getKlass());
		assertTrue(resourceEntity.getInstances().containsKey(resource));
		AuthorizationResourceInstance rinstance = resourceEntity.getInstances().get(resource);
		assertEquals(rinstance.getIds().size(), 1);
		assertTrue(rinstance.getIds().containsKey(PermissionChecker.RESOURCEID));
		assertTrue(rinstance.getIds().get(PermissionChecker.RESOURCEID).equals("iii"));
	}

	public void testAccessGrantedNoParameter() throws Exception {
		ModelContext context = new ModelContext(ModelContextLibrary.getCompoundModelContext(Subject.class, Resource.class));
		ModelFactory factory = new ModelFactory(context);
		PermissionChecker checker = factory.newInstance(PermissionChecker.class);
		Resource resource = factory.newInstance(Resource.class, "Pool", 1., checker);
		Resource resource1 = factory.newInstance(Resource.class, "Pool2", 17., checker);
		Subject user = factory.newInstance(Subject.class, 1, "user");
		Subject admin = factory.newInstance(Subject.class, 2, "admin");
		assertEquals(user.getResource("Pool"), 1.);
		assertEquals(admin.getResource("Pool2"), 17.);
	}

	public void testAccessDenied() throws Exception {
		ModelContext context = new ModelContext(ModelContextLibrary.getCompoundModelContext(Subject.class, Resource.class));
		ModelFactory factory = new ModelFactory(context);
		PermissionChecker checker = factory.newInstance(PermissionChecker.class);
		Resource resource = factory.newInstance(Resource.class, "Pool", 1., checker);
		Subject nobody = factory.newInstance(Subject.class, 2, "toto");
		try {
			nobody.getResource("Pool");
			fail();
		} catch (ModelExecutionException e) {
			e.printStackTrace();
		}
	}

	public void testAccessGrantedWithParameter() throws Exception {
		ModelContext context = new ModelContext(ModelContextLibrary.getCompoundModelContext(Subject.class, Resource.class));
		ModelFactory factory = new ModelFactory(context);
		PermissionChecker checker = factory.newInstance(PermissionChecker.class);
		Resource resource = factory.newInstance(Resource.class, "Pool", 1., checker);
		Resource resource1 = factory.newInstance(Resource.class, "Pool2", 17., checker);
		Subject admin = factory.newInstance(Subject.class, 2, "admin");
		admin.setResource("Pool", 42.);
		assertEquals(admin.getResource("Pool"), 42.);
		admin.setResource("Pool2", -42.);
		assertEquals(admin.getResource("Pool2"), -42.);
	}

	public void testResourceIdInvariant() throws Exception {
		ModelContext context = new ModelContext(ModelContextLibrary.getCompoundModelContext(Subject.class, Resource.class));
		ModelFactory factory = new ModelFactory(context);

		PermissionChecker checker = factory.newInstance(PermissionChecker.class);
		Resource r1 = factory.newInstance(Resource.class, "ID", 3.14, checker);

		try {
			r1.setID("ROGUE");
			fail();
		} catch (ModelExecutionException e) {
			e.printStackTrace();
		}
	}

	public void testResourceFinalCheckerInvariant() throws Exception {
		ModelContext context = new ModelContext(ModelContextLibrary.getCompoundModelContext(Subject.class, Resource.class));
		ModelFactory factory = new ModelFactory(context);

		PermissionChecker checker = factory.newInstance(PermissionChecker.class);
		Resource r1 = factory.newInstance(Resource.class, "ID", 3.14, checker);

		try {
			r1.setChecker(null);
			fail();
		} catch (ModelExecutionException e) {
			e.printStackTrace();
		}
	}

	public void testWrongInitResourceChecker() throws Exception {
		ModelContext context = new ModelContext(ModelContextLibrary.getCompoundModelContext(Subject.class, Resource.class));
		ModelFactory factory = new ModelFactory(context);

		try {
			Resource r1 = factory.newInstance(Resource.class, "ID", 3.14, null);
			fail();
		} catch (ModelExecutionException e) {
			e.printStackTrace();
		}
	}

	public void testSubjectIdInvariant() throws Exception {
		ModelContext context = new ModelContext(ModelContextLibrary.getCompoundModelContext(Subject.class, Resource.class));
		ModelFactory factory = new ModelFactory(context);

		Subject user = factory.newInstance(Subject.class, 42, "ID");

		try {
			user.setID(-1);
			fail();
		} catch (ModelExecutionException e) {
			e.printStackTrace();
		}
	}

	public void testSubjectIdInvariantBis() throws Exception {
		ModelContext context = new ModelContext(ModelContextLibrary.getCompoundModelContext(Subject.class, Resource.class));
		ModelFactory factory = new ModelFactory(context);

		Subject user = factory.newInstance(Subject.class, 42, "ID");

		try {
			user.setStringID("ROGUE");
			fail();
		} catch (ModelExecutionException e) {
			e.printStackTrace();
		}
	}
}
