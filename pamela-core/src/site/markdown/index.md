# Openflexo PAMELA
 
## Introduction
 
### Introduction to PAMELA

PAMELA is an annotation-based Java modelling framework. 
The strong idea behind this technology is a smooth integration between model and code, without code generation nor externalized model serialization.
We aim to avoid separation between model and code to simplify consistency management and avoid round-tripping issues. 
  
#### Key features of PAMELA

 - Model/code strong coupling
 - No code generation: the 'model' is serialized in the Java code with annotations
 - Custom implementations might be redefined by the developper
 - All is executed at run-time: Java dynamic binding is overriden
 - Support for multiple inheritance in Java
 - XML serialization/deserialization
 - On the fly metamodel closure computation (powerfull management of metamodel fragmentation)
 - Integrated notification management
 - Embedding management
 - Object graph closure computation
 - Deletion management
 - Clipboard operations (copy, cut, paste) management
 - Multi-level undo/redo support
 - A graphical editor is also provided in this project, allowing to graphically view and edit PAMELA models (see [Pamela-editor component](./pamela-editor/index.html)).
  
### Getting PAMELA

There are several way to get PAMELA.

#### OpenFlexo

[OpenFlexo](http://www.openflexo.org) make instensive use of PAMELA. If you're using OpenFlexo you have access to PAMELA.

#### Maven 

PAMELA is built using maven (soon it will use Gradle) and publish it's artifacts to `TODO`

```xml
<dependency>
  <groupId>org.openflexo</groupId>
  <artifactId>pamela-core</artifactId>
  <version>RELEASE</version>
</dependency>
```
#### Gradle

TODO
  
#### Direct download

TODO

#### Build it your self

In order to get the most up to date code you can build PAMELA yourself.

**Prerequisite**

- git
- Java JDK 7
- Maven

Building PAMELA is easy, follow these steps:

TODO

## Reference documentation

The javadoc for Pamela can be found [here](./apidocs/index.html).
  
  

## Frequently Asked Questions

### Class `myClass` cannot access its superinterface

If you encounter an Exception like the following:
```
Exception in thread "main" java.lang.RuntimeException: by java.lang.IllegalAccessError: class org.javassist.tmp.java.lang.Object_$$_jvst41b_0 cannot access its superinterface MyClass
```

This means that the interface `MyClass` isn't accessible, it must be `public`.

