package org.openflexo.pamela.test;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openflexo.connie.type.ParameterizedTypeImpl;
import org.openflexo.connie.type.WildcardTypeImpl.DefaultWildcardType;
import org.openflexo.pamela.converter.TypeConverter;

public class TestTypeConverter extends AbstractPAMELATest {

	private TypeConverter typeConverter;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Override
	@Before
	public void setUp() throws Exception {
		typeConverter = new TypeConverter(null);

		/*new File("/tmp").mkdirs();
		pamelaMetaModel = new PamelaMetaModel(FlexoProcess.class);
		factory = new PamelaModelFactory(pamelaMetaModel);*/
	}

	@Override
	@After
	public void tearDown() throws Exception {
	}

	public void test1() throws Exception {
		assertEquals(String.class, typeConverter.convertFromString("java.lang.String", null));
	}

	public void test2() throws Exception {
		assertEquals(List.class, typeConverter.convertFromString("java.util.List", null));
	}

	public void test3() throws Exception {
		assertEquals(new ParameterizedTypeImpl(List.class, String.class),
				typeConverter.convertFromString("java.util.List<java.lang.String>", null));
	}

	public void test4() throws Exception {
		assertEquals(new ParameterizedTypeImpl(Map.class, String.class, new ParameterizedTypeImpl(Map.class, String.class, Object.class)),
				typeConverter.convertFromString("java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.Object>>", null));
	}

	public void test5() throws Exception {
		assertEquals(DefaultWildcardType.makeUpperBoundWilcard(Object.class),
				typeConverter.convertFromString("? extends java.lang.Object", null));
	}

	public void test6() throws Exception {
		assertEquals(new DefaultWildcardType(), typeConverter.convertFromString("?", null));
	}

	public void test7() throws Exception {
		assertEquals(
				new ParameterizedTypeImpl(Map.class, DefaultWildcardType.makeUpperBoundWilcard(Object.class),
						DefaultWildcardType.makeUpperBoundWilcard(new ParameterizedTypeImpl(List.class, new DefaultWildcardType()))),
				typeConverter.convertFromString("java.util.Map<? extends java.lang.Object, ? extends java.util.List<?>>", null));
	}

}
