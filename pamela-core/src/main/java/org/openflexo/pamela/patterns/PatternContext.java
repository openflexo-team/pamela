package org.openflexo.pamela.patterns;

import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.patterns.authenticator.AuthenticatorPattern;
import org.openflexo.pamela.patterns.authenticator.annotations.AuthenticatorSubject;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.patterns.authorization.AuthorizationPattern;
import org.openflexo.pamela.patterns.authorization.annotations.AuthorizationSubject;
import org.openflexo.pamela.patterns.authorization.annotations.ProtectedResource;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Class wrapping all the known patterns of the model.<br>
 * It has the responsibility of:
 * <ul><li>Instantiating the discovered patterns when creating the model.</li>
 * <li>Saving all known patterns.</li>
 * <li>Relaying method execution, checks and instance discovery to the relevant patterns.</li></ul>
 *
 *  @author C. SILVA
 */
public class PatternContext {
    private final HashMap<String, AbstractPattern> patterns;
    private final HashMap<Class, ArrayList<String>> classesOfInterest;
    private final ModelContext modelContext;
    private boolean inConstructor;
    private final HashMap<Object, ArrayList<PatternClassWrapper>> knownInstances;

    /**
     * Constructor of the class
     * @param context reference to the {@link ModelContext} in which this constructor was called.
     */
    public PatternContext(ModelContext context) {
        this.patterns = new HashMap<>();
        this.classesOfInterest = new HashMap<>();
        this.knownInstances = new HashMap<>();
        this.modelContext = context;
        this.inConstructor = false;
    }

    /**
     * Method called when a new class is discovered by the {@link ModelContext}.<br>
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
                    if (!this.classesOfInterest.containsKey(klass)) {
                        this.classesOfInterest.put(klass, new ArrayList<>());
                    }
                    if (!this.classesOfInterest.get(klass).contains(subjectAnnotation.patternID())){
                        this.classesOfInterest.get(klass).add(subjectAnnotation.patternID());
                        this.patterns.get(subjectAnnotation.patternID()).attachClass(klass);
                    }
                }
                if (a instanceof AuthorizationSubject){
                    AuthorizationSubject subjectAnnotation = (AuthorizationSubject) a;
                    if (!this.patterns.containsKey(subjectAnnotation.patternID())){
                        this.patterns.put(subjectAnnotation.patternID(), new AuthorizationPattern(this, subjectAnnotation.patternID()));
                    }
                    if (!this.classesOfInterest.containsKey(klass)) {
                        this.classesOfInterest.put(klass, new ArrayList<>());
                    }
                    if (!this.classesOfInterest.get(klass).contains(subjectAnnotation.patternID())){
                        this.classesOfInterest.get(klass).add(subjectAnnotation.patternID());
                        this.patterns.get(subjectAnnotation.patternID()).attachClass(klass);
                    }
                }

                if (a instanceof ProtectedResource){
                    ProtectedResource resourceAnnotation = (ProtectedResource) a;
                    if (!this.patterns.containsKey(resourceAnnotation.patternID())){
                        this.patterns.put(resourceAnnotation.patternID(), new AuthorizationPattern(this, resourceAnnotation.patternID()));
                    }
                    if (!this.classesOfInterest.containsKey(klass)) {
                        this.classesOfInterest.put(klass, new ArrayList<>());
                    }
                    if (!this.classesOfInterest.get(klass).contains(resourceAnnotation.patternID())){
                        this.classesOfInterest.get(klass).add(resourceAnnotation.patternID());
                        this.patterns.get(resourceAnnotation.patternID()).attachClass(klass);
                    }
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
                if (this.classesOfInterest.containsKey(klass)) {
                    for (String id : this.classesOfInterest.get(klass)) {
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
     * Saves the the class with the given pattern identifier. This method is to be called by {@link AbstractPattern}
     * when its analysis lead to the discovery of a new Class of interest.
     * @param baseClass Class to save
     * @param id Identifier of the pattern in which <code>baseClass</code> is involved in
     */
    public void attachClassFromAbstractPattern(Class baseClass, String id) {
        if (this.classesOfInterest.containsKey(baseClass) && !this.classesOfInterest.get(baseClass).contains(id)) {
            this.classesOfInterest.get(baseClass).add(id);
        } else if (!this.classesOfInterest.containsKey(baseClass)) {
            this.classesOfInterest.put(baseClass, new ArrayList<>(Collections.singletonList(id)));
        }
    }

    /**
     * Analyze a class to determine the patterns it is involved in.
     * @param baseClass Class to analyze
     * @return the list of {@link PatternClassWrapper} containing the patterns <code>baseClass</code> is involved in as well
     * as the pattern-related class of its class tree.
     */
    public ArrayList<PatternClassWrapper> getRelatedPatternsFromClass(Class baseClass) {
        ArrayList<PatternClassWrapper> wrappers = new ArrayList<>();
        for (Class klass : PatternLibrary.getClassHierarchy(baseClass)) {
            if (this.classesOfInterest.containsKey(klass)) {
                for (String id : this.classesOfInterest.get(klass)) {
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

    /**
     * @return the {@link HashMap} mapping known instances with the patterns they are involved in, wrapped in a list of {@link PatternClassWrapper}
     */
    public HashMap<Object, ArrayList<PatternClassWrapper>> getKnownInstances() {
        return this.knownInstances;
    }
}
