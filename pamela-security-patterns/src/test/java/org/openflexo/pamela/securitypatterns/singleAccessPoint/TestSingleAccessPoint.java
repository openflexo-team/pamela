package org.openflexo.pamela.securitypatterns.singleAccessPoint;

import junit.framework.TestCase;
import org.junit.Test;
import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.securitypatterns.singleAccessPoint.model.*;

public class TestSingleAccessPoint extends TestCase {
    static ModelContext context;

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
        context = new ModelContext(Accessor.class);
        ModelFactory factory = new ModelFactory(context);

        ProtectedSystem system = factory.newInstance(ProtectedSystem.class);
        Accessor accessor = factory.newInstance(Accessor.class);
        accessor.setSystem(system);
        accessor.setToken(42);
        accessor.first();
        assertTrue(accessor.getCounter() >= 1);
        assertTrue(accessor.HasChecked());
        assertTrue(accessor.getCheck1());
        accessor.setHasChecked(false);
        assertEquals(18, accessor.second());
        assertTrue(accessor.HasChecked());
        assertTrue(accessor.getCounter() >= 2);

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
            accessor.setToken(42);
            assertTrue(accessor.getCounter() >= 1);
            assertTrue(accessor.HasChecked());
            assertFalse(accessor.getCheck1());
            accessor.setHasChecked(false);
            accessor.setToken(0);
            try {
                accessor.second();
                fail();
            }
            catch (ModelExecutionException ee){
                accessor.setToken(42);
                assertTrue(accessor.HasChecked());
                assertTrue(accessor.getCounter() >= 2);
            }
        }
    }
}
