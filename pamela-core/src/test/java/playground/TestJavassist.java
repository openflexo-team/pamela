package playground;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewMethod;

public class TestJavassist {

	public static void main(String[] args) throws CannotCompileException, InstantiationException, IllegalAccessException,
			NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		ClassPool pool = ClassPool.getDefault();
		CtClass evalClass = pool.makeClass("Eval");
		evalClass.addMethod(CtNewMethod.make("public double eval (double x) { return (" + args[0] + ") ; }", evalClass));
		Class clazz = evalClass.toClass();
		Object obj = clazz.newInstance();
		Class[] formalParams = new Class[] { double.class };
		Method meth = clazz.getDeclaredMethod("eval", formalParams);
		Object[] actualParams = new Object[] { Double.valueOf(17) };
		double result = ((Double) meth.invoke(obj, actualParams)).doubleValue();
		System.out.println(result);
	}

}
