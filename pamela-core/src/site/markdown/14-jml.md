# Support for Contract Programming, JML

The Design by Contract (DbC) concept was coined by B. Meyer as an approach to design reliable software based on the idea that elements of a software system collaborate with each other on the basis of mutual obligations and benefits.

The whole DbC approach relies on the idea of contract. Meyer indeed realized that most of the software systems, and in particular object-oriented systems, depend on the division of work. This means that tasks are classically divided in several sub-tasks, each being conducted by a program unit. 
%This kind of organization can also be observed in most professional situations. 
Most of the time, the completion of a given task is made possible by the division of labor between several actors. When this happens, the actual interaction of the actors is entirely defined in a *contract*. This contract contains the liabilities and benefits of the interaction for all parties involved. This analogy led Meyer to the idea of software contracting \cite{meyer1992applying}: to define contracts between clients (i.e., routine’s callers) and suppliers (i.e., routines, functions or methods).
A contract is defined as the aggregation of two assertions to a routine or method:

- A precondition:  Boolean condition that needs to be verified before calling the routine. It summarizes the client's obligations towards the supplier.
- A postcondition: Boolean condition that needs to be verified after the call is made. It summarizes the supplier's obligations towards the client.

The whole idea of the DbC approach is that since a contract is formally defined for each service (routine or method), bugs are unlikely to appear at run-time because of a misunderstanding between program units.

In addition to these two assertions, Meyer defined a third type of Boolean expression called a *class invariant*. This notion relies on the class concept. In object-oriented designs, a class should be the representation of some specific concept, usually referred to as object or model. Most of the time, a few properties characterize the essence of the class and should be true at all time and for every instance. A classic example of this idea is the binary tree node class in which all nodes are connected to at most two nodes. A node instance of such a structure should verify at any time that both its children reference it as their respective parent node. This property is then an *Invariant* for this class.

Java Modelling Language (JML) is a specification language  that enables engineers to write DbC assertions as comments in Java code. These comments are then parsed by a specific compiler (called JMLC). The resulting java bytecode is the aggregation of java compile code and java assertions enforcing JML expressions.
The JML language allows referencing the namespace of the Java program and the logical operators of the Java language. It also has some keywords and symbols that correspond, for the most part, to the concepts of the DbC approach. For example:

- **forall**, **exist**, $=>$ and $<=>$, which correspond to the universal quantification, existential quantification, and logical implication and equivalence, respectively;
- **invariant**, **requires** and **ensures** are used to represent, respectively, the invariants, preconditions and postconditions of contracts;
- assignable **<name>** to specify that a variable can be assigned in the method it specifies;;
- **old<name>** to reference the value of a variable before the call of the method it specifies;
- **result** to reference the return value of the method it specifies;
- **signals** describes the exceptions thrown by the method it specifies;
- **pure** specifies that the specified method does not have side effects;
- **also**: declares that a method inherits JML specification (preconditions and postconditions) from its supertype and adds specific specifications.

JML is supported by various tools such as OpenJML, which enables static or run-time checking of the validity of the annotations through static code verification and dynamic assertion checking capabilities.

Instead of declaring JML assertions in comments, PAMELA framework offers a set of annotations which have to be declared 

1. at *ModelEntity* level for *class invariant*
2. at method level for preconditions and postconditions.

Following listing shows a basic example of a DbC-based `BankAccount` implementation. *Class invariant* are declared using `@Invariant` annotation, while pre and post condition are respectively declared with `@Requires` and `@Ensures` annotations.

```java
@ModelEntity
@ImplementationClass(BankAccountImpl.class)
@Invariant("(balance >= 0) && (balance <= 1000)")
public interface BankAccount extends AccessibleProxyObject, SpecifiableProxyObject {

	static final int MAX_BALANCE = 1000;
	static final String BALANCE = "balance";
	static final String LOCKED = "isLocked";

	@Getter(value = BALANCE, defaultValue = "0")
	int getBalance();

	@Setter(BALANCE)
	@Requires("aBalance >= 0")
	@Ensures("balance >= 0")
	public void setBalance(@MethodParameter("aBalance") int aBalance);

	@Getter(value = LOCKED, defaultValue = "false")
	boolean isLocked();

	@Setter(LOCKED)
	public void setLocked(boolean locked);

	@Initializer
	@Assignable(BALANCE)
	@Ensures("balance==0")
	public BankAccount init();

	@Requires("amount>0")
	@Ensures("balance == /old(balance)+amount")
	@Assignable(BALANCE)
	public void credit(@MethodParameter("amount") int amount);

	@Requires("(amount>0) && (amount <= balance) && (!isLocked)")
	@Ensures("balance == /old(balance)-amount")
	@Assignable(BALANCE)
	public void debit(@MethodParameter("amount") int amount);

	@Ensures("(isLocked==true)")
	public void lockAccount();

	public static abstract class BankAccountImpl implements BankAccount {

		@Override
		public void credit(int amount) {
			System.out.println("****** credit with " + amount);
			// Thread.dumpStack();
			setBalance(getBalance() + amount);
		}

		@Override
		public void debit(int amount) {
			// Thread.dumpStack();
			setBalance(getBalance() - amount);
			System.out.println("****** debit with " + amount);
		}

		@Override
		public void lockAccount() {
			setLocked(true);
		}

	}
}
```

A major interest in PAMELA framework use is the dynamic assertion checking at run-time. When activated, dynamic assertion checking is weaved with the functional behaviour. If the executed model defines JML contract management clauses (assertions, pre and post conditions) and if the execution configuration forces the assertion checking (API is available through the `SpecifiableProxyObject` interface), following execution are performed:

- Assertions clauses are executed for all methods of class where assertion was defined.
- Precondition clauses are executed before the execution of method where precondition is defined.
- Postcondition clauses are executed after the execution of method where postcondition is defined.

