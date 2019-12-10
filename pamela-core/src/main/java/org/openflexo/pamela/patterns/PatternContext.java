package org.openflexo.pamela.patterns;

import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.patterns.authenticator.AuthenticatorPattern;
import org.openflexo.pamela.patterns.authenticator.annotations.AuthenticatorSubject;

import java.lang.annotation.Annotation;
import java.util.*;

public class PatternContext {
    private HashMap<String, AuthenticatorPattern> authenticatorPatterns;
    private HashMap<Class, ArrayList<String>> authenticatorSubjectClasses;
    private HashMap<Class, ArrayList<String>> authenticatorClasses;
    private ModelContext modelContext;
    private boolean inConstructor;

    private HashMap<Object, ArrayList<PatternClassWrapper>> knownInstances;

    public PatternContext(ModelContext context) {
        this.authenticatorPatterns = new HashMap<>();
        this.authenticatorSubjectClasses = new HashMap<>();
        this.authenticatorClasses = new HashMap<>();
        this.knownInstances = new HashMap<>();
        this.modelContext = context;
        this.inConstructor = false;
    }

    public void attachClass(Class baseClass) throws NoSuchMethodException, ModelDefinitionException {
        for (Class klass : PatternLibrary.getClassHierarchy(baseClass)) {
            for (Annotation a : klass.getAnnotations()) {
                if (a instanceof AuthenticatorSubject) {
                    AuthenticatorSubject subjectAnnotation = (AuthenticatorSubject) a;
                    if (!this.authenticatorPatterns.containsKey(subjectAnnotation.patternID())) {
                        this.authenticatorPatterns.put(subjectAnnotation.patternID(), new AuthenticatorPattern(this, subjectAnnotation.patternID()));
                    }
                    if (!this.authenticatorSubjectClasses.containsKey(klass)) {
                        this.authenticatorSubjectClasses.put(klass, new ArrayList<>());
                    }
                    this.authenticatorSubjectClasses.get(klass).add(subjectAnnotation.patternID());
                    this.authenticatorPatterns.get(subjectAnnotation.patternID()).attachClass(klass);
                }
            }
        }
    }

    public void insideConstructor(){
        this.inConstructor = true;
    }

    public void leavingConstructor(){
        this.inConstructor = false;
    }

    public ArrayList<PatternClassWrapper> getRelatedPatternsFromInstance(Object instance) {
        ArrayList<PatternClassWrapper> wrappers;
        if (this.knownInstances.containsKey(instance)) {
            wrappers = this.knownInstances.get(instance);
        } else {
            wrappers = new ArrayList<>();
            for (Class klass : PatternLibrary.getClassHierarchy(instance.getClass())) {
                if (this.authenticatorSubjectClasses.containsKey(klass)) {
                    for (String id : this.authenticatorSubjectClasses.get(klass)) {
                        wrappers.add(new PatternClassWrapper(this.authenticatorPatterns.get(id), klass));
                        if (!this.inConstructor)this.authenticatorPatterns.get(id).discoverInstance(instance, klass);
                    }
                }
                if (this.authenticatorClasses.containsKey(klass)) {
                    for (String id : this.authenticatorClasses.get(klass)) {
                        wrappers.add(new PatternClassWrapper(this.authenticatorPatterns.get(id), klass));
                        if (!this.inConstructor)this.authenticatorPatterns.get(id).discoverInstance(instance, klass);
                    }
                }
            }
            if (!this.inConstructor)this.knownInstances.put(instance, wrappers);
        }
        return wrappers;
    }

    public HashMap<Class, ArrayList<String>> getAuthenticatorSubjectClasses() {
        return authenticatorSubjectClasses;
    }

    public HashMap<String, AuthenticatorPattern> getAuthenticatorPatterns() {
        return authenticatorPatterns;
    }

    public void attachAuthenticatorClass(Class baseClass, String id) {
        if (this.authenticatorClasses.containsKey(baseClass) && !this.authenticatorClasses.get(baseClass).contains(id)) {
            this.authenticatorClasses.get(baseClass).add(id);
        } else if (!this.authenticatorClasses.containsKey(baseClass)) {
            this.authenticatorClasses.put(baseClass, new ArrayList<>(Arrays.asList(id)));
        }
    }

    public ArrayList<PatternClassWrapper> getRelatedPatternsFromClass(Class baseClass) {
        ArrayList<PatternClassWrapper> wrappers = new ArrayList<>();
        for (Class klass : PatternLibrary.getClassHierarchy(baseClass)) {
            if (this.authenticatorSubjectClasses.containsKey(klass)) {
                for (String id : this.authenticatorSubjectClasses.get(klass)) {
                    wrappers.add(new PatternClassWrapper(this.authenticatorPatterns.get(id), klass));
                }
            }
            if (this.authenticatorClasses.containsKey(klass)) {
                for (String id : this.authenticatorClasses.get(klass)) {
                    wrappers.add(new PatternClassWrapper(this.authenticatorPatterns.get(id), klass));
                }
            }
        }
        return wrappers;
    }

    public HashMap<Object, ArrayList<PatternClassWrapper>> getKnownInstances() {
        return knownInstances;
    }

    public boolean isInConstructor() {
        return this.inConstructor;
    }

    public ModelContext getContext() {
        return this.modelContext;
    }
}
