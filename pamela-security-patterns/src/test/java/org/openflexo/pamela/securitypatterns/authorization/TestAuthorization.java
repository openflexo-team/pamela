package org.openflexo.pamela.securitypatterns.authorization;

import junit.framework.TestCase;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.securitypatterns.authorization.model.PermissionChecker;
import org.openflexo.pamela.securitypatterns.authorization.model.Resource;
import org.openflexo.pamela.securitypatterns.authorization.model.Subject;

public class TestAuthorization extends TestCase {

	public void testPatternAnalysis() throws Exception {
		PamelaMetaModel context = new PamelaMetaModel(PamelaMetaModelLibrary.getCompoundModelContext(Subject.class, Resource.class));
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
		PamelaMetaModel context = new PamelaMetaModel(PamelaMetaModelLibrary.getCompoundModelContext(Subject.class, Resource.class));
		PamelaModelFactory factory = new PamelaModelFactory(context);
		AuthorizationPatternDefinition patternDefinition = context.getPatternDefinitions(AuthorizationPatternDefinition.class).iterator().next();
		AuthorizationPatternInstance<Subject, Resource, PermissionChecker> patternInstance;

		Subject subject = factory.newInstance(Subject.class, 42, "id");
		assertEquals(1, context.getPatternInstances(patternDefinition).size());
		patternInstance = (AuthorizationPatternInstance<Subject, Resource, PermissionChecker>) context.getPatternInstances(patternDefinition).iterator().next();
		assertEquals(1,patternInstance.getSubjects().size());
		assertTrue(patternInstance.getSubjects().containsKey(subject));
		AuthorizationPatternInstance.SubjectWrapper wrapper = patternInstance.getSubjects().get(subject);
		assertTrue(wrapper.getIdentifiers().containsKey(PermissionChecker.SUBJECTID));
		assertEquals(subject.getID(),wrapper.getIdentifiers().get(PermissionChecker.SUBJECTID));
		assertTrue(wrapper.getIdentifiers().containsKey(PermissionChecker.SUBJECTSTRINGID));
		assertEquals(subject.getStringID(),wrapper.getIdentifiers().get(PermissionChecker.SUBJECTSTRINGID));

		PermissionChecker checker = factory.newInstance(PermissionChecker.class);
		Resource r1 = factory.newInstance(Resource.class, "resource id", 42., checker);
		assertEquals(1, context.getPatternInstances(patternDefinition).size());
		patternInstance = (AuthorizationPatternInstance<Subject, Resource, PermissionChecker>) context.getPatternInstances(patternDefinition).iterator().next();
		assertEquals(1, patternInstance.getResources().size());
		assertTrue(patternInstance.getResources().containsKey(r1));
		AuthorizationPatternInstance.ResourceWrapper<PermissionChecker> rWrapper = patternInstance.getResources().get(r1);
		assertTrue(rWrapper.isValid());
		assertEquals(checker, rWrapper.getChecker());
		assertTrue(rWrapper.getIdentifiers().containsKey(PermissionChecker.RESOURCEID));
		assertEquals(r1.getID(),  rWrapper.getIdentifiers().get(PermissionChecker.RESOURCEID));
	}

	public void testCheckerChange() throws Exception{
		PamelaMetaModel context = new PamelaMetaModel(PamelaMetaModelLibrary.getCompoundModelContext(Subject.class, Resource.class));
		PamelaModelFactory factory = new PamelaModelFactory(context);AuthorizationPatternDefinition patternDefinition = context.getPatternDefinitions(AuthorizationPatternDefinition.class).iterator().next();
		AuthorizationPatternInstance<Subject, Resource, PermissionChecker> patternInstance;
		PermissionChecker checker = factory.newInstance(PermissionChecker.class);
		Resource r1 = factory.newInstance(Resource.class, "resource id", 42.);
		assertEquals(1, context.getPatternInstances(patternDefinition).size());
		patternInstance = (AuthorizationPatternInstance<Subject, Resource, PermissionChecker>) context.getPatternInstances(patternDefinition).iterator().next();
		assertEquals(1, patternInstance.getResources().size());
		assertTrue(patternInstance.getResources().containsKey(r1));
		AuthorizationPatternInstance.ResourceWrapper<PermissionChecker> rWrapper = patternInstance.getResources().get(r1);
		assertNull(rWrapper.getChecker());
		assertFalse(rWrapper.isValid());
		r1.setChecker(checker);
		assertTrue(rWrapper.isValid());
		assertEquals(checker, rWrapper.getChecker());
		assertTrue(rWrapper.getIdentifiers().containsKey(PermissionChecker.RESOURCEID));
		assertEquals(r1.getID(),  rWrapper.getIdentifiers().get(PermissionChecker.RESOURCEID));
	}


	public void testAccessGrantedNoParameter() throws Exception {
		PamelaMetaModel context = new PamelaMetaModel(PamelaMetaModelLibrary.getCompoundModelContext(Subject.class, Resource.class));
		PamelaModelFactory factory = new PamelaModelFactory(context);
		PermissionChecker checker = factory.newInstance(PermissionChecker.class);
		Resource resource = factory.newInstance(Resource.class, "Pool", 1., checker);
		Resource resource1 = factory.newInstance(Resource.class, "Pool2", 17., checker);
		Subject user = factory.newInstance(Subject.class, 1, "user");
		Subject admin = factory.newInstance(Subject.class, 2, "admin");
		assertEquals(1. ,user.getResource("Pool"));
		assertEquals(17., admin.getResource( "Pool2"));
	}

	public void testAccessDenied() throws Exception {
		PamelaMetaModel context = new PamelaMetaModel(PamelaMetaModelLibrary.getCompoundModelContext(Subject.class, Resource.class));
		PamelaModelFactory factory = new PamelaModelFactory(context);
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
		PamelaMetaModel context = new PamelaMetaModel(PamelaMetaModelLibrary.getCompoundModelContext(Subject.class, Resource.class));
		PamelaModelFactory factory = new PamelaModelFactory(context);
		PermissionChecker checker = factory.newInstance(PermissionChecker.class);
		Resource resource = factory.newInstance(Resource.class, "Pool", 1., checker);
		Resource resource1 = factory.newInstance(Resource.class, "Pool2", 17., checker);
		Subject admin = factory.newInstance(Subject.class, 2, "admin");
		admin.setResource("Pool", 42.);
		assertEquals(42., admin.getResource("Pool"));
		admin.setResource("Pool2", -42.);
		assertEquals(-42., admin.getResource("Pool2"));
	}

	public void testOverrideAttempt() throws Exception{
		PamelaMetaModel context = new PamelaMetaModel(PamelaMetaModelLibrary.getCompoundModelContext(Subject.class, Resource.class));
		PamelaModelFactory factory = new PamelaModelFactory(context);
		PermissionChecker checker = factory.newInstance(PermissionChecker.class);
		Resource resource = factory.newInstance(Resource.class, "Pool", 1., checker);
		try {
			resource.getR1();
			fail();
		}
		catch (ModelExecutionException e){
			e.printStackTrace();
		}
	}


	public void testResourceIdInvariant() throws Exception {
		PamelaMetaModel context = new PamelaMetaModel(PamelaMetaModelLibrary.getCompoundModelContext(Subject.class, Resource.class));
		PamelaModelFactory factory = new PamelaModelFactory(context);

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
		PamelaMetaModel context = new PamelaMetaModel(PamelaMetaModelLibrary.getCompoundModelContext(Subject.class, Resource.class));
		PamelaModelFactory factory = new PamelaModelFactory(context);

		PermissionChecker checker = factory.newInstance(PermissionChecker.class);
		Resource r1 = factory.newInstance(Resource.class, "ID", 3.14, checker);

		try {
			r1.setChecker(null);
			fail();
		} catch (ModelExecutionException e) {
			e.printStackTrace();
		}
	}

	public void testSubjectIdInvariant() throws Exception {
		PamelaMetaModel context = new PamelaMetaModel(PamelaMetaModelLibrary.getCompoundModelContext(Subject.class, Resource.class));
		PamelaModelFactory factory = new PamelaModelFactory(context);

		Subject user = factory.newInstance(Subject.class, 42, "ID");

		try {
			user.setID(-1);
			fail();
		} catch (ModelExecutionException e) {
			e.printStackTrace();
		}
	}

	public void testSubjectIdInvariantBis() throws Exception {
		PamelaMetaModel context = new PamelaMetaModel(PamelaMetaModelLibrary.getCompoundModelContext(Subject.class, Resource.class));
		PamelaModelFactory factory = new PamelaModelFactory(context);

		Subject user = factory.newInstance(Subject.class, 42, "ID");

		try {
			user.setStringID("ROGUE");
			fail();
		} catch (ModelExecutionException e) {
			e.printStackTrace();
		}
	}

}
