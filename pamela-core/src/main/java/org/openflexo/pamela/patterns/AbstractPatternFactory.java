package org.openflexo.pamela.patterns;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.ModelEntity;

public abstract class AbstractPatternFactory<P extends PatternDefinition> {

	private Map<String, P> patternDefinitions;
	private ModelContext modelContext;

	public AbstractPatternFactory(ModelContext modelContext) {
		patternDefinitions = new HashMap<>();
		this.modelContext = modelContext;
	}

	private Class<P> getPatternDefinitionClass() {
		return (Class<P>) TypeUtils.getTypeArgument(getClass(), AbstractPatternFactory.class, 0);
	}

	protected P getPatternDefinition(String patternId, boolean createWhenNonExistant) {
		P returned = patternDefinitions.get(patternId);
		if (returned == null && createWhenNonExistant) {
			try {
				Constructor<P> constructor = getPatternDefinitionClass().getConstructor(String.class, ModelContext.class);
				returned = constructor.newInstance(patternId, modelContext);
				patternDefinitions.put(patternId, returned);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return returned;
	}

	public Map<String, P> getPatternDefinitions() {
		return patternDefinitions;
	}

	public abstract void discoverEntity(ModelEntity<?> entity);

}
