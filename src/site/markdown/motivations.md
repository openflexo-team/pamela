---
sidebar_position: 3
---

# Motivations

Model-Driven Software Development focuses on managing domain-specific models, which
represent concerns at conceptual level. Such models are generally represented as various
artefacts, in many languages and representations. Model-Driven approach also generally
requires code generators to generate source code from models. The underlying semantics of
this generated code is generally encoded in the code generators and **can be inlined or
implicitly defined by code generation process, or may be sometimes explicit to the code
generation**. The following figure illustrates the classical vision for for Model-Driven
Engineering where some Java classes (on the right) are generated from a UML class diagram
(on the left).

![ClassicalVision](https://support.openflexo.org/images/components/pamela/ClassicalVision.png)

A first major drawback of this approach is the gap created between the conceptual level
(the model) and the source code, where semantics may be totally hidden or implicit.
Another classical issue resides in the development or maintenance process, where model and
source code must evolve independently (and sometimes by different actors, *e.g.* architect
and developer). Round-tripping mechanisms are commonly used to overcome those
difficulties. But round-tripping tools are difficult to use and maintain, and the process
can lead to inextricable issues in a context of concurrent modifications of model and
source code. This explains why lots of major software developments projects abandon the idea of
maintaining the link between models and source code during the development process. In that
context, the model is developed at the early stages of development process, and is used to
prototype software application. The model may be manually reverse engineered at the end of
development process, for documentation purposes.
