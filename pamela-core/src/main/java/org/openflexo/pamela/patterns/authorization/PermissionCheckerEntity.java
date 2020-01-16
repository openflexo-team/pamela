package org.openflexo.pamela.patterns.authorization;

import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.patterns.authorization.annotations.CheckAccess;
import org.openflexo.pamela.patterns.authorization.annotations.ResourceID;
import org.openflexo.pamela.patterns.authorization.annotations.SubjectID;
import org.openflexo.pamela.patterns.authorization.exception.InconsistentPermissionCheckerEntityException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.HashMap;

public class PermissionCheckerEntity {
    private final AuthorizationPattern pattern;
    private final Class baseClass;
    private final HashMap<String, Integer> subjectIdParameters;
    private final HashMap<String, Integer> resourceIdParameters;
    private Method checkMethod;
    private int methodIdIndex;
    private HashMap<Object, PermissionCheckerInstance> instances;

    protected PermissionCheckerEntity(AuthorizationPattern pattern, Class baseClass) throws InconsistentPermissionCheckerEntityException{
        this.pattern = pattern;
        this.baseClass = baseClass;
        this.resourceIdParameters = new HashMap<>();
        this.subjectIdParameters = new HashMap<>();
        this.methodIdIndex = -1;
        this.instances = new HashMap<>();
        this.analyzeClass();
    }

    protected HashMap<String, Integer> getSubjectIdParameters(){
        return this.subjectIdParameters;
    }

    protected HashMap<String, Integer> getResourceIdParameters() {
        return this.resourceIdParameters;
    }

    public Class getBaseClass() {
        return this.baseClass;
    }

    private void analyzeClass() throws InconsistentPermissionCheckerEntityException {
        for (Method m : this.baseClass.getMethods()){
            CheckAccess checkAnnotation = m.getAnnotation(CheckAccess.class);
            if (checkAnnotation != null && checkAnnotation.patternID().compareTo(this.pattern.getID()) == 0){
                this.processCheckMethod(m, checkAnnotation);
            }
        }
    }

    private void processCheckMethod(Method m, CheckAccess checkAnnotation) throws InconsistentPermissionCheckerEntityException{
        if (this.checkMethod == null){
            if (!boolean.class.isAssignableFrom(m.getReturnType())){
                throw new InconsistentPermissionCheckerEntityException(String.format("Check method %s in class %s does not return a boolean.", m.getName(), this.baseClass));
            }
            this.checkMethod = m;
            for (int i=0;i<m.getParameters().length;i++){
                Parameter param = m.getParameters()[i];
                SubjectID subjectAnnotation = param.getAnnotation(SubjectID.class);
                ResourceID resourceAnnotation = param.getAnnotation(ResourceID.class);
                if (subjectAnnotation != null && resourceAnnotation != null){
                    throw new InconsistentPermissionCheckerEntityException("Parameter with both @SubjectID and @ResourceID annotations with same patternID " + this.pattern.getID() + " in class " + this.baseClass.getSimpleName());
                }
                if (subjectAnnotation != null && subjectAnnotation.patternID().compareTo(this.pattern.getID()) == 0){
                    if (!this.subjectIdParameters.containsKey(subjectAnnotation.paramID())){
                        this.subjectIdParameters.put(subjectAnnotation.paramID(), i);
                    }
                    else {
                        throw new InconsistentPermissionCheckerEntityException("Duplicate @SubjectID annotation with same patternID " + subjectAnnotation.patternID() + " and paramID " + subjectAnnotation.paramID() + " in method " + m.getName() + " of class " + this.baseClass.getSimpleName());
                    }
                }
                else if (resourceAnnotation != null && resourceAnnotation.patternID().compareTo(this.pattern.getID()) == 0){
                    if (!this.resourceIdParameters.containsKey(resourceAnnotation.paramID())){
                        this.resourceIdParameters.put(resourceAnnotation.paramID(), i);
                    }
                    else {
                        throw new InconsistentPermissionCheckerEntityException("Duplicate @ResourceID annotation with same patternID " + resourceAnnotation.patternID() + " and paramID " + resourceAnnotation.paramID() + " in method " + m.getName() + " of class " + this.baseClass.getSimpleName());
                    }
                }
                else if (this.methodIdIndex == -1){
                    this.methodIdIndex = i;
                }
                else {
                    throw new InconsistentPermissionCheckerEntityException("Unidentified parameter at position " + i + " in method " + m.getName() + " in class " + this.baseClass.getSimpleName());
                }
            }
        }
        else {
            throw new InconsistentPermissionCheckerEntityException("Duplicate @CheckAccess method with same patternID " + checkAnnotation.patternID() + " in class " + this.baseClass.getSimpleName());
        }
    }

    public boolean isComplete() {
        return this.baseClass != null && this.checkMethod != null && this.pattern != null && this.methodIdIndex != -1;
    }

    public void discoverInstance(Object instance) {
        if (!this.instances.containsKey(instance)){
            this.instances.put(instance, new PermissionCheckerInstance(instance, this));
            this.instances.get(instance).init();
        }
    }

    public HashMap<Object, PermissionCheckerInstance> getInstances() {
        return instances;
    }

    public boolean performAccessCheck(HashMap<String, Object> subjectIDs, HashMap<String, Object> resourceIDs, String methodID, Object checkerObject) throws InvocationTargetException, IllegalAccessException {
        Object[] checkArgs = new Object[this.checkMethod.getParameterCount()];
        for (String paramId : subjectIDs.keySet()){
            checkArgs[this.subjectIdParameters.get(paramId)] = subjectIDs.get(paramId);
        }
        for (String paramId : resourceIDs.keySet()){
            checkArgs[this.resourceIdParameters.get(paramId)] = resourceIDs.get(paramId);
        }
        checkArgs[this.methodIdIndex] = methodID;
        return (boolean)this.checkMethod.invoke(checkerObject,checkArgs);
    }
}
