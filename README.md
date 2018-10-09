# MQTT Bee

[![Build Status](https://travis-ci.org/mqtt-bee/mqtt-bee.svg?branch=develop)](https://travis-ci.org/mqtt-bee/mqtt-bee)

MQTT 5.0 and 3.1.1 compatible client library with a reactive API and back pressure support.

# Status
IMPORTANT: PRE RELEASE STATUS, DO NOT USE IN PRODUCTION YET!

## DONE
 - MQTT 3.1.1 and MQTT 5.0 functionality
 - Reactive API
 - Backpressure support for QoS 1 and 2
 - SSL
 - WebSocket
 - Additional MQTT 5 features: Server-Reauth

## TODO
 - Backpressure support for QoS 0
 - Reconnect Handling and Message redelivery
 - Simpler APIs

# How to use

## Add MQTT Bee to your project with Gradle
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

## Rx API

### Subscribe

``` Java
final Mqtt3Client client = MqttClient.builder()
        .identifier(UUID.randomUUID().toString())
        .serverHost("broker.hivemq.com")
        .serverPort(1883)
        .useMqttVersion3()
        .buildReactive();

/*
 * As we use the reactive API, the following line does not connect yet, but return a reactive type.
 * e.g. Single is something like a lazy and reusable future.
 */
final Single<Mqtt3ConnAck> connAckSingle = client.connect().keepAlive(10, TimeUnit.SECONDS).done();

/*
 * Same here: the following line does not subscribe yet, but return a reactive type.
 * FlowableWithSingle is a combination of the single SUBACK message and a Flowable of PUBLISH messages.
 * A Flowable is an asynchronous stream, that enables back pressure from the application over MQTT Bee to the broker.
 */
final FlowableWithSingle<Mqtt3SubAck, Mqtt3Publish> subAckAndMatchingPublishes = client.subscribeWithStream()
        .addSubscription().topicFilter("a/b/c").qos(MqttQos.AT_LEAST_ONCE).done()
        .addSubscription().topicFilter("a/b/c/d").qos(MqttQos.EXACTLY_ONCE).done()
        .done();

/*
 * The reactive types offer as many operators that will not be covered here.
 * Here we register simple callbacks to print messages when we received the CONNACK, SUBACK and matching PUBLISH messages.
 */
final Single<Mqtt3ConnAck> connectScenario = connAckSingle
        .doOnSuccess(connAck -> System.out.println("Connected with return code " + connAck.getReturnCode()))
        .doOnError(throwable -> System.out.println("Connection failed, " + throwable.getMessage()));

final Flowable<Mqtt3Publish> subscribeScenario = subAckAndMatchingPublishes
        .doOnSingle((subAck, subscription) ->
                System.out.println("Subscribed with return codes " + subAck.getReturnCodes()))
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
final Flowable<Mqtt3Publish> connectAndSubscribe = connectScenario.toCompletable().andThen(subscribeScenario);

/*
 * By subscribing to reactive types, the sequence is executed
 */
connectAndSubscribe.blockingSubscribe();
```

### Publish

``` Java
final Mqtt3Client client = MqttClient.builder()
        .identifier(UUID.randomUUID().toString())
        .serverHost("broker.hivemq.com")
        .serverPort(1883)
        .useMqttVersion3()
        .buildReactive();

final Single<Mqtt3ConnAck> connAckSingle = client.connect().keepAlive(10, TimeUnit.SECONDS).done();

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

## Simple API

The Simple API is not yet available

## How to build

JDK 8 is required.

## Branching model

* master: the release branch
* develop: where features are merged into
* every feature or bugfix will have its own branch, branched from develop,
it will be merged after the code review of the pull request

### Branching guidelines

* branch types: feature, bugfix
* branch names: starting with feature/, bugfix/ + lower case name of the task, spaces replaced with -
* also branches from a feature branch are called feature/ or bugfix/
whether it contributes to the feature as a sub-feature or a bugfix

### Commit message guidelines

* Commits in feature/bugfix branches should be as atomic as possible.
* Commits in feature/bugfix branches do not have to follow strict guidelines,
but they should describe the changes clearly.
* When feature/bugfix branches are merged into develop, the commit message should follow these guidelines: tbd

## Code style guidelines

* Please import the code style settings found in the codeStyle folder.
* The project uses its own @NotNull and @Nullable annotations,
every non-primitive parameter should be annotated with one of them.
* Optional will only be used in the public API.
