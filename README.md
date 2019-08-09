# HiveMQ MQTT Client

<img src="https://www.hivemq.com/img/svg/hivemq-mqtt-client.svg" width="500">

[![Build Status](https://travis-ci.org/hivemq/hivemq-mqtt-client.svg?branch=develop)](https://travis-ci.org/hivemq/hivemq-mqtt-client)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.hivemq/hivemq-mqtt-client/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.hivemq/hivemq-mqtt-client)
[![JitPack](https://jitpack.io/v/hivemq/hivemq-mqtt-client.svg)](https://jitpack.io/#hivemq/hivemq-mqtt-client)

MQTT 5.0 and 3.1.1 compatible and feature-rich high-performance Java client library with different API flavours and 
backpressure support.

## Documentation

A detailed documentation can be found [here](https://hivemq.github.io/hivemq-mqtt-client)

## Features

- All MQTT 3.1.1 and MQTT 5.0 features
- API flavors:
  - Reactive, Async and Blocking
  - Flexible switching
  - Consistent and clearly separated
- Backpressure support:
  - QoS 1 and 2
  - QoS 0 (dropping incoming messages, if necessary)
- Transports:
  - TCP
  - SSL/TLS
  - WebSocket, Secure WebSocket
- Automatic and configurable thread management
- Automatic and configurable reconnect handling and message redelivery
- Lifecycle listeners (connected, disconnected)
- MQTT 5 specific:
  - Pluggable Enhanced Auth support (additional to MQTT specification: server-triggered reauth)
  - Automatic Topic Alias mapping
  - Interceptors for QoS flows

## How to use

Java 8 or higher is required.

### Dependency

#### Gradle

If you use Gradle, just include the following inside your `build.gradle` file.

```groovy
dependencies {
    compile group: 'com.hivemq', name: 'hivemq-mqtt-client', version: '1.1.2'
}
```

#### Maven

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
            <groupId>com.hivemq</groupId>
            <artifactId>hivemq-mqtt-client</artifactId>
            <version>1.1.2</version>
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
    compile group: 'com.hivemq', name: 'hivemq-mqtt-client-shaded', version: '1.1.2'
}
```

#### Maven

```xml
<project>
    ...
    <dependencies>
        <dependency>
            <groupId>com.hivemq</groupId>
            <artifactId>hivemq-mqtt-client-shaded</artifactId>
            <version>1.1.2</version>
        </dependency>
    </dependencies>
    ...
</project>
```

### Snapshots

Snapshots can be obtained using [JitPack](https://jitpack.io/#hivemq/hivemq-mqtt-client).

#### Gradle

```groovy
repositories {
    ...
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.hivemq.hivemq-mqtt-client:hivemq-mqtt-client:develop-SNAPSHOT'
}
```

#### Maven

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

Change the artifact name to `hivemq-mqtt-client-shaded` to get snapshots of the shaded version.

JitPack works for all branches and also specific commits by specifying in the version.

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
Mqtt5Disconnect disconnectMessage = Mqtt5Disconnect.builder().reasonString("test").build();
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
        .thenCompose(publishResult -> client.disconnect());
```

#### Connect

`connect()`, `connectWith()` and `connect(Mqtt3/5Connect)` method calls are analog to the Blocking API but return
`CompletableFuture`.

#### Publish

`publishWith()` and `publish(Mqtt3/5Publish)` method calls are analog to the Blocking API but return
`CompletableFuture`.

#### Subscribe

`subscribeWith()` and `subscribe(Mqtt3/5Subscribe)` method calls are analog to the Blocking API but return
`CompletableFuture`.

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

`unsubscribeWith()` and `unsubscribe(Mqtt3/5Unsubscribe)` method calls are analog to the Blocking API but return
`CompletableFuture`.

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

`disconnect()`, `disconnectWith()` and `disconnect(Mqtt5Disconnect)` method calls are analog to the Blocking API but
return `CompletableFuture`.

#### Reauth (only MQTT 5)

`reauth()` method call is analog to the Blocking API but returns `CompletableFuture`.

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
// A Flowable is an asynchronous stream that enables backpressure from the application over the client to the broker.
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

#### Connect

`connect()`, `connectWith()` and `connect(Mqtt3/5Connect)` method calls are analog to the Async and Blocking API but
return `Single<ConnAck>`.

#### Publish

`publish` takes a reactive stream of Publish messages (`Flowable`) and returns a reactive stream of Publish results
(`Flowable`).

The Reactive API is usually not used for publishing single messages.
Nevertheless it is possible with the following code.

```java
Single<Mqtt5PublishResult> result =
        client.publish(Flowable.just(Mqtt5Publish.builder()
                .topic("test/topic")
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload("payload".getBytes())
                .build())).singleOrError();

```

#### Subscribe

`subscribeWith()` and `subscribe(Mqtt3/5Subscribe)` method calls are analog to the Async and Blocking API but return
`Single<SubAck>`.

Additionally messages can be consumed per subscribe:
```java
Flowable<Mqtt5Publish> result =
        client.subscribeStreamWith()
                .topicFilter("test/topic")
                .qos(MqttQos.EXACTLY_ONCE)
                .applySubscribe()
                .doOnSingle(subAck -> System.out.println("subscribed"))
                .doOnNext(publish -> System.out.println("received publish"));
```
Or with pre-built Subscribe message:
```java
Mqtt5Subscribe subscribeMessage = Mqtt5Subscribe.builder()
        .topicFilter("test/topic")
        .qos(MqttQos.EXACTLY_ONCE)
        .build();
Flowable<Mqtt5Publish> result =
        client.subscribeStreamWith(subscribeMessage)
                .doOnSingle(subAck -> System.out.println("subscribed"))
                .doOnNext(publish -> System.out.println("received publish"));
```

#### Unsubscribe

`unsubscribeWith()` and `unsubscribe(Mqtt3/5Unsubscribe)` method calls are analog to the Async and Blocking API but
return `Single<UnsubAck>`.

#### Consume messages

Messages can either be consumed per subscribe (described above) or globally:

```java
Flowable<Mqtt5Publish> result =
        client.publishes(MqttGlobalPublishFilter.ALL).doOnNext(System.out::println);
```

`publishes` must be called before `subscribe` to ensure no message is lost.
It can be called before `connect` to receive messages of a previous session.

#### Disconnect

`disconnect()`, `disconnectWith()` and `disconnect(Mqtt5Disconnect)` method calls are analog to the Async and Blocking
API but return `Completable`.

#### Reauth (only MQTT 5)

`reauth()` method call is analog to the Async and Blocking API but returns `Completable`.

## Versioning

[Semantic Versioning](https://semver.org/) is used.

All code inside `com.hivemq.client.internal` packages must not be used directly. It can change at any time and is not
part of the public API.

Interfaces annotated with `DoNotImplement` must not be implemented. The implementation is provided by the library.
This allows the library to later add methods to the interface without breaking backwards compatibility with implementing
classes.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md)

## License

See [LICENSE](LICENSE)
