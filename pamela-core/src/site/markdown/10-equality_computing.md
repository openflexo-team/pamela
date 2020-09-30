# Equality computing support

PAMELA framework additionnaly offers various interesting features in the context of object graph manipulations, such as equality computation, visiting patterns, and diff/merge support with differential updating.

Object graph comparison features are offered by `AccessibleProxyObject` base API, with the method `equalsObject(Object)`. Semantics if this equality computation is performed on the whole object graphs (the one represented by invoked instance and the opposite one), regarding properties considered as persistent (non derived or ignored properties). The two object graphs are considered as equals if and only if they expose the same graph topology regarding properties to consider, and if all matching pair of objects are equal. To be equals the two objects should have all their property values equals with following semantics:

- if values are PAMELA object, they should be equals according to PAMELA semantics (the one we are about to describe);
- if values are convertable to `String`, their conversion to string must match;
- otherwise, they should be equals according to Java language semantics.

 

    
  
