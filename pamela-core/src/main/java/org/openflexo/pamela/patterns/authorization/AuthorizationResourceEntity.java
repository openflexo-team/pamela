package org.openflexo.pamela.patterns.authorization;


import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.patterns.authenticator.exceptions.InconsistentSubjectEntityException;
import org.openflexo.pamela.patterns.authorization.annotations.AccessResource;
import org.openflexo.pamela.patterns.authorization.annotations.PermissionCheckerGetter;
import org.openflexo.pamela.patterns.authorization.annotations.ResourceID;
import org.openflexo.pamela.patterns.authorization.annotations.SubjectID;
import org.openflexo.pamela.patterns.authorization.exception.InconsistentPermissionCheckerEntityException;
import org.openflexo.pamela.patterns.authorization.exception.InconsistentResourceEntityException;
import playground.authorization.interfaces.PermissionChecker;

import java.lang.reflect.Method;
import java.util.HashMap;

public class AuthorizationResourceEntity {
    private final AuthorizationPattern pattern;
    private final Class baseClass;
    private final HashMap<String, Method> accessMethods;
    private final HashMap<String, Method> idGetters;
    private boolean successLinking;
    private Method checkerGetter;

    public AuthorizationResourceEntity(AuthorizationPattern authorizationPattern, Class klass) throws ModelDefinitionException {
        this.pattern = authorizationPattern;
        this.baseClass = klass;
        this.accessMethods = new HashMap<>();
        this.idGetters = new HashMap<>();
        this.analyzeClass();
        this.successLinking = false;
        this.link();
    }

    protected boolean isComplete() {
        return this.successLinking && this.baseClass!= null && this.pattern != null && this.checkerGetter != null && !this.idGetters.isEmpty() && !this.accessMethods.values().isEmpty();
    }

    protected HashMap<String, Method> getAccessMethods() {
        return this.accessMethods;
    }

    private void analyzeClass() throws ModelDefinitionException {
        for (Method m : this.baseClass.getMethods()){
            ResourceID idAnnotation = m.getAnnotation(ResourceID.class);
            if (idAnnotation != null && idAnnotation.patternID().compareTo(pattern.getID()) == 0){
                this.processID(m, idAnnotation);
            }
            AccessResource accessResourceAnnotation = m.getAnnotation(AccessResource.class);
            if (accessResourceAnnotation != null && accessResourceAnnotation.patternID().compareTo(pattern.getID()) == 0){
                this.processAccessResource(m, accessResourceAnnotation);
            }
            PermissionCheckerGetter checkerGetterAnnotation = m.getAnnotation(PermissionCheckerGetter.class);
            if (checkerGetterAnnotation != null && checkerGetterAnnotation.patternID().compareTo(this.pattern.getID()) == 0){
                this.processCheckerGetter(m, checkerGetterAnnotation);
            }
        }
    }

    private void processCheckerGetter(Method m, PermissionCheckerGetter checkerGetterAnnotation) throws ModelDefinitionException {
        if (this.checkerGetter == null){
            this.checkerGetter = m;
            this.pattern.attachAuthorizationCheckerFromResource(m.getReturnType());
        }
        else {
            throw new InconsistentResourceEntityException("Duplicate @PermissionCheckerGetter annotated methods with same patternID " + checkerGetterAnnotation.patternID() + " in class " + this.baseClass.getSimpleName());
        }
    }

    private void processAccessResource(Method method, AccessResource annotation)  throws ModelDefinitionException{
        if (!this.accessMethods.containsKey(annotation.methodID())){
            this.accessMethods.put(annotation.methodID(), method);
            this.pattern.notifyAccessMethod(annotation.methodID(), this);
        }
        else {
            throw new InconsistentResourceEntityException("Duplicate methodID " + annotation.methodID() + " in class " + this.baseClass.getSimpleName());
        }
    }

    private void processID(Method method, ResourceID annotation) throws InconsistentResourceEntityException {
        if (!this.idGetters.containsKey(annotation.paramID())){
            this.idGetters.put(annotation.paramID(), method);
        }
        else {
            throw new InconsistentResourceEntityException("Duplicate @ResourceID annotation with same pattern ID (" + this.pattern.getID() + ") and paramID (" + annotation.paramID() + ") in " + this.baseClass.getSimpleName());
        }
    }

    private void link() {
        if (this.pattern.getCheckerEntity() != null){
            boolean linked = true;
            for (String paramID : this.pattern.getCheckerEntity().getResourceIdParameters().keySet()){
                 linked = linked && this.idGetters.containsKey(paramID);
            }
            this.successLinking = linked;
        }
    }
}
