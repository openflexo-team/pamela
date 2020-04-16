package org.openflexo.pamela.securitypatterns.authorization;

import org.openflexo.pamela.PamelaUtils;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.patterns.PatternInstance;
import org.openflexo.pamela.patterns.ReturnWrapper;
import org.openflexo.toolbox.HasPropertyChangeSupport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

public class AuthorizationPatternInstance<S, R, C>  extends PatternInstance<AuthorizationPatternDefinition> implements PropertyChangeListener {

    public static class SubjectWrapper{
        HashMap<String, Object> identifiers;

        private SubjectWrapper(HashMap<String, Object> identifiers){
            this.identifiers = identifiers;
        }

        public HashMap<String, Object> getIdentifiers(){
            return this.identifiers;
        }
    }

    public static class ResourceWrapper<T>{
        HashMap<String, Object> identifiers;
        HashSet<Method> allowedMethods;
        T checker;
        boolean valid;

        private ResourceWrapper(HashMap<String, Object> identifiers){
            this.identifiers = identifiers;
            this.allowedMethods = new HashSet<>();
            this.valid = false;
        }

        public HashMap<String, Object> getIdentifiers(){
            return this.identifiers;
        }

        public T getChecker(){
            return this.checker;
        }

        private void allowMethod(Method m){
            this.allowedMethods.add(m);
        }

        private void removeMethod(Method m){
            this.allowedMethods.remove(m);
        }

        private void attachChecker(T checker){
            this.checker = checker;
            if (checker != null){
                this.valid = true;
            }
        }

        public boolean isValid(){
            return this.valid;
        }
    }

    private HashMap<S, SubjectWrapper> subjects;
    private HashMap<R, ResourceWrapper<C>> resources;
    private boolean checking;

    protected <I> AuthorizationPatternInstance(AuthorizationPatternDefinition patternDefinition) {
        super(patternDefinition);
        this.subjects = new HashMap<>();
        this.resources = new HashMap<>();
        this.checking = false;
    }

    @Override
    public ReturnWrapper processMethodBeforeInvoke(Object instance, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (this.subjects.containsKey(instance)){
            return this.processSubjectMethodBeforeInvoke(instance, method, args);
        }
        else if (this.resources.containsKey(instance)){
            return this.processResourceMethodBeforeInvoke(instance, method, args);
        }
        return new ReturnWrapper(true, null);
    }

    private ReturnWrapper processResourceMethodBeforeInvoke(Object instance, Method method, Object[] args) {
        if (!this.checking){
            //this.checkResourceInvariant(instance);
        }
        for (Method m : this.getPatternDefinition().getResourceAccessMethods().values()){
            if (PamelaUtils.methodIsEquivalentTo(m,method)){
                if (!this.resources.get(instance).isValid()){
                    throw new ModelExecutionException("Attempt to access a resource without permission checker.");
                }
                if (!this.resources.get(instance).allowedMethods.contains(m)){
                    throw new ModelExecutionException("Attempt to override access checking on protected resource");
                }

            }
        }
        return new ReturnWrapper(true, null);
    }

    private ReturnWrapper processSubjectMethodBeforeInvoke(Object instance, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        if (this.checking){
            //this.checkSubjectInvariant(instance);
        }
        for (Method m : this.getPatternDefinition().getSubjectAccessMethods().keySet()){
            if (PamelaUtils.methodIsEquivalentTo(m, method)){
                return this.processSubjectAccess(instance, m, args);
            }
        }
        return new ReturnWrapper(true, null);
    }

    private ReturnWrapper processSubjectAccess(Object instance, Method m, Object[] args) throws InvocationTargetException, IllegalAccessException {
        AuthorizationPatternDefinition.SubjectAccessMethodWrapper wrapper = this.getPatternDefinition().getSubjectAccessMethods().get(m);
        //get the requested resource ids in a map
        HashMap<String, Object> resIDs = new HashMap<>();
        for (String id : wrapper.getParamMapping().keySet()){
            resIDs.put(id, args[wrapper.getParamMapping().get(id)]);
        }
        //get the requested resource object
        R resourceInstance = null;
        for (R resource : this.resources.keySet()){
            boolean matched = true;
            for (String id : resIDs.keySet()){
                matched = matched && (this.resources.get(resource).identifiers.get(id) == resIDs.get(id));
            }
            if (matched) {
                resourceInstance = resource;
                break;
            }
        }
        if (resourceInstance == null){
            throw new ModelExecutionException("Attempt to access unknwon resource");
        }
        boolean allowed = this.performAccessCheck(m, instance, resourceInstance);
        if (allowed){
            this.resources.get(resourceInstance).allowMethod(getPatternDefinition().getResourceAccessMethods().get(getPatternDefinition().getSubjectAccessMethods().get(m).getMethodID()));
            return this.processAllowedAccess(m, instance, resourceInstance, args);
        }
        else {
            throw new ModelExecutionException("Attempt to access resource without permission.");
        }
    }

    private boolean performAccessCheck(Method m, Object instance, R resourceInstance) throws InvocationTargetException, IllegalAccessException {
        C checker = this.resources.get(resourceInstance).getChecker();
        int nbParam = this.getPatternDefinition().getResourceIdParameters().size() + getPatternDefinition().getSubjectIdParameters().size() + 1;
        Object[] args = new Object[nbParam];
        for (String subId : getPatternDefinition().getSubjectIdParameters().keySet()){
            args[getPatternDefinition().getSubjectIdParameters().get(subId)] = getPatternDefinition().getSubjectIdGetters().get(subId).invoke(instance);
        }
        for (String resId : getPatternDefinition().getResourceIdParameters().keySet()){
            args[getPatternDefinition().getResourceIdParameters().get(resId)] = getPatternDefinition().getResourceIdGetters().get(resId).invoke(resourceInstance);
        }
        args[getPatternDefinition().getMethodIdIndex()] = getPatternDefinition().getSubjectAccessMethods().get(m).getMethodID();
        return (boolean) this.getPatternDefinition().getCheckMethod().invoke(checker,args);
    }

    private ReturnWrapper processAllowedAccess(Method m, Object instance, R resourceInstance, Object[] args) throws InvocationTargetException, IllegalAccessException {
        Method resourceMethod = getPatternDefinition().getResourceAccessMethods().get(getPatternDefinition().getSubjectAccessMethods().get(m).getMethodID());
        int nbParam = this.getPatternDefinition().getSubjectAccessMethods().get(m).getRealIndexes().size();
        Object[] params = new Object[nbParam];
        int j = 0;
        for (Integer i : this.getPatternDefinition().getSubjectAccessMethods().get(m).getRealIndexes()){
            params[j] = args[i];
            j++;
        }
        Object returnValue = resourceMethod.invoke(resourceInstance,params);
        this.resources.get(resourceInstance).removeMethod(resourceMethod);
        return new ReturnWrapper(false, returnValue);
    }

    @Override
    public void processMethodAfterInvoke(Object instance, Method method, Object returnValue, Object[] args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (this.subjects.containsKey(instance)){
            this.processSubjectMethodAfterInvoke(instance, method, returnValue, args);
        }
        else if (this.resources.containsKey(instance)){
            this.processResourceMethodAfterInvoke(instance, method, returnValue, args);
        }
    }

    private void processResourceMethodAfterInvoke(Object instance, Method method, Object returnValue, Object[] args) {
        for (Method m : this.getPatternDefinition().getResourceAccessMethods().values()){
            if (PamelaUtils.methodIsEquivalentTo(m,method)){
                if (!this.resources.get(instance).isValid()){
                    throw new ModelExecutionException("Attempt to access a resource without permission checker.");
                }
            }
        }
    }

    private void processSubjectMethodAfterInvoke(Object instance, Method method, Object returnValue, Object[] args) {
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (this.resources.containsKey(evt.getSource()) && !this.resources.get((R)evt.getSource()).isValid()) {
            if (this.updateChecker((R) evt.getSource())){
                ((HasPropertyChangeSupport)evt.getSource()).getPropertyChangeSupport().removePropertyChangeListener(this);
            }
        }
    }

    protected void attachSubject(S newInstance) {
        this.registerStakeHolder(newInstance, AuthorizationPatternDefinition.SUBJECT_ROLE);
        try {
            this.subjects.put(newInstance, new SubjectWrapper(this.retrieveSubjectIdentifiers(newInstance)));
        }
        catch (InvocationTargetException | IllegalAccessException e){
            e.printStackTrace();
        }
    }

    protected void attachResource(R newInstance) {
        this.registerStakeHolder(newInstance, AuthorizationPatternDefinition.RESOURCE_ROLE);
        try {
            ResourceWrapper<C> wrapper = new ResourceWrapper<C>(this.retrieveResourceIdentifiers(newInstance));
            this.resources.put(newInstance, wrapper);
            wrapper.attachChecker(this.retrieveChecker(newInstance));
            if (!wrapper.isValid() && newInstance instanceof HasPropertyChangeSupport){
                ((HasPropertyChangeSupport) newInstance).getPropertyChangeSupport().addPropertyChangeListener(this);
            }
        }
        catch (InvocationTargetException | IllegalAccessException e){
            e.printStackTrace();
        }
    }

    private C retrieveChecker(R resource) throws InvocationTargetException, IllegalAccessException{
        return (C) this.getPatternDefinition().getCheckerGetter().invoke(resource);
    }

    private HashMap<String, Object> retrieveSubjectIdentifiers(S subject) throws InvocationTargetException, IllegalAccessException {
        HashMap<String, Object> map = new HashMap<>();
        for (String key : getPatternDefinition().getSubjectIdGetters().keySet()) {
            map.put(key, getPatternDefinition().getSubjectIdGetters().get(key).invoke(subject));
        }
        return map;
    }

    private HashMap<String, Object> retrieveResourceIdentifiers(R resource) throws InvocationTargetException, IllegalAccessException {
        HashMap<String, Object> map = new HashMap<>();
        for (String key : getPatternDefinition().getResourceIdGetters().keySet()) {
            map.put(key, getPatternDefinition().getResourceIdGetters().get(key).invoke(resource));
        }
        return map;
    }

    private boolean updateChecker(R resource){
        boolean valid = false;
        try{
            this.resources.get(resource).attachChecker(this.retrieveChecker(resource));
            valid =  this.resources.get(resource).isValid();
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return valid;
    }

    public HashMap<S, SubjectWrapper> getSubjects(){
        return this.subjects;
    }

    public HashMap<R, ResourceWrapper<C>> getResources(){
        return this.resources;
    }
}
