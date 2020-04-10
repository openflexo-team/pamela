package org.openflexo.pamela.securitypatterns.authorization;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.patterns.AbstractPattern;
import org.openflexo.pamela.patterns.PatternContext;
import org.openflexo.pamela.patterns.PatternUtils;
import org.openflexo.pamela.patterns.ReturnWrapper;
import org.openflexo.pamela.securitypatterns.authenticator.exceptions.InconsistentSubjectEntityException;
import org.openflexo.pamela.securitypatterns.authorization.annotations.AuthorizationChecker;
import org.openflexo.pamela.securitypatterns.authorization.annotations.AuthorizationSubject;
import org.openflexo.pamela.securitypatterns.authorization.annotations.ProtectedResource;
import org.openflexo.pamela.securitypatterns.authorization.exception.InconsistentPermissionCheckerEntityException;
import org.openflexo.pamela.securitypatterns.authorization.exception.InconsistentResourceEntityException;

public class AuthorizationPattern extends AbstractPattern {
    private final HashMap<Class, AuthorizationSubjectEntity> subjects;
    private final HashMap<Class, AuthorizationResourceEntity> resources;
    private final HashMap<String, AuthorizationResourceEntity> resourceMethods;
    private PermissionCheckerEntity checker;

    public AuthorizationPattern(PatternContext context, String id){
        super(context,id);
        this.subjects = new HashMap<>();
        this.resources = new HashMap<>();
        this.resourceMethods = new HashMap<>();
    }

    @Override
    public void attachClass(Class baseClass) throws ModelDefinitionException {
        AuthorizationSubject subjectAnnotation = (AuthorizationSubject) baseClass.getAnnotation(AuthorizationSubject.class);
        if (subjectAnnotation != null && subjectAnnotation.patternID().compareTo(this.id) == 0){
            this.attachSubject(baseClass);
        }
        ProtectedResource resourceAnnotation = (ProtectedResource) baseClass.getAnnotation(ProtectedResource.class);
        if (resourceAnnotation != null && resourceAnnotation.patternID().compareTo(this.id) == 0){
            this.attachResource(baseClass);
        }

    }

    @Override
    public ReturnWrapper processMethodBeforeInvoke(Object self, Method method, Class klass, Object[] args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (context.notInConstructor()){
            this.checkBeforeInvoke(self, method, klass, args);
        }
        ReturnWrapper returned = new ReturnWrapper(true,null);
        if (this.subjects.containsKey(klass)){
            returned = this.subjects.get(klass).processMethodBeforeInvoke(self, method, klass, args);
        }
        return returned;
    }

    private void checkBeforeInvoke(Object self, Method method, Class klass, Object[] args) {
        if (this.subjects.containsKey(klass) && this.subjects.get(klass).getInstances().containsKey(self)){
            this.subjects.get(klass).getInstances().get(self).checkBeforeInvoke(method, args);
        }
        if (this.checker.getBaseClass().equals(klass) && this.checker.getInstances().containsKey(self)){
            this.checker.getInstances().get(self).checkBeforeInvoke(method);
        }
        if (this.resources.containsKey(klass) && this.resources.get(klass).getInstances().containsKey(self)){
            this.resources.get(klass).getInstances().get(self).checkBeforeInvoke(method, args);
        }
    }

    @Override
    public void discoverInstance(Object instance, Class klass) {
        if (this.subjects.containsKey(klass)){
            this.subjects.get(klass).discoverInstance(instance);
        }
        if (this.checker.getBaseClass().equals(klass)){
            this.checker.discoverInstance(instance);
        }
        if (this.resources.containsKey(klass)){
            this.resources.get(klass).discoverInstance(instance);
        }
    }

    @Override
    public void processMethodAfterInvoke(Object self, Method method, Class klass, Object returnValue, Object[] args) {
        if (method.getAnnotation(Initializer.class) != null && !this.context.notInConstructor()){
            this.context.leavingConstructor();
            this.context.getRelatedPatternsFromInstance(self);
            this.context.enteringConstructor();
        }
        if (context.notInConstructor()) {
            this.checkAfterInvoke(self, method, klass, returnValue, args);
        }

    }

    private void checkAfterInvoke(Object self, Method method, Class klass, Object returnValue, Object[] args) {
        if (this.subjects.containsKey(klass) && this.subjects.get(klass).getInstances().containsKey(self)){
            this.subjects.get(klass).getInstances().get(self).checkAfterInvoke(method, args, returnValue);
        }
        if (this.checker.getBaseClass().equals(klass) && this.checker.getInstances().containsKey(self)){
            this.checker.getInstances().get(self).checkAfterInvoke(method, returnValue);
        }
        if (this.resources.containsKey(klass) && this.resources.get(klass).getInstances().containsKey(self)){
            this.resources.get(klass).getInstances().get(self).checkAfterInvoke(method, args, returnValue);
        }
    }

    public PermissionCheckerEntity getCheckerEntity() {
        return this.checker;
    }

    public HashMap<Class, AuthorizationSubjectEntity> getSubjects() {
        return this.subjects;
    }

    protected boolean knowsResourceMethod(String methodId) {
        return this.resourceMethods.containsKey(methodId);
    }

    private void attachResource(Class klass) throws ModelDefinitionException {
        AuthorizationResourceEntity resource = new AuthorizationResourceEntity(this, klass);
        if (resource.isComplete()){
            this.resources.put(klass, resource);
            this.updateSubjects();
        }
        else {
            throw new InconsistentResourceEntityException("Missing annotations in " + klass.getSimpleName() + "Authorization subject definition with ID " + this.id);
        }
    }

    private void updateSubjects() {
        for (AuthorizationSubjectEntity subject : this.subjects.values()){
            if (!subject.isLinked()){
                subject.link();
            }
        }
    }

    private void attachSubject(Class klass) throws ModelDefinitionException{
        AuthorizationSubjectEntity subject = new AuthorizationSubjectEntity(this, klass);
        if (subject.isComplete()){
            this.subjects.put(klass, subject);
        }
        else {
            throw new InconsistentSubjectEntityException("Missing annotations in " + klass.getSimpleName() + "Authorization subject definition with ID " + this.id);
        }
    }

    protected Method requestResourceMethod(String methodId) {
        if (this.resourceMethods.containsKey(methodId)){
            return this.resourceMethods.get(methodId).getAccessMethods().get(methodId);
        }
        return null;
    }

    protected void notifyAccessMethod(String methodID, AuthorizationResourceEntity authorizationResourceEntity) throws ModelDefinitionException {
        if (!this.resourceMethods.containsKey(methodID)) {
            this.resourceMethods.put(methodID, authorizationResourceEntity);
        }
        else {
            throw new ModelDefinitionException("Duplicate access method with same method id " + methodID + " in pattern " + this.id + " model.");
        }
    }

    protected void attachAuthorizationCheckerFromResource(Class checkerClass) throws InconsistentPermissionCheckerEntityException {
        if (this.checker == null){
            Class finalKlass = null;
            for (Class klass : PatternUtils.getClassHierarchy(checkerClass)){
                AuthorizationChecker annotation = (AuthorizationChecker) klass.getAnnotation(AuthorizationChecker.class);
                if (annotation != null && annotation.patternID().compareTo(this.getID()) == 0){
                    finalKlass = klass;
                }
            }
            if (finalKlass == null){
                throw new InconsistentPermissionCheckerEntityException("Missing annotations in " + checkerClass.getSimpleName() + "Authorization Checker definition with ID " + this.id);
            }
            PermissionCheckerEntity permissionChecker = new PermissionCheckerEntity(this, finalKlass);
            if  (permissionChecker.isComplete()){
                this.checker = permissionChecker;
                this.context.attachClassFromAbstractPattern(this.checker.getBaseClass(), this.id);
            }
            else {
                throw new InconsistentPermissionCheckerEntityException("Missing annotations in " + finalKlass.getSimpleName() + "Authorization Checker definition with ID " + this.id);
            }
        }
    }

    public HashMap<Class, AuthorizationResourceEntity> getResources() {
        return this.resources;
    }

    public ReturnWrapper processAccessResource(HashMap<String, Object> subjectIDs, HashMap<String, Object> resourceIDs, String methodID, ArrayList<Integer> paramsIndexes, Object self, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        //Récupérer l'instance de resource à partir des identifiants
        AuthorizationResourceInstance resourceInstance = this.searchForResource(resourceIDs);
        Object checkerObject = resourceInstance.getEntity().getCheckerGetter().invoke(resourceInstance.getInstance());
        boolean allowed = this.checker.performAccessCheck(subjectIDs,resourceIDs,methodID, checkerObject);
        if (allowed){
            return resourceInstance.getEntity().performAccess(methodID, args, paramsIndexes, resourceInstance.getInstance());
        }
        else{
            throw new ModelExecutionException(String.format("Invalid access of subject with ids %s to resource with ids %s using method %s", subjectIDs.toString(), resourceIDs.toString(), methodID));
        }
    }

    private AuthorizationResourceInstance searchForResource(HashMap<String, Object> resourceIDs) {
        AuthorizationResourceInstance returnValue = null;
        for (AuthorizationResourceEntity entity : this.resources.values()){
            returnValue = entity.searchForResource(resourceIDs);
            if (returnValue != null){
                return returnValue;
            }
        }
        throw new ModelExecutionException(String.format("Unknown Resource with ids %s", resourceIDs.toString()));
    }
}
