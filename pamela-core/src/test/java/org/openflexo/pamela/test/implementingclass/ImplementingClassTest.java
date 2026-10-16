package org.openflexo.pamela.test.implementingclass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.test.AbstractPAMELATest;
import org.openflexo.pamela.test.implementingclass.impl.ItemImpl;

/**
 * An implementing class set at run time, in another package than the implemented interface (PAMELA-D-3)
 */
public class ImplementingClassTest extends AbstractPAMELATest {

	@Test
	public void testImplementingClassInAnotherPackage() throws Exception {
		PamelaModelFactory factory = new PamelaModelFactory(Item.class);
		factory.setImplementingClassForInterface(ItemImpl.class, Item.class);

		Item item = factory.newInstance(Item.class);
		item.setName("foo");
		assertEquals("Item foo", item.describe());

		assertSame(ItemImpl.class, factory.getImplementingClassForInterface(Item.class));
		ProxyMethodHandler<Item> handler = factory.getHandler(item);
		assertSame(ItemImpl.class, handler.getOverridingSuperClass());
		assertSame(ItemImpl.class, handler.getSuperClass());
	}
}
