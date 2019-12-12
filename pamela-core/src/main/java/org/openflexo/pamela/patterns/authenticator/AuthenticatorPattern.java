package org.openflexo.pamela.patterns.authenticator;

import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.patterns.AbstractPattern;
import org.openflexo.pamela.patterns.PatternContext;
import org.openflexo.pamela.patterns.PatternLibrary;
import org.openflexo.pamela.patterns.authenticator.annotations.Authenticator;
import org.openflexo.pamela.patterns.authenticator.annotations.AuthenticatorSubject;
import org.openflexo.pamela.patterns.authenticator.exceptions.InconsistentAuthenticatorEntityException;
import org.openflexo.pamela.patterns.authenticator.exceptions.InconsistentSubjectEntityException;
import org.openflexo.pamela.patterns.authenticator.annotations.AuthenticateMethod;
import org.openflexo.pamela.patterns.authenticator.annotations.AuthenticatorGetter;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * This class represents an instance of an <code>Authenticator Pattern</code>. An instance is uniquely identified by
 * the <code>patternID</code> field of associated annotations.<br>
 * It has the responsibility of:
 *  <ul><li>Creating and saving the {@link AuthenticatorEntity} and all {@link SubjectEntity} relevant to this pattern.</li>
 *  <li>Throwing {@link InconsistentAuthenticatorEntityException} and {@link InconsistentAuthenticatorEntityException} when
 *  the associated entity instanciation has failed.</li>
 *  <li>Performing the authentication in case of {@link AuthenticateMethod} call.</li>
 *  <li>Relaying to the relevant entities the instance discovery.</li>
 *  <li>Relaying to the relevant entities the checks and execution of methods.</li></ul>
 *
 *  @author C. SILVA
 */
public class AuthenticatorPattern extends AbstractPattern {
    private final PatternContext context;
    private final String id;
    private final HashMap<Class, SubjectEntity> subjects;
    private AuthenticatorEntity authenticator;

    /**
     * Constructor of the class.
     * @param context Reference to the {@link PatternContext} instance
     * @param id Unique identifier of the pattern
     */
    public AuthenticatorPattern(PatternContext context, String id){
        this.context = context;
        this.id = id;
        this.subjects = new HashMap<>();
    }

    /**
     * Identifies the entity type to instantiate with the given class, and if relevant, instantiate the associated entity.
     * @param baseClass Class to analyze
     * @throws ModelDefinitionException In case of inconsistent subject or authenticator definition
     */
    public void attachClass(Class baseClass) throws ModelDefinitionException {
        AuthenticatorSubject subjectAnnotation = (AuthenticatorSubject) baseClass.getAnnotation(AuthenticatorSubject.class);
        if (subjectAnnotation != null && subjectAnnotation.patternID().compareTo(this.id) == 0){
            this.attachSubject(baseClass);
        }
    }

    /**
     * Method called before every <code>Authenticator Pattern</code> class method. It relays the check and execution to
     * the relevant entity.
     * @param instance Object on which the method is called
     * @param method Called method
     * @param klass Pattern-related class of identified im the class tree of <code>instance</code>
     * @return true if the execution should continue after this method, false if not.
     * @throws InvocationTargetException if an error occurred when internally invoking a method
     * @throws IllegalAccessException if an error occurred when internally invoking a method
     * @throws NoSuchMethodException if an error occurred when internally invoking a method
     */
    @Override
    public boolean processMethodBeforeInvoke(Object instance, Method method, Class klass) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (context.notInConstructor()){
            this.checkBeforeInvoke(instance, method, klass);
        }
        boolean returned = true;
        if (this.subjects.containsKey(klass)){
            returned = this.subjects.get(klass).processMethodBeforeInvoke(instance, method, klass);
        }
        return returned;
    }

    /**
     * Relay the instance discovery to the relevant entity. This method is to be called when a new
     * instance is discovered by the {@link PatternContext} instance.
     * @param instance instance to discovered.
     * @param klass Pattern-related class of identified im the class tree of <code>instance</code>
     */
    @Override
    public void discoverInstance(Object instance, Class klass){
        if (this.subjects.containsKey(klass)){
            this.subjects.get(klass).discoverInstance(instance);
        }
        if (this.authenticator.getBaseClass().equals(klass)){
            this.authenticator.discoverInstance(instance);
        }
    }

    /**
     * Method called after every <code>Authenticator Pattern</code> class method. It relays the check to
     * the relevant entity.
     * @param instance Object on which the method is called
     * @param method Called method
     * @param klass Pattern-related class of identified im the class tree of <code>instance</code>
     * @param returnValue Return value of the <code>method</code> invoke
     */
    @Override
    public void processMethodAfterInvoke(Object instance, Method method, Class klass, Object returnValue) {
        if (context.notInConstructor()) {
            this.checkAfterInvoke(instance, method, klass, returnValue);
        }
    }

    /**
     * @return the {@link HashMap} mapping {@link AuthenticatorSubject} annotated class with the corresponding {@link SubjectEntity}
     */
    public HashMap<Class, SubjectEntity> getSubjects() {
        return subjects;
    }

    /**
     * @return the {@link PatternContext} wrapping this pattern
     */
    public PatternContext getContext(){
        return this.context;
    }

    /**
     * @return the unique identifier of this pattern
     */
    public String getID(){
        return this.id;
    }

    /**
     * @return the {@link AuthenticatorEntity} of this pattern
     */
    public AuthenticatorEntity getAuthenticator(){
        return this.authenticator;
    }

    /**
     * Instantiate an {@link AuthenticatorEntity} with the given class. This method is to be called
     * when parsing the {@link AuthenticatorSubject} annotated class with the return type of the {@link AuthenticatorGetter} annotated method.
     * @param authenticatorClass {@link Authenticator} annotated class.
     * @throws InconsistentAuthenticatorEntityException In case of inconsistent authenticator definition
     */
    void attachAuthenticatorFromAuthenticatorGetter(Class authenticatorClass) throws InconsistentAuthenticatorEntityException {
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
                this.context.attachClassFromAbstractPattern(this.authenticator.getBaseClass(), this.id);
            }
            else {
                throw new InconsistentAuthenticatorEntityException("Missing annotations in " + authenticatorClass.getSimpleName() + "Authenticator definition with ID " + this.id);
            }
        }
    }

    /**
     * Performs the authentication of the given instance.
     * @param instance Object on which the authentication is to be performed on.
     * @param idProofSetter Setter method of the <code>Proof of Identity</code>
     * @param args Ordered list of <code>instance</code> getters of the <code>request</code> method parameters
     * @param authenticatorGetter Getter method of the <code>instance</code> authenticator instance
     * @param subjectEntity {@link SubjectEntity} wrapping the instance
     * @throws InvocationTargetException if an error occurred when internally invoking a method
     * @throws IllegalAccessException if an error occurred when internally invoking a method
     */
    void performAuthentication(Object instance, Method idProofSetter, Method[] args, Method authenticatorGetter, SubjectEntity subjectEntity) throws InvocationTargetException, IllegalAccessException {
        Object authenticatorInstance = authenticatorGetter.invoke(instance);
        Object[] instanceArgs = new Object[args.length];
        int current = 0;
        for (Method m : args){
            instanceArgs[current] = m.invoke(instance);
            current++;
        }
        Object proof = this.authenticator.getRequestMethod().invoke(authenticatorInstance, instanceArgs);
        subjectEntity.getInstances().get(instance).setIDProof(proof);
        idProofSetter.invoke(instance, proof);
    }

    /**
     * Instantiate a {@link SubjectEntity} with the given class.
     * @param klass {@link AuthenticatorSubject} annotated class
     * @throws ModelDefinitionException In case of inconsistent subject definition.
     */
    private void attachSubject(Class klass) throws ModelDefinitionException {
        SubjectEntity subject = new SubjectEntity(this, klass);
        if (subject.isComplete()){
            this.subjects.put(klass, subject);
        }
        else {
            throw new InconsistentSubjectEntityException("Missing annotations in " + klass.getSimpleName() + "Authenticator subject definition with ID " + this.id);
        }
    }

    /**
     * Relay the check of invariant and precondition on method call to the relevant instance wrappers (i.e.
     * {@link SubjectInstance} and\or {@link AuthenticatorInstance})
     * @param instance Object on which the method is called
     * @param method Called method
     * @param klass Pattern-related class of identified im the class tree of <code>instance</code>
     */
    private void checkBeforeInvoke(Object instance, Method method, Class klass) {
        if (this.subjects.containsKey(klass) && this.subjects.get(klass).getInstances().containsKey(instance)){
            this.subjects.get(klass).getInstances().get(instance).checkBeforeInvoke(method);
        }
        if (this.authenticator.getBaseClass().equals(klass) && this.authenticator.getInstances().containsKey(instance)){
            this.authenticator.getInstances().get(instance).checkBeforeInvoke(method);
        }
    }

    /**
     * Relay the check of invariant and postcondition after method call to the relevant instance wrappers (i.e.
     * {@link SubjectInstance} and\or {@link AuthenticatorInstance})
     * @param instance Object on which the method is called
     * @param method Called method
     * @param klass Pattern-related class of identified im the class tree of <code>instance</code>
     * @param returnValue Return value of the <code>method</code> invoke
     */
    private void checkAfterInvoke(Object instance, Method method, Class klass, Object returnValue) {
        if (this.subjects.containsKey(klass) && this.subjects.get(klass).getInstances().containsKey(instance)){
            this.subjects.get(klass).getInstances().get(instance).checkAfterInvoke(method, returnValue);
        }
        if (this.authenticator.getBaseClass().equals(klass) && this.authenticator.getInstances().containsKey(instance)){
            this.authenticator.getInstances().get(instance).checkAfterInvoke(method, returnValue);
        }
    }
}
