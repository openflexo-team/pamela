package org.openflexo.pamela.securitypatterns.owner;

import junit.framework.TestCase;
import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.securitypatterns.owner.model.MyObject;
import org.openflexo.pamela.securitypatterns.owner.model.MyOwner;

import java.util.concurrent.ExecutionException;

public class TestOwner extends TestCase {

    @Test
    public void testPatternAnalysis() throws Exception {
        PamelaMetaModel context = new PamelaMetaModel(MyObject.class);

        assertEquals(1, context.getPatternDefinitions(OwnerPatternDefinition.class).size());
        OwnerPatternDefinition patternDefinition = context.getPatternDefinitions(OwnerPatternDefinition.class).get(0);
        assertEquals(context.getModelEntity(MyObject.class), patternDefinition.ownedObjectEntity);
        assertEquals(MyObject.class.getMethod("getOwner"),patternDefinition.ownerGetter);
        assertEquals(2,patternDefinition.pureMethods.size());
        assertTrue(patternDefinition.pureMethods.contains(MyObject.class.getMethod("getOwner")));
        assertTrue(patternDefinition.pureMethods.contains(MyObject.class.getMethod("getOK")));
        assertTrue(patternDefinition.isValid);
    }

    @Test
    public void testAuthorizedAccess() throws Exception {
        PamelaMetaModel context = new PamelaMetaModel(MyObject.class);
        PamelaModelFactory factory = new PamelaModelFactory(context);

        MyObject object = factory.newInstance(MyObject.class);
        assertEquals(42, object.method2(42));
        object.setOK(false);
        MyOwner owner = factory.newInstance(MyOwner.class);
        owner.setObject(object);
        object.setOwner(owner);
        assertEquals(owner, object.getOwner());
        owner.m1();
        assertTrue(object.getOK());
        owner.initOK();
        assertFalse(object.getOK());
        assertEquals(42,owner.m2());
    }

    @Test
    public void testUnauthorizedAccess() throws Exception{
        PamelaMetaModel context = new PamelaMetaModel(MyObject.class);
        PamelaModelFactory factory = new PamelaModelFactory(context);

        MyObject object = factory.newInstance(MyObject.class);
        assertEquals(42, object.method2(42));
        object.setOK(false);
        MyOwner owner = factory.newInstance(MyOwner.class);
        MyOwner rogue = factory.newInstance(MyOwner.class);
        rogue.setObject(object);
        object.setOwner(owner);
        assertEquals(owner, object.getOwner());
        try {
            rogue.m1();
            fail();
        }
        catch (ModelExecutionException e){
            assertFalse(object.getOK());
            try {
                assertEquals(42,rogue.m2());
                fail();
            }
            catch (ModelExecutionException ee){
                ee.printStackTrace();
            }
        }

    }
}
