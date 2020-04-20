package org.openflexo.pamela.securitypatterns.singleAccessPoint;

import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.ModelEntity;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.patterns.ExecutionMonitor;
import org.openflexo.pamela.patterns.PatternDefinition;
import org.openflexo.pamela.securitypatterns.executionMonitors.CustomStack;
import org.openflexo.pamela.securitypatterns.singleAccessPoint.annotations.Checkpoint;
import org.openflexo.pamela.securitypatterns.singleAccessPoint.annotations.RequiredForAccess;

import java.lang.reflect.Method;
import java.util.HashMap;

public class SingleAccessPointPatternDefinition extends PatternDefinition {

    public static class AccessorWrapper{
        private HashMap<String, Method> getterMap;

        private AccessorWrapper(HashMap<String, Method> map){
            this.getterMap = map;
        }

        public HashMap<String, Method> getGettersMap(){
            return this.getterMap;
        }

    }
    private ModelEntity<?> systemEntity;
    private HashMap<ModelEntity<?>, AccessorWrapper> accessorEntities;

    private Method checkpoint;
    private HashMap<String, Integer> checkpointParams;

    private String message;
    private boolean isValid;
    protected CustomStack customStack;

    public SingleAccessPointPatternDefinition(String identifier, ModelContext modelContext) {
        super(identifier, modelContext);
        this.accessorEntities = new HashMap<>();
        this.checkpointParams = new HashMap<>();
        this.message = "\n";
        this.isValid = true;
    }

    @Override
    public void finalizeDefinition() throws ModelDefinitionException {
        for (ModelEntity<?> accessor : this.getAccessorEntities().keySet()){
            AccessorWrapper wrapper = this.getAccessorEntities().get(accessor);
            for (String paramID : this.checkpointParams.keySet()){
                if (!wrapper.getGettersMap().containsKey(paramID)){
                    this.isValid = false;
                    this.message += "Invalid SingleAccessPointClient entity (class " + accessor.getImplementedInterface().getSimpleName() + ") for pattern " + this.getIdentifier() + ": missing RequiredForAccess annotation with paramID " + paramID + ".\n";
                }
            }
            for (ExecutionMonitor monitor : this.getModelContext().getExecutionMonitors()){
                if (monitor instanceof CustomStack){
                    this.customStack = (CustomStack) monitor;
                }
            }
            if (this.customStack == null){
                this.customStack = new CustomStack(getModelContext());
            }
        }
        if (!this.isValid){
            throw new ModelDefinitionException(this.message);
        }

    }

    @Override
    public boolean isMethodInvolvedInPattern(Method m) {
        try {
            this.systemEntity.getImplementedInterface().getMethod(m.getName(), m.getParameterTypes());
            return true;
        }
        catch (NoSuchMethodException e){
            for (ModelEntity<?> accessorEntity : this.accessorEntities.keySet()){
                try {
                    accessorEntity.getImplementedInterface().getMethod(m.getName(),m.getParameterTypes());
                    return true;
                }
                catch (NoSuchMethodException ignored){

                }
            }
            return false;
        }
    }

    @Override
    public <I> void notifiedNewInstance(I newInstance, ModelEntity<I> modelEntity) {
        if (modelEntity == this.systemEntity){
            SingleAccessPointPatternInstance<I> patternInstance = new SingleAccessPointPatternInstance<I>(this, newInstance);
        }
    }

    public ModelEntity<?> getSystemEntity(){
        return this.systemEntity;
    }

    protected void setSystemEntity(ModelEntity<?> systemEntity){
        this.systemEntity = systemEntity;
        for (Method m : systemEntity.getImplementedInterface().getMethods()){
            Checkpoint a = m.getAnnotation(Checkpoint.class);
            if (a != null && a.patternID().compareTo(this.getIdentifier()) == 0){
                if (this.checkpoint == null){
                    this.checkpoint = m;
                    if (!boolean.class.isAssignableFrom(this.checkpoint.getReturnType())){
                        this.isValid = false;
                        this.message += "Invalid Protected system entity for pattern " + this.getIdentifier() + ": checkpoint method does not return a boolean.\n";
                    }
                    for (int i = 0; i < m.getParameterCount(); i++){
                        RequiredForAccess r = m.getParameters()[i].getAnnotation(RequiredForAccess.class);
                        if (r == null || r.patternID().compareTo(getIdentifier()) != 0){
                            this.isValid = false;
                            message += "Invalid Protected system entity for pattern " + this.getIdentifier() + ": missing @RequiredForAccess annotation for parameter #" + i + " of @Checkpoint method.\n";
                        }
                        else {
                            if (!this.checkpointParams.containsKey(r.paramID())){
                                this.checkpointParams.put(r.paramID(),i);
                            }
                            else {
                                this.isValid = false;
                                this.message += "Invalid Protected system entity for pattern " + this.getIdentifier() + ": duplicate @RequiredFoAccess annotation with same paramID " + r.paramID() + ".\n";
                            }
                        }
                    }
                }
                else {
                    this.isValid = false;
                    this.message += "Invalid Protected system entity for pattern " + this.getIdentifier() + ": duplicate @Checkpoint annotation.\n";
                }
            }
        }
        if (this.checkpoint == null){
            this.isValid = false;
            this.message += "Invalid Protected system entity for pattern " + this.getIdentifier() + ": missing @Checkpoint annotation.\n";
        }
    }

    protected void addAccessorEntity(ModelEntity<?> accessorEntity) {
        if (!this.accessorEntities.containsKey(accessorEntity)){
            HashMap<String, Method> requiredGetters = new HashMap<>();
            for (Method m : accessorEntity.getImplementedInterface().getMethods()){
                RequiredForAccess r = m.getAnnotation(RequiredForAccess.class);
                if (r != null && r.patternID().compareTo(this.getIdentifier()) == 0){
                    if (!requiredGetters.containsKey(r.paramID())){
                        requiredGetters.put(r.paramID(), m);
                    }
                    else {
                        this.isValid = false;
                        this.message += "Invalid SingleAccessPointClient entity (class " + accessorEntity.getImplementedInterface().getSimpleName() + ") for pattern " + this.getIdentifier() + ": duplicate @RequiredForAccess with same paramId " + r.paramID() + ".\n";
                    }
                }
            }
            this.accessorEntities.put(accessorEntity, new AccessorWrapper(requiredGetters));
        }
    }

    public HashMap<ModelEntity<?>, AccessorWrapper> getAccessorEntities(){
        return this.accessorEntities;
    }

    protected Method getCheckpoint() {
        return this.checkpoint;
    }

    protected HashMap<String, Integer> getCheckpointParams(){
        return this.checkpointParams;
    }
}
