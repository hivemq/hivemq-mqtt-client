---
layout: default
title: User Guide
nav_order: 100
search_exclude: true
---

# User Guide

## Quick start

If you just want to dip your toes in, check out our [Quick Start Guide](quick_start.md)
to get you up and running in no time!

## Installation

In order to use the HiveMQ MQTT Client library, you can use one of the popular build tools like Gradle or Maven in order
to include it as a dependency in your project, or you can also build it yourself and manually
include all of the necessary JARs in your project.

Releases of the HiveMQ MQTT Client library are published to Maven Central, so any build tool which supports Maven style
dependency management should be able to include it via that.

Snapshot / nightly builds of the HiveMQ MQTT Client library are also published to the JFrog OSS Artifactory Maven
repository. So if you want to get a build with the latest features, then you can configure this
repository (see below). Be aware that the SNAPSHOT version is unstable and not recommended for us in
production.


### Gradle

Simply add `compile 'com.hivemq:hivemq-mqtt-client:{{ site.version }}'` to your dependencies, and you're good to go.

If you want to use the nightly snapshot, then include this repository:

```groovy
repositories {
    maven { url 'https://oss.jfrog.org/artifactory/oss-snapshot-local' }
}
```

And then add `compile 'com.hivemq:hivemq-mqtt-client:{{ site.version }}-SNAPSHOT'` to your dependencies.


### Maven

Include the following dependency in your `pom.xml`:

```xml
<dependency>
    <groupId>com.hivemq</groupId>
    <artifactId>hivemq-mqtt-client</artifactId>
    <version>{{ site.version }}</version>
</dependency>
```

If you want to use the nightly snapshot, then first configure the required repository:

```xml
<repositories>
    <repository>
        <id>jfrog-snapshots</id>
        <name>jfrog snapshots</name>
        <url>https://oss.jfrog.org/artifactory/oss-snapshot-local</url>
    </repository>
</repositories>
```

and then include this dependency:

```xml
<dependency>
    <groupId>com.hivemq</groupId>
    <artifactId>hivemq-mqtt-client</artifactId>
    <version>{{ site.version }}-SNAPSHOT</version>
</dependency>
```


### Building from source

The HiveMQ MQTT Client project uses Gradle to build. A gradle wrapper configuration is included, so after
cloning the repository from github, simply change into the directory containing the project and
execute `./gradlew build`.


## The API flavours

The HiveMQ MQTT Client library offers three distinct API flavours: async, blocking and reactive. The first two are
primarily targeted at application developers, the latter is targeted at people who like the reactive
API or who are looking to integrate the HiveMQ MQTT Client library in a framework or library, as it gives you more control
over the inner workings.

All three API flavours are based around a fluent, builder based API, and differ primarily in the
return types of the various methods. That is, for the async API, you will generally receive a
`CompletableFuture`, in the blocking API you will generally receive the concrete result directly and
with the reactive API you will generally receive a flowable type.


### Async

Use this API if you want to handle the mqtt operations asynchronously, but are not familiar with
reactive or are looking for a slightly simpler API variant than that.

Most calls with return a `CompletableFuture` for the result type, and you can use all of the usual
methods to register callbacks or compose the futures. A simple example is when connecting the
client, you can use the `whenComplete` method to register a callback which gets triggered when the
connection either fails or succeeds:

```java
client.connect()
        .whenComplete((mqtt3ConnAck, throwable) -> {
            if (throwable != null) {
                // handle failure here
            } else {
                // handle success here, e.g. by creating subscriptions
            }
        });
```


### Blocking

This is the simplest API, but as the name suggests, will block execution until the operations has
completed.

Because there is no callback style mechanims with the blocking API, you create a 'publishes'
instance, which has various `receive` methods, which can be used to wait for a message to become
available. You then need to inspect the message to see on which topic it arrived, as the publishes
will receive the messages for all subscriptions.

```java
Mqtt3BlockingClient.Mqtt3Publishes publishes = client.publishes(MqttGlobalPublishFilter.ALL_SUBSCRIPTIONS);
Mqtt3Publish receivedMessage = publishes.receive(5, TimeUnit.SECONDS).orElseThrow(() -> new RuntimeException("No message received."));
```

The above example sets up a `publishes` instance for all messages arriving because of a
subscription, then calls `receive` with a timeout of five seconds, and will also throw an exception
if no message was received (it returns an `Optional` with the message, hence could be empty).


### Reactive

The reactive API is based on `RxJava` and is intended for use by those who want access to the
nitty-gritty inside features of the HiveMQ MQTT Client library. Or if you simply dig the reactive API.

An explanation of how RxJava works or is to be used is outside the purview of this documentation. We
refer you to the [official site](https://github.com/ReactiveX/RxJava) if you are interested in
learning more about RxJava.

One caveat when using this API is being aware of both RxJava and the HiveMQ MQTT Client library having the concept of
'subscribing'. You have to be careful not to confuse subscribing to a stream with subscribing to an
mqtt topic.
