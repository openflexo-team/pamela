package org.openflexo.pamela.securitypatterns.singleAccessPoint;

import java.lang.annotation.Annotation;

import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.patterns.AbstractPatternFactory;
import org.openflexo.pamela.securitypatterns.singleAccessPoint.annotations.SingleAccessPointClient;
import org.openflexo.pamela.securitypatterns.singleAccessPoint.annotations.SingleAccessPointSystem;

public class SingleAccessPointPatternFactory extends AbstractPatternFactory<SingleAccessPointPatternDefinition> {

    public SingleAccessPointPatternFactory(PamelaMetaModel pamelaMetaModel) {
        super(pamelaMetaModel);
    }

	@Override
	public void discoverEntity(ModelEntity<?> entity) {
		for (Annotation a : entity.getImplementedInterface().getAnnotations()) {
			if (a instanceof SingleAccessPointSystem) {
				SingleAccessPointPatternDefinition patternDefinition = this.getPatternDefinition(((SingleAccessPointSystem) a).patternID(),
						true);
				patternDefinition.setSystemEntity(entity);
			}
			if (a instanceof SingleAccessPointClient) {
				SingleAccessPointPatternDefinition patternDefinition = this.getPatternDefinition(((SingleAccessPointClient) a).patternID(),
						true);
				patternDefinition.addAccessorEntity(entity);
			}
		}

		super.discoverEntity(entity);
	}
}
