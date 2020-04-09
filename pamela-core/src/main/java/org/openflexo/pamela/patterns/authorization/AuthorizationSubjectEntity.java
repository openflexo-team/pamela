package org.openflexo.pamela.patterns.authorization;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.patterns.ReturnWrapper;
import org.openflexo.pamela.patterns.authenticator.exceptions.InconsistentSubjectEntityException;
import org.openflexo.pamela.patterns.authorization.annotations.AccessResource;
import org.openflexo.pamela.patterns.authorization.annotations.ResourceID;
import org.openflexo.pamela.patterns.authorization.annotations.SubjectID;

public class AuthorizationSubjectEntity {
    private final AuthorizationPattern pattern;
    private final Class baseClass;
    private final HashMap<String, Method> idGetters;
    private final HashMap<Method, String > accessResourceMethods;
    private final HashMap<String, Method> distantAccessMethods;
    private final HashMap<Method, HashMap<String, Integer>> mappingAccessMethodParameters;
    private final HashMap<Object, AuthorizationSubjectInstance> instances;
    private final HashMap<Method, ArrayList<Integer>> methodRealParamIndexes;
    private boolean successIDLinking;
    private boolean successAccessLinking;
    private boolean successResIDLinking;

    public AuthorizationSubjectEntity(AuthorizationPattern authorizationPattern, Class klass) throws ModelDefinitionException {
        this.pattern = authorizationPattern;
        this.baseClass = klass;
        this.idGetters = new HashMap<>();
        this.accessResourceMethods = new HashMap<>();
        this.distantAccessMethods = new HashMap<>();
        this.instances = new HashMap<>();
        this.mappingAccessMethodParameters = new HashMap<>();
        this.methodRealParamIndexes = new HashMap<>();
        this.analyzeClass();
        this.successIDLinking = false;
        this.successAccessLinking = false;
        this.successResIDLinking = false;
        this.link();
    }

    protected void link() {
        if (!this.successIDLinking){
            this.linkID();
        }
        if (!this.successAccessLinking){
            this.linkAccess();
        }
        if (!this.successResIDLinking){
            this.linkResID();
        }
    }

    public boolean isLinked(){
        return this.successResIDLinking && this.successIDLinking && this.successAccessLinking;
    }

    protected boolean isComplete(){
        return this.baseClass != null && this.pattern != null && !this.idGetters.isEmpty();
    }

    private void analyzeClass() throws ModelDefinitionException {
        for (Method m : this.baseClass.getMethods()){
            SubjectID idAnnotation = m.getAnnotation(SubjectID.class);
            if (idAnnotation != null && idAnnotation.patternID().compareTo(pattern.getID()) == 0){
                this.processID(m, idAnnotation);
            }
            AccessResource accessResourceAnnotation = m.getAnnotation(AccessResource.class);
            if (accessResourceAnnotation != null && accessResourceAnnotation.patternID().compareTo(pattern.getID()) == 0){
                this.processAccessResource(m, accessResourceAnnotation);
            }
        }
    }

    private void processAccessResource(Method method, AccessResource annotation) throws ModelDefinitionException {
        if (!this.accessResourceMethods.containsKey(method)){
            this.accessResourceMethods.put(method,annotation.methodID());
            HashMap<String, Integer> paramMapping = new HashMap<>();
            ArrayList<Integer> realParams = new ArrayList<>();
            for (int i = 0;i<method.getParameterCount();i++){
                Parameter param = method.getParameters()[i];
                ResourceID a = param.getAnnotation(ResourceID.class);
                if (a != null && a.patternID().compareTo(this.pattern.getID()) == 0){
                    if (!paramMapping.containsKey(a.paramID())){
                        paramMapping.put(a.paramID(), i);
                    }
                    else {
                        throw new ModelDefinitionException(String.format("Duplicate ResourceID annotation with same paramID %s in method %s parameters of class %s", a.paramID(),method.getName(), this.baseClass.getSimpleName()));
                    }
                }
                else {
                    realParams.add(i);
                }
            }
            this.mappingAccessMethodParameters.put(method, paramMapping);
            this.methodRealParamIndexes.put(method, realParams);
        }
    }

    private void processID(Method method, SubjectID annotation) throws InconsistentSubjectEntityException {
        if (!this.idGetters.containsKey(annotation.paramID())){
            this.idGetters.put(annotation.paramID(), method);
        }
        else {
            throw new InconsistentSubjectEntityException("Duplicate @SubjectID annotation with same pattern ID (" + this.pattern.getID() + ") and paramID (" + annotation.paramID() + ") in " + this.baseClass.getSimpleName());
        }
    }

    private void linkResID(){
        boolean linked = true;
        if (this.pattern.getCheckerEntity() != null){
            for (String paramID : this.pattern.getCheckerEntity().getResourceIdParameters().keySet()){
                for (Method m : this.accessResourceMethods.keySet()){
                    boolean foundInMethod = false;
                    for (Parameter param : m.getParameters()){
                        ResourceID annotation = param.getAnnotation(ResourceID.class);
                        if (annotation != null && annotation.patternID().compareTo(this.pattern.getID()) == 0 && annotation.paramID().compareTo(paramID) == 0){
                            foundInMethod = true;
                        }
                    }
                    linked = linked && foundInMethod;
                }
            }
        }
        this.successResIDLinking = linked;
    }

    private void linkAccess() {
        for (String methodId : this.accessResourceMethods.values()){
            if (this.pattern.knowsResourceMethod(methodId)){
                this.distantAccessMethods.put(methodId, this.pattern.requestResourceMethod(methodId));
            }
        }
        if (this.accessResourceMethods.values().size() == this.distantAccessMethods.keySet().size()){
            this.successAccessLinking = true;
        }
    }

    private void linkID() {
        boolean linked = true;
        if (this.pattern.getCheckerEntity() != null){
            for (String paramID : this.pattern.getCheckerEntity().getSubjectIdParameters().keySet()){
                linked = linked && (this.idGetters.containsKey(paramID));
            }
        }
        this.successIDLinking = linked;
    }

    protected void discoverInstance(Object instance) {
        if (!this.instances.containsKey(instance)){
            this.instances.put(instance, new AuthorizationSubjectInstance(instance, this));
            this.instances.get(instance).init();
        }
    }

    public HashMap<String, Method> getIdGetters() {
        return this.idGetters;
    }

    public Map<Object, AuthorizationSubjectInstance> getInstances() {
        return this.instances;
    }

    public ReturnWrapper processMethodBeforeInvoke(Object self, Method method, Class klass, Object[] args) throws InvocationTargetException, IllegalAccessException {
        if (this.accessResourceMethods.containsKey(method)){
            //Récupérer parmi les arguments les identifiants de resource
            HashMap<String, Object> resourceIDs = new HashMap<>();
            for (String paramID : this.mappingAccessMethodParameters.get(method).keySet()){
                resourceIDs.put(paramID,args[this.mappingAccessMethodParameters.get(method).get(paramID)]);
            }
            HashMap<String, Object> subjectIDs = new HashMap<>();
            for (String paramID : this.idGetters.keySet()){
                subjectIDs.put(paramID,this.idGetters.get(paramID).invoke(self));
            }
            return this.pattern.processAccessResource(subjectIDs, resourceIDs, this.accessResourceMethods.get(method), this.methodRealParamIndexes.get(method), self, method, args);
        }
        return new ReturnWrapper(true,null);
    }
}
