# MQTT Bee

[![Build Status](https://travis-ci.org/mqtt-bee/mqtt-bee.svg?branch=develop)](https://travis-ci.org/mqtt-bee/mqtt-bee)

MQTT 5.0 and 3.1.1 compatible and feature-rich high-performance Java client library with different API flavours and 
backpressure support.

# Status
IMPORTANT: PRE RELEASE STATUS, DO NOT USE IN PRODUCTION YET!

# Features

## Available
 - All MQTT 3.1.1 and MQTT 5.0 features
 - API flavors:
   - Reactive, Async and Blocking
   - Flexible switching
   - Consistent and clearly separated
 - Backpressure support for QoS 1 and 2
 - SSL/TLS
 - WebSocket
 - Automatic and configurable thread management
 - MQTT 5 specific:
   - Pluggable enhanced auth support
   - Additional to MQTT specification: server-triggered reauth
   - Interceptors for QoS flows

## Done soon
 - Automatic reconnect handling and message redelivery
 - Backpressure support for QoS 0

# How to use

## Dependency

### Snapshots

Every time a PR is merged into the `develop` branch, a new snapshot is published.
A snapshot can be included as a normal dependency if the snapshot repository is added to the build file.

#### Gradle
```
repositories {
    jcenter()
    mavenCentral()
    maven { url 'https://oss.jfrog.org/artifactory/oss-snapshot-local' }
}

dependencies {
    compile(group: 'org.mqttbee', name: 'mqtt-bee', version: '1.0.0-SNAPSHOT')
}
```

## General principles

 - API and implementation are clearly separated. All classes inside `internal` packages must not be used directly.
 - The API is mostly fluent and uses fluent builders to create clients, configurations and messages.
 - The API is designed to be consistent:
   - The same principles are used throughout the library.
   - The MQTT 3 and 5 interfaces are as consistent as possible with only version-specific differences.

## Creation of clients

Base classes: `Mqtt3Client`, `Mqtt5Client`

``` Java
Mqtt5Client client = MqttClient.builder()
        .identifier(UUID.randomUUID().toString())
        .serverHost("broker.hivemq.com")
        .useMqttVersion5()
        .build();
Mqtt3Client client = MqttClient.builder()...useMqttVersion3().build();
```
Or if the version is known upfront:
``` Java
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

#### Connect

``` Java
client.connect();
```
Or with customized properties of the CONNECT message:
``` Java
client.connectWith().keepAlice(10).send();
```
Or with pre-built CONNECT message:
``` Java
Mqtt5Connect connectMessage = Mqtt5Connect.builder().keepAlice(10).build();
client.connect(connectMessage);
```

#### Publish

``` Java
client.publishWith()
        .topic("test/topic")
        .qos(MqttQos.AT_LEAST_ONCE)
        .payload("payload".getBytes())
        .send();
```
Or with pre-built PUBLISH message:
``` Java
Mqtt5Publish publishMessage = Mqtt5Publish.builder()
        .topic("test/topic")
        .qos(MqttQos.AT_LEAST_ONCE)
        .payload("payload".getBytes())
        .build();
client.publish(publishMessage);
```

#### Subscribe

``` Java
client.subscribeWith().topicFilter("test/topic").qos(MqttQos.EXACTLY_ONCE).send();
```
Or with pre-built SUBSCRIBE message:
``` Java
Mqtt5Subscribe subscribeMessage = Mqtt5Subscribe.builder()
        .topicFilter("test/topic")
        .qos(MqttQos.EXACTLY_ONCE)
        .build();
client.subscribe(subscribeMessage);
```

#### Unsubscribe

``` Java
client.unsubscribeWith().topicFilter("test/topic").send();
```
Or with pre-built UNSUBSCRIBE message:
``` Java
Mqtt5Unsubscribe unsubscribeMessage = Mqtt5Unsubscribe.builder().topicFilter("test/topic").build();
client.unsubscribe(unsubscribeMessage);
```

#### Consume messages

``` Java
try (Mqtt5BlockingClient.Mqtt5Publishes publishes = client.publishes(MqttGlobalPublishFilter.ALL_PUBLISHES)) {
    Mqtt5Publish publishMessage = publishes.receive();
    // or with timeout
    Optional<Mqtt5Publish> publishMessage = publishes.receive(10, TimeUnit.SECONDS);
    // or without blocking
    Optional<Mqtt5Publish> publishMessage = publishes.receiveNow();
}
```

`publishes` must be called before `subscribe` to ensure all messages no message is lost.
It can be called before `connect` to receive messages of a previous session.

#### Disconnect

``` Java
client.disconnect();
```
Or with customized properties of the DISCONNECT message (only MQTT 5):
``` Java
client.disconnectWith().reasonString("test").send();
```
Or with pre-built DISCONNECT message (only MQTT 5):
``` Java
Mqtt5Disconnect disconnectMessage = Mqtt5Disconnect.builder().disconnectWith().reasonString("test").build();
client.disconnect();
```

#### Reauth (only MQTT 5)

``` Java
client.reauth();
```

### Async API

 - Builder method: `buildAsync()`
 - Switch method: `client.toAsync()`

### Reactive API

 - Builder method: `buildRx()`
 - Switch method: `client.toRx()`

#### Subscribe example

``` Java
final Mqtt3RxClient client = MqttClient.builder()
        .identifier(UUID.randomUUID().toString())
        .serverHost("broker.hivemq.com")
        .serverPort(1883)
        .useMqttVersion3()
        .buildRx();

/*
 * As we use the reactive API, the following line does not connect yet, but return a reactive type.
 * e.g. Single is something like a lazy and reusable future.
 */
final Single<Mqtt3ConnAck> connAckSingle = client.connectWith().keepAlive(10).applyConnect();

/*
 * Same here: the following line does not subscribe yet, but return a reactive type.
 * FlowableWithSingle is a combination of the single SUBACK message and a Flowable of PUBLISH messages.
 * A Flowable is an asynchronous stream, that enables back pressure from the application over MQTT Bee to the broker.
 */
final FlowableWithSingle<Mqtt3Publish, Mqtt3SubAck> subAckAndMatchingPublishes = client.subscribeStreamWith()
        .topicFilter("a/b/c").qos(MqttQos.AT_LEAST_ONCE)
        .addSubscription().topicFilter("a/b/c/d").qos(MqttQos.EXACTLY_ONCE).applySubscription()
        .applySubscribe();

/*
 * The reactive types offer as many operators that will not be covered here.
 * Here we register simple callbacks to print messages when we received the CONNACK, SUBACK and matching PUBLISH messages.
 */
final Single<Mqtt3ConnAck> connectScenario = connAckSingle
        .doOnSuccess(connAck -> System.out.println("Connected with return code " + connAck.getReturnCode()))
        .doOnError(throwable -> System.out.println("Connection failed, " + throwable.getMessage()));

final Flowable<Mqtt3Publish> subscribeScenario = subAckAndMatchingPublishes
        .doOnSingle(subAck -> System.out.println("Subscribed with return codes " + subAck.getReturnCodes()))
        .doOnNext(publish ->
                System.out.println("Received publish" +
                        ", topic: " + publish.getTopic() +
                        ", QoS: " + publish.getQos() +
                        ", payload: " + new String(publish.getPayloadAsBytes())
                )
        );

/*
 * Reactive types let us compose a sequence of actions
 */
final Flowable<Mqtt3Publish> connectAndSubscribe = connectScenario.ignoreElement().andThen(subscribeScenario);

/*
 * By subscribing to reactive types, the sequence is executed
 */
connectAndSubscribe.blockingSubscribe();
```

#### Publish example

``` Java
final Mqtt3RxClient client = MqttClient.builder()
        .identifier(UUID.randomUUID().toString())
        .serverHost("broker.hivemq.com")
        .serverPort(1883)
        .useMqttVersion3()
        .buildRx();

final Single<Mqtt3ConnAck> connAckSingle = client.connectWith().keepAlive(10).applyConnect();

/*
 * Fake a stream of PUBLISH messages to publish with incrementing ids
 */
final AtomicInteger counter = new AtomicInteger();
Flowable<Mqtt3Publish> messagesToPublish = Flowable.generate(emitter -> {
    final int id = counter.incrementAndGet();
    final Mqtt3Publish publishMessage = Mqtt3Publish.builder()
            .topic("a/b/c").qos(MqttQos.AT_LEAST_ONCE).payload(("test " + id).getBytes()).build();
    emitter.onNext(publishMessage);
});
/*
 * Emit 1 message only every 100 milliseconds
 */
messagesToPublish = messagesToPublish.zipWith(Flowable.interval(100, TimeUnit.MILLISECONDS), (publish, aLong) -> publish);

final Single<Mqtt3ConnAck> connectScenario = connAckSingle
        .doOnSuccess(connAck -> System.out.println("Connected with return code " + connAck.getReturnCode()))
        .doOnError(throwable -> System.out.println("Connection failed, " + throwable.getMessage()));

final Flowable<Mqtt3PublishResult> publishScenario = client.publish(messagesToPublish)
        .doOnNext(publishResult -> System.out.println("Publish acknowledged: " + new String(publishResult.getPublish().getPayloadAsBytes())));

connectScenario.toCompletable().andThen(publishScenario).blockingSubscribe();
```

## How to build

JDK 8+ is required.

## How to contribute

### Branching model

 - `master`: release branch
 - `develop`: snapshot branch, branch where features for the next release are merged into
 - Every feature or bugfix will have its own branch, branched from develop, merged back into develop after the code 
review of the pull request

### Branching guidelines

 - Branch types: feature, bugfix, improvement, cleanup (same as the label of a corresponding GitHub Issue)
 - Branch names:
   - Starting with type: `feature/`, `bugfix/`, `improvement/`, `cleanup/`
   - \+ task: lower case, spaces replaced with `-`

### Commit message guidelines

 - Commits should be as atomic as possible.
 - Commits do not have to follow strict guidelines, but they should describe the changes clearly.

## Code style guidelines

 - Please import the code style settings found in the codeStyle folder.
 - The project uses Nullability annotations to avoid NullPointerExceptions: `@NotNull`, `@Nullable`,
every non-primitive parameter/return type/field should be annotated with one of them.
