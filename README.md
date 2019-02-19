# MQTT Bee

[![Build Status](https://travis-ci.org/mqtt-bee/mqtt-bee.svg?branch=develop)](https://travis-ci.org/mqtt-bee/mqtt-bee)

MQTT 5.0 and 3.1.1 compatible and feature-rich high-performance Java client library with different API flavours and 
backpressure support.

# Documentation

A detailed documentation can be found  [here](https://mqtt-bee.github.io)

# Features

## Available

 - All MQTT 3.1.1 and MQTT 5.0 features
 - API flavors:
   - Reactive, Async and Blocking
   - Flexible switching
   - Consistent and clearly separated
 - Backpressure support:
   - QoS 1 and 2
   - QoS 0 (dropping incoming messages if necessary)
 - SSL/TLS
 - WebSocket
 - Automatic and configurable thread management
 - MQTT 5 specific:
   - Pluggable Enhanced Auth support
     - Additional to MQTT specification: server-triggered reauth
   - Automatic Topic Alias mapping
   - Interceptors for QoS flows

## Done soon

 - Automatic reconnect handling and message redelivery

# How to use

Java 8 or higher is required.

## Dependency

### Gradle

If you use Gradle, just include the following inside your `build.gradle` file.
```groovy
dependencies {
    compile group: 'org.mqttbee', name: 'mqtt-bee', version: '1.0.0'
}
```

### Maven

If you use Maven, just include the following inside your `pom.xml` file.
NOTE: You have to set the compiler version to `1.8` or higher.
```xml
<project>
    ...
    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.mqttbee</groupId>
            <artifactId>mqtt-bee</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
    ...
</project>
```

### Shaded version

If you are experiencing problems with transitive dependencies, you can try the shaded version.
This version packs the transitive dependencies which are only used internal under a different package name.
To use the shaded version just append `-shaded` to the artifact name.

#### Gradle

```groovy
dependencies {
    compile group: 'org.mqttbee', name: 'mqtt-bee-shaded', version: '1.0.0'
}
```

#### Maven

```xml
<project>
    ...
    <dependencies>
        <dependency>
            <groupId>org.mqttbee</groupId>
            <artifactId>mqtt-bee-shaded</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
    ...
</project>
```

### Snapshots

Every time a PR is merged into the `develop` branch, a new snapshot is published.
A snapshot can be included as a normal dependency if the snapshot repository is added to the build file.

#### Gradle

```groovy
repositories {
    jcenter()
    mavenCentral()
    maven { url 'https://oss.jfrog.org/artifactory/oss-snapshot-local' }
}

dependencies {
    compile group: 'org.mqttbee', name: 'mqtt-bee', version: '1.0.0-SNAPSHOT'
}
```

#### Maven

If you use Maven, you have to set the compiler version to `1.8` or higher.
```xml
<project>
    ...
    <repositories>
        <repository>
            <id>oss.jfrog.org</id>
            <url>https://oss.jfrog.org/artifactory/oss-snapshot-local</url>
        </repository>
    </repositories>
    
    <dependencies>
        <dependency>
            <groupId>org.mqttbee</groupId>
            <artifactId>mqtt-bee</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
    ...
</project>
```

## General principles

 - API and implementation are clearly separated. All classes inside `internal` packages must not be used directly.
 - The API is mostly fluent and uses fluent builders to create clients, configurations and messages.
 - The API is designed to be consistent:
   - The same principles are used throughout the library.
   - The MQTT 3 and 5 interfaces are as consistent as possible with only version-specific differences.

## Creation of clients

Base classes: `Mqtt3Client`, `Mqtt5Client`

```java
Mqtt5Client client = MqttClient.builder()
        .identifier(UUID.randomUUID().toString())
        .serverHost("broker.hivemq.com")
        .useMqttVersion5()
        .build();
Mqtt3Client client = MqttClient.builder()...useMqttVersion3().build();
```
Or if the version is known upfront:
```java
Mqtt5Client client = Mqtt5Client.builder()...build();
Mqtt3Client client = Mqtt3Client.builder()...build();
```

For each API style exists a specific `build...()` method.

## API flavours

Each API style has its own interface to separate them clearly.
At any time it is possible to switch the API style.

### Blocking API

 - Builder method: `buildBlocking()`
 - Switch method: `client.toBlocking()`

#### Examples

##### Subscribe example

```java
final Mqtt5BlockingClient client = Mqtt5Client.builder()
        .identifier(UUID.randomUUID().toString())
        .serverHost("broker.hivemq.com")
        .buildBlocking();

client.connect();

try (final Mqtt5Publishes publishes = client.publishes(MqttGlobalPublishFilter.ALL)) {

    client.subscribeWith().topicFilter("test/topic").qos(MqttQos.AT_LEAST_ONCE).send();

    publishes.receive(1, TimeUnit.SECONDS).ifPresent(System.out::println);
    publishes.receive(100, TimeUnit.MILLISECONDS).ifPresent(System.out::println);

} finally {
    client.disconnect();
}
```

##### Publish example

```java
Mqtt5BlockingClient client = Mqtt5Client.builder()
        .identifier(UUID.randomUUID().toString())
        .serverHost("broker.hivemq.com")
        .buildBlocking();

client.connect();
client.publishWith().topic("test/topic").qos(MqttQos.AT_LEAST_ONCE).payload("1".getBytes()).send();
client.disconnect();
```

#### Connect

```java
client.connect();
```
Or with customized properties of the Connect message:
```java
client.connectWith().keepAlive(10).send();
```
Or with pre-built Connect message:
```java
Mqtt5Connect connectMessage = Mqtt5Connect.builder().keepAlive(10).build();
client.connect(connectMessage);
```

#### Publish

```java
client.publishWith()
        .topic("test/topic")
        .qos(MqttQos.AT_LEAST_ONCE)
        .payload("payload".getBytes())
        .send();
```
Or with pre-built Publish message:
```java
Mqtt5Publish publishMessage = Mqtt5Publish.builder()
        .topic("test/topic")
        .qos(MqttQos.AT_LEAST_ONCE)
        .payload("payload".getBytes())
        .build();
client.publish(publishMessage);
```

#### Subscribe

```java
client.subscribeWith().topicFilter("test/topic").qos(MqttQos.EXACTLY_ONCE).send();
```
Or with pre-built Subscribe message:
```java
Mqtt5Subscribe subscribeMessage = Mqtt5Subscribe.builder()
        .topicFilter("test/topic")
        .qos(MqttQos.EXACTLY_ONCE)
        .build();
client.subscribe(subscribeMessage);
```

#### Unsubscribe

```java
client.unsubscribeWith().topicFilter("test/topic").send();
```
Or with pre-built Unsubscribe message:
```java
Mqtt5Unsubscribe unsubscribeMessage = Mqtt5Unsubscribe.builder().topicFilter("test/topic").build();
client.unsubscribe(unsubscribeMessage);
```

#### Consume messages

```java
try (Mqtt5BlockingClient.Mqtt5Publishes publishes = client.publishes(MqttGlobalPublishFilter.ALL)) {
    Mqtt5Publish publishMessage = publishes.receive();
    // or with timeout
    Optional<Mqtt5Publish> publishMessage = publishes.receive(10, TimeUnit.SECONDS);
    // or without blocking
    Optional<Mqtt5Publish> publishMessage = publishes.receiveNow();
}
```

`publishes` must be called before `subscribe` to ensure no message is lost.
It can be called before `connect` to receive messages of a previous session.

#### Disconnect

```java
client.disconnect();
```
Or with customized properties of the DISCONNECT message (only MQTT 5):
```java
client.disconnectWith().reasonString("test").send();
```
Or with pre-built Disconnect message (only MQTT 5):
```java
Mqtt5Disconnect disconnectMessage = Mqtt5Disconnect.builder().disconnectWith().reasonString("test").build();
client.disconnect(disconnectMessage);
```

#### Reauth (only MQTT 5)

```java
client.reauth();
```

### Async API

 - Builder method: `buildAsync()`
 - Switch method: `client.toAsync()`

#### Examples

##### Subscribe example

```java
Mqtt5BlockingClient client = Mqtt5Client.builder()
        .identifier(UUID.randomUUID().toString())
        .serverHost("broker.hivemq.com")
        .buildBlocking();

client.connect();

client.toAsync().subscribeWith()
        .topicFilter("test/topic")
        .qos(MqttQos.AT_LEAST_ONCE)
        .callback(System.out::println)
        .send();
```

##### Publish example

```java
Mqtt5AsyncClient client = Mqtt5Client.builder()
        .identifier(UUID.randomUUID().toString())
        .serverHost("broker.hivemq.com")
        .buildAsync();

client.connect()
        .thenCompose(connAck -> client.publishWith().topic("test/topic").payload("1".getBytes()).send())
        .thenCompose(publish -> client.disconnect());
```

#### Connect

Method calls are analog to the Blocking API but return `CompletableFuture`.

#### Publish

Method calls are analog to the Blocking API but return `CompletableFuture`.

#### Subscribe

Method calls are analog to the Blocking API but return `CompletableFuture`.

Additionally messages can be consumed per subscribe:
```java
client.subscribeWith()
        .topicFilter("test/topic")
        .qos(MqttQos.EXACTLY_ONCE)
        .callback(System.out::println)
        .executor(executor) // optional
        .send();
```
Or with pre-built Subscribe message:
```java
Mqtt5Subscribe subscribeMessage = Mqtt5Subscribe.builder()
        .topicFilter("test/topic")
        .qos(MqttQos.EXACTLY_ONCE)
        .build();
client.subscribe(subscribeMessage, System.out::println);
client.subscribe(subscribeMessage, System.out::println, executor);
```

#### Unsubscribe

Method calls are analog to the Blocking API but return `CompletableFuture`.

#### Consume messages

Messages can either be consumed per subscribe (described above) or globally:

```java
client.publishes(MqttGlobalPublishFilter.ALL, System.out::println);
```
Or with executing the callback on a specified executor:
```java
client.publishes(MqttGlobalPublishFilter.ALL, System.out::println, executor);
```

`publishes` must be called before `subscribe` to ensure no message is lost.
It can be called before `connect` to receive messages of a previous session.

#### Disconnect

Method calls are analog to the Blocking API but return `CompletableFuture`.

#### Reauth (only MQTT 5)

Method calls are analog to the Blocking API but return `CompletableFuture`.

### Reactive API

 - Builder method: `buildRx()`
 - Switch method: `client.toRx()`

#### Examples

##### Subscribe example

```java
Mqtt5RxClient client = Mqtt5Client.builder()
        .identifier(UUID.randomUUID().toString())
        .serverHost("broker.hivemq.com")
        .buildRx();

// As we use the reactive API, the following line does not connect yet, but returns a reactive type.
// e.g. Single is something like a lazy and reusable future. Think of it as a source for the ConnAck message.
Single<Mqtt5ConnAck> connAckSingle = client.connect();

// Same here: the following line does not subscribe yet, but returns a reactive type.
// FlowableWithSingle is a combination of the single SubAck message and a Flowable of Publish messages.
// A Flowable is an asynchronous stream, that enables back pressure from the application over MQTT Bee to the broker.
FlowableWithSingle<Mqtt5Publish, Mqtt5SubAck> subAckAndMatchingPublishes = client.subscribeStreamWith()
        .topicFilter("a/b/c").qos(MqttQos.AT_LEAST_ONCE)
        .addSubscription().topicFilter("a/b/c/d").qos(MqttQos.EXACTLY_ONCE).applySubscription()
        .applySubscribe();

// The reactive types offer many operators that will not be covered here.
// Here we register callbacks to print messages when we received the CONNACK, SUBACK and matching PUBLISH messages.
Completable connectScenario = connAckSingle
        .doOnSuccess(connAck -> System.out.println("Connected, " + connAck.getReasonCode()))
        .doOnError(throwable -> System.out.println("Connection failed, " + throwable.getMessage()))
        .ignoreElement();

Completable subscribeScenario = subAckAndMatchingPublishes
        .doOnSingle(subAck -> System.out.println("Subscribed, " + subAck.getReasonCodes()))
        .doOnNext(publish -> System.out.println(
                "Received publish" + ", topic: " + publish.getTopic() + ", QoS: " + publish.getQos() +
                        ", payload: " + new String(publish.getPayloadAsBytes())))
        .ignoreElements();

// Reactive types can be easily and flexibly combined
connectScenario.andThen(subscribeScenario).blockingAwait();
```

##### Publish example

```java
Mqtt5RxClient client = Mqtt5Client.builder()
        .identifier(UUID.randomUUID().toString())
        .serverHost("broker.hivemq.com")
        .buildRx();

// As we use the reactive API, the following line does not connect yet, but returns a reactive type.
Completable connectScenario = client.connect()
        .doOnSuccess(connAck -> System.out.println("Connected, " + connAck.getReasonCode()))
        .doOnError(throwable -> System.out.println("Connection failed, " + throwable.getMessage()))
        .ignoreElement();

// Fake a stream of Publish messages with an incrementing number in the payload
Flowable<Mqtt5Publish> messagesToPublish = Flowable.range(0, 10_000)
        .map(i -> Mqtt5Publish.builder()
                .topic("a/b/c")
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload(("test " + i).getBytes())
                .build())
        // Emit 1 message only every 100 milliseconds
        .zipWith(Flowable.interval(100, TimeUnit.MILLISECONDS), (publish, i) -> publish);

// As we use the reactive API, the following line does not publish yet, but returns a reactive type.
Completable publishScenario = client.publish(messagesToPublish)
        .doOnNext(publishResult -> System.out.println(
                "Publish acknowledged: " + new String(publishResult.getPublish().getPayloadAsBytes())))
        .ignoreElements();

// As we use the reactive API, the following line does not disconnect yet, but returns a reactive type.
Completable disconnectScenario = client.disconnect().doOnComplete(() -> System.out.println("Disconnected"));

// Reactive types can be easily and flexibly combined
connectScenario.andThen(publishScenario).andThen(disconnectScenario).blockingAwait();
```

# How to contribute

## Branching model

 - `master`: release branch
 - `develop`: snapshot branch, branch where features for the next release are merged into
 - Every feature or bugfix will have its own branch, branched from develop, merged back into develop after the code 
review of the pull request

## Branching guidelines

 - Branch types: feature, bugfix, improvement, cleanup (same as the label of a corresponding GitHub Issue)
 - Branch names:
   - Starting with type: `feature/`, `bugfix/`, `improvement/`, `cleanup/`
   - \+ task: lower case, spaces replaced with `-`

## Commit message guidelines

 - Commits should be as atomic as possible.
 - Commits do not have to follow strict guidelines, but they should describe the changes clearly.

## Code style guidelines

 - Please import the code style settings found in the codeStyle folder.
 - The project uses Nullability annotations to avoid NullPointerExceptions: `@NotNull`, `@Nullable`,
every non-primitive parameter/return type/field should be annotated with one of them.
