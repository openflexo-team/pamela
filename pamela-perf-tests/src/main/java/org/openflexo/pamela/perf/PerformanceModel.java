package org.openflexo.pamela.perf;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.kvc.InvalidKeyValuePropertyException;
import org.openflexo.toolbox.FileUtils;

public class PerformanceModel {

	private List<Entity> entities;
	private String basePackageName = "org.openflexo.pamela.perf.generated";

	public PerformanceModel(int depth) {
		entities = new ArrayList<>();

		// int index = 1;
		/*Entity entity =*/ makeEntity(1, depth);
		// entities.add(entity);

		/*int index = 1;
		int currentDepth = depth;
		Entity parentEntity = null;
		while (currentDepth >=1) {
			Entity entity = new Entity("entity"+index, this)
		}
		
		entities.add(makeEntity("entity1"));*/
	}

	private Entity makeEntity(int index, int depth) {
		Entity returned = new Entity("entity" + index, this);
		entities.add(returned);
		if (depth > 1) {
			Entity child = makeEntity(index + 1, depth - 1);
			returned.setChildEntity(child);
			child.setParentEntity(returned);
			SimpleProperty parentProperty = new SimpleProperty("parent", returned, this);
			MultipleProperty childProperty = new MultipleProperty(child.getName(), child, this);
			child.addToProperties(parentProperty);
			returned.addToProperties(childProperty);
			returned.setChildEntityProperty(childProperty);
			child.setParentEntityProperty(parentProperty);
		}
		return returned;
	}

	public void generateSourceCode() {
		String currentDir = System.getProperty("user.dir");
		System.out.println("currentDir=" + currentDir);
		File generatedDirectory = new File(currentDir + "/build/generated-sources/main");
		File plainCodeGeneratedDirectory = new File(generatedDirectory, getPlainPackageName().replace(".", "/"));
		File pamelaCodeGeneratedDirectory = new File(generatedDirectory, getPamelaPackageName().replace(".", "/"));
		for (Entity entity : entities) {
			entity.generateSourceCode(plainCodeGeneratedDirectory, pamelaCodeGeneratedDirectory);
		}
		generatePlainMainClass(plainCodeGeneratedDirectory);
		generatePamelaMainClass(pamelaCodeGeneratedDirectory);
	}

	private void generatePlainMainClass(File plainCodeGeneratedDirectory) {
		File output = new File(plainCodeGeneratedDirectory, "Main.java");
		System.out.println("File: " + output);

		// Object contents = MultipleParametersBindingEvaluator.evaluateBinding("Coucou+{$name}", this, name);
		// System.out.println("contents = " + contents + " of " + contents.getClass());

		// String contents = "Coucou+{$name.substring(0,2)}+ prout={$plainPackageName}";
		try {
			FileUtils.saveToFile(output, fromTemplate(Templating.PLAIN_MAIN_JAVA_CLASS_TEMPLATE));
		} catch (InvalidKeyValuePropertyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TypeMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullReferenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void generatePamelaMainClass(File pamelaCodeGeneratedDirectory) {
		File output = new File(pamelaCodeGeneratedDirectory, "Main.java");
		System.out.println("File: " + output);

		// Object contents = MultipleParametersBindingEvaluator.evaluateBinding("Coucou+{$name}", this, name);
		// System.out.println("contents = " + contents + " of " + contents.getClass());

		// String contents = "Coucou+{$name.substring(0,2)}+ prout={$plainPackageName}";
		try {
			FileUtils.saveToFile(output, fromTemplate(Templating.PAMELA_MAIN_JAVA_CLASS_TEMPLATE));
		} catch (InvalidKeyValuePropertyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TypeMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullReferenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String fromTemplate(String contents)
			throws InvalidKeyValuePropertyException, TypeMismatchException, NullReferenceException, InvocationTargetException {
		return Templating.fromTemplate(contents, this);
	}

	public String getPlainInternalCode() {
		StringBuffer sb = new StringBuffer();
		for (Entity entity : entities) {
			sb.append(entity.getPlainCallerCode());
		}
		return sb.toString();
	}

	public String getPamelaInternalCode() {
		StringBuffer sb = new StringBuffer();
		for (Entity entity : entities) {
			sb.append(entity.getPamelaCallerCode());
		}
		return sb.toString();
	}

	public List<Entity> getEntities() {
		return entities;
	}

	public String getPlainPackageName() {
		return basePackageName + ".plain";
	}

	public String getPamelaPackageName() {
		return basePackageName + ".pamela";
	}

	public static void main(String[] args) {
		PerformanceModel generatedModel = new PerformanceModel(4);
		generatedModel.generateSourceCode();
	}
}
