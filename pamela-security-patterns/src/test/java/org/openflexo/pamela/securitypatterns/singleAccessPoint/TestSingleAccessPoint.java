package org.openflexo.pamela.securitypatterns.singleAccessPoint;

import junit.framework.TestCase;
import org.junit.Test;
import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.securitypatterns.singleAccessPoint.model.*;

public class TestSingleAccessPoint extends TestCase {

    @Test
    public void testPatternAnalysis() throws Exception{
        ModelContext context = new ModelContext(Accessor.class);

        assertEquals(1, context.getPatternDefinitions(SingleAccessPointPatternDefinition.class).size());
        SingleAccessPointPatternDefinition patternDefinition = context.getPatternDefinitions(SingleAccessPointPatternDefinition.class).get(0);

        assertEquals(ProtectedSystem.class, patternDefinition.getSystemEntity().getImplementedInterface());
        assertEquals(ProtectedSystem.class.getMethod("onEntry", int.class), patternDefinition.getCheckpoint());
        assertEquals(1, patternDefinition.getCheckpointParams().size());
        assertEquals((Integer)0, patternDefinition.getCheckpointParams().get(ProtectedSystem.PARAM_ID));

        assertEquals(1, patternDefinition.getAccessorEntities().size());
        assertTrue(patternDefinition.getAccessorEntities().containsKey(context.getModelEntity(Accessor.class)));
        SingleAccessPointPatternDefinition.AccessorWrapper wrapper = patternDefinition.getAccessorEntities().get(context.getModelEntity(Accessor.class));
        assertEquals(Accessor.class.getMethod("getToken"), wrapper.getGettersMap().get(ProtectedSystem.PARAM_ID));
    }

    @Test
    public void testValidAccess() throws Exception{
        ModelContext context = new ModelContext(Accessor.class);
        ModelFactory factory = new ModelFactory(context);

        ProtectedSystem system = factory.newInstance(ProtectedSystem.class);
        Accessor accessor = factory.newInstance(Accessor.class);
        accessor.setSystem(system);
        accessor.setToken(42);
        accessor.first();
        assertEquals(1,system.getCounter());
        assertTrue(system.HasChecked());
        assertTrue(system.getCheck1());
        system.setHasChecked(false);
        assertEquals(18, accessor.second());
        assertTrue(system.HasChecked());
        assertEquals(2,system.getCounter());

    }

    @Test
    public void testUnauthorizedAccess() throws Exception{
        ModelContext context = new ModelContext(Accessor.class);
        ModelFactory factory = new ModelFactory(context);

        ProtectedSystem system = factory.newInstance(ProtectedSystem.class);
        Accessor accessor = factory.newInstance(Accessor.class);
        accessor.setSystem(system);
        accessor.setToken(0);
        try{
            accessor.first();
            fail();
        }
        catch (ModelExecutionException e){
            System.out.println("toto");
            assertEquals(1,system.getCounter());
            assertTrue(system.HasChecked());
            assertFalse(system.getCheck1());
            system.setHasChecked(false);
            try {
                accessor.second();
                fail();
            }
            catch (ModelExecutionException ee){
                assertTrue(system.HasChecked());
                assertEquals(2,system.getCounter());
            }
        }
    }
}
