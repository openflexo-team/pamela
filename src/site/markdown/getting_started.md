# Getting started with PAMELA

There are several way to get and/or use Pamela.

- The Pamela artifacts (official releases) are published in the following maven repository [https://maven.openflexo.org/artifactory/openflexo-release](https://maven.openflexo.org/artifactory/openflexo-release)
- The Pamela artifacts (snapshot releases) are published in the following maven repository [https://maven.openflexo.org/artifactory/openflexo-deps](https://maven.openflexo.org/artifactory/openflexo-deps)


#### 1. Use PAMELA with gradle

It's easy to use it from Gradle. 

In order to use Pamela in your gradle project, add the repository adding the following in the `build.gradle`:

```groovy
maven {
    url "https://maven.openflexo.org/artifactory/openflexo-release/"
}
```

or to use a SNAPSHOT version

```groovy
maven {
    url "https://maven.openflexo.org/artifactory/openflexo-deps/"
}
```

Add also the dependency to the `pamela-core` artifact:

```groovy
implementation group: 'org.openflexo', name: 'pamela-core', version: '1.5'
```

or to use a SNAPSHOT version:

```groovy
implementation group: 'org.openflexo', name: 'pamela-core', version: '1.5.1-SNAPSHOT'
```


#### 2. Use PAMELA with Maven

In order to use Pamela in your maven project, add the repository adding the following in the `pom.xml`:

```xml
<repositories>
    <repository>
        <id>openflexo-release</id>
        <name>openflexo-release</name>
        <url>https://maven.openflexo.org/artifactory/openflexo-release/</url>
    </repository>
</repositories>
```

or to use a SNAPSHOT version

```xml
<repositories>
    <repository>
        <id>openflexo-release</id>
        <name>openflexo-release</name>
        <url>https://maven.openflexo.org/artifactory/openflexo-deps/</url>
    </repository>
</repositories>
```

Add also the dependency to the `pamela-core` artifact:

```xml
<dependency>
  <groupId>org.openflexo</groupId>
  <artifactId>pamela-core</artifactId>
  <version>1.5</version>
</dependency>
```

or to use a SNAPSHOT version

```xml
<dependency>
  <groupId>org.openflexo</groupId>
  <artifactId>pamela-core</artifactId>
  <version>1.5.1-SNAPSHOT</version>
</dependency>
```

#### 3. Download and build PAMELA framework in your environment

In order to get the most up to date code you can clone and build Pamela yourself.

**Prerequisite**

- [git](https://git-scm.com).
- [Java](http://www.oracle.com/technetwork/java/index.html) JDK 8

**Download sources**

PAMELA framework is hosted on github: [https://github.com/openflexo-team/pamela](https://github.com/openflexo-team/pamela).

Clone PAMELA in your environment:

```
git clone git@github.com:openflexo-team/pamela.git
```

#### 4. Run tests

Unit tests located in src/test/java in pamela-core are a good starting point to discover the framework.


#### 5. Make your first model

To build your first model, you only have to defined the interface, Pamela does the implementation for you.
Just write:

```java
@ModelEntity
interface Person {
	String NAME = "name";

	@Getter(NAME)
	String getName();

	@Setter(NAME)
	void setName(String name);
}
```

Pamela will build a class that implements the `getName` getter and the `setName` setter.
The getter is really simple, it returns the stored property `name`.
The setter is much more evolved sine it:

- sets the value,
- notify the value change (if the new value is actually different) and
- saves the change to an undo manager.

The setter will also handle the opposite property when needed as we see later on.


**Gotcha**

Since Pamela constructs it's own implementation of the interface, you can also build your own.

```java
public class PersonImpl {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
```

But this implementation is really basic and doesn't provide advanced capabilities as Pamela does.

#### Reference documentation

The javadoc for Pamela can be found [here](./pamela-core/apidocs/index.html).
   
  
