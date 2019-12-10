package org.openflexo.pamela.patterns;

import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.patterns.authenticator.AuthenticatorPattern;
import org.openflexo.pamela.patterns.authenticator.annotations.AuthenticatorSubject;
import org.openflexo.pamela.patterns.authenticator.annotations.Authenticator;
import org.openflexo.pamela.factory.ProxyMethodHandler;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author C. SILVA
 *
 * Class wrapping all the known patterns of the model.
 * It has the responsibility of:
 *  - Instantiate the discovered patterns when creating the model.
 *  - Saves all known patterns.
 *  - Relay method execution, checks and instance discovery to teh relevant patterns.
 */
public class PatternContext {
    private final HashMap<String, AbstractPattern> patterns;
    private final HashMap<Class, ArrayList<String>> authenticatorSubjectClasses;
    private final HashMap<Class, ArrayList<String>> authenticatorClasses;
    private final ModelContext modelContext;
    private boolean inConstructor;
    private final HashMap<Object, ArrayList<PatternClassWrapper>> knownInstances;

    /**
     * Constructor of the class
     * @param context reference to the {@link ModelContext} in which this constructor was called.
     */
    public PatternContext(ModelContext context) {
        this.patterns = new HashMap<>();
        this.authenticatorSubjectClasses = new HashMap<>();
        this.authenticatorClasses = new HashMap<>();
        this.knownInstances = new HashMap<>();
        this.modelContext = context;
        this.inConstructor = false;
    }

    /**
     * Method called when a new class is discovered by the {@link ModelContext}.
     * Search the <code>baseClass</code> for pattern-related annotations and instantiate the relevant patterns.
     * @param baseClass class to search
     * @throws ModelDefinitionException In case of error during analysis of the class
     */
    public void attachClass(Class baseClass) throws ModelDefinitionException {
        for (Class klass : PatternLibrary.getClassHierarchy(baseClass)) {
            for (Annotation a : klass.getAnnotations()) {
                if (a instanceof AuthenticatorSubject) {
                    AuthenticatorSubject subjectAnnotation = (AuthenticatorSubject) a;
                    if (!this.patterns.containsKey(subjectAnnotation.patternID())) {
                        this.patterns.put(subjectAnnotation.patternID(), new AuthenticatorPattern(this, subjectAnnotation.patternID()));
                    }
                    if (!this.authenticatorSubjectClasses.containsKey(klass)) {
                        this.authenticatorSubjectClasses.put(klass, new ArrayList<>());
                    }
                    this.authenticatorSubjectClasses.get(klass).add(subjectAnnotation.patternID());
                    this.patterns.get(subjectAnnotation.patternID()).attachClass(klass);
                }
            }
        }
    }

    /**
     * Method called when entering in a pattern-related constructor
     */
    public void insideConstructor(){
        this.inConstructor = true;
    }

    /**
     * Method called when leaving a pattern-related constructor
     */
    public void leavingConstructor(){
        this.inConstructor = false;
    }

    /**
     * Analyze an instance to determine the patterns it is involved in.
     * @param instance Object to analyze
     * @return the list of {@link PatternClassWrapper} containing the patterns <code>instance</code> is involved in as well
     * as the pattern-related class of its class tree.
     */
    public ArrayList<PatternClassWrapper> getRelatedPatternsFromInstance(Object instance) {
        ArrayList<PatternClassWrapper> wrappers;
        if (this.knownInstances.containsKey(instance)) {
            wrappers = this.knownInstances.get(instance);
        } else {
            wrappers = new ArrayList<>();
            for (Class klass : PatternLibrary.getClassHierarchy(instance.getClass())) {
                if (this.authenticatorSubjectClasses.containsKey(klass)) {
                    for (String id : this.authenticatorSubjectClasses.get(klass)) {
                        wrappers.add(new PatternClassWrapper(this.patterns.get(id), klass));
                        if (!this.inConstructor)this.patterns.get(id).discoverInstance(instance, klass);
                    }
                }
                if (this.authenticatorClasses.containsKey(klass)) {
                    for (String id : this.authenticatorClasses.get(klass)) {
                        wrappers.add(new PatternClassWrapper(this.patterns.get(id), klass));
                        if (!this.inConstructor)this.patterns.get(id).discoverInstance(instance, klass);
                    }
                }
            }
            if (!this.inConstructor)this.knownInstances.put(instance, wrappers);
        }
        return wrappers;
    }

    /**
     * @return the {@link HashMap} mapping all known pattern identifiers with the corresponding {@link AbstractPattern}.
     */
    public HashMap<String, AbstractPattern> getPatterns() {
        return patterns;
    }

    /**
     * Saves the the class as an <code>Authenticator</code>
     * @param baseClass {@link Authenticator} annotated class to save
     * @param id Identifier of the pattern in which <code>baseClass</code> is involved in
     */
    public void attachAuthenticatorClass(Class baseClass, String id) {
        if (this.authenticatorClasses.containsKey(baseClass) && !this.authenticatorClasses.get(baseClass).contains(id)) {
            this.authenticatorClasses.get(baseClass).add(id);
        } else if (!this.authenticatorClasses.containsKey(baseClass)) {
            this.authenticatorClasses.put(baseClass, new ArrayList<>(Arrays.asList(id)));
        }
    }

    /**
     * Analyze a class to determine the patterns it is involved in.
     * @param baseClass Class to analyze
     * @return the list of {@link PatternClassWrapper} containing the patterns <code>class/code> is involved in as well
     * as the pattern-related class of its class tree.
     */
    public ArrayList<PatternClassWrapper> getRelatedPatternsFromClass(Class baseClass) {
        ArrayList<PatternClassWrapper> wrappers = new ArrayList<>();
        for (Class klass : PatternLibrary.getClassHierarchy(baseClass)) {
            if (this.authenticatorSubjectClasses.containsKey(klass)) {
                for (String id : this.authenticatorSubjectClasses.get(klass)) {
                    wrappers.add(new PatternClassWrapper(this.patterns.get(id), klass));
                }
            }
            if (this.authenticatorClasses.containsKey(klass)) {
                for (String id : this.authenticatorClasses.get(klass)) {
                    wrappers.add(new PatternClassWrapper(this.patterns.get(id), klass));
                }
            }
        }
        return wrappers;
    }

    /**
     * @return true if the {@link ProxyMethodHandler} is not currently handling a pattern-related constructor call.
     */
    public boolean notInConstructor() {
        return !this.inConstructor;
    }

    /**
     * @return the {@link ModelContext} wrapping this object.
     */
    public ModelContext getContext() {
        return this.modelContext;
    }
}
