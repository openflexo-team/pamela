package org.openflexo.pamela.securitypatterns.authorization;

import org.openflexo.pamela.patterns.PatternInstance;
import org.openflexo.pamela.patterns.ReturnWrapper;
import org.openflexo.toolbox.HasPropertyChangeSupport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

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
        T checker;
        boolean valid;

        private ResourceWrapper(HashMap<String, Object> identifiers){
            this.identifiers = identifiers;
            this.valid = false;
        }

        public HashMap<String, Object> getIdentifiers(){
            return this.identifiers;
        }

        public T getChecker(){
            return this.checker;
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

    HashMap<S, SubjectWrapper> subjects;
    HashMap<R, ResourceWrapper<C>> resources;

    protected <I> AuthorizationPatternInstance(AuthorizationPatternDefinition patternDefinition) {
        super(patternDefinition);
        this.subjects = new HashMap<>();
        this.resources = new HashMap<>();
    }

    @Override
    public ReturnWrapper processMethodBeforeInvoke(Object instance, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        return null;
    }

    @Override
    public void processMethodAfterInvoke(Object instance, Method method, Object returnValue, Object[] args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {

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
