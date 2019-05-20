---
layout: default
title: Installation
nav_order: 2
---

# Installation

The HiveMQ MQTT Client library is available in the Maven Central repository and therefore can be setup easily in your
project by using a dependency management tool like Gradle or Maven.

{% capture tab_content %}

Gradle
===

If you use Gradle, just include the following inside your `build.gradle` file.

```groovy
dependencies {
    compile group: 'com.hivemq', name: 'hivemq-mqtt-client', version: '1.0.0'
}
```

====

Maven
===

If you use Maven, just include the following inside your `pom.xml` file.

```xml
<project>
    ...
    <dependencies>
        <dependency>
            <groupId>com.hivemq</groupId>
            <artifactId>hivemq-mqtt-client</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
    ...
</project>
```

NOTE
{: .label }
You have to set the compiler version to `1.8` or higher.

```xml
<project>
    ...
    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
    </properties>
    ...
</project>
```

{% endcapture %}
{% include tabs.html tab_group="build-tool" %}


## Shaded version

If you are experiencing problems with transitive dependencies, you can try the shaded version.
This version packs the transitive dependencies which are only used internal under a different package name.
To use the shaded version just append `-shaded` to the artifact name.

{% capture tab_content %}

Gradle
===

```groovy
dependencies {
    compile group: 'com.hivemq', name: 'hivemq-mqtt-client-shaded', version: '1.0.0'
}
```

====

Maven
===

```xml
<project>
    ...
    <dependencies>
        <dependency>
            <groupId>com.hivemq</groupId>
            <artifactId>hivemq-mqtt-client-shaded</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
    ...
</project>
```

{% endcapture %}
{% include tabs.html tab_group="build-tool" %}


## Building from source

The HiveMQ MQTT Client project uses Gradle to build. A gradle wrapper configuration is included, so after cloning the 
repository from GitHub, simply change into the directory containing the project and execute `./gradlew build`.