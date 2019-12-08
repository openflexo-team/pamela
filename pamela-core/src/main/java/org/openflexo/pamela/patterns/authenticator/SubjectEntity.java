package org.openflexo.pamela.patterns.authenticator;

import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.patterns.authenticator.annotations.AuthenticationInformation;
import org.openflexo.pamela.patterns.authenticator.annotations.AuthenticatorGetter;
import org.openflexo.pamela.patterns.authenticator.annotations.ProofOfIdentity;
import org.openflexo.pamela.patterns.authenticator.annotations.AuthenticateMethod;
import org.openflexo.pamela.patterns.authenticator.exceptions.InconsistentSubjectEntityException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;

public class SubjectEntity {
    private AuthenticatorPattern pattern;
    private Class baseClass;
    private Method idProofSetter;
    private ArrayList<Method> authenticateMethods;
    private HashMap<String, Method> authInfoGetters;
    private HashMap<Object, SubjectInstance> instances;
    private Method[] args;
    private boolean successLinking;
    private Method authenticatorGetter;

    protected SubjectEntity(AuthenticatorPattern pattern, Class klass) throws ModelDefinitionException, NoSuchMethodException {
        this.pattern = pattern;
        this.baseClass = klass;
        this.authInfoGetters = new HashMap<>();
        this.authenticateMethods = new ArrayList<>();
        this.instances = new HashMap<>();
        this.analyzeClass();
        this.successLinking = false;
        this.link();
    }

    private void analyzeClass() throws ModelDefinitionException, NoSuchMethodException {
        for (Method m : this.baseClass.getMethods()){
            AuthenticationInformation authInfoAnnotation = m.getAnnotation(AuthenticationInformation.class);
            if (authInfoAnnotation != null && authInfoAnnotation.patternID().compareTo(pattern.getID()) == 0){
                this.processAuthenticationInformation(m, authInfoAnnotation);
            }
            ProofOfIdentity idProofAnnotation = m.getAnnotation(ProofOfIdentity.class);
            if (idProofAnnotation != null && idProofAnnotation.patternID().compareTo(pattern.getID()) == 0){
                this.processIdProof(m, idProofAnnotation);
            }
            AuthenticateMethod authenticateMethodAnnotation =  m.getAnnotation(AuthenticateMethod.class);
            if (authenticateMethodAnnotation != null && authenticateMethodAnnotation.patternID().compareTo(pattern.getID()) == 0){
                this.processAuthenticateMethod(m, authenticateMethodAnnotation);
            }
            AuthenticatorGetter authenticatorGetterAnnotation = m.getAnnotation(AuthenticatorGetter.class);
            if (authenticatorGetterAnnotation != null && authenticatorGetterAnnotation.patternID().compareTo(pattern.getID()) == 0){
                this.processAuthenticatorGetter(m, authenticatorGetterAnnotation);
            }
        }
    }

    private void processAuthenticatorGetter(Method m, AuthenticatorGetter annotation) throws ModelDefinitionException{
        if (this.authenticatorGetter == null) {
            this.authenticatorGetter = m;
        }
        this.pattern.attachAuthenticatorFromAuthenticatorGetter(m.getReturnType());
    }

    private void processAuthenticationInformation(Method method, AuthenticationInformation annotation) throws InconsistentSubjectEntityException {
        if (!this.authInfoGetters.containsKey(annotation.paramID())){
            this.authInfoGetters.put(annotation.paramID(), method);
        }
        else {
            throw new InconsistentSubjectEntityException("Duplicate @AuthenticationInformation annotation with same pattern ID (" + this.pattern.getID() + ") and paramID (" + annotation.paramID() + ") in " + this.baseClass.getClass().getSimpleName());
        }
    }

    private void processIdProof(Method method, ProofOfIdentity annotation) throws InconsistentSubjectEntityException {
        if (this.idProofSetter == null){
            this.idProofSetter = method;
        }
        else {
            throw new InconsistentSubjectEntityException("Duplicate @ProofOfIdentity annotation with same pattern ID (" + this.pattern.getID() + ") in " + this.baseClass.getSimpleName());
        }
    }

    private void processAuthenticateMethod(Method method, AuthenticateMethod annotation){
        this.authenticateMethods.add(method);
    }

    private void link(){
        Method request = this.pattern.getAuthenticator().getMethod();
        this.args = new Method[request.getParameterCount()];
        int current  = 0;
        boolean success = true;
        for (Parameter p : request.getParameters()){
            if (this.authInfoGetters.containsKey(p.getAnnotation(AuthenticationInformation.class).paramID())){
                this.args[current] = this.authInfoGetters.get(p.getAnnotation(AuthenticationInformation.class).paramID());
            }
            else {
                success = success && false;
            }
            current++;
        }
        this.successLinking = success;
    }

    protected boolean isComplete(){
        return (pattern != null) && (this.baseClass != null) && (idProofSetter != null) && this.successLinking;
    }


    public boolean processMethodBeforeInvoke(Object instance, Method method, Class klass) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method superMethod = klass.getMethod(method.getName(), method.getParameterTypes());
        if (this.authenticateMethods.contains(method)){
            pattern.performAuthentication(instance,this.idProofSetter, this.args, this.authenticatorGetter);
            return !Modifier.isAbstract(method.getModifiers());
        }
        else if (this.authenticateMethods.contains(superMethod)){
            pattern.performAuthentication(instance,this.idProofSetter, this.args, this.authenticatorGetter);
            return !Modifier.isAbstract(method.getModifiers());
        }

        if (this.idProofSetter.equals(method)){
            return pattern.handleIdProofSetterCall(instance, method, this.args);
        }
        else if (this.idProofSetter.equals(superMethod)){
            return pattern.handleIdProofSetterCall(instance, method, this.args);
        }
        return true;
    }

    public Class getBaseClass() {
        return baseClass;
    }

    public Method getIdProofSetter() {
        return idProofSetter;
    }

    public ArrayList<Method> getAuthenticateMethods() {
        return authenticateMethods;
    }

    public HashMap<String, Method> getAuthInfoGetters() {
        return authInfoGetters;
    }

    public Method[] getArgs() {
        return args;
    }

    public void discoverInstance(Object instance) {
        if (!this.instances.containsKey(instance)){
            this.instances.put(instance, new SubjectInstance(instance, this));
            this.instances.get(instance).init();
        }
    }

    public HashMap<Object, SubjectInstance> getInstances() {
        return instances;
    }
}
