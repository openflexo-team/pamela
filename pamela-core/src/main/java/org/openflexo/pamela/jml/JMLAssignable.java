package org.openflexo.pamela.jml;

import java.lang.reflect.Method;

import org.openflexo.pamela.ModelEntity;
import org.openflexo.pamela.annotations.jml.Assignable;

public class JMLAssignable<I> extends JMLExpressionBasedOnMethod<Object, I> {

	public JMLAssignable(Assignable annotation, ModelEntity<I> entity, Method method) {
		super(entity, method);
		init(annotation.value(), Object.class);
	}

}
