---
sidebar_position: 13
---

# Differential updating

Differential updating for object graphs is natively supported using method 
`updateWith(Object)` provided in `AccessibleProxyObject` base API. Calling this method will trigger the computing of the differences between the two referenced object graphs (the receiver of the method and the argument of the method). 

Underlying algorithm perform a match between object beeing addressed for all object properties, trying to associate "similar" objects, and detecting new or absent objects. The reveiver object is then beeing updated according to supplied object, from an incremental manner composed of required method calls for write-access property protocols (`set:`, `addTo:`, `removeFrom:` and `reindex:To:`). 

Similarity between two objects is obtained as the result of the computation of the "distance" between that two objects, using following rules:

- the distance between two objects is always between 0.0 and 1.0
- objects beeing equals with the semantics defined in section \ref{subsub:EqualityComputationSupport} have a related distance equals to 0.0;
- dissimilar objects (objects with different types) have their related distance equals to 1.0;
- distance between two objects with the same type and which are serializable as String (in the context of PAMELA serialization scheme) is given by the levenshtein distance between the two string representations;
 - distance between PAMELA objects is computed while computing distance between property values for all declared *ModelProperty*, with a ponderation specific to related *ModelProperty* (if type of *ModelProperty* refers to a *ModelEntity*, ponderation equals the number of *ModelProperty* of related type, otherwise ponderation values 1).

 

    
  
