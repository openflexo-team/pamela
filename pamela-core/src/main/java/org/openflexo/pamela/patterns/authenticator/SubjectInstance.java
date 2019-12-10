package org.openflexo.pamela.patterns.authenticator;

import org.openflexo.pamela.ModelEntity;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.exceptions.ModelExecutionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class SubjectInstance {
    private Object instance;
    private SubjectEntity entity;
    private Object authenticatorInstance;
    private ArrayList<Object> authInfos;
    private Object defaultIdProof;
    private Object idProof;
    private boolean initializing;
    private boolean checking;

    public SubjectInstance(Object instance, SubjectEntity entity){
        this.instance = instance;
        this.entity = entity;
        this.authInfos = new ArrayList<>();
        this.initializing = false;
        this.checking = false;
    }

    public void init(){
        try {
            this.initializing = true;
            for (Method m : this.entity.getAuthInfoGetters().values()){
                Object info = m.invoke(instance , new Object[] {});
                this.authInfos.add(info);
            }
            this.authenticatorInstance = entity.getAuthenticatorGetter().invoke(instance, new Object[] {});
            this.defaultIdProof = this.entity.getIdProofGetter().invoke(this.instance);
            this.idProof = null;
            this.initializing = false;
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void checkBeforeInvoke(Method method) {
        if (!this.initializing && !this.checking){
            this.checking = true;
            this.checkInvariant();
            this.checkPreconditions(method);
            this.checking = false;
        }
    }

    private void checkInvariant(){
        this.checkAuthInfoUniqueness();
        this.checkAuthenticatorIsFinal();
        this.checkAuthInfoIsFinal();
        this.checkIdProofIsValid();
    }

    private void checkIdProofIsValid() {
        try {
            Object currentProof = this.entity.getIdProofGetter().invoke(this.instance);
            if ((this.idProof == null && !currentProof.equals(this.defaultIdProof)) || (this.idProof != null && !currentProof.equals(this.idProof) && !currentProof.equals(this.defaultIdProof))) {
                throw new ModelExecutionException("Subject Invariant Violation: Proof of identity has been forged");
            }
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void checkAuthInfoIsFinal() {
        int i = 0;
        try {
            for (Method getter : this.entity.getAuthInfoGetters().values()) {
                Object currentauthInfo = null;
                currentauthInfo = getter.invoke(this.instance);
                if (!currentauthInfo.equals(this.authInfos.get(i))) {
                    break;
                }
                i++;
            }
            if (i != this.authInfos.size()) {
                throw new ModelExecutionException("Subject Invariant Violation: Authentication Information has changed since initialization");
            }
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void checkAuthenticatorIsFinal() {
        try {
            Object currentAuthenticator = this.entity.getAuthenticatorGetter().invoke(this.instance, new Object[] {});
            if (!currentAuthenticator.equals(this.authenticatorInstance)){
                throw new ModelExecutionException("Subject Invariant Violation: Authenticator has changed since initialization");
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void checkAuthInfoUniqueness(){
        for (SubjectInstance otherInstance : this.entity.getInstances().values()) {
            int i;
            for (i = 0; i < authInfos.size(); i++) {
                if (authInfos.get(i) != otherInstance.getAuthInfos().get(i)) {
                    break;
                }
            }
            if (i == authInfos.size() && !otherInstance.getInstance().equals(this.instance)) {
                throw new ModelExecutionException("Subject Invariant Violation: Authentication information are not unique");
            }
        }
    }

    public Object getInstance(){
        return this.instance;
    }

    private ArrayList<Object> getAuthInfos() {
        return this.authInfos;
    }

    public void checkAfterInvoke(Method method, Object returnValue) {
        if (!this.initializing && !this.checking){
            this.checking = true;
            this.checkInvariant();
            this.checkPostcondition(method, returnValue);
            this.checking = false;
        }

    }

    private void checkPostcondition(Method method, Object returnValue) {
        if (this.entity.getAuthenticateMethods().contains(method)){
            this.checkPostConditionAuthenticate();
        }
    }

    private void checkPostConditionAuthenticate() {
        if (this.idProof == null){
            throw new ModelExecutionException(String.format("Subject authenticate method postcondition violation (Pattern %s, Class %s)", this.entity.getPattern().getID(), this.entity.getBaseClass().getSimpleName()));
        }
    }

    private void checkPreconditions(Method method) {

    }

    public void setIDProof(Object proof) {
        if (this.idProof == null){
            this.idProof = proof;
        }
    }
}
