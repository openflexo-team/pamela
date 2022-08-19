---
sidebar_position: 4
---

# Meta-programming support

Following figure represents PAMELA metamodel. 

![PamelaMetaModel](https://support.openflexo.org/images/components/pamela/PamelaMetaModel.png)

Concept of *ModelProperty* represent access to a value (simple or with multiple cardinality). This read-access is implicitely implemented using `get:` protocol. Following the same logic, PAMELA properties may expose following protocols used to define semantics of the *ModelProperty* :

- `get:` read-access to a data. This protocol is implemented by all kind of *ModelProperties*.
- `set:` write-access to a data. This protocol is implemented by *ModelProperties* implementing `SettablePropertyImplementation` API.
- `add:` and `add:AtIndex:` add-access to a multiple cardinality data. These protocols are implemented by *ModelProperties* implementing `MultiplePropertyImplementation` API.
- `reindex:To:` reindex a value to a given index in a multiple cardinality data. This protocol is implemented by *ModelProperties* implementing `MultiplePropertyImplementation` API.
- `remove:` remove-access to a multiple cardinality data. This protocol is implemented by *ModelProperties* implementing `MultiplePropertyImplementation` API.
- `delete:` and `undelete:` delete/undelete protocols, implemented by all *ModelProperties*

Unless specified in `@PropertyImplementation` PAMELA annotations, default implementation is provided for the PAMELA interpreter. This default implementation, also called the *ModelProperty* standard semantics, is encoded in well identified classes which are extendable to provide specific *ModelProperty* semantics.

PAMELA allows programmers to define their custom implementation for a given set of properties, while providing a class implementing `@PropertyImplementation` API and some protocol implementation (depending on the nature of the *ModelProperty*. Following excerpt of code illustrate use of two custom property implementations, with specific semantics.

```java
@ModelEntity
public interface MyConcept {

	static final String VALUE = "value";
	static final String SUB_CONCEPTS = "someSubConcepts";

	@Getter(value = VALUE)
	@PropertyImplementation(MyPropertyImplementation.class)
	String getValue();

	@Setter(VALUE)
	public void setValue(String value);

	@Getter(value = SUB_CONCEPTS, cardinality = Cardinality.LIST)
	@PropertyImplementation(MyListCardinalityPropertyImplementation.class)
	@Embedded
	List<MySubConcept> getSubConcepts();

	@Adder(SUB_CONCEPTS)
	void addToSubConcepts(MySubConcept subConcept);

	@Remover(SUB_CONCEPTS)
	void removeFromSubConcepts(MySubConcept subConcept);
}

public class MyPropertyImplementation extends DefaultSinglePropertyImplementation<Concept, String> {

	public MyPropertyImplementation(ProxyMethodHandler<Concept> handler, ModelProperty<Concept> property)
			throws InvalidDataException {
		super(handler, property);
	}

    // Implements get: protocol
    // ('concatenate' semantics)
	@Override
	public void set(String aValue) throws ModelDefinitionException {
		String oldValue = get();
		if (get() != null) {
			super.set(aValue + get());
		}
		else {
			super.set(aValue);
		}
	}
}

public class MyListCardinalityPropertyImplementation<I, T> extends AbstractPropertyImplementation<I, List<T>>
		implements MultiplePropertyImplementation<I, T> {
	...
	// Implements get: protocol
	public List<T> get() {
		...
	}
	// Implements add: protocol
	public void addTo(T aValue) throws ModelDefinitionException {
		...
	}
	// Implements remove: protocol
	public void removeFrom(T aValue) {
		...
	}
}
```


Such mechanisms are really usefull to control and fine-tune code implementation. Suppose for example that you have a code used in monothreading context: all your multiple cardinality properties will rely for example on a `ArrayList` implementation. Instead of replacing all occurences of `ArrayList` by `Vector` (multi-thread safe version of Java `List`) in generated code, the developer has just one modication to do, in adequate `PropertyImplementation`.



    
  
