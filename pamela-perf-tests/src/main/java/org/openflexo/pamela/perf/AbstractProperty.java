package org.openflexo.pamela.perf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.kvc.InvalidKeyValuePropertyException;
import org.openflexo.toolbox.JavaUtils;

public abstract class AbstractProperty {

	private PerformanceModel model;
	private String name;
	private String getterName;
	private Type type;

	public AbstractProperty(String name, Type type, PerformanceModel model) {
		this.model = model;
		this.type = type;
		this.name = JavaUtils.getVariableName(name);

		String propertyNameWithFirstCharToUpperCase = name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
		getterName = "get" + propertyNameWithFirstCharToUpperCase;

	}

	public String getTypeAsString() {
		return TypeUtils.simpleRepresentation(type);
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	public String getInternalVariableName() {
		return name;
	}

	public String getGetterName() {
		return getterName;
	}

	public abstract String getPamelaDefaultValue();

	public abstract String getPlainInternalCode();

	public abstract String getPlainGetterCode();

	public abstract String getPamelaGetterCode();

	protected String fromTemplate(String contents)
			throws InvalidKeyValuePropertyException, TypeMismatchException, NullReferenceException, InvocationTargetException {
		return Templating.fromTemplate(contents, this);
	}

}
