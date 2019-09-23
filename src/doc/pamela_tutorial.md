Par Victor Gambier.

Veuillez d'abord lire cette introduction : https://pamela.openflexo.org/SNAPSHOT/pamela-core/index.html

# Comment créer un modèle simple

Pour créer un modèle Pamela, il suffit de créer une interface et de définir implicitement autant d'attributs que vous le souhaitez :

```
public interface ClassModel {
    String NAME = "name";

    @Getter(NAME)
    String getName();

    @Setter(NAME)
    void setName(String name);
}
```

Pamela interprètera ces annotations et créera implicitement une variable de type String accessible via `getName()` et modifiable via `setName()`. Si l'on veut que l'attribut soit une liste, il est possible de faire ceci :

```
public interface ClassModel {
    String NAMES = "names";

    @Getter(NAMES)
    public List<String> getNames();

    @Adder(NAMES)
    public void addName(String name);

    @Remover(NAMES)
    public void removeString(String name);
}
```

Si l'on veut créer un attribut dont le type est une classe que l'on a créée nous-mêmes (et non pas un type primitif comme String, par exemple), il faut ajouter l'annotation  `@ModelEntity` au-dessus de la classe concernée.

Pour instancier le modèle, il faut utiliser une factory :

```
ModelFactory classFactory = new ModelFactory(ClassModel.class);
```

# Définir des méthodes ou attributs hors de Pamela

Si l'on souhaite ajouter des méthodes ou attributs sans avoir recours à Pamela, il faut définir une implémentation :

```
public interface ClassModel {

    // Pamela attributes go here:
    // ...

    // Non-Pamela method:
    abstract class ClassModelImpl implements ClassModel {
        public void HelloWorld() {
            System.out.println("Hello World!");
	}
    }
}
```

# Paramètres d'annotations

Il est possible d'adopter une modélisation sophistiquée en utilisant plus d'annotations ou de paramètres. Par exemple :

L'annotation `@Getter` peut accepter plusieurs paramètres différents. S'il n'y en a qu'un, Pamela considère qu'il s'agit de l'attribut "value". Sinon, il faut préciser. Par exemple :

```
@Getter(value = COMPILATION_UNIT, cardinality = Cardinality.LIST, inverse = CompilationUnitModel.PACKAGE)
```

Le paramètre inverse indique une relation inverse entre l'attribut actuel et l'attribut de value PACKAGE du modèle Pamela CompilationUnitModel. Autre exemple :

```
@Getter(value = PROJECT, isDerived = true)
```

Le flag `isDerived` indique à Pamela que l'attribut peut être déduit d'autres informations contenues dans les modèles. Cela a notamment comme effet d'éviter des StackOverflowException lorsque l'on utilise `updateWith` sur un modèle qui a comme attribut un modèle Pamela si les deux modèles ont une relation inverse. Sans le flag `isDerived`, Pamela essaiera de recalculer le modèle parent, puis le modèle enfant, puis le modèle parent etc.

# Sérialisation et désérialisation XML

Il est possible de sérialiser un modèle afin de générer un fichier XML décrivant ledit modèle. Pour qu'un modèle soit sérialisable, il faut ajouter l'annotation `@XMLElement` à l'interface du modèle ainsi qu'à chaque attribut que l'on souhaite modéliser. Exemple :

```
@XMLElement
public interface ClassModel {

    // Attributes and methods regarding the name of the class

    String NAME = "name";

    @Getter(NAME)
    @XMLAttribute
    String getName();

    @Setter(NAME)
    void setName(String name);

    // Attributes and methods regarding the parent compilation unit

    String COMPILATION_UNIT = "compilationUnit";

    @Getter(COMPILATION_UNIT)
    CompilationUnitModel getCompilationUnit();

    @Setter(COMPILATION_UNIT)
    void setCompilationUnit(CompilationUnitModel compilationUnitModel);
}
```

Ici, seul l'attribut correspondant à `getName()` paraîtra dans la sérialisation XML.

Ensuite, pour effectuer la sérialisation, il suffit de faire ceci :

```
projectFactory.serialize(projectModel, fos, SerializationPolicy.EXTENSIVE, true);
```

où `projectModel` est une instance du modèle à sérialiser, et `fos` est un FileOutputStream. Par exemple :

```
File xmlFile = new File(xmlPath);
mlFile.createNewFile();
FileOutputStream fos = new FileOutputStream(xmlFile);
```

Il est également possible de désérialiser un fichier XML en modèle Pamela :

```
ProjectModel projectModel = (ProjectModel) projectFactory.deserialize(fis, DeserializationPolicy.RESTRICTIVE);
```

où `fis` est un FileInputStream, par exemple :

```
FileInputStream fis = new FileInputStream(xmlFile);
```

# updateWith method

Pamela contient une méthode nommée `updateWith` qui permet de mettre un jour un modèle A à partir d'un modèle B. Suite à cette opération, les informations stockées dans le modèle A seront identiques à celles du modèle B, mais contrairement à un simple `ClassModel modelA = modelB;`, les pointers, références, etc. du modèle A ne seront pas affectés.

Exemple d'utilisation :

```
oldModel.updateWith(newModel);
```


