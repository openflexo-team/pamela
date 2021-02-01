package org.openflexo.pamela.perf;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.openflexo.connie.BindingEvaluator;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.kvc.InvalidKeyValuePropertyException;
import org.openflexo.toolbox.FileUtils;

public class Templating {

	public static final File MAIN_JAVA_CLASS_TEMPLATE_FILE;
	public static final File PLAIN_JAVA_CLASS_TEMPLATE_FILE;
	public static final File PLAIN_JAVA_SIMPLE_PROPERTY_INTERNAL_CODE_TEMPLATE_FILE;
	public static final File PLAIN_JAVA_MULTIPLE_PROPERTY_INTERNAL_CODE_TEMPLATE_FILE;
	public static final File PLAIN_JAVA_SIMPLE_PROPERTY_GETTER_CODE_TEMPLATE_FILE;
	public static final File PLAIN_JAVA_MULTIPLE_PROPERTY_GETTER_CODE_TEMPLATE_FILE;
	public static final File PLAIN_JAVA_PROPERTY_SETTER_CODE_TEMPLATE_FILE;
	public static final File PLAIN_JAVA_PRIMITIVE_PROPERTY_SETTER_CODE_TEMPLATE_FILE;
	public static final File PLAIN_JAVA_PROPERTY_ADDER_CODE_TEMPLATE_FILE;
	public static final File PLAIN_JAVA_PROPERTY_REMOVER_CODE_TEMPLATE_FILE;
	public static final File ENTITY_CALLER_CODE_TEMPLATE_FILE;
	public static final File ENTITY_CALLER_CODE_INTERNAL_TEMPLATE_FILE;

	public static String MAIN_JAVA_CLASS_TEMPLATE = null;
	public static String PLAIN_JAVA_CLASS_TEMPLATE = null;
	public static String PLAIN_JAVA_SIMPLE_PROPERTY_INTERNAL_CODE_TEMPLATE = null;
	public static String PLAIN_JAVA_MULTIPLE_PROPERTY_INTERNAL_CODE_TEMPLATE = null;
	public static String PLAIN_JAVA_SIMPLE_PROPERTY_GETTER_CODE_TEMPLATE = null;
	public static String PLAIN_JAVA_MULTIPLE_PROPERTY_GETTER_CODE_TEMPLATE = null;
	public static String PLAIN_JAVA_PROPERTY_SETTER_CODE_TEMPLATE = null;
	public static String PLAIN_JAVA_PRIMITIVE_PROPERTY_SETTER_CODE_TEMPLATE = null;
	public static String PLAIN_JAVA_PROPERTY_ADDER_CODE_TEMPLATE = null;
	public static String PLAIN_JAVA_PROPERTY_REMOVER_CODE_TEMPLATE = null;
	public static String ENTITY_CALLER_CODE_TEMPLATE = null;
	public static String ENTITY_CALLER_CODE_INTERNAL_TEMPLATE = null;

	static {
		String currentDir = System.getProperty("user.dir");
		System.out.println("currentDir=" + currentDir);
		File templateDirectory = new File(currentDir + "/src/main/resources/Templates");
		MAIN_JAVA_CLASS_TEMPLATE_FILE = new File(templateDirectory, "MainJavaClass.java.tpl");
		PLAIN_JAVA_CLASS_TEMPLATE_FILE = new File(templateDirectory, "PlainJavaClass.java.tpl");
		PLAIN_JAVA_SIMPLE_PROPERTY_INTERNAL_CODE_TEMPLATE_FILE = new File(templateDirectory, "PlainSimplePropertyInternalCode.tpl");
		PLAIN_JAVA_MULTIPLE_PROPERTY_INTERNAL_CODE_TEMPLATE_FILE = new File(templateDirectory, "PlainMultiplePropertyInternalCode.tpl");
		PLAIN_JAVA_SIMPLE_PROPERTY_GETTER_CODE_TEMPLATE_FILE = new File(templateDirectory, "PlainSimplePropertyGetterCode.tpl");
		PLAIN_JAVA_MULTIPLE_PROPERTY_GETTER_CODE_TEMPLATE_FILE = new File(templateDirectory, "PlainMultiplePropertyGetterCode.tpl");
		PLAIN_JAVA_PROPERTY_SETTER_CODE_TEMPLATE_FILE = new File(templateDirectory, "PlainPropertySetterCode.tpl");
		PLAIN_JAVA_PRIMITIVE_PROPERTY_SETTER_CODE_TEMPLATE_FILE = new File(templateDirectory, "PlainPrimitivePropertySetterCode.tpl");
		PLAIN_JAVA_PROPERTY_ADDER_CODE_TEMPLATE_FILE = new File(templateDirectory, "PlainPropertyAdderCode.tpl");
		PLAIN_JAVA_PROPERTY_REMOVER_CODE_TEMPLATE_FILE = new File(templateDirectory, "PlainPropertyRemoverCode.tpl");
		ENTITY_CALLER_CODE_TEMPLATE_FILE = new File(templateDirectory, "EntityCallerCode.tpl");
		ENTITY_CALLER_CODE_INTERNAL_TEMPLATE_FILE = new File(templateDirectory, "EntityCallerCodeInternal.tpl");
		try {
			MAIN_JAVA_CLASS_TEMPLATE = FileUtils.fileContents(MAIN_JAVA_CLASS_TEMPLATE_FILE);
			PLAIN_JAVA_CLASS_TEMPLATE = FileUtils.fileContents(PLAIN_JAVA_CLASS_TEMPLATE_FILE);
			PLAIN_JAVA_SIMPLE_PROPERTY_INTERNAL_CODE_TEMPLATE = FileUtils
					.fileContents(PLAIN_JAVA_SIMPLE_PROPERTY_INTERNAL_CODE_TEMPLATE_FILE);
			PLAIN_JAVA_MULTIPLE_PROPERTY_INTERNAL_CODE_TEMPLATE = FileUtils
					.fileContents(PLAIN_JAVA_MULTIPLE_PROPERTY_INTERNAL_CODE_TEMPLATE_FILE);
			PLAIN_JAVA_SIMPLE_PROPERTY_GETTER_CODE_TEMPLATE = FileUtils.fileContents(PLAIN_JAVA_SIMPLE_PROPERTY_GETTER_CODE_TEMPLATE_FILE);
			PLAIN_JAVA_MULTIPLE_PROPERTY_GETTER_CODE_TEMPLATE = FileUtils
					.fileContents(PLAIN_JAVA_MULTIPLE_PROPERTY_GETTER_CODE_TEMPLATE_FILE);
			PLAIN_JAVA_PROPERTY_SETTER_CODE_TEMPLATE = FileUtils.fileContents(PLAIN_JAVA_PROPERTY_SETTER_CODE_TEMPLATE_FILE);
			PLAIN_JAVA_PRIMITIVE_PROPERTY_SETTER_CODE_TEMPLATE = FileUtils
					.fileContents(PLAIN_JAVA_PRIMITIVE_PROPERTY_SETTER_CODE_TEMPLATE_FILE);
			PLAIN_JAVA_PROPERTY_ADDER_CODE_TEMPLATE = FileUtils.fileContents(PLAIN_JAVA_PROPERTY_ADDER_CODE_TEMPLATE_FILE);
			PLAIN_JAVA_PROPERTY_REMOVER_CODE_TEMPLATE = FileUtils.fileContents(PLAIN_JAVA_PROPERTY_REMOVER_CODE_TEMPLATE_FILE);
			ENTITY_CALLER_CODE_TEMPLATE = FileUtils.fileContents(ENTITY_CALLER_CODE_TEMPLATE_FILE);
			ENTITY_CALLER_CODE_INTERNAL_TEMPLATE = FileUtils.fileContents(ENTITY_CALLER_CODE_INTERNAL_TEMPLATE_FILE);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String fromTemplate(String contents, Object receiver)
			throws InvalidKeyValuePropertyException, TypeMismatchException, NullReferenceException, InvocationTargetException {
		int index = 0;
		String returned = contents;
		while (returned.contains("{$")) {
			int startIndex = returned.indexOf("{$");
			int endIndex = returned.indexOf("}", startIndex);
			String expression = returned.substring(startIndex + 2, endIndex);
			String evaluatedExpression = BindingEvaluator.evaluateBinding(expression, receiver).toString();
			// System.out.println("Found at index " + index + " " + expression + "=" + evaluatedExpression);
			returned = returned.substring(0, startIndex) + evaluatedExpression + returned.substring(endIndex + 1);
			index++;
		}
		return returned;
	}

}
