package org.openflexo.pamela.securitypatterns.owner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.patterns.AbstractPatternFactory;
import org.openflexo.pamela.securitypatterns.owner.annotations.OwnedObject;
import org.openflexo.pamela.securitypatterns.owner.annotations.Owner;
import org.openflexo.pamela.securitypatterns.owner.annotations.Pure;

public class OwnerPatternFactory extends AbstractPatternFactory<OwnerPatternDefinition> {

	public OwnerPatternFactory(PamelaMetaModel pamelaMetaModel) {
		super(pamelaMetaModel);
	}

	@Override
	public void discoverEntity(ModelEntity entity) throws ModelDefinitionException {
		for (Annotation a : entity.getImplementedInterface().getAnnotations()) {
			if (a instanceof OwnedObject) {
				OwnerPatternDefinition patternDefinition = getPatternDefinition(((OwnedObject) a).patternID(), true);
				patternDefinition.ownedObjectEntity = entity;
				for (Method m : entity.getImplementedInterface().getMethods()) {
					Owner ownerAnnotation = m.getAnnotation(Owner.class);
					if (ownerAnnotation != null && ownerAnnotation.patternID().compareTo(((OwnedObject) a).patternID()) == 0) {
						if (patternDefinition.ownerGetter != null) {
							patternDefinition.isValid = false;
							patternDefinition.message += "Invalid OwnedObject entity: duplicate owner getter method.\n";
						}
						else {
							patternDefinition.ownerGetter = m;
						}
					}
					Pure pureAnnotation = m.getAnnotation(Pure.class);
					if (pureAnnotation != null && pureAnnotation.patternID().compareTo(((OwnedObject) a).patternID()) == 0) {
						patternDefinition.pureMethods.add(m);
					}
				}
			}
		}

		super.discoverEntity(entity);

	}
}
