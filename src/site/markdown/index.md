---
sidebar_position: 1
---

# Pamela : an annotation-based Java modelling framework

PAMELA is an annotation-based Java Modelling framework. PAMELA provides a smooth integration between model and code, and enable Java developers to handle software development both at conceptual level and at source-code level, without code transformation and/or generation, thus avoiding round-tripping issues.

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

- [Motivations](./motivations.md)
- [Approach overview](./overview.md)
- [A basic example](./example.md)
- [Common annotations](./annotations.md)
- [Behind the scene](./behind_the_scene.md)

# Features

- [Model at runtime computation, model fragmentation management](./pamela-core/1-model_at_runtime.md)
- [Life-cycle management](./pamela-core/2-life_cycle_management.md)
- [Meta-programming support](./pamela-core/3-metaprogramming_support.md)
- [Multiple inheritance and traits programming](./pamela-core/4-multiple_inheritance.md)
- [Containment management](./pamela-core/5-containment_management.md)
- [Cloning support](./pamela-core/6-cloning_support.md)
- [Clipboard operations](./pamela-core/7-clipboard_operations.md)
- [Notification support](./pamela-core/8-notification_support.md)
- [Persistance support, XML serialization/deserialization](./pamela-core/9-persistence_support.md)
- [Equality computing support](./pamela-core/10-equality_computing.md)
- [Visiting features](./pamela-core/11-visiting_features.md)
- [Differential updating](./pamela-core/12-differential_updating.md)
- [Validation API](./pamela-core/13-validation_api.md)
- [Contract programming, JML](./pamela-core/14-jml.md)
- [Design patterns, aspect programming](./pamela-core/15-design_patterns.md)

# Getting started

Want to start with Pamela ? Read our ["Getting started" guide](./getting_started.md)

# Contents of this package

PAMELA project contains two separate components (defined here as modules):

- [Pamela-core component](./pamela-core/index.md), which contains the Pamela core library
- [Pamela-SecurityPatterns component](./pamela-security-patterns/index.md), which is a library of security-oriented patterns
