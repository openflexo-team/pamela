package org.openflexo.pamela.patterns.authenticator;

import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.patterns.AbstractPattern;
import org.openflexo.pamela.patterns.PatternContext;
import org.openflexo.pamela.patterns.PatternLibrary;
import org.openflexo.pamela.patterns.ReturnWrapper;
import org.openflexo.pamela.patterns.authenticator.annotations.Authenticator;
import org.openflexo.pamela.patterns.authenticator.annotations.AuthenticatorSubject;
import org.openflexo.pamela.patterns.authenticator.exceptions.InconsistentAuthenticatorEntityException;
import org.openflexo.pamela.patterns.authenticator.exceptions.InconsistentSubjectEntityException;
import org.openflexo.pamela.patterns.authenticator.annotations.AuthenticateMethod;
import org.openflexo.pamela.patterns.authenticator.annotations.AuthenticatorGetter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * This class represents an instance of an <code>Authenticator Pattern</code>. An instance is uniquely identified by
 * the <code>patternID</code> field of associated annotations.<br>
 * It has the responsibility of:
 *  <ul><li>Creating and saving the {@link AuthenticatorEntity} and all {@link AuthenticatorSubjectEntity} relevant to this pattern.</li>
 *  <li>Throwing {@link InconsistentAuthenticatorEntityException} and {@link InconsistentAuthenticatorEntityException} when
 *  the associated entity instanciation has failed.</li>
 *  <li>Performing the authentication in case of {@link AuthenticateMethod} call.</li>
 *  <li>Relaying to the relevant entities the instance discovery.</li>
 *  <li>Relaying to the relevant entities the checks and execution of methods.</li></ul>
 *
 *  @author C. SILVA
 */
public class AuthenticatorPattern extends AbstractPattern {
    private final HashMap<Class, AuthenticatorSubjectEntity> subjects;
    private AuthenticatorEntity authenticator;

    /**
     * Constructor of the class.
     * @param context Reference to the {@link PatternContext} instance
     * @param id Unique identifier of the pattern
     */
    public AuthenticatorPattern(PatternContext context, String id){
        super(context,id);
        this.subjects = new HashMap<>();
    }

    /**
     * Identifies the entity type to instantiate with the given class, and if relevant, instantiate the associated entity.
     * @param baseClass Class to analyze
     * @throws ModelDefinitionException In case of inconsistent subject or authenticator definition
     */
    @Override
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
     * @param args Argument passed to the to-be-called method
     * @return a {@link ReturnWrapper} wrapping true if the execution should continue after this method, false if not.
     * @throws InvocationTargetException if an error occurred when internally invoking a method
     * @throws IllegalAccessException if an error occurred when internally invoking a method
     * @throws NoSuchMethodException if an error occurred when internally invoking a method
     */
    @Override
    public ReturnWrapper processMethodBeforeInvoke(Object instance, Method method, Class klass, Object[] args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (context.notInConstructor()){
            this.checkBeforeInvoke(instance, method, klass, args);
        }
        ReturnWrapper returned = new ReturnWrapper(true,null);
        if (this.subjects.containsKey(klass)){
            returned = this.subjects.get(klass).processMethodBeforeInvoke(instance, method, klass, args);
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
     * @param args Argument passed to the just-invoked methods
     */
    @Override
    public void processMethodAfterInvoke(Object instance, Method method, Class klass, Object returnValue, Object[] args) {
        if (context.notInConstructor()) {
            this.checkAfterInvoke(instance, method, klass, returnValue, args);
        }
    }

    /**
     * @return the {@link HashMap} mapping {@link AuthenticatorSubject} annotated class with the corresponding {@link AuthenticatorSubjectEntity}
     */
    public HashMap<Class, AuthenticatorSubjectEntity> getSubjects() {
        return subjects;
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
                Authenticator annotation = (Authenticator) klass.getAnnotation(Authenticator.class);
                if (annotation != null && annotation.patternID().compareTo(this.id) == 0){
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
     * @param authenticatorSubjectEntity {@link AuthenticatorSubjectEntity} wrapping the instance
     * @throws InvocationTargetException if an error occurred when internally invoking a method
     * @throws IllegalAccessException if an error occurred when internally invoking a method
     */
    void performAuthentication(Object instance, Method idProofSetter, Method[] args, Method authenticatorGetter, AuthenticatorSubjectEntity authenticatorSubjectEntity) throws InvocationTargetException, IllegalAccessException {
        Object authenticatorInstance = authenticatorGetter.invoke(instance);
        Object[] instanceArgs = new Object[args.length];
        int current = 0;
        for (Method m : args){
            instanceArgs[current] = m.invoke(instance);
            current++;
        }
        Object proof = this.authenticator.getRequestMethod().invoke(authenticatorInstance, instanceArgs);
        authenticatorSubjectEntity.getInstances().get(instance).setIDProof(proof);
        idProofSetter.invoke(instance, proof);
    }

    /**
     * Instantiate a {@link AuthenticatorSubjectEntity} with the given class.
     * @param klass {@link AuthenticatorSubject} annotated class
     * @throws ModelDefinitionException In case of inconsistent subject definition.
     */
    private void attachSubject(Class klass) throws ModelDefinitionException {
        AuthenticatorSubjectEntity subject = new AuthenticatorSubjectEntity(this, klass);
        if (subject.isComplete()){
            this.subjects.put(klass, subject);
        }
        else {
            throw new InconsistentSubjectEntityException("Missing annotations in " + klass.getSimpleName() + "Authenticator subject definition with ID " + this.id);
        }
    }

    /**
     * Relay the check of invariant and precondition on method call to the relevant instance wrappers (i.e.
     * {@link AuthenticatorSubjectInstance} and\or {@link AuthenticatorInstance})
     * @param instance Object on which the method is called
     * @param method Called method
     * @param klass Pattern-related class of identified im the class tree of <code>instance</code>
     * @param args Argument passed to the to-be-called methods
     */
    private void checkBeforeInvoke(Object instance, Method method, Class klass, Object[] args) {
        if (this.subjects.containsKey(klass) && this.subjects.get(klass).getInstances().containsKey(instance)){
            this.subjects.get(klass).getInstances().get(instance).checkBeforeInvoke(method);
        }
        if (this.authenticator.getBaseClass().equals(klass) && this.authenticator.getInstances().containsKey(instance)){
            this.authenticator.getInstances().get(instance).checkBeforeInvoke(method);
        }
    }

    /**
     * Relay the check of invariant and postcondition after method call to the relevant instance wrappers (i.e.
     * {@link AuthenticatorSubjectInstance} and\or {@link AuthenticatorInstance})
     * @param instance Object on which the method is called
     * @param method Called method
     * @param klass Pattern-related class of identified im the class tree of <code>instance</code>
     * @param returnValue Return value of the <code>method</code> invoke
     * @param args Argument passed to the just-invoked methods
     */
    private void checkAfterInvoke(Object instance, Method method, Class klass, Object returnValue, Object[] args) {
        if (this.subjects.containsKey(klass) && this.subjects.get(klass).getInstances().containsKey(instance)){
            this.subjects.get(klass).getInstances().get(instance).checkAfterInvoke(method, returnValue);
        }
        if (this.authenticator.getBaseClass().equals(klass) && this.authenticator.getInstances().containsKey(instance)){
            this.authenticator.getInstances().get(instance).checkAfterInvoke(method, returnValue);
        }
    }
}
