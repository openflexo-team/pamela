package org.openflexo.pamela.perf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.kvc.InvalidKeyValuePropertyException;
import org.openflexo.toolbox.JavaUtils;

public class MultipleProperty extends AbstractProperty {

	private String adderName;
	private String removerName;
	private String paramName;
	private String internalVariableName;

	public MultipleProperty(String name, Type type, PerformanceModel model) {
		super(name, type, model);
		String propertyNameWithFirstCharToUpperCase = name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
		String finalS = propertyNameWithFirstCharToUpperCase.endsWith("s") ? "" : "s";
		adderName = "addTo" + propertyNameWithFirstCharToUpperCase + finalS;
		removerName = "removeFrom" + propertyNameWithFirstCharToUpperCase + finalS;
		paramName = "a" + propertyNameWithFirstCharToUpperCase;
		internalVariableName = JavaUtils.getVariableName(name) + finalS;
	}

	public String getAdderName() {
		return adderName;
	}

	public String getRemoverName() {
		return removerName;
	}

	public String getParamName() {
		return paramName;
	}

	@Override
	public String getInternalVariableName() {
		return internalVariableName;
	}

	@Override
	public String getPamelaDefaultValue() {
		return "";
	}

	@Override
	public String getPlainInternalCode() {
		try {
			return fromTemplate(Templating.PLAIN_JAVA_MULTIPLE_PROPERTY_INTERNAL_CODE_TEMPLATE);
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
			return fromTemplate(Templating.PLAIN_JAVA_MULTIPLE_PROPERTY_GETTER_CODE_TEMPLATE);
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

	public String getPlainAdderCode() {
		try {
			return fromTemplate(Templating.PLAIN_JAVA_PROPERTY_ADDER_CODE_TEMPLATE);
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

	public String getPlainRemoverCode() {
		try {
			return fromTemplate(Templating.PLAIN_JAVA_PROPERTY_REMOVER_CODE_TEMPLATE);
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
	public String getPamelaGetterCode() {
		try {
			return fromTemplate(Templating.PAMELA_JAVA_MULTIPLE_PROPERTY_GETTER_CODE_TEMPLATE);
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

	public String getPamelaAdderCode() {
		try {
			return fromTemplate(Templating.PAMELA_JAVA_PROPERTY_ADDER_CODE_TEMPLATE);
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

	public String getPamelaRemoverCode() {
		try {
			return fromTemplate(Templating.PAMELA_JAVA_PROPERTY_REMOVER_CODE_TEMPLATE);
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
