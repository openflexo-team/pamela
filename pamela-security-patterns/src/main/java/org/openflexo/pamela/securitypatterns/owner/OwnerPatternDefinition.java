package org.openflexo.pamela.securitypatterns.owner;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModel;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;
import org.openflexo.pamela.patterns.PatternDefinition;

public class OwnerPatternDefinition extends PatternDefinition {
	protected ModelEntity<?> ownedObjectEntity;
	protected Method ownerGetter;
	protected String message;
	protected boolean isValid;
	protected Set<Method> pureMethods;
	protected ModelEntity<?> ownerEntity;
	protected ModelProperty ownerProperty;

	public OwnerPatternDefinition(String identifier, PamelaMetaModel pamelaMetaModel) {
		super(identifier, pamelaMetaModel);
		this.isValid = true;
		this.pureMethods = new HashSet<>();
		this.message = "\n";
	}

	@Override
	public void finalizeDefinition() throws ModelDefinitionException {
		if (ownerGetter == null) {
			this.isValid = false;
			message += "Invalid OwnedObject entity for pattern " + this.getIdentifier() + ": no owner getter.\n";
		}
		if (!isValid) {
			throw new ModelDefinitionException(this.message);
		}
		this.ownerProperty = this.ownedObjectEntity.getPropertyForMethod(this.ownerGetter);
		this.ownerEntity = this.getMetaModel().getModelEntity(this.ownerGetter.getReturnType());
	}

	@Override
	public boolean isMethodInvolvedInPattern(Method m) {
		try {
			this.ownerEntity.getImplementedInterface().getMethod(m.getName(), m.getParameterTypes());
			return true;
		} catch (NoSuchMethodException e) {
			try {
				this.ownedObjectEntity.getImplementedInterface().getMethod(m.getName(), m.getParameterTypes());
				return true;
			} catch (NoSuchMethodException ignored) {

			}
		}
		return false;
	}

	@Override
	public <I> void notifiedNewInstance(I newInstance, ModelEntity<I> modelEntity, PamelaModel model) {
		if (modelEntity == this.ownedObjectEntity) {
			OwnerPatternInstance<I> patternInstance = new OwnerPatternInstance<>(this, model, newInstance);
		}
	}
}
