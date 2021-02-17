# Getting started with PAMELA

There are several way to get and/or use Pamela.

1. You just want to use (test) PAMELA (you are a developer and you want to define your first PAMELA model)
2. You want to download PAMELA sources, review the code, run the tests, define your own annotations, implementations, and contribute to the project

### 1. You want to use PAMELA

You just need to bring Pamela artifacts in your environment.

- The Pamela artifacts (official releases) are published in the following maven repository [https://maven.openflexo.org/artifactory/openflexo-release](https://maven.openflexo.org/artifactory/openflexo-release)
- The Pamela artifacts (snapshot releases) are published in the following maven repository [https://maven.openflexo.org/artifactory/openflexo-deps](https://maven.openflexo.org/artifactory/openflexo-deps)
- 1.5 is the last stable version
- 1.6.1(-SNAPSHOT) is the current development version (default)

**Prerequisite**

- [Maven](https://maven.apache.org/) or [Gradle](https://gradle.org)
- Using an IDE (such as Eclise or IntelliJ) is recommended and might help
- As a developper tool, this "Getting started" supposes that you are familiar with development tools such as gradle or maven, and IDE manipulations.

#### 1.1. Use PAMELA with gradle

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

Add also the dependency to the `pamela-core` artifact (Java 8 required):

```groovy
implementation group: 'org.openflexo', name: 'pamela-core', version: '1.5'
```

or to use a SNAPSHOT version (Java 11 or later required):

```groovy
implementation group: 'org.openflexo', name: 'pamela-core', version: '1.6.1-SNAPSHOT'
```

**Important notice : **
Note that Java 8 (1.8) is required for 1.5 last stable version but Java 11 is required from 1.6.1 version.

#### 1.2. Use PAMELA with Maven

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

Add also the dependency to the `pamela-core` artifact (Java 8 required):

```xml
<dependency>
  <groupId>org.openflexo</groupId>
  <artifactId>pamela-core</artifactId>
  <version>1.5</version>
</dependency>
```

or to use a SNAPSHOT version (Java 11 or later required)

```xml
<dependency>
  <groupId>org.openflexo</groupId>
  <artifactId>pamela-core</artifactId>
  <version>1.6.1-SNAPSHOT</version>
</dependency>
```

**Important notice : **
Note that Java 8 (1.8) is required for 1.5 last stable version but Java 11 is required from 1.6.1 version.

#### 1.3. Test PAMELA by building your first model

The best way to understand PAMELA is to build your first model.

[Build your first model](./example.html)

Look at the examples (download links) at the end of this webpage, and run examples.

For each example, we can do :

```
gradle test
```

and

```
gradle run
```

Here are the different versions:

1. [v1.zip](https://support.openflexo.org/images/components/pamela/examples/v1.zip) : minimal example
2. [v2.zip](https://support.openflexo.org/images/components/pamela/examples/v2.zip) : adding behaviour
3. [v3.zip](https://support.openflexo.org/images/components/pamela/examples/v3.zip) : behaviour modifications (AccessibleProxyObject)
4. [v4.zip](https://support.openflexo.org/images/components/pamela/examples/v4.zip) : a more complex example

### 2. Download and build PAMELA framework in your environment

Advanced users : do that only if you want to download PAMELA sources, review the code, run the tests, define your own annotations, implementations, and/or contribute to the project.

**Prerequisite**

- [Maven](https://maven.apache.org/) or [Gradle](https://gradle.org)
- Using an IDE (such as Eclise or IntelliJ) is recommended and might help

#### 2.1. Download sources

In order to get the most up to date code you can clone and build Pamela yourself.

**Prerequisite**

- [git](https://git-scm.com).
- [Java](http://www.oracle.com/technetwork/java/index.html) JDK 8

**Checkout sources**

PAMELA framework is hosted on github: [https://github.com/openflexo-team/pamela](https://github.com/openflexo-team/pamela).

Clone PAMELA in your environment:

```
git clone git@github.com:openflexo-team/pamela.git
```

#### 2.2 Run tests

Unit tests located in src/test/java in pamela-core are a good starting point to discover the framework (run the tests and analyse the test source code).

All tests are sorted and labelled with an explicit name which help to know which feature is beeing tested.

For example the package ``org.openflexo.pamela.test.jml`` contains unit tests for JML features. ``BankAccount.java`` is the class containing business code to be tested while ``JMLTests.java`` contains the tests themselves. It is really interesting to read and understand both classes to have a good overview and understanding for JML feature.

The package ``org.openflexo.pamela.test.test1`` contains basic unit tests for core features.

#### 2.3 Discover PAMELA main features

- [Model at runtime computation, model fragmentation management](./pamela-core/1-model_at_runtime.html)
- [Life-cycle management](./pamela-core/2-life_cycle_management.html)
- [Meta-programming support](./pamela-core/3-metaprogramming_support.html)
- [Multiple inheritance and traits programming](./pamela-core/4-multiple_inheritance.html)
- [Containment management](./pamela-core/5-containment_management.html)
- [Cloning support](./pamela-core/6-cloning_support.html)
- [Clipboard operations](./pamela-core/7-clipboard_operations.html)
- [Notification support](./pamela-core/8-notification_support.html)
- [Persistance support, XML serialization/deserialization](./pamela-core/9-persistence_support.html)
- [Equality computing support](./pamela-core/10-equality_computing.html)
- [Visiting features](./pamela-core/11-visiting_features.html)
- [Differential updating](./pamela-core/12-differential_updating.html)
- [Validation API](./pamela-core/13-validation_api.html)
- [Contract programming, JML](./pamela-core/14-jml.html)
- [Design patterns, aspect programming](./pamela-core/15-design_patterns.html)

#### 2.4 Reference documentation

The javadoc for Pamela can be found [here](./pamela-core/apidocs/index.html).

   
  
