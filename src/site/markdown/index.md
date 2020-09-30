# Pamela : an annotation-based Java modelling framework

PAMELA is an annotation-based Java Modelling framework. PAMELA provides a smooth integration between model and code, and enable Java developers to handle software development both at conceptual level and at source-code level, without code transformation and/or generation, and avoiding round-tripping issues. 

Exposed metamodel provides meta-programming support, multiple inheritance and traits programming, contract programming, aspect programming and run-time weaving. This framework also offers operational features derived from  model-level edition such as notification management, validation, persistence, comparison and object graph computation. 

PAMELA approach has been tested and validated on some java-based industrial projects, making it a credible and reliable alternative in the context of Model-Driven Engineering.

# Highlights

- Continuous modeling process 
- Strong coupling between model and code with smooth integration  
- Modeling without code generation: no need to generate POJO (plain old java objects), as their execution follow the standard semantics (less code, less bugs)
- Interpretation of models at runtime
- Code instrumentation
- Meta-programming
- Contract programming with assertions checking at runtime
- Dynamic code weaving at runtime (aspect programming without compilation)
  
# Overview

- [Motivations](./motivations.html)
- [Approach overview](./overview.html)
- [A basic example](./example.html)
- [Common annotations](./annotations.html)
- [Behind the scene](./behind_the_scene.html)
  
# Features
 
- [Model at runtime computation, model fragmentation management](./pamela-core/1-model_at_runtime.html)
- Life-cycle management
- Meta-programming support
- Multiple inheritance and traits programming
- Containment management
- Cloning support
- Clipboard operations
- Notification support
- Persistance support, XML serialization/deserialization
- Equality computing support
- Visiting features
- Differential updating
- Validation API
- Contract programming, JML
- Design patterns, aspect programming
  
# Getting started

Want to start with Pamela ? Read our ["Getting started" guide](./getting_started.html)

# Contents of this package

PAMELA project contains two separate components (defined here as modules):
  
- [Pamela-core component](./pamela-core/index.html), which contains the Pamela core library
- [Pamela-SecurityPatterns component](./pamela-security-patterns/index.html), which is a library of security-oriented patterns    
  
