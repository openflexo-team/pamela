package org.flexo.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Implementation;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.factory.ModelFactory;

/**
 * Unit test for <a href="https://bugs.openflexo.org/browse/PAMELA-27">PAMELA-27</a>
 * 
 * Method dispatch execution issue while using partial implementations with @Implementation
 * 
 * @author sylvain
 *
 */
public class DelegateImplementationTest {

	@ModelEntity
	public interface AnEntity {
		String FOO = "foo";

		@Getter(value = FOO, defaultValue = "4")
		@XMLAttribute(xmlTag = FOO)
		int getFoo();

		@Setter(FOO)
		void setFoo(int foo);

		@Implementation
		public static abstract class AnEntityImpl1 implements AnEntity {
			@Override
			public int getFoo() {
				return 10;
			}
		}

		@Implementation
		public static abstract class AnEntityImpl2 implements AnEntity {
			@Override
			public String toString() {
				return "ShouldReturnThis";
			}
		}
	}

	private ModelFactory factory;
	private ModelContext modelContext;

	@Before
	public void setUp() throws Exception {
		new File("/tmp").mkdirs();
		modelContext = new ModelContext(AnEntity.class);
		factory = new ModelFactory(modelContext);
	}

	@Test
	public void testGetterImplementation() {
		assertNotNull(factory.getModelContext().getModelEntity(AnEntity.class));
		AnEntity entity = factory.newInstance(AnEntity.class);
		assertEquals(10, entity.getFoo());
	}

	@Test
	public void testToString() {
		assertNotNull(factory.getModelContext().getModelEntity(AnEntity.class));
		AnEntity entity = factory.newInstance(AnEntity.class);
		assertEquals("ShouldReturnThis", entity.toString());
	}
}
