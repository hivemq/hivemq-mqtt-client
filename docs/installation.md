---
layout: default
title: Installation
nav_order: 2
has_children: true
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
    implementation group: 'com.hivemq', name: 'hivemq-mqtt-client', version: '{{ site.version }}'
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
            <version>{{ site.version }}</version>
        </dependency>
    </dependencies>
    ...
</project>
```

{% include admonition.html type="note" title="Note" content="

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

"%}

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
    implementaion group: 'com.hivemq', name: 'hivemq-mqtt-client-shaded', version: '{{ site.version }}'
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
            <version>{{ site.version }}</version>
        </dependency>
    </dependencies>
    ...
</project>
```

{% endcapture %}
{% include tabs.html tab_group="build-tool" %}


## Snapshots

Snapshots can be obtained using [JitPack](https://jitpack.io/#hivemq/hivemq-mqtt-client).

{% capture tab_content %}

Gradle
===

```groovy
repositories {
    ...
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.hivemq.hivemq-mqtt-client:hivemq-mqtt-client:develop-SNAPSHOT'
}
```

====

Maven
===

```xml
<project>
    ...
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
    
    <dependencies>
        <dependency>
            <groupId>com.github.hivemq.hivemq-mqtt-client</groupId>
            <artifactId>hivemq-mqtt-client</artifactId>
            <version>develop-SNAPSHOT</version>
        </dependency>
    </dependencies>
    ...
</project>
```

{% endcapture %}
{% include tabs.html tab_group="build-tool" %}

Change the artifact name to `hivemq-mqtt-client-shaded` to get snapshots of the shaded version.

JitPack works for all branches and also specific commits by specifying in the version.


## Building from source

The HiveMQ MQTT Client project uses Gradle to build. A gradle wrapper configuration is included, so after cloning the 
repository from GitHub, simply change into the directory containing the project and execute `./gradlew build`.