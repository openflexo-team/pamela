---
sidebar_position: 16
---

# Design patterns / Aspect programming / Code weaving

PAMELA framework offers some Aspect-Oriented Programming (AOP) features enabling the definition of additional behaviour to existing Java code. Code weaving is performed at run-time.

We define a *Pattern* as a composite of multiple stakeholders, whose roles are played by various Java classes declared as a PAMELA entity. A *Pattern* regroups and implements all concerns related to concept association, which are orthogonal to each functional class concern. Each method of involved classes can be considered as a *pointcut* as of AOP terminology. Such *Pattern* is instantiated, and provides a statefull environment. Code instrumentation and code weaving are operated at run-time by PAMELA interpreter. A *Pattern* comes with its business logic (acting on control flow of all pattern-related methods), and a set of assertions (invariants, pre-conditions and post-conditions) which are evaluated at run-time, while functional code is executed.

The *PAMELA Pattern* approach:

- provides a way to get an execution while dynamically weaving existing functional code as encoded in plain Java with orthogonal concerns (such as for example authorization/permissions, logging, visualization, etc...);
- provides a way to secure existing code, with contract-based security assertions (PAMELA provides handlers both at entry and exit of methods execution, allowing execution of pre and post conditions);
- *Patterns* are reified (instantiated) and provides statefull environment, allowing the computation of assertions using many paradigms (e.g., Linear Temporal Logic),
- because patterns are reified, they may be composed;
- *Patterns* may offer some behavioural features, exposing a specific control flow process, executed by PAMELA interpreter,
- *Patterns* may offer and expose some structural features, which can be used while composing other *Patterns*,
- PAMELA framework offers multiple extension points: ability to redefine or specialize an existing pattern, and ability to provide a new pattern definition.

*Patterns* are defined in PAMELA using three classes, each one representing a different conceptual level:

- a `PatternFactory`. This class is responsible for identifying, at run-time, the declared patterns in the Java byte-code.
- a `PatternDefinition`. This class represents an occurrence of the pattern in the supplied byte-code. It has the responsibility of maintaining links with all classes and methods involved in the pattern, as well as managing the life-cycle of its `PatternInstances`.
- a `PatternInstance`. This class represents the instance of a pattern at run-time. It is responsible for maintaining the pattern state and providing pattern behavior and contract enforcement.

To declare a *Pattern* on existing code, pattern elements such as *Pattern Stakeholders* and methods need to be annotated with provided pattern-specific  annotations. These annotations will be discovered at run-time by the `PatternFactory` and stored in `PatternDefinition` attributes.

![PAMELAAuthenticator_CD](https://support.openflexo.org/images/components/pamela/PAMELAAuthenticator_CD.png)

To validate the implementation of our approach in PAMELA we propose an implementation of the \emph{Authenticator pattern} as a cyber-security contract. Previous figure presents the previous class structure in the case of the Authenticator pattern. Note that each attribute of the `AuthenticatorPatternDefinition` class has a corresponding annotation (displayed as a comment).

![AuthenticatorPattern4](https://support.openflexo.org/images/components/pamela/AuthenticatorPattern4.png)

Previous figure presents the composition of the *Authenticator* pattern with an existing base of code. The  *Authenticator pattern* requires the definition of two *stakeholders* (*Authenticator* role and *Subject* role) which have to be played by instances of provided Java classes. A set of annotations coming with the security pattern definition is used to explicit that roles (respectively `@Authenticator` and `@AuthenticatorSubject` annotations). The definition of the pattern also requires the distribution of responsibilities and relationships according to the underlying semantics of the pattern (here, the authentication concerns). The request authentication method is identified using the `@RequestAuthentication` annotation. In the same way, we must identify some responsibilities in the `Client` class: the method providing the  *Authenticator* access, the method providing the authentication information, the method setting the proof of identity and the `authenticate()` method itself.

With that implementation, contract assertions do not require to be explicitly defined by the programmer, but are inherent to the security pattern, and are encoded in plain Java code. From an operational point of view, the security pattern is reified as a Java instance, and maintains a statefull environment on which contract assertions may be defined.

The excerpt of code presented in following listing shows how the Authenticator pattern is used by means of annotations to an existing base of code. An instance of the `Manager` class plays the *Authenticator* role, while an instance of the `Client` class plays the *Subject* role.

```java
@ModelEntity
@Authenticator(patternID = "MyPatternId")
public class Manager {

	@RequestAuthentication(patternID = "MyPatternId")
	public int request(@AuthenticationInformation(patternID = "MyPatternId", paramID = "id") String id) {
		return ...;
	}
}

@ModelEntity
@AuthenticatorSubject(patternID = "MyPatternId")
public class Client {

	public Client(Manager authenticator, String id) {
		...
	}

	@AuthenticationInformation(patternID = "MyPatternId", paramID = "id")
	public String getAuthInfo() {
		return ...;
	}

	@ProofOfIdentityGetter(patternID = "MyPatternId")
	public int getIDProof() {
		return ...;
	}

	@AuthenticatorGetter(patternID = "MyPatternId")
	public Manager getManager() {
		return ...;
	}

	@AuthenticateMethod(patternID = "MyPatternId")
	public void authenticate() {
		setIDProof(getManager().request(getAuthInfo()));
	}

	@RequiresAuthentication
	public void securedMethod() {
		...
	}
}
```

*Authenticator* pattern implementation is provided by a Java implementation gathering three classes representing a different conceptual level as defined in the Meta-Object Facility (MOF):

- `AuthenticatorPatternFactory.java` (implements `PatternFactory.java`, layer M2) : provides features to identify in byte-code at run-time the implementation of an *Authenticator* pattern, on the basis of \texttt{patternId} identifier.
- `AuthenticatorPatternDefinition.java` (implements `PatternDefinition.java` layer M1) : represents an occurrence of *Authenticator* pattern in supplied byte-code.
- `AuthenticatorPatternInstance.java` (implements `PatternInstance.java`, layer M0) : represents an instance of *AuthenticatorPatternDefinition* pattern. This class has the responsability of maintaining state of pattern instance, and managing  business logic as offered by the pattern.

At run-time, both functional code and security pattern logic, i.e pattern behavior and contract enforcement, are weaved by the PAMELA framework. 

This implementation, unlike JML, provides a contract enforcement mechanism which is hard-coded within PAMELA *Pattern* classes. This abstraction is closer to the requirements of the end-user, who may want to add an authentication mechanism to an existing code. The user only need to annotate his code and PAMELA will automatically handle the pattern business logic (both, pattern behavior and assertion checking).

![AuthenticatorControlFlow](https://support.openflexo.org/images/components/pamela/AuthenticatorControlFlow.png)

Previous figure depicts  the control flow of the execution of a method annotated with `RequiresAuthentication`. 
This annotation is used to identify methods which must trigger the authentication process before being executed. The call will thus be handled as follows:

- The method is identified as a method of interest because this method requires authentication.
- The method is passed to the relevant `AuthenticatorPatternInstance` (second object on the Figure) before the method execution.
- The authentication process is to be executed, because subject is not authenticated yet (`isAuthenticated` is `false`).
- Before executing the called method, contract invariants are checked (properties P1 to P4) via the `checkInvariants()` call. If a clause breach is detected, an exception is thrown and the method is not executed. This is also at this time that preconditions are checked (method `checkPreConditions`).
- The method is invoked.
- The method is once again passed to the `AuthenticatorPatternInstance` after the method execution. Contract invariants are checked. If a clause breach is detected, an exception is thrown. Similarly,  post-conditions are checked (method `checkPostConditions`).
- Finally, the result of the method is returned to the caller.

