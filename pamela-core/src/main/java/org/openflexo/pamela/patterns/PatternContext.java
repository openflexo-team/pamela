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

    public PatternContext(ModelContext context){
        this.authenticatorPatterns = new HashMap<>();
        this.authenticatorSubjectClasses = new HashMap<>();
        this.modelContext = context;
    }

    public void attachClass(Class baseClass) throws NoSuchMethodException, ModelDefinitionException {
        for (Class klass : PatternLibrary.getClassHierarchy(baseClass)) {
            for (Annotation a : klass.getAnnotations()) {
                if (a instanceof AuthenticatorSubject) {
                    AuthenticatorSubject annotation = (AuthenticatorSubject) a;
                    if (!this.authenticatorPatterns.containsKey(annotation.patternID())) {
                        this.authenticatorPatterns.put(annotation.patternID(), new AuthenticatorPattern(this, annotation.patternID()));
                    }
                    if (!this.authenticatorSubjectClasses.containsKey(klass)) {
                        this.authenticatorSubjectClasses.put(klass, new ArrayList<>());
                    }
                    this.authenticatorSubjectClasses.get(klass).add(annotation.patternID());
                    this.authenticatorPatterns.get(annotation.patternID()).attachClass(klass);
                }
            }
        }
    }

    public ArrayList<PatternClassWrapper> getRelatedPatterns(Object instance){
        ArrayList<PatternClassWrapper> wrappers = new ArrayList<>();
        for (Class klass : PatternLibrary.getClassHierarchy(instance.getClass())){
            if (this.authenticatorSubjectClasses.containsKey(klass)){
                for (String id : this.authenticatorSubjectClasses.get(klass)){
                    wrappers.add(new PatternClassWrapper(this.authenticatorPatterns.get(id), klass));
                }
            }
        }
        return wrappers;
    }

    public HashMap<Class, ArrayList<String>> getAuthenticatorSubjectClasses() {
        return authenticatorSubjectClasses;
    }

    public HashMap<String, AuthenticatorPattern> getAuthenticatorPatterns() {
        return authenticatorPatterns;
    }
}
