package org.openflexo.pamela.securitypatterns.authorization;

import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.ModelEntity;
import org.openflexo.pamela.patterns.AbstractPatternFactory;
import org.openflexo.pamela.securitypatterns.authorization.annotations.AuthorizationChecker;
import org.openflexo.pamela.securitypatterns.authorization.annotations.AuthorizationSubject;
import org.openflexo.pamela.securitypatterns.authorization.annotations.ProtectedResource;

import java.lang.annotation.Annotation;

/**
 * Represent the factory for {@link AuthorizationPatternDefinition}
 *
 * @author Caine Silva, Sylvain Guerin
 */
public class AuthorizationPatternFactory extends AbstractPatternFactory<AuthorizationPatternDefinition> {

	public AuthorizationPatternFactory(ModelContext modelContext) {
		super(modelContext);
	}

	@Override
	public void discoverEntity(ModelEntity<?> entity) {

		for (Annotation a : entity.getImplementedInterface().getAnnotations()) {
			if (a instanceof AuthorizationSubject) {
				AuthorizationSubject subjectAnnotation = (AuthorizationSubject) a;
				String patternId = subjectAnnotation.patternID();
				AuthorizationPatternDefinition patternDefinition = getPatternDefinition(patternId, true);
				patternDefinition.addSubjectModelEntity(entity);
			}
			if (a instanceof ProtectedResource) {
				ProtectedResource resourceAnnotation = (ProtectedResource) a;
				String patternId = resourceAnnotation.patternID();
				AuthorizationPatternDefinition patternDefinition = getPatternDefinition(patternId, true);
				patternDefinition.addResourceModelEntity(entity);
			}
			if (a instanceof AuthorizationChecker) {
				AuthorizationChecker checkerAnnotation = (AuthorizationChecker) a;
				String patternId = checkerAnnotation.patternID();
				AuthorizationPatternDefinition patternDefinition = getPatternDefinition(patternId, true);
				patternDefinition.addCheckerModelEntity(entity);
			}
		}
	}

}
