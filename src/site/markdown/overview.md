# Approach overview

## Model serialization in source code

We advocate for a strong coupling between model and source-code, to give architects and developers a way to both interact during the whole development cycle. PAMELA is an annotation-based Java modeling framework providing a smooth integration between model and code, without code generation nor externalized model serialization. The idea is to avoid separation between modeling and code to facilitate consistency management and avoid round-tripping issues.

To do so, we argue that source code is the right artefact to encode the model with metadata information stored in tagged code. This requires an annotation-enabled language. Such language supports the attribute-oriented programming if its grammar allows adding custom declarative tags to annotate standard program elements. Java programming language from version 1.5 is a good candidate with the support of annotations.

Following figure shows PAMELA approach for storing model in source code. The model is inlined in many source code files, with a set of annotations covering PAMELA metamodel as presented in the next subsection.
    
![PamelaVisionV2](https://support.openflexo.org/images/components/pamela/PamelaVisionV2.png)

## PAMELA use process
    
Coupling model and code into the same artefact open new ways of programming. The classical way relies on 
\emph{programmers} that produce code reusing pre-existing modeling concepts. These concepts are implemented 
by \emph{modelers} that provides the right annotations the programmers use. This is, for instance, the 
process followed by JEE developers reusing JEE specific annotations. The evolution rhythm between models 
and code is low. This programming way is still possible with PAMELA, but we allow the ability to reach a 
high evolution rhythm when the programmer becomes also the modeler. In fact, when a pattern, an abstraction, 
a generalization is identified by the programmer, s/he can use PAMELA to develop and capitalize on this 
abstraction by increasing PAMELA metamodel. 

The developed metamodels are implemented by annotations that relies on Java/JVM entities and mechanisms. They include consistency checking that constrains their use and help the programmer. We have experimented their use with setter/getter to define POJO entities, with traits to implement multiple inheritance or roles and rules to set security rules on classes.

Our experience shows that introducing and reusing new concepts (1) reduce the size of the code, (2) reduce the risk of errors and (3) improve the code structure. The cycle of development between the model and the code can then be drastically reduced, leading to what we call \emph{continuous modeling}.

The code size is reduced because abstractions factorize the recognize pattern and the previous code is replaced by the use of the abstraction at the right place. This also reduce the risk since the previous code is now generated by the PAMELA framework with all the required checks. And finally, the code structure is improved since it matches the way the programmer conceptualizes (models) her/his code. 
    

    
  