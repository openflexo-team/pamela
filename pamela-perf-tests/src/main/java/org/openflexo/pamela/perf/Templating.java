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

	public static final File PLAIN_MAIN_JAVA_CLASS_TEMPLATE_FILE;
	public static final File PLAIN_JAVA_CLASS_TEMPLATE_FILE;
	public static final File PLAIN_JAVA_SIMPLE_PROPERTY_INTERNAL_CODE_TEMPLATE_FILE;
	public static final File PLAIN_JAVA_MULTIPLE_PROPERTY_INTERNAL_CODE_TEMPLATE_FILE;
	public static final File PLAIN_JAVA_SIMPLE_PROPERTY_GETTER_CODE_TEMPLATE_FILE;
	public static final File PLAIN_JAVA_MULTIPLE_PROPERTY_GETTER_CODE_TEMPLATE_FILE;
	public static final File PLAIN_JAVA_PROPERTY_SETTER_CODE_TEMPLATE_FILE;
	public static final File PLAIN_JAVA_PRIMITIVE_PROPERTY_SETTER_CODE_TEMPLATE_FILE;
	public static final File PLAIN_JAVA_PROPERTY_ADDER_CODE_TEMPLATE_FILE;
	public static final File PLAIN_JAVA_PROPERTY_REMOVER_CODE_TEMPLATE_FILE;
	public static final File PLAIN_ENTITY_CALLER_CODE_TEMPLATE_FILE;
	public static final File PLAIN_ENTITY_CALLER_CODE_INTERNAL_TEMPLATE_FILE;

	public static final File PAMELA_MAIN_JAVA_CLASS_TEMPLATE_FILE;
	public static final File PAMELA_JAVA_CLASS_TEMPLATE_FILE;
	public static final File PAMELA_JAVA_SIMPLE_PROPERTY_GETTER_CODE_TEMPLATE_FILE;
	public static final File PAMELA_JAVA_MULTIPLE_PROPERTY_GETTER_CODE_TEMPLATE_FILE;
	public static final File PAMELA_JAVA_PROPERTY_SETTER_CODE_TEMPLATE_FILE;
	public static final File PAMELA_JAVA_PRIMITIVE_PROPERTY_SETTER_CODE_TEMPLATE_FILE;
	public static final File PAMELA_JAVA_PROPERTY_ADDER_CODE_TEMPLATE_FILE;
	public static final File PAMELA_JAVA_PROPERTY_REMOVER_CODE_TEMPLATE_FILE;
	public static final File PAMELA_ENTITY_CALLER_CODE_TEMPLATE_FILE;
	public static final File PAMELA_ENTITY_CALLER_CODE_INTERNAL_TEMPLATE_FILE;

	public static String PLAIN_MAIN_JAVA_CLASS_TEMPLATE = null;
	public static String PLAIN_JAVA_CLASS_TEMPLATE = null;
	public static String PLAIN_JAVA_SIMPLE_PROPERTY_INTERNAL_CODE_TEMPLATE = null;
	public static String PLAIN_JAVA_MULTIPLE_PROPERTY_INTERNAL_CODE_TEMPLATE = null;
	public static String PLAIN_JAVA_SIMPLE_PROPERTY_GETTER_CODE_TEMPLATE = null;
	public static String PLAIN_JAVA_MULTIPLE_PROPERTY_GETTER_CODE_TEMPLATE = null;
	public static String PLAIN_JAVA_PROPERTY_SETTER_CODE_TEMPLATE = null;
	public static String PLAIN_JAVA_PRIMITIVE_PROPERTY_SETTER_CODE_TEMPLATE = null;
	public static String PLAIN_JAVA_PROPERTY_ADDER_CODE_TEMPLATE = null;
	public static String PLAIN_JAVA_PROPERTY_REMOVER_CODE_TEMPLATE = null;
	public static String PLAIN_ENTITY_CALLER_CODE_TEMPLATE = null;
	public static String PLAIN_ENTITY_CALLER_CODE_INTERNAL_TEMPLATE = null;

	public static String PAMELA_MAIN_JAVA_CLASS_TEMPLATE = null;
	public static String PAMELA_JAVA_CLASS_TEMPLATE = null;
	public static String PAMELA_JAVA_SIMPLE_PROPERTY_INTERNAL_CODE_TEMPLATE = null;
	public static String PAMELA_JAVA_MULTIPLE_PROPERTY_INTERNAL_CODE_TEMPLATE = null;
	public static String PAMELA_JAVA_SIMPLE_PROPERTY_GETTER_CODE_TEMPLATE = null;
	public static String PAMELA_JAVA_MULTIPLE_PROPERTY_GETTER_CODE_TEMPLATE = null;
	public static String PAMELA_JAVA_PROPERTY_SETTER_CODE_TEMPLATE = null;
	public static String PAMELA_JAVA_PRIMITIVE_PROPERTY_SETTER_CODE_TEMPLATE = null;
	public static String PAMELA_JAVA_PROPERTY_ADDER_CODE_TEMPLATE = null;
	public static String PAMELA_JAVA_PROPERTY_REMOVER_CODE_TEMPLATE = null;
	public static String PAMELA_ENTITY_CALLER_CODE_TEMPLATE = null;
	public static String PAMELA_ENTITY_CALLER_CODE_INTERNAL_TEMPLATE = null;

	static {
		String currentDir = System.getProperty("user.dir");
		System.out.println("currentDir=" + currentDir);

		File plainTemplateDirectory = new File(currentDir + "/src/main/resources/Templates/Plain");
		PLAIN_MAIN_JAVA_CLASS_TEMPLATE_FILE = new File(plainTemplateDirectory, "MainJavaClass.java.tpl");
		PLAIN_JAVA_CLASS_TEMPLATE_FILE = new File(plainTemplateDirectory, "PlainJavaClass.java.tpl");
		PLAIN_JAVA_SIMPLE_PROPERTY_INTERNAL_CODE_TEMPLATE_FILE = new File(plainTemplateDirectory, "PlainSimplePropertyInternalCode.tpl");
		PLAIN_JAVA_MULTIPLE_PROPERTY_INTERNAL_CODE_TEMPLATE_FILE = new File(plainTemplateDirectory,
				"PlainMultiplePropertyInternalCode.tpl");
		PLAIN_JAVA_SIMPLE_PROPERTY_GETTER_CODE_TEMPLATE_FILE = new File(plainTemplateDirectory, "PlainSimplePropertyGetterCode.tpl");
		PLAIN_JAVA_MULTIPLE_PROPERTY_GETTER_CODE_TEMPLATE_FILE = new File(plainTemplateDirectory, "PlainMultiplePropertyGetterCode.tpl");
		PLAIN_JAVA_PROPERTY_SETTER_CODE_TEMPLATE_FILE = new File(plainTemplateDirectory, "PlainPropertySetterCode.tpl");
		PLAIN_JAVA_PRIMITIVE_PROPERTY_SETTER_CODE_TEMPLATE_FILE = new File(plainTemplateDirectory, "PlainPrimitivePropertySetterCode.tpl");
		PLAIN_JAVA_PROPERTY_ADDER_CODE_TEMPLATE_FILE = new File(plainTemplateDirectory, "PlainPropertyAdderCode.tpl");
		PLAIN_JAVA_PROPERTY_REMOVER_CODE_TEMPLATE_FILE = new File(plainTemplateDirectory, "PlainPropertyRemoverCode.tpl");
		PLAIN_ENTITY_CALLER_CODE_TEMPLATE_FILE = new File(plainTemplateDirectory, "EntityCallerCode.tpl");
		PLAIN_ENTITY_CALLER_CODE_INTERNAL_TEMPLATE_FILE = new File(plainTemplateDirectory, "EntityCallerCodeInternal.tpl");
		try {
			PLAIN_MAIN_JAVA_CLASS_TEMPLATE = FileUtils.fileContents(PLAIN_MAIN_JAVA_CLASS_TEMPLATE_FILE);
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
			PLAIN_ENTITY_CALLER_CODE_TEMPLATE = FileUtils.fileContents(PLAIN_ENTITY_CALLER_CODE_TEMPLATE_FILE);
			PLAIN_ENTITY_CALLER_CODE_INTERNAL_TEMPLATE = FileUtils.fileContents(PLAIN_ENTITY_CALLER_CODE_INTERNAL_TEMPLATE_FILE);
		} catch (IOException e) {
			e.printStackTrace();
		}

		File pamelaTemplateDirectory = new File(currentDir + "/src/main/resources/Templates/Pamela");
		PAMELA_MAIN_JAVA_CLASS_TEMPLATE_FILE = new File(pamelaTemplateDirectory, "MainJavaClass.java.tpl");
		PAMELA_JAVA_CLASS_TEMPLATE_FILE = new File(pamelaTemplateDirectory, "PamelaJavaClass.java.tpl");
		PAMELA_JAVA_SIMPLE_PROPERTY_GETTER_CODE_TEMPLATE_FILE = new File(pamelaTemplateDirectory, "PamelaSimplePropertyGetterCode.tpl");
		PAMELA_JAVA_MULTIPLE_PROPERTY_GETTER_CODE_TEMPLATE_FILE = new File(pamelaTemplateDirectory, "PamelaMultiplePropertyGetterCode.tpl");
		PAMELA_JAVA_PROPERTY_SETTER_CODE_TEMPLATE_FILE = new File(pamelaTemplateDirectory, "PamelaPropertySetterCode.tpl");
		PAMELA_JAVA_PRIMITIVE_PROPERTY_SETTER_CODE_TEMPLATE_FILE = new File(pamelaTemplateDirectory,
				"PamelaPrimitivePropertySetterCode.tpl");
		PAMELA_JAVA_PROPERTY_ADDER_CODE_TEMPLATE_FILE = new File(pamelaTemplateDirectory, "PamelaPropertyAdderCode.tpl");
		PAMELA_JAVA_PROPERTY_REMOVER_CODE_TEMPLATE_FILE = new File(pamelaTemplateDirectory, "PamelaPropertyRemoverCode.tpl");
		PAMELA_ENTITY_CALLER_CODE_TEMPLATE_FILE = new File(pamelaTemplateDirectory, "EntityCallerCode.tpl");
		PAMELA_ENTITY_CALLER_CODE_INTERNAL_TEMPLATE_FILE = new File(pamelaTemplateDirectory, "EntityCallerCodeInternal.tpl");
		try {
			PAMELA_MAIN_JAVA_CLASS_TEMPLATE = FileUtils.fileContents(PAMELA_MAIN_JAVA_CLASS_TEMPLATE_FILE);
			PAMELA_JAVA_CLASS_TEMPLATE = FileUtils.fileContents(PAMELA_JAVA_CLASS_TEMPLATE_FILE);
			PAMELA_JAVA_SIMPLE_PROPERTY_GETTER_CODE_TEMPLATE = FileUtils
					.fileContents(PAMELA_JAVA_SIMPLE_PROPERTY_GETTER_CODE_TEMPLATE_FILE);
			PAMELA_JAVA_MULTIPLE_PROPERTY_GETTER_CODE_TEMPLATE = FileUtils
					.fileContents(PAMELA_JAVA_MULTIPLE_PROPERTY_GETTER_CODE_TEMPLATE_FILE);
			PAMELA_JAVA_PROPERTY_SETTER_CODE_TEMPLATE = FileUtils.fileContents(PAMELA_JAVA_PROPERTY_SETTER_CODE_TEMPLATE_FILE);
			PAMELA_JAVA_PRIMITIVE_PROPERTY_SETTER_CODE_TEMPLATE = FileUtils
					.fileContents(PAMELA_JAVA_PRIMITIVE_PROPERTY_SETTER_CODE_TEMPLATE_FILE);
			PAMELA_JAVA_PROPERTY_ADDER_CODE_TEMPLATE = FileUtils.fileContents(PAMELA_JAVA_PROPERTY_ADDER_CODE_TEMPLATE_FILE);
			PAMELA_JAVA_PROPERTY_REMOVER_CODE_TEMPLATE = FileUtils.fileContents(PAMELA_JAVA_PROPERTY_REMOVER_CODE_TEMPLATE_FILE);
			PAMELA_ENTITY_CALLER_CODE_TEMPLATE = FileUtils.fileContents(PAMELA_ENTITY_CALLER_CODE_TEMPLATE_FILE);
			PAMELA_ENTITY_CALLER_CODE_INTERNAL_TEMPLATE = FileUtils.fileContents(PAMELA_ENTITY_CALLER_CODE_INTERNAL_TEMPLATE_FILE);
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
