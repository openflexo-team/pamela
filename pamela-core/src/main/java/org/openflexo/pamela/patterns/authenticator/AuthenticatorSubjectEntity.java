package org.openflexo.pamela.patterns.authenticator;

import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.patterns.authenticator.annotations.*;
import org.openflexo.pamela.patterns.authenticator.exceptions.InconsistentAuthenticatorEntityException;
import org.openflexo.pamela.patterns.authenticator.exceptions.InconsistentSubjectEntityException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class wraps all the static authenticator pattern related information extracted from parsing a
 * {@link AuthenticatorSubject} annotated class.<br>
 * It has the responsibility of:
 * <ul><li>Parsing the class.</li>
 * <li>Saving the relevant pattern related information.</li>
 * <li>Discovering and saving at runtime the known instance of the associated class.</li></ul>
 *
 *  @author C. SILVA
 */
public class AuthenticatorSubjectEntity {
    private final AuthenticatorPattern pattern;
    private final Class baseClass;
    private Method idProofSetter;
    private final ArrayList<Method> authenticateMethods;
    private final HashMap<String, Method> authInfoGetters;
    private final HashMap<Object, SubjectInstance> instances;
    private Method[] args;
    private boolean successLinking;
    private Method authenticatorGetter;
    private Method idProofGetter;

    /**
     * Constructor of the class.<br>
     * The constructor performs the class analysis.
     * @param pattern Reference of the associated {@link AuthenticatorPattern}
     * @param klass {@link AuthenticatorSubject} annotated class to analyze
     * @throws ModelDefinitionException When malformed or missing annotation in the subject class or associated authenticator class
     */
    AuthenticatorSubjectEntity(AuthenticatorPattern pattern, Class klass) throws ModelDefinitionException {
        this.pattern = pattern;
        this.baseClass = klass;
        this.authInfoGetters = new HashMap<>();
        this.authenticateMethods = new ArrayList<>();
        this.instances = new HashMap<>();
        this.analyzeClass();
        this.successLinking = false;
        this.link();
    }

    /**
     * @return the {@link AuthenticatorPattern} instance wrapping this object
     */
    public AuthenticatorPattern getPattern() {
        return this.pattern;
    }

    /**
     * @return the {@link AuthenticatorSubject} annotated class used to instantiate this object
     */
    public Class getBaseClass() {
        return baseClass;
    }

    /**
     * @return the setter method of the <code>Proof of Identity</code> field
     */
    public Method getIdProofSetter() {
        return idProofSetter;
    }

    /**
     * @return the list of {@link AuthenticateMethod} annotated methods in <code>baseClass</code>
     */
    public ArrayList<Method> getAuthenticateMethods() {
        return authenticateMethods;
    }

    /**
     * @return the ordered list of getters of the <code>request</code> method parameters
     */
    public Method[] getArgs() {
        return args;
    }

    /**
     * @return the {@link HashMap} mapping known instance references with the associated {@link SubjectInstance}
     */
    public HashMap<Object, SubjectInstance> getInstances() {
        return instances;
    }

    /**
     * @return the {@link HashMap} mapping parameters identifier of the {@link AuthenticationInformation} annotated getters with the associated getters
     */
    HashMap<String, Method> getAuthInfoGetters() {
        return authInfoGetters;
    }

    /**
     * Method called to instantiate a new {@link SubjectInstance} if the given object is not already known
     * @param instance Instance to discover
     */
    void discoverInstance(Object instance) {
        if (!this.instances.containsKey(instance)){
            this.instances.put(instance, new SubjectInstance(instance, this));
            this.instances.get(instance).init();
        }
    }

    /**
     * @return the getter of the associate <code>Authenticator</code>
     */
    Method getAuthenticatorGetter() {
        return this.authenticatorGetter;
    }

    /**
     * @return the getter of the <code>Proof of Identity</code>
     */
    Method getIdProofGetter() {
        return this.idProofGetter;
    }

    /**
     * @return true if all required annotation has been found in the class and field <code>successLinking</code> is true.
     */
    boolean isComplete(){
        return (pattern != null) && (this.baseClass != null) && (idProofSetter != null) && this.successLinking;
    }

    /**
     * Method called before every <code>baseClass</code> method invoke. Performs the execution, if relevant.
     * @param instance Object on which the method is called
     * @param method Called method
     * @param klass Pattern-related class of identified im the class tree of <code>instance</code>
     * @return true if the execution of the invoke should go one after the call, false if not.
     * @throws InvocationTargetException if an error occurred when internally invoking a method
     * @throws IllegalAccessException if an error occurred when internally invoking a method
     * @throws NoSuchMethodException if an error occurred when internally invoking a method
     */
    boolean processMethodBeforeInvoke(Object instance, Method method, Class klass) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method superMethod = klass.getMethod(method.getName(), method.getParameterTypes());
        if (this.authenticateMethods.contains(method)){
            pattern.performAuthentication(instance,this.idProofSetter, this.args, this.authenticatorGetter, this);
            return !Modifier.isAbstract(method.getModifiers());
        }
        else if (this.authenticateMethods.contains(superMethod)){
            pattern.performAuthentication(instance,this.idProofSetter, this.args, this.authenticatorGetter, this);
            return !Modifier.isAbstract(method.getModifiers());
        }
        return true;
    }

    /**
     * Perform the class analysis
     * @throws ModelDefinitionException In case of inconsistent authenticator or subject definition
     */
    private void analyzeClass() throws ModelDefinitionException{
        for (Method m : this.baseClass.getMethods()){
            AuthenticationInformation authInfoAnnotation = m.getAnnotation(AuthenticationInformation.class);
            if (authInfoAnnotation != null && authInfoAnnotation.patternID().compareTo(pattern.getID()) == 0){
                this.processAuthenticationInformation(m, authInfoAnnotation);
            }
            ProofOfIdentitySetter idProofAnnotation = m.getAnnotation(ProofOfIdentitySetter.class);
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

    /**
     * Saves the <code>Authenticator</code> getter and relays the instantiation of the associated {@link AuthenticatorEntity} to the pattern.
     * @param method Getter of the <code>Authenticator</code>
     * @param annotation {@link AuthenticatorGetter} annotation found on the method
     * @throws InconsistentAuthenticatorEntityException In case of inconsistent authenticator definition (i.e. malformed annotations)
     */
    private void processAuthenticatorGetter(Method method, AuthenticatorGetter annotation) throws InconsistentAuthenticatorEntityException {
        if (this.authenticatorGetter == null) {
            this.authenticatorGetter = method;
        }
        this.pattern.attachAuthenticatorFromAuthenticatorGetter(method.getReturnType());
    }

    /**
     * Saves the <code>Authentication Information</code> getter
     * @param method Getter of an <code>Authentication Information</code>
     * @param annotation {@link AuthenticationInformation} annotation found on the method
     * @throws InconsistentSubjectEntityException In case of malformed {@link AuthenticationInformation} annotation
     */
    private void processAuthenticationInformation(Method method, AuthenticationInformation annotation) throws InconsistentSubjectEntityException {
        if (!this.authInfoGetters.containsKey(annotation.paramID())){
            this.authInfoGetters.put(annotation.paramID(), method);
        }
        else {
            throw new InconsistentSubjectEntityException("Duplicate @AuthenticationInformation annotation with same pattern ID (" + this.pattern.getID() + ") and paramID (" + annotation.paramID() + ") in " + this.baseClass.getSimpleName());
        }
    }

    /**
     * Saves the getter and setter of the <code>Proof of Identity</code>
     * @param method Setter of the <code>Proof of Identity</code>
     * @param annotation {@link ProofOfIdentitySetter} annotation found on the method
     * @throws ModelDefinitionException In case of malformed annotation or missing getter for <code>Proof of Identity</code>
     */
    private void processIdProof(Method method, ProofOfIdentitySetter annotation) throws ModelDefinitionException {
        if (this.idProofSetter == null){
            this.idProofSetter = method;
        }
        else {
            throw new InconsistentSubjectEntityException("Duplicate @ProofOfIdentity annotation with same pattern ID (" + this.pattern.getID() + ") in " + this.baseClass.getSimpleName());
        }
        Setter setter = method.getAnnotation(Setter.class);
        if (setter == null){
            throw new InconsistentSubjectEntityException("Proof of identity annotation not on a setter in " + this.baseClass.getSimpleName());
        }
        this.idProofGetter = this.pattern.getContext().getContext().getModelEntity(this.baseClass).getModelProperty(setter.value()).getGetterMethod();
        if (this.idProofGetter == null){
            throw new InconsistentSubjectEntityException("No getter for Proof of identity in " + this.baseClass.getSimpleName());
        }
    }

    /**
     * Saves the <code>Authenticate Method</code>
     * @param method <code>Authenticate</code> method
     * @param annotation {@link AuthenticateMethod} annotation found on the method
     */
    private void processAuthenticateMethod(Method method, AuthenticateMethod annotation){
        this.authenticateMethods.add(method);
    }

    /**
     * Attempt to create the ordered list of getter for the <code>Request</code> method parameters of the associated <code>Authenticator</code>.<br>
     * If all parameters are found, the field <code>successLinking</code> is set to true.
     */
    private void link(){
        Method request = this.pattern.getAuthenticator().getRequestMethod();
        this.args = new Method[request.getParameterCount()];
        int current  = 0;
        boolean success = true;
        for (Parameter p : request.getParameters()){
            if (this.authInfoGetters.containsKey(p.getAnnotation(AuthenticationInformation.class).paramID())){
                this.args[current] = this.authInfoGetters.get(p.getAnnotation(AuthenticationInformation.class).paramID());
            }
            else {
                success = false;
            }
            current++;
        }
        this.successLinking = success;
    }
}
