# Pamela Security-Patterns
 
## Introduction to SecurityPatterns library
 
A significant experiment in PAMELA is the implementation of security patterns weaved on domain code.

Security patterns library was developped in the context of [Design patterns and Aspect programming](../pamela-core/15-design_patterns.md).

In this context, the PAMELA framework is extended to include the notion of Pattern, i.e. a composition of multiple classes. Included to this experiment, the security pattern is specified by expected behavior defined and formalized by a pattern contract. This contract is defined by formal properties and the PAMELA framework ensures the property verification at run-time.

Related to the security pattern implementation, PAMELA enables the definition of additional security behavior to existing Java code.
Patterns are defined in PAMELA using three classes, each one representing a different conceptual level `PatternFactory`, `PatternDefinition`, `PatternInstance`s.

To declare a Pattern on existing code, pattern elements such as Pattern Stakeholders and methods need to be annotated with provided security pattern-specific annotations. These annotations will be discovered at run-time by the PatternFactory and stored in PatternDefinition attributes.

Summarizing, implementing Patterns with PAMELA provides the ability to monitor the execution of the application code; the ability to offer extra structural and behavioral features, executed by the PAMELA interpreter; a representation of Patterns as stateful objects. Such objects can then evolve throughout run-time and compute assertions.
  
## Implemented patterns

Security patterns library todays contains following patterns:

 - [Authenticator](Authenticator.md)
 - [Authorization](Authorization.md)
 - [SingleAccessPoint](SingleAccessPoint.md)
 - [Owner](Owner.md)
 
 


