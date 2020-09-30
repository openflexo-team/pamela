# Multiple inheritance and traits programming

*Traits programming* is generally described as a way to organize the code around the notion of feature (while a more classical methodology in object-oriented programming present a class-oriented structure, where a class generally combines many features around the notion of responsability). With traits programming, classes are defined as the composition of traits using inheritance operation. Multiple inheritance is thus a requirement for such a programming methodology, at the condition that naming collisions (known as diamond problem) may be solved by explicit disambiguation. 

PAMELA framework offers a way to implement multiple inheritance in the context of traits programming. From a technical point of view, a trait will be reflected in PAMELA framework as a set of methods. Major conceptual issues are raised by Java language semantics which does not allow native multiple inheritance. The composition of multiple *ModelEntity* in a inheritance scheme is easy when no custom implementation was provided for parent entities (because they are reflected by basic Java interfaces). When some parent *ModelEntity* define an implementation, PAMELA offers `Implementation` annotation, allowing to supply a local implementation (this is the trait implementation). Those local implementations are composed in any *ModelEntity* inheriting from multiple traits.

Following example shows the classical diamond problem, with a `Calculator` concept combining two traits (`PlusProcessor` and `MinusProcessor`), both extending `IntegerStorage`, also defined as a trait.

```java
@ModelEntity
public interface IntegerStorage extends AccessibleProxyObject {

     public static final String STORED_VALUE = "storedValue";

     @Getter(value = STORED_VALUE, defaultValue = "-1")
     public int getStoredValue();

     @Setter(value = STORED_VALUE)
     public void setStoredValue(int aValue);

     public void reset();

     @Implementation
     public abstract class IntegerStorageImpl implements IntegerStorage {

        public void reset() {
            setStoredValue(0);
	    }

	    public void setStoredValue(int aValue) {
            performSuperSetter(STORED_VALUE, aValue);
            System.out.println("Sets stored value to be " + aValue);
	    }
    }
}
```

`PlusProcessor` extends `IntegerStorage` and provides `processPlus(int)` :

```java
@ModelEntity
public interface PlusProcessor extends IntegerStorage {

    public void processPlus(int value);

    @Implementation
    public abstract class PlusProcessorImpl implements PlusProcessor {

        public void processPlus (int value) {
            setStoredValue(getStoredValue()+value);
            return getStoredValue();
        }
    }
}
```

`MinusProcessor` extends `IntegerStorage` and provides `processMinus(int)` :

```java
@ModelEntity
public interface MinusProcessor extends IntegerStorage {

    public void processMinus(int value);

    @Implementation
    public abstract class MinusProcessorImpl implements MinusProcessor {

        public void processMinus (int value) {
            setStoredValue(getStoredValue()-value);
            return getStoredValue();
        }
    }
}
```

`Calculator` concept is combining two traits: (no required additional code)

```java
@ModelEntity
public interface Calculator extends PlusProcessor, MinusProcessor {

}
```

 

    
  
