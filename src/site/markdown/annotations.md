# Common annotations

Here is a non-exhaustive list of the most common Java annotations used for a PAMELA model definition.

- `@ModelEntity`: tag annotating `interface` as *ModelEntity*. May declares an abstract entity.
- `@ImplementationClass`: tag annotating *ModelEntity* `interface` and precising an abstract Java class to be used as base implementation.
- `@Implementation`: tag annotating a partial implementation (abstract inner `class` defined in implemented `interface`), and used in the context of multiple inheritance.
- `@Getter(String)`: tag annotating method as unique getter for implicit *ModelProperty* whose identifier is the declared String value. May also declares cardinality, eventual inverse property, default value and some other features.
- `@Setter(String)`: tag annotating method as unique setter for implicit *ModelProperty* whose identifier is the declared String value.
- `@Adder(String)`: tag annotating method as unique adder for implicit multiple cardinality *ModelProperty* whose identifier is the declared String value.
- `@Remover(String)`: tag annotating method as unique remover for implicit multiple cardinality *ModelProperty* whose identifier is the declared String value.
- `@Reindexer(String)`: tag annotating method as unique reindexer for implicit multiple cardinality *ModelProperty* whose identifier is the declared String value.
- `@Initializer`: tag annotating a method used as a constructor for related *ModelEntity*.
- `@Deleter`: tag annotating a method used as explicit destructor for related *ModelEntity*.
- `@Finder(String,String)`: tag annotating method as a fetching request for a given *ModelProperty* with a given attribute.
- `@CloningStrategy`: allows to customize cloning strategy for a given *ModelProperty*.
- `@Embedded`: allows to declare a given *ModelProperty* as embedded according to PAMELA semantics.
- `@Imports` and `@Imports`: allows to declare entities to be included in the inferred metamodel by `ModelContext` computation.
- `@XMLElement` and `@XMLAttribute`: used to specify XML serialization for PAMELA instances.
