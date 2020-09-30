# Getting started with PAMELA

### Getting Pamela

There are several way to get and/or use Pamela.

#### Maven

Pamela is built using gradle and publish it's artifacts to the [OpenFlexo maven repository](https://maven.openflexo.org/artifactory/openflexo-release).
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
Add also the dependency to the `pamela-core` artifact:

```xml
<dependency>
  <groupId>org.openflexo</groupId>
  <artifactId>pamela-core</artifactId>
  <version>RELEASE</version>
</dependency>
```

#### Gradle

It's easy to use it from Gradle. The Pamela artifact are publish in the [OpenFlexo maven repository](https://maven.openflexo.org/artifactory/openflexo-release).
In order to use Pamela in your gradle project, add the repository adding the following in the `build.gradle`:

```groovy
maven {
    url "https://maven.openflexo.org/artifactory/openflexo-release/"
}
```

Add also the dependency to the `pamela-core` artifact:

```groovy
implementation group: 'org.openflexo', name: 'pamela-core', version: '1.5.1'
```

#### Direct download

Pamela can be directly downloaded from the [OpenFlexo maven repository](https://maven.openflexo.org/artifactory/openflexo-release) along with it dependencies:
- `org.openflexo:pamela-core:1.5.1`,
- `org.openflexo:connie-core:1.5.1`,
- `org.openflexo:flexoutils:1.5.1`,
- `org.openflexo:tools-configuration:0.5`.
- `com.google.guava:guava:27.0-jre`,
- `org.apache.commons:commons-lang3:3.8.1`,
- `org.javassist:javassist:3.22.0-GA`,

**TODO**

#### Build it your self

In order to get the most up to date code you can build Pamela yourself.

**Prerequisite**

- [git](https://git-scm.com).
- [Java](http://www.oracle.com/technetwork/java/index.html) JDK 8


Building Pamela is easy, follow these steps:

**TODO**


## Your first model

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

## Reference documentation

The javadoc for Pamela can be found [here](./pamela-core/apidocs/index.html).
   
  
