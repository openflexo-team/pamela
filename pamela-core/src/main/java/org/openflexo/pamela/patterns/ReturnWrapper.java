package org.openflexo.pamela.patterns;

public class ReturnWrapper {
    private boolean keepGoing;
    private Object returnValue;

    public ReturnWrapper(boolean keepGoing, Object returnValue){
        this.keepGoing = keepGoing;
        this.returnValue = returnValue;
    }

    public boolean mustContinue(){
        return keepGoing;
    }

    public Object getReturnValue(){
        return returnValue;
    }
}
