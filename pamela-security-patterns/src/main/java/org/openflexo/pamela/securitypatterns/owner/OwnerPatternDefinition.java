package org.openflexo.pamela.securitypatterns.owner;

import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;
import org.openflexo.pamela.patterns.ExecutionMonitor;
import org.openflexo.pamela.patterns.PatternDefinition;
import org.openflexo.pamela.securitypatterns.executionMonitors.CustomStack;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class OwnerPatternDefinition extends PatternDefinition {
    protected ModelEntity<?> ownedObjectEntity;
    protected Method ownerGetter;
    protected String message;
    protected boolean isValid;
    protected Set<Method> pureMethods;
    protected ModelEntity<?> ownerEntity;
    protected ModelProperty ownerProperty;
    protected CustomStack customStack;

    public OwnerPatternDefinition(String identifier, PamelaMetaModel pamelaMetaModel) {
        super(identifier, pamelaMetaModel);
        this.isValid = true;
        this.pureMethods = new HashSet<>();
        this.message = "\n";
    }

    @Override
    public void finalizeDefinition() throws ModelDefinitionException {
        if (ownerGetter == null){
            this.isValid = false;
            message += "Invalid OwnedObject entity for pattern " + this.getIdentifier() + ": no owner getter.\n";
        }
        if (!isValid){
            throw new ModelDefinitionException(this.message);
        }
        this.ownerProperty = this.ownedObjectEntity.getPropertyForMethod(this.ownerGetter);
        this.ownerEntity = this.getModelContext().getModelEntity(this.ownerGetter.getReturnType());

        for (ExecutionMonitor monitor : this.getModelContext().getExecutionMonitors()){
            if (monitor instanceof CustomStack){
                this.customStack = (CustomStack) monitor;
            }
        }
        if (this.customStack == null){
            this.customStack = new CustomStack(getModelContext());
        }
    }

    @Override
    public boolean isMethodInvolvedInPattern(Method m) {
        try {
            this.ownerEntity.getImplementedInterface().getMethod(m.getName(), m.getParameterTypes());
            return true;
        }
        catch (NoSuchMethodException e){
            try {
                this.ownedObjectEntity.getImplementedInterface().getMethod(m.getName(), m.getParameterTypes());
                return true;
            }
            catch (NoSuchMethodException ignored) {

            }
        }
        return false;
    }

    @Override
    public <I> void notifiedNewInstance(I newInstance, ModelEntity<I> modelEntity) {
        if (modelEntity == this.ownedObjectEntity){
            OwnerPatternInstance<I> patternInstance = new OwnerPatternInstance<>(this, newInstance);
        }
    }
}