package org.openflexo.pamela.patterns.authenticator;

import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.patterns.PatternContext;
import org.openflexo.pamela.patterns.authenticator.annotations.AuthenticatorSubject;
import org.openflexo.pamela.patterns.authenticator.exceptions.InconsistentAuthenticatorEntityException;
import org.openflexo.pamela.patterns.authenticator.exceptions.InconsistentSubjectEntityException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class AuthenticatorPattern {
    private PatternContext context;
    private String id;
    private HashMap<Class, SubjectEntity> subjects;
    private AuthenticatorEntity authenticator;

    public AuthenticatorPattern(PatternContext context, String id){
        this.context = context;
        this.id = id;
        this.subjects = new HashMap<Class, SubjectEntity>();
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

    protected void attachAuthenticatorFromRequiresMethod(Class authenticatorClass) throws ModelDefinitionException {
        if (this.authenticator == null){
            AuthenticatorEntity authenticator = new AuthenticatorEntity(this, authenticatorClass);
            if  (authenticator.isComplete()){
                this.authenticator = authenticator;
            }
            else {
                throw new InconsistentAuthenticatorEntityException("Missing annotations in " + authenticatorClass.getSimpleName() + "Authenticator definition with ID " + this.id);
            }
        }
        else if (this.authenticator.getClassName().compareTo(authenticatorClass.getName()) != 0){
            throw new ModelDefinitionException("Duplicate @Authenticator for pattern " + this.id);
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

    public boolean processMethodBeforeInvoke(Object instance, Method method, Class klass) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        boolean returned = false;
        if (this.subjects.containsKey(klass)){
            returned = this.subjects.get(klass).processMethodBeforeInvoke(instance, method, klass);
        }
        return returned;
    }

    public void performAuthentication(Object instance, Method idProofSetter, Method[] args, String authenticatorGetter) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Object authenticatorInstance = instance.getClass().getMethod(authenticatorGetter).invoke(instance);
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
        if (cause.length < 11 || cause[11].getClassName().compareTo(this.getClass().getName()) != 0){
            return true;
        }
        return false;
    }
}
