package org.openflexo.pamela.patterns;

import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.patterns.authenticator.AuthenticatorPattern;
import org.openflexo.pamela.patterns.authenticator.annotations.AuthenticatorSubject;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public class PatternContext {
    private HashMap<String, AuthenticatorPattern> authenticatorPatterns;
    private HashMap<Class, ArrayList<String>> authenticatorSubjectClasses;
    private ModelContext modelContext;
    private boolean pamela_on = false;

    public PatternContext(ModelContext context, boolean pamela_on){
        this.authenticatorPatterns = new HashMap<>();
        this.authenticatorSubjectClasses = new HashMap<>();
        this.pamela_on = pamela_on;
        this.modelContext = context;
    }

    public PatternContext(){
        this(null, false);
    }

    public ModelContext getContext(){
        return this.modelContext;
    }

    public void attachClass(Class klass) throws NoSuchMethodException, ModelDefinitionException {
        for (Annotation a : klass.getAnnotations()){
            if (a instanceof AuthenticatorSubject){
                AuthenticatorSubject annotation = (AuthenticatorSubject) a;
                if (!this.authenticatorPatterns.containsKey(annotation.patternID())){
                    this.authenticatorPatterns.put(annotation.patternID(), new AuthenticatorPattern(this, annotation.patternID()));
                }
                if (!this.authenticatorSubjectClasses.containsKey(klass)){
                    this.authenticatorSubjectClasses.put(klass, new ArrayList<>());
                }
                this.authenticatorSubjectClasses.get(klass).add(annotation.patternID());
                this.authenticatorPatterns.get(annotation.patternID()).attachClass(klass);
            }
        }
    }

    public boolean processMethodBeforeInvoke(Object instance, Method method) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        boolean returned = false;
        if (!this.pamela_on){
            if (this.authenticatorSubjectClasses.containsKey(instance.getClass())){
                for (String id : this.authenticatorSubjectClasses.get(instance.getClass())){
                    returned = returned || this.authenticatorPatterns.get(id).processMethodBeforeInvoke(instance, method, instance.getClass());
                }
            }
            return returned;
        }
        else {
            for (Class interf : instance.getClass().getInterfaces()){
                if (this.authenticatorSubjectClasses.containsKey(interf)){
                    for (String id : this.authenticatorSubjectClasses.get(interf)){
                        returned = returned || this.authenticatorPatterns.get(id).processMethodBeforeInvoke(instance, method, interf);
                    }
                    return returned;
                }
            }
            return returned;
        }
    }
}
