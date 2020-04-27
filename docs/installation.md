---
layout: default
title: Installation
nav_order: 2
has_children: true
redirect_from: /docs/installation.html
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


## Optional features

The HiveMQ MQTT Client provides optional features via additional modules.

| Module name | Description |
| ----------- | ----------- |
| `hivemq-mqtt-client-websocket` | Adds dependencies for the WebSocket transport |
| `hivemq-mqtt-client-proxy` | Adds dependencies for the proxy transport |
| `hivemq-mqtt-client-epoll` | Adds dependencies for the native epoll socket implementation |
| `hivemq-mqtt-client-reactor` | [Reactor](https://github.com/reactor/reactor-core) API for the HiveMQ MQTT Client |

If you want to use these optional features, also include the respective modules in addition to the base dependency.

{% capture tab_content %}

Gradle
===

  {% capture tab_content %}

  Base
  ===

```groovy
dependencies {
    implementation group: 'com.hivemq', name: 'hivemq-mqtt-client', version: '{{ site.version }}'
}
```

  ====

  WebSocket
  ===

```groovy
dependencies {
    implementation group: 'com.hivemq', name: 'hivemq-mqtt-client-websocket', version: '{{ site.version }}'
}
```

  ====

  Proxy
  ===

```groovy
dependencies {
    implementation group: 'com.hivemq', name: 'hivemq-mqtt-client-proxy', version: '{{ site.version }}'
}
```

  ====

  Epoll
  ===

```groovy
dependencies {
    implementation group: 'com.hivemq', name: 'hivemq-mqtt-client-epoll', version: '{{ site.version }}'
}
```

  ====

  Reactor
  ===

```groovy
dependencies {
    implementation group: 'com.hivemq', name: 'hivemq-mqtt-client-reactor', version: '{{ site.version }}'
}
```

  {% endcapture %}
  {% include tabs.html tab_group="module" %}

====

Maven
===

  {% capture tab_content %}

  Base
  ===

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

  ====

  WebSocket
  ===

```xml
<project>
    ...
    <dependencies>
        <dependency>
            <groupId>com.hivemq</groupId>
            <artifactId>hivemq-mqtt-client-websocket</artifactId>
            <version>{{ site.version }}</version>
        </dependency>
    </dependencies>
    ...
</project>
```

  ====

  Proxy
  ===

```xml
<project>
    ...
    <dependencies>
        <dependency>
            <groupId>com.hivemq</groupId>
            <artifactId>hivemq-mqtt-client-proxy</artifactId>
            <version>{{ site.version }}</version>
        </dependency>
    </dependencies>
    ...
</project>
```

  ====

  Epoll
  ===

```xml
<project>
    ...
    <dependencies>
        <dependency>
            <groupId>com.hivemq</groupId>
            <artifactId>hivemq-mqtt-client-epoll</artifactId>
            <version>{{ site.version }}</version>
        </dependency>
    </dependencies>
    ...
</project>
```

  ====

  Reactor
  ===

```xml
<project>
    ...
    <dependencies>
        <dependency>
            <groupId>com.hivemq</groupId>
            <artifactId>hivemq-mqtt-client-reactor</artifactId>
            <version>{{ site.version }}</version>
        </dependency>
    </dependencies>
    ...
</project>
```

  {% endcapture %}
  {% include tabs.html tab_group="module" %}

{% endcapture %}
{% include tabs.html tab_group="build-tool" %}


## Shaded version

If you are experiencing problems with transitive dependencies, you can try the shaded version.
This version packs the transitive dependencies which are only used internal under a different package name.
The shaded version includes the websocket, proxy and epoll modules.
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
```

  {% capture tab_content %}

  Base
  ===

```groovy
dependencies {
    implementation 'com.github.hivemq.hivemq-mqtt-client:hivemq-mqtt-client:develop-SNAPSHOT'
}
```

  ====

  WebSocket
  ===

```groovy
dependencies {
    implementation 'com.github.hivemq.hivemq-mqtt-client:hivemq-mqtt-client-websocket:develop-SNAPSHOT'
}
```

  ====

  Proxy
  ===

```groovy
dependencies {
    implementation 'com.github.hivemq.hivemq-mqtt-client:hivemq-mqtt-client-proxy:develop-SNAPSHOT'
}
```

  ====

  Epoll
  ===

```groovy
dependencies {
    implementation 'com.github.hivemq.hivemq-mqtt-client:hivemq-mqtt-client-epoll:develop-SNAPSHOT'
}
```

  ====

  Reactor
  ===

```groovy
dependencies {
    implementation 'com.github.hivemq.hivemq-mqtt-client:hivemq-mqtt-client-reactor:develop-SNAPSHOT'
}
```

  {% endcapture %}
  {% include tabs.html tab_group="module" %}

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
    ...
</project>
```


  {% capture tab_content %}

  Base
  ===

```xml
<project>
    ...
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

  ====

  WebSocket
  ===

```xml
<project>
    ...
    <dependencies>
        <dependency>
            <groupId>com.github.hivemq.hivemq-mqtt-client</groupId>
            <artifactId>hivemq-mqtt-client-websocket</artifactId>
            <version>develop-SNAPSHOT</version>
        </dependency>
    </dependencies>
    ...
</project>
```

  ====

  Proxy
  ===

```xml
<project>
    ...
    <dependencies>
        <dependency>
            <groupId>com.github.hivemq.hivemq-mqtt-client</groupId>
            <artifactId>hivemq-mqtt-client-proxy</artifactId>
            <version>develop-SNAPSHOT</version>
        </dependency>
    </dependencies>
    ...
</project>
```

  ====

  Epoll
  ===

```xml
<project>
    ...
    <dependencies>
        <dependency>
            <groupId>com.github.hivemq.hivemq-mqtt-client</groupId>
            <artifactId>hivemq-mqtt-client-epoll</artifactId>
            <version>develop-SNAPSHOT</version>
        </dependency>
    </dependencies>
    ...
</project>
```

  ====

  Reactor
  ===

```xml
<project>
    ...
    <dependencies>
        <dependency>
            <groupId>com.github.hivemq.hivemq-mqtt-client</groupId>
            <artifactId>hivemq-mqtt-client-reactor</artifactId>
            <version>develop-SNAPSHOT</version>
        </dependency>
    </dependencies>
    ...
</project>
```

  {% endcapture %}
  {% include tabs.html tab_group="module" %}

{% endcapture %}
{% include tabs.html tab_group="build-tool" %}

Change the artifact name to `hivemq-mqtt-client-shaded` to get snapshots of the shaded version.

JitPack works for all branches and also specific commits.
Just specify `<branch>-SNAPSHOT` or the first 10 digits of the commit id in the version.


## Building from source

The HiveMQ MQTT Client project uses Gradle to build. A gradle wrapper configuration is included, so after cloning the 
repository from GitHub, simply change into the directory containing the project and execute `./gradlew build`.