package org.openflexo.pamela.patterns.authenticator;

import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.patterns.AbstractPattern;
import org.openflexo.pamela.patterns.PatternContext;
import org.openflexo.pamela.patterns.PatternLibrary;
import org.openflexo.pamela.patterns.authenticator.annotations.Authenticator;
import org.openflexo.pamela.patterns.authenticator.annotations.AuthenticatorSubject;
import org.openflexo.pamela.patterns.authenticator.exceptions.InconsistentAuthenticatorEntityException;
import org.openflexo.pamela.patterns.authenticator.exceptions.InconsistentSubjectEntityException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class AuthenticatorPattern extends AbstractPattern {
    private PatternContext context;
    private String id;
    private HashMap<Class, SubjectEntity> subjects;
    private AuthenticatorEntity authenticator;

    public AuthenticatorPattern(PatternContext context, String id){
        this.context = context;
        this.id = id;
        this.subjects = new HashMap<>();
    }

    public PatternContext getContext(){
        return this.context;
    }

    public void attachClass(Class baseClass) throws ModelDefinitionException, NoSuchMethodException {
        AuthenticatorSubject subjectAnnotation = (AuthenticatorSubject) baseClass.getAnnotation(AuthenticatorSubject.class);
        if (subjectAnnotation != null && subjectAnnotation.patternID().compareTo(this.id) == 0){
            this.attachSubject(baseClass);
        }
    }

    protected void attachAuthenticatorFromAuthenticatorGetter(Class authenticatorClass) throws ModelDefinitionException {
        if (this.authenticator == null){
            Class auth = null;
            for (Class klass : PatternLibrary.getClassHierarchy(authenticatorClass)){
                Annotation annotation = klass.getAnnotation(Authenticator.class);
                if (annotation != null){
                    auth = klass;
                }
            }
            if (auth == null){
                throw new InconsistentAuthenticatorEntityException("Missing annotations in " + authenticatorClass.getSimpleName() + "Authenticator definition with ID " + this.id);
            }
            AuthenticatorEntity authenticator = new AuthenticatorEntity(this, auth);
            if  (authenticator.isComplete()){
                this.authenticator = authenticator;
                this.context.attachAuthenticatorClass(this.authenticator.getBaseClass(), this.id);
            }
            else {
                throw new InconsistentAuthenticatorEntityException("Missing annotations in " + authenticatorClass.getSimpleName() + "Authenticator definition with ID " + this.id);
            }
        }
    }

    private void attachSubject(Class klass) throws ModelDefinitionException, NoSuchMethodException {
        SubjectEntity subject = new SubjectEntity(this, klass);
        if (subject.isComplete()){
            this.subjects.put(klass, subject);
        }
        else {
            throw new InconsistentSubjectEntityException("Missing annotations in " + klass.getSimpleName() + "Authenticator subject definition with ID " + this.id);
        }
    }

    public String getID(){
        return this.id;
    }

    public AuthenticatorEntity getAuthenticator(){
        return this.authenticator;
    }

    @Override
    public boolean processMethodBeforeInvoke(Object instance, Method method, Class klass) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        boolean returned = true;
        if (this.subjects.containsKey(klass)){
            returned = this.subjects.get(klass).processMethodBeforeInvoke(instance, method, klass);
        }
        return returned;
    }

    @Override
    public void discoverInstance(Object instance, Class klass){
        if (this.subjects.containsKey(klass)){
            this.subjects.get(klass).discoverInstance(instance);
        }
        if (this.authenticator.getBaseClass().equals(klass)){
            this.authenticator.discoverInstance(instance);
        }
    }

    public void performAuthentication(Object instance, Method idProofSetter, Method[] args, Method authenticatorGetter) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Object authenticatorInstance = authenticatorGetter.invoke(instance);
        Object[] instanceArgs = new Object[args.length];
        int current = 0;
        for (Method m : args){
            instanceArgs[current] = m.invoke(instance);
            current++;
        }
        Object proof = this.authenticator.getMethod().invoke(authenticatorInstance, instanceArgs);
        idProofSetter.invoke(instance, proof);
    }

    public boolean handleIdProofSetterCall(Object instance, Method method, Method[] args) {
        StackTraceElement[] cause = Thread.currentThread().getStackTrace();
        if (cause.length < 12 || cause[12].getClassName().compareTo(this.getClass().getName()) != 0){
            return false;
        }
        return true;
    }

    public HashMap<Class, SubjectEntity> getSubjects() {
        return subjects;
    }
}
