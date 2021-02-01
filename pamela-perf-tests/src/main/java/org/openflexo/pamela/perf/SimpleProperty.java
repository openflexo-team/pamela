package org.openflexo.pamela.perf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.kvc.InvalidKeyValuePropertyException;

public class SimpleProperty extends AbstractProperty {

	private String setterName;

	public SimpleProperty(String name, Type type, PerformanceModel model) {
		super(name, type, model);
		String propertyNameWithFirstCharToUpperCase = name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
		setterName = "set" + propertyNameWithFirstCharToUpperCase;
	}

	public String getSetterName() {
		return setterName;
	}

	@Override
	public String getPlainInternalCode() {
		try {
			return fromTemplate(Templating.PLAIN_JAVA_SIMPLE_PROPERTY_INTERNAL_CODE_TEMPLATE);
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

	@Override
	public String getPlainGetterCode() {
		try {
			return fromTemplate(Templating.PLAIN_JAVA_SIMPLE_PROPERTY_GETTER_CODE_TEMPLATE);
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

	public String getPlainSetterCode() {
		try {
			if (TypeUtils.isPrimitive(getType())) {
				return fromTemplate(Templating.PLAIN_JAVA_PRIMITIVE_PROPERTY_SETTER_CODE_TEMPLATE);
			}
			else {
				return fromTemplate(Templating.PLAIN_JAVA_PROPERTY_SETTER_CODE_TEMPLATE);
			}
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

}
