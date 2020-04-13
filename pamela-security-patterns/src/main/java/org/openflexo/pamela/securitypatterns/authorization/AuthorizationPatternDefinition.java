package org.openflexo.pamela.securitypatterns.authorization;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.ModelEntity;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.patterns.PatternDefinition;
import org.openflexo.pamela.securitypatterns.authorization.annotations.AccessResource;
import org.openflexo.pamela.securitypatterns.authorization.exception.InconsistentPermissionCheckerEntityException;
import playground.authorization.interfaces.PermissionChecker;

public class AuthorizationPatternDefinition extends PatternDefinition {

	private Set<ModelEntity<?>> subjects;
	private Set<ModelEntity<?>> resources;
	private ModelEntity<?> checker;

	private boolean isCorrect;

	public AuthorizationPatternDefinition(String identifier, ModelContext modelContext) {
		super(identifier, modelContext);
		this.subjects = new HashSet<>();
		this.resources = new HashSet<>();
		this.isCorrect = true;
	}

	@Override
	public void finalizeDefinition() throws ModelDefinitionException {
		if (!this.isCorrect){
			throw new InconsistentPermissionCheckerEntityException("Duplicate @AuthorizationChecker annotations with same pattern ID " + getIdentifier() + " in model.");
		}
	}

	@Override
	public <I> void notifiedNewInstance(I newInstance, ModelEntity<I> modelEntity) {
		System.out.println("Tiens on cree un " + newInstance + " of " + newInstance.getClass());
	}

	public boolean isValid() {
		// Perform here required checks
		return this.isCorrect;
	}

	@Override
	public boolean isMethodInvolvedInPattern(Method m) {
		// TODO Auto-generated method stub
		return false;
	}

	public void addSubjectModelEntity(ModelEntity<?> entity) {
		this.subjects.add(entity);
		/*for (Method m : entity.getImplementedInterface().getMethods()) {
			AccessResource accessResourceMethodAnnotation = m.getAnnotation(AccessResource.class);
			if (accessResourceMethodAnnotation != null) {
				this.attachNewAccessMethod(m,entity);
			}
		}*/
	}

	public void addResourceModelEntity(ModelEntity<?> entity) {
		this.resources.add(entity);
	}

	public void addCheckerModelEntity(ModelEntity<?> entity) {
		if (this.checker != null){
			this.isCorrect = false;
		}
		this.checker = entity;
	}

	public ModelEntity<?> getChecker() {
		return checker;
	}

	public Set<ModelEntity<?>> getResources() {
		return resources;
	}

	public Set<ModelEntity<?>> getSubjects() {
		return subjects;
	}
}
