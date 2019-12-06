package org.openflexo.pamela.patterns;

public class PatternClassWrapper {
    private AbstractPattern pattern;
    private Class klass;

    public PatternClassWrapper(AbstractPattern pattern, Class klass){
        this.pattern = pattern;
        this.klass = klass;
    }

    public AbstractPattern getPattern(){
        return this.pattern;
    }

    public Class getKlass(){
        return this.klass;
    }
}
