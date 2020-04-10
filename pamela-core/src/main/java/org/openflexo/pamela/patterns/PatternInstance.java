package org.openflexo.pamela.patterns;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class PatternInstance<P extends PatternDefinition> {

	private P patternDefinition;

	public PatternInstance(P patternDefinition) {
		this.patternDefinition = patternDefinition;
	}

	public P getPatternDefinition() {
		return patternDefinition;
	}

	protected void registerStakeHolder(Object stakeHolder, String role) {
		patternDefinition.getModelContext().registerStakeHolderForPatternInstance(stakeHolder, role, this);
	}

	public abstract ReturnWrapper processMethodBeforeInvoke(Object instance, Method method /*, Class klass*/, Object[] args)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException;

}
