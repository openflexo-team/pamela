package org.openflexo.pamela.securitypatterns.singleAccessPoint;

import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.ModelEntity;
import org.openflexo.pamela.patterns.AbstractPatternFactory;
import org.openflexo.pamela.securitypatterns.singleAccessPoint.annotations.SingleAccessPointClient;
import org.openflexo.pamela.securitypatterns.singleAccessPoint.annotations.SingleAccessPointSystem;

import java.lang.annotation.Annotation;

@Deprecated
public class SingleAccessPointPatternFactory extends AbstractPatternFactory<SingleAccessPointPatternDefinition> {

    public SingleAccessPointPatternFactory(ModelContext modelContext) {
        super(modelContext);
    }

    @Override
    public void discoverEntity(ModelEntity<?> entity) {
        for (Annotation a : entity.getImplementedInterface().getAnnotations()){
            if (a instanceof SingleAccessPointSystem){
                SingleAccessPointPatternDefinition patternDefinition = this.getPatternDefinition(((SingleAccessPointSystem) a).patternID(), true);
                patternDefinition.setSystemEntity(entity);
            }
            if (a instanceof SingleAccessPointClient){
                SingleAccessPointPatternDefinition patternDefinition = this.getPatternDefinition(((SingleAccessPointClient) a).patternID(), true);
                patternDefinition.addAccessorEntity(entity);
            }
        }
    }
}
