package org.openflexo.pamela.jml;

import java.lang.reflect.Method;

import org.openflexo.pamela.ModelEntity;
import org.openflexo.pamela.PamelaUtils;
import org.openflexo.pamela.annotations.jml.Assignable;
import org.openflexo.pamela.annotations.jml.Ensures;
import org.openflexo.pamela.annotations.jml.Requires;

/**
 * Wraps JML instructions for a given {@link Method} relative to a {@link ModelEntity}
 * 
 * @author sylvain
 *
 * @param <I>
 */
public class JMLMethodDefinition<I> {

	private final ModelEntity<I> entity;
	private final Method method;
	private String signature;

	private JMLAssignable<I> assignable;
	private JMLRequires<I> requires;
	private JMLEnsures<I> ensures;

	public static boolean hasJMLAnnotations(Method method) {
		return method.isAnnotationPresent(Assignable.class) || method.isAnnotationPresent(Ensures.class)
				|| method.isAnnotationPresent(Requires.class);
	}

	public JMLMethodDefinition(Method method, ModelEntity<I> entity) {
		this.method = method;
		this.entity = entity;
		signature = PamelaUtils.getSignature(method, entity.getImplementedInterface(), true);
		if (method.isAnnotationPresent(Assignable.class)) {
			assignable = new JMLAssignable<>(method.getAnnotation(Assignable.class), entity, method);
		}
		if (method.isAnnotationPresent(Requires.class)) {
			requires = new JMLRequires<>(method.getAnnotation(Requires.class), entity, method);
		}
		if (method.isAnnotationPresent(Ensures.class)) {
			ensures = new JMLEnsures<>(method.getAnnotation(Ensures.class), entity, method);
		}

		// System.out.println("JMLMethodDefinition: " + getSignature());
		// System.out.println("Assignable=" + assignable);
		// System.out.println("Requires=" + requires);
		// System.out.println("Ensures=" + ensures);
	}

	public Method getMethod() {
		return method;
	}

	public String getSignature() {
		return signature;
	}

	public ModelEntity<I> getEntity() {
		return entity;
	}

	public JMLAssignable<I> getAssignable() {
		return assignable;
	}

	public JMLRequires<I> getRequires() {
		return requires;
	}

	public JMLEnsures<I> getEnsures() {
		return ensures;
	}

}
