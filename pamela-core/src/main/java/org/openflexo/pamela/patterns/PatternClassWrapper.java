package org.openflexo.pamela.patterns;

/**
 * Wrapper of an {@link AbstractPattern} and a {@link Class} of interest for this pattern.
 * This wrapper is used as a return type when searching an object or class for the patterns it is involved in.
 */
public class PatternClassWrapper {
    private final AbstractPattern pattern;
    private final Class klass;

    /**
     * Constructor of the class
     * @param pattern {@link AbstractPattern} to wrap
     * @param klass {@link Class} of interest to wrap
     */
    PatternClassWrapper(AbstractPattern pattern, Class klass){
        this.pattern = pattern;
        this.klass = klass;
    }

    /**
     * @return the wrapped {@link AbstractPattern}
     */
    public AbstractPattern getPattern(){
        return this.pattern;
    }

    /**
     * @return the wrapped {@link Class}
     */
    public Class getKlass(){
        return this.klass;
    }
}
