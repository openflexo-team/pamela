# Frequently Asked Questions

### 1. Class cannot access its superinterface ?

If you encounter an Exception like the following:
```
Exception in thread "main" java.lang.RuntimeException: by java.lang.IllegalAccessError: class org.javassist.tmp.java.lang.Object_$$_jvst41b_0 cannot access its superinterface MyClass
```

This means that the interface `MyClass` isn't accessible, it must be `public`.

### 2. How to check if my model is sound ?

Pamela offer high level model definitions but it does so by tricking the Java compiler using `abstract` constructions. The compiler cannot detect which methods are unimplemented and are subject to raise an unbound exception `NotImplementedError`.

You can easily check if your model is sound with a simple test:

```java
ModelFactory factory = new ModelFactory(MyRootModelClass.class);
factory.checkMethodImplementations();
```


