package org.openflexo.pamela.patterns.authorization;

import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.patterns.AbstractPattern;
import org.openflexo.pamela.patterns.PatternContext;
import org.openflexo.pamela.patterns.PatternLibrary;
import org.openflexo.pamela.patterns.authenticator.AuthenticatorEntity;
import org.openflexo.pamela.patterns.authenticator.annotations.Authenticator;
import org.openflexo.pamela.patterns.authenticator.exceptions.InconsistentAuthenticatorEntityException;
import org.openflexo.pamela.patterns.authenticator.exceptions.InconsistentSubjectEntityException;
import org.openflexo.pamela.patterns.authorization.annotations.*;
import org.openflexo.pamela.patterns.authorization.exception.InconsistentPermissionCheckerEntityException;
import org.openflexo.pamela.patterns.authorization.exception.InconsistentResourceEntityException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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
    public boolean processMethodBeforeInvoke(Object self, Method method, Class klass) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        return true;
    }

    @Override
    public void discoverInstance(Object instance, Class klass) {

    }

    @Override
    public void processMethodAfterInvoke(Object self, Method method, Class klass, Object returnValue) {

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
            for (Class klass : PatternLibrary.getClassHierarchy(checkerClass)){
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
}
