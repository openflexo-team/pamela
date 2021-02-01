package org.openflexo.pamela.perf;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.kvc.InvalidKeyValuePropertyException;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.JavaUtils;

public class Entity implements CustomType {

	private PerformanceModel model;
	private String name;
	private List<AbstractProperty> properties;
	private Entity childEntity;
	private Entity parentEntity;
	private MultipleProperty childEntityProperty;
	private SimpleProperty parentEntityProperty;

	public Entity(String name, PerformanceModel model) {
		this.model = model;
		this.name = JavaUtils.getClassName(name);
		properties = new ArrayList<>();
		addToProperties(new SimpleProperty("aString", String.class, model));
		addToProperties(new SimpleProperty("anInteger", Integer.TYPE, model));
		addToProperties(new SimpleProperty("aDouble", Double.TYPE, model));
	}

	public String getName() {
		return name;
	}

	public String getPlainPackageName() {
		return model.getPlainPackageName();
	}

	public void addToProperties(AbstractProperty property) {
		properties.add(property);
	}

	public String getPropertiesInternalCode() {
		StringBuffer sb = new StringBuffer();
		for (AbstractProperty property : properties) {
			sb.append(property.getPlainInternalCode());
		}
		return sb.toString();
	}

	public String getConstructorInternalCode() {
		StringBuffer sb = new StringBuffer();
		for (AbstractProperty property : properties) {
			String assignValue = "null";
			if (property instanceof SimpleProperty) {
				if (TypeUtils.isPrimitive(property.getType())) {
					assignValue = "0";
				}
			}
			else if (property instanceof MultipleProperty) {
				assignValue = "new ArrayList<" + TypeUtils.simpleRepresentation(property.getType()) + ">()";
			}
			sb.append("\t\t" + property.getInternalVariableName() + " = " + assignValue + ";\n");
		}
		return sb.toString();
	}

	public String getPropertiesCode() {
		StringBuffer sb = new StringBuffer();
		for (AbstractProperty property : properties) {
			sb.append(property.getPlainGetterCode());
			if (property instanceof SimpleProperty) {
				sb.append(((SimpleProperty) property).getPlainSetterCode());
			}
			else if (property instanceof MultipleProperty) {
				sb.append(((MultipleProperty) property).getPlainAdderCode());
				sb.append(((MultipleProperty) property).getPlainRemoverCode());
			}
		}
		return sb.toString();
	}

	public String getCallerCode() {
		try {
			return fromTemplate(Templating.ENTITY_CALLER_CODE_TEMPLATE);
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
		}
		return null;

	}

	public String getInternalCallerCode() {
		if (getChildEntity() != null) {
			try {
				return fromTemplate(Templating.ENTITY_CALLER_CODE_INTERNAL_TEMPLATE);
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
			}
			return null;
		}
		else {
			return "";
		}

	}

	public void generateSourceCode(File plainCodeGeneratedDirectory, File pamelaCodeGeneratedDirectory) {
		System.out.println("Generate " + name + ".java in " + plainCodeGeneratedDirectory);
		generatePlainCode(plainCodeGeneratedDirectory);
	}

	private void generatePlainCode(File plainCodeGeneratedDirectory) {
		File output = new File(plainCodeGeneratedDirectory, name + ".java");
		System.out.println("File: " + output);

		// Object contents = MultipleParametersBindingEvaluator.evaluateBinding("Coucou+{$name}", this, name);
		// System.out.println("contents = " + contents + " of " + contents.getClass());

		// String contents = "Coucou+{$name.substring(0,2)}+ prout={$plainPackageName}";
		try {
			FileUtils.saveToFile(output, fromTemplate(Templating.PLAIN_JAVA_CLASS_TEMPLATE));
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

	@Override
	public boolean isTypeAssignableFrom(Type aType, boolean permissive) {
		// TODO
		return false;
	}

	@Override
	public boolean isOfType(Object object, boolean permissive) {
		// TODO
		return false;
	}

	/**
	 * Return simple (human understandable) representation for this type
	 * 
	 * @return
	 */
	@Override
	public String simpleRepresentation() {
		return name;
	}

	/**
	 * Return full qualified representation (machine understandable) representation for this type
	 * 
	 * @return
	 */
	@Override
	public String fullQualifiedRepresentation() {
		return getPlainPackageName() + "." + name;
	}

	@Override
	public Class<?> getBaseClass() {
		return Object.class;
	}

	@Override
	public String getSerializationRepresentation() {
		return fullQualifiedRepresentation();
	}

	@Override
	public boolean isResolved() {
		return true;
	}

	@Override
	public void resolve() {
	}

	public Entity getChildEntity() {
		return childEntity;
	}

	public void setChildEntity(Entity childEntity) {
		this.childEntity = childEntity;
	}

	public Entity getParentEntity() {
		return parentEntity;
	}

	public void setParentEntity(Entity parentEntity) {
		this.parentEntity = parentEntity;
	}

	public MultipleProperty getChildEntityProperty() {
		return childEntityProperty;
	}

	public void setChildEntityProperty(MultipleProperty childEntityProperty) {
		this.childEntityProperty = childEntityProperty;
	}

	public SimpleProperty getParentEntityProperty() {
		return parentEntityProperty;
	}

	public void setParentEntityProperty(SimpleProperty parentEntityProperty) {
		this.parentEntityProperty = parentEntityProperty;
	}

}
