---
layout: default
title: Quick Start
nav_order: 1
search_exclude: true
---

# Quick Start

The following contains all the steps necessary to integrate the HiveMQ MQTT Client library into a project, connect to a
broker and then subscribe to a topic and publish messages to a topic using the MQTT 3 asynchronous
API flavour of the HiveMQ MQTT Client library.

To see more examples, including usage of the other API flavours, see the examples project repository
here: [https://github.com/mqtt-bee/mqtt-bee-examples](https://github.com/mqtt-bee/mqtt-bee-examples)

For a more detailed description of how to use the client, its API flavours and the more powerful
features, see the [User Guide](user_guide.md).


## Adding the HiveMQ MQTT Client library to your project

In order to include the HiveMQ MQTT Client library in your gradle project, add the following dependency:

```groovy
dependencies {
    compile 'com.hivemq:hivemq-mqtt-client:{{ site.version }}'
}
```

In order to get the nightly snapshots, include `compile 'com.hivemq-hivemq-mqtt-client:{{ site.version }}-SNAPSHOT'`
instead and also add the following repository:

```groovy
repositories {
    maven { url 'https://oss.jfrog.org/artifactory/oss-snapshot-local' }
}
```

Similarly, for Maven, include the following dependency:

```xml
<dependencies>
    <dependency>
        <groupId>com.hivemq</groupId>
        <artifactId>hivemq-mqtt-client</artifactId>
        <version>{{ site.version }}</version>
    </dependency>
</dependencies>
```

For the nightly snapshot change the version to `<version>{{ site.version }}-SNAPSHOT</version>` and also setup
the following repository:

```xml
<repositories>
    <repository>
        <id>jfrog-snapshots</id>
        <name>jfrog snapshots</name>
        <url>https://oss.jfrog.org/artifactory/oss-snapshot-local</url>
    </repository>
</repositories>
```

## Creating the client

Using the builder accessible from the `com.hivemq.client.mqtt.MqttClient` class you are able to
configure and create an mqtt client, which you can then use to connect to the broker, subscribe to
topics and publish messages. The following example shows connecting to a local mqtt broker using SSL
and the v3 mqtt protocol:

```java
Mqtt3AsyncClient client = MqttClient.builder()
        .useMqttVersion3()
        .identifier("my-mqtt-client-id")
        .serverHost("localhost")
        .serverPort(1883)
        .useSslWithDefaultConfig()
        .buildAsync();
```

Similarly you can build mqtt v5 clients, RxJava API clients, blocking clients, etc. Check out the
JavaDoc for all available features.

## Connecting to the broker

Once you have created your client, the next step is to actually connect to the broker. The following
example shows connecting using a username and password:

```java
client.connectWith()
        .simpleAuth()
            .username("my-user")
            .password("my-password".getBytes())
            .applySimpleAuth()
        .send()
        .whenComplete((mqtt3ConnAck, throwable) -> {
            if (throwable != null) {
                // handle failure
            } else {
                // setup subscribes or start publishing
            }
        });
```

## Subscribing to a topic

Given a client which has successfully connected to a broker, you can setup your subscriptions:

```java
client.subscribeWith()
        .topicFilter("the/topic")
        .callback(mqtt3Publish -> {
            // Process the received message
        })
        .send()
        .whenComplete((mqtt3SubAck, throwable) -> {
            if (throwable != null) {
                // Handle failure to subscribe
            } else {
                // Handle successful subscription, e.g. logging or incrementing a metric
            }
        });
```

## Publishing to a topic

Similar to subscribing, once you have a connected client, you can also publish messages to a topic:

```java
client.publishWith()
        .topic("the/topic")
        .payload(myPayload)
        .send()
        .whenComplete((mqtt3Publish, throwable) -> {
            if (throwable != null) {
                // handle failure to publish
            } else {
                // handle successful publish, e.g. logging or incrementing a metric
            }
        });
```

## Disconnecting the client

Once you are finished using the client, for example you've sent all the messages you wanted to send
or the application is shutting down, you simply call `disconnect()` on the client. As with the other
messages, you will receive a completable future where you can react to success or failure of the
disconnect.
