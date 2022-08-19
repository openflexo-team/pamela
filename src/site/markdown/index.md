---
sidebar_position: 1
---

# Introduction to PAMELA

PAMELA is an annotation-based Java modelling framework.

The strong idea behind this technology is a smooth integration between model and code, without code generation nor externalized model serialization.

We want here to avoid separation between modelling and code to facilitate consistency management and avoid round-tripping issues. 
  
Key features of PAMELA

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
- Multi-delevel undo/redo support
- A graphical editor is also provided in this project, allowing to graphically view and edit PAMELA models
  
Contents of this package

PAMELA project contains two separate components (defined here as modules):
  
- [Pamela-core component](./pamela-core/index.md), which contains the Pamela core library itself
- [Pamela-SecurityPatterns component](./pamela-security-patterns/index.md), which is a library of security-oriented patterns