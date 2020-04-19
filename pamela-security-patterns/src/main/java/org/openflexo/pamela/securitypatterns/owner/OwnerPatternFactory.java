package org.openflexo.pamela.securitypatterns.owner;

import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.ModelEntity;
import org.openflexo.pamela.patterns.AbstractPatternFactory;
import org.openflexo.pamela.patterns.PatternDefinition;
import org.openflexo.pamela.securitypatterns.owner.annotations.OwnedObject;
import org.openflexo.pamela.securitypatterns.owner.annotations.Owner;
import org.openflexo.pamela.securitypatterns.owner.annotations.Pure;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Deprecated
public class OwnerPatternFactory extends AbstractPatternFactory<OwnerPatternDefinition> {

    public OwnerPatternFactory(ModelContext modelContext) {
        super(modelContext);
    }

    @Override
    public void discoverEntity(ModelEntity entity) {
        for (Annotation a : entity.getImplementedInterface().getAnnotations()){
            if (a instanceof OwnedObject){
                OwnerPatternDefinition patternDefinition = getPatternDefinition(((OwnedObject)a).patternID(), true);
                patternDefinition.ownedObjectEntity = entity;
                for (Method m : entity.getImplementedInterface().getMethods()){
                    Owner ownerAnnotation = m.getAnnotation(Owner.class);
                    if (ownerAnnotation != null && ownerAnnotation.patternID().compareTo(((OwnedObject) a).patternID()) == 0){
                        if (patternDefinition.ownerGetter != null){
                            patternDefinition.isValid = false;
                            patternDefinition.message += "Invalid OwnedObject entity: duplicate owner getter method.\n";
                        }
                        else {
                            patternDefinition.ownerGetter = m;
                        }
                    }
                    Pure pureAnnotation = m.getAnnotation(Pure.class);
                    if (pureAnnotation != null && pureAnnotation.patternID().compareTo(((OwnedObject) a).patternID()) == 0){
                        patternDefinition.pureMethods.add(m);
                    }
                }
            }
        }
    }
}
