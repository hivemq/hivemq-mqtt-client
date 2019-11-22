---
layout: default
title: Publish
parent: MQTT Operations
nav_order: 2
---

# Publish

Messages are published with a topic.
The MQTT broker needs the topic to route the message to subscribers.
Hence the topic is mandatory for a Publish message (it is the only required property).

{% capture tab_content %}

MQTT 5.0
===

 {% capture tab_content %}

 Blocking
 ===

The blocking API directly returns a `Mqtt5PublishResult` if publishing was successful.

```java
Mqtt5PublishResult publishResult = client.publishWith().topic("test/topic").send();
```

 ====

 Async
 ===

The asynchronous API returns a `CompletableFuture` which completes with a `Mqtt5PublishResult` message if publishing 
was successful.

```java
CompletableFuture<Mqtt5PublishResult> publishResultFuture = 
        client.publishWith().topic("test/topic").send();
```

 ====

 Reactive
 ===

The reactive API does not publish a single message, but an asynchronous stream of messages: a `Flowable<Mqtt5Publish>`.
Hence it also returns an asynchronous stream of results: `Flowable<Mqtt5PublishResult>`.
Each published message will cause a result in the returned stream.
As the `Flowable` is a reactive type, the following line does not publish immediately but only after you subscribe to it 
(in terms of Reactive Streams).

```java
Flowable<Mqtt5PublishResult> publishResultFlowable = client.publish(
        Flowable.range(0, 100).map(i -> Mqtt5Publish.builder().topic("test/topic" + i).build()));
```

 {% endcapture %}{% include tabs.html tab_group="api-flavour" %}

Depending on the Quality of Service (QoS) of the Publish message the result can be:

| `Mqtt5PublishResult` | QoS 0 | returned when the message is written to the transport |
| `Mqtt5Qos1Result`    | QoS 1 | returned when the message is acknowledged, contains the `Mqtt5PubAck` message |
| `Mqtt5Qos2Result`    | QoS 2 | returned when the message is acknowledged, contains the `Mqtt5PubRec` message |

 {% capture tab_content %}

 Blocking
 ===

If publishing was not successful, it throws:

| `Mqtt5PubAckException`        | for QoS 1 if the PubAck message contained an error code (the PubAck message is contained in the exception) |
| `Mqtt5PubRecException`        | for QoS 2 if the PubRec message contained an error code (the PubRec message is contained in the exception) |
| `ConnectionClosedException`   | for QoS 0 if the connection was closed during writing the message to the transport |
| `MqttSessionExpiredException` | if the session expired before the message has been acknowledged completely |
| `MqttEncodeException`         | if the maximum packet size was exceeded |
| `MqttClientStateException`    | if the client is not connected and also not reconnecting |

 ====

 Async
 ===

If publishing was not successful, the `CompletableFuture` completes exceptionally with:

| `Mqtt5PubAckException`        | for QoS 1 if the PubAck message contained an error code (the PubAck message is contained in the exception) |
| `Mqtt5PubRecException`        | for QoS 2 if the PubRec message contained an error code (the PubRec message is contained in the exception) |
| `ConnectionClosedException`   | for QoS 0 if the connection was closed during writing the message to the transport |
| `MqttSessionExpiredException` | if the session expired before the message has been acknowledged completely |
| `MqttEncodeException`         | if the maximum packet size was exceeded |
| `MqttClientStateException`    | if the client is not connected and also not reconnecting |

 ====

 Reactive
 ===

If publishing a single message was not successful, the corresponding publish result contains an error:

| `Mqtt5PubAckException`        | for QoS 1 if the PubAck message contained an error code (the PubAck message is contained in the exception) |
| `Mqtt5PubRecException`        | for QoS 2 if the PubRec message contained an error code (the PubRec message is contained in the exception) |
| `ConnectionClosedException`   | for QoS 0 if the connection was closed during writing the message to the transport |
| `MqttSessionExpiredException` | if the session expired before the message has been acknowledged completely |
| `MqttEncodeException`         | if the maximum packet size was exceeded |

The result stream will always emit a publish result for every message in the input stream.
This means that the result stream only errors completely if the provided message stream errors itself and after all 
publish results have been received.
Similarly, the result stream only completes normally if the provided message stream completes normally itself and after 
all publish results have been received.

 {% endcapture %}{% include tabs.html tab_group="api-flavour" tab_no_header=true %}

====


MQTT 3.1.1
===

 {% capture tab_content %}

 Blocking
 ===

The blocking API returns nothing if publishing was successful.

```java
client.publishWith().topic("test/topic").send();
```

If publishing was not successful, it throws:

| `ConnectionClosedException`   | for QoS 0 if the connection was closed during writing the message to the transport |
| `MqttSessionExpiredException` | if the session expired before the message has been acknowledged completely |
| `MqttEncodeException`         | if the maximum packet size was exceeded |
| `MqttClientStateException`    | if the client is not connected and also not reconnecting |

 ====

 Async
 ===

The asynchronous API returns a `CompletableFuture` which completes with the `Mqtt3Publish` message (as context) if 
publishing was successful.

```java
CompletableFuture<Mqtt3Publish> publishResultFuture = 
        client.publishWith().topic("test/topic").send();
```

If publishing was not successful, the `CompletableFuture` completes exceptionally with:

| `ConnectionClosedException`   | for QoS 0 if the connection was closed during writing the message to the transport |
| `MqttSessionExpiredException` | if the session expired before the message has been acknowledged completely |
| `MqttEncodeException`         | if the maximum packet size was exceeded |
| `MqttClientStateException`    | if the client is not connected and also not reconnecting |

 ====

 Reactive
 ===

The reactive API does not publish a single message, but an asynchronous stream of messages: a `Flowable<Mqtt3Publish>`.
Hence it also returns an asynchronous stream of results: `Flowable<Mqtt3PublishResult>`.
Each published message will cause a result in the returned stream.
As the `Flowable` is a reactive type, the following line does not publish immediately but only after you subscribe to it 
(in terms of Reactive Streams).

```java
Flowable<Mqtt3PublishResult> publishResultFlowable = client.publish(
        Flowable.range(0, 100).map(i -> Mqtt3Publish.builder().topic("test/topic" + i).build()));
```

If publishing a single message was not successful, the corresponding publish result contains an error:

| `ConnectionClosedException`   | for QoS 0 if the connection was closed during writing the message to the transport |
| `MqttSessionExpiredException` | if the session expired before the message has been acknowledged completely |
| `MqttEncodeException`         | if the maximum packet size was exceeded |

The result stream will always emit a publish result for every message in the input stream.
This means that the result stream only errors completely if the provided message stream errors itself and after all 
publish results have been received.
Similarly, the result stream only completes normally if the provided message stream completes normally itself and after 
all publish results have been received.

 {% endcapture %}{% include tabs.html tab_group="api-flavour" %}

{% endcapture %}{% include tabs.html tab_group="mqtt-version" %}

***



{% capture tab_content %}

MQTT 5.0
===

The rest of this section describes all possible properties of a `Mqtt5Publish` message.
They can be set via a fluent builder API.

 {% capture tab_content %}

 Blocking
 ===

  {% capture tab_content %}

  Fluent
  ===

```java
Mqtt5PublishResult publishResult = client.publishWith()
        ... // here you can specify multiple properties which are described below
        .send();
```

  ====

  Prebuilt message
  ===

```java
Mqtt5Publish publishMessage = Mqtt5Publish.builder()
        ... // here you can specify multiple properties which are described below
        .build();

Mqtt5PublishResult publishResult = client.publish(publishMessage);
```

  {% endcapture %}{% include tabs.html tab_group="mqtt-operation-style" %}

 ====

 Async
 ===

  {% capture tab_content %}

  Fluent
  ===

```java
CompletableFuture<Mqtt5PublishResult> publishResultFuture = client.publishWith()
        ... // here you can specify multiple properties described below
        .send();
```

  ====

  Prebuilt message
  ===

```java
Mqtt5Publish publishMessage = Mqtt5Publish.builder()
        ... // here you can specify multiple properties described below
        .build();

CompletableFuture<Mqtt5PublishResult> publishResultFuture = client.publish(publishMessage);
```

  {% endcapture %}{% include tabs.html tab_group="mqtt-operation-style" %}

 ====

 Reactive
 ===

  {% capture tab_content %}
  -===
  {% endcapture %}{% include tabs.html tab_group="dummy" %}

```java
Flowable<Mqtt5PublishResult> publishResultFlowable = client.publish(
        Flowable.range(0, 100).map(i -> 
                Mqtt5Publish.builder()
                        ... // here you can specify multiple properties described below
                        .build()
        ));
```

 {% endcapture %}{% include tabs.html tab_group="api-flavour" tab_merge=true %}

====


MQTT 3.1.1
===

The rest of this section describes all possible properties of a `Mqtt3Publish` message.
They can be set via a fluent builder API.

 {% capture tab_content %}

 Blocking
 ===

  {% capture tab_content %}

  Fluent
  ===

```java
client.publishWith()
        ... // here you can specify multiple properties which are described below
        .send();
```

  ====

  Prebuilt message
  ===

```java
Mqtt3Publish publishMessage = Mqtt3Publish.builder()
        ... // here you can specify multiple properties which are described below
        .build();

Mqtt3PublishResult publishResult = client.publish(publishMessage);
```

  {% endcapture %}{% include tabs.html tab_group="mqtt-operation-style" %}

 ====

 Async
 ===

  {% capture tab_content %}

  Fluent
  ===

```java
CompletableFuture<Mqtt3Publish> publishResultFuture = client.publishWith()
        ... // here you can specify multiple properties described below
        .send();
```

  ====

  Prebuilt message
  ===

```java
Mqtt3Publish publishMessage = Mqtt5PuMqtt3Publishblish.builder()
        ... // here you can specify multiple properties described below
        .build();

CompletableFuture<Mqtt3Publish> publishResultFuture = client.publish(publishMessage);
```

  {% endcapture %}{% include tabs.html tab_group="mqtt-operation-style" %}

 ====

 Reactive
 ===

  {% capture tab_content %}
  -===
  {% endcapture %}{% include tabs.html tab_group="dummy" %}

```java
Flowable<Mqtt3PublishResult> publishResultFlowable = client.publish(
        Flowable.range(0, 100).map(i -> 
                Mqtt3Publish.builder()
                        ... // here you can specify multiple properties described below
                        .build()
        ));
```

 {% endcapture %}{% include tabs.html tab_group="api-flavour" tab_merge=true %}

{% endcapture %}{% include tabs.html tab_group="mqtt-version" tab_no_header=true %}



{% capture tab_content %}

MQTT 5.0
===

- [Topic](#topic)
- [Payload](#payload)
- [Quality of Service (QoS)](#quality-of-service-qos)
- [Retain](#retain)
- [Message Expiry Interval](#message-expiry-interval)
- [Payload Format Indicator](#payload-format-indicator)
- [Content Type](#content-type)
- [Response Topic](#response-topic)
- [Correlation Data](#correlation-data)
- [User Properties](#user-properties)

====

MQTT 3.1.1
===

- [Topic](#topic)
- [Payload](#payload)
- [Quality of Servive (QoS)](#quality-of-service-qos)
- [Retain](#retain)

{% endcapture %}{% include tabs.html tab_group="mqtt-version" %}

***


## Topic

Messages are published with a topic.
The MQTT broker needs the topic to route the message to subscribers.
Hence the topic is mandatory for a Publish message (it is the only required property).
A topic can be hierarchically structured in multiple topic levels (divided by `/`) enabling easier filtering for
subscribers.

{% capture tab_content %}

MQTT 5.0
===

| Property | Values | Default | MQTT Specification |
| -------- | ------ | ------- | ------------------ |
| `topic` | `String`/`MqttTopic` | mandatory | [3.3.2.1](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901107){:target="_blank"} |

 {% capture tab_content %}

 Fluent
 ===

```java
client.publishWith().topic("test/topic")...;
```

 ====

 Prebuilt message
 ===

```java
Mqtt5Publish publishMessage = Mqtt5Publish.builder().topic("test/topic")...build();
```

 {% endcapture %}{% include tabs.html tab_group="mqtt-operation-style" %}

====

MQTT 3.1.1
===

| Property | Values | Default | MQTT Specification |
| -------- | ------ | ------- | ------------------ |
| `topic` | `String`/`MqttTopic` | mandatory | [3.3.2.1](http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/errata01/os/mqtt-v3.1.1-errata01-os-complete.html#_Toc385349267){:target="_blank"} |

 {% capture tab_content %}

 Fluent
 ===

```java
client.publishWith().topic("test/topic")...;
```

 ====

 Prebuilt message
 ===

```java
Mqtt3Publish publishMessage = Mqtt3Publish.builder().topic("test/topic")...build();
```

 {% endcapture %}{% include tabs.html tab_group="mqtt-operation-style" %}

{% endcapture %}{% include tabs.html tab_group="mqtt-version" tab_no_header=true %}

***



## Payload

The payload of a Publish message carries the actual application data.
MQTT is data-agnostic so you can use any format for the payload. 

{% capture tab_content %}

MQTT 5.0
===

| Property | Values | Default | MQTT Specification |
| -------- | ------ | ------- | ------------------ |
| `payload` | `byte[]`/`ByteBuffer` | - | [3.3.3](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901119){:target="_blank"} |

 {% capture tab_content %}

 Fluent
 ===

```java
client.publishWith()
        .topic("test/topic")
        .payload("hello world".getBytes())
        ...;
```

 ====

 Prebuilt message
 ===

```java
Mqtt5Publish publishMessage = Mqtt5Publish.builder()
        .topic("test/topic")
        .payload("hello world".getBytes())
        ...build();
```

 {% endcapture %}{% include tabs.html tab_group="mqtt-operation-style" %}

====

MQTT 3.1.1
===

| Property | Values | Default | MQTT Specification |
| -------- | ------ | ------- | ------------------ |
| `payload` | `byte[]`/`ByteBuffer` | - | [3.3.3](http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/errata01/os/mqtt-v3.1.1-errata01-os-complete.html#_Toc442180853){:target="_blank"} |

 {% capture tab_content %}

 Fluent
 ===

```java
client.publishWith()
        .topic("test/topic")
        .payload("hello world".getBytes())
        ...;
```

 ====

 Prebuilt message
 ===

```java
Mqtt3Publish publishMessage = Mqtt3Publish.builder()
        .topic("test/topic")
        .payload("hello world".getBytes())
        ...build();
```

 {% endcapture %}{% include tabs.html tab_group="mqtt-operation-style" %}

{% endcapture %}{% include tabs.html tab_group="mqtt-version" tab_no_header=true %}

***



## Quality of Service (QoS)

The QoS levels ensure different message delivery guarantees in case of connection failures.
The QoS level should be chosen based on the use case.

| QoS 0 | AT MOST ONCE  | Messages are not redelivered after a failure. Some messages may be lost. |
| QoS 1 | AT LEAST ONCE | Messages are redelivered after a failure if they were not acknowledged by the broker. Some messages may be delivered more than once (initial delivery attempt + redelivery attempt(s)). |
| QoS 2 | EXACTLY ONCE  | Messages are redelivered after a failure if they were not acknowledged by the broker. The broker additionally filters duplicate messages based on message ids. |

The trade-off between the QoS levels is lower or higher latency and the amount of state that has to be stored on sender 
and receiver.

Keep in mind that the MQTT QoS levels cover guarantees between the client and the broker (not directly the subscribers)
as MQTT is an asynchronous protocol (which is an advantage because it decouples publishers and subscribers and makes the 
system more robust and scalable).
Different brokers might provide different guarantees for end-to-end communication (especially if they are clustered).


{% capture tab_content %}

MQTT 5.0
===

| Property | Values | Default | MQTT Specification |
| -------- | ------ | ------- | ------------------ |
| `qos` | `AT_MOST_ONCE` <br/> `AT_LEAST_ONCE` <br/> `EXACTLY_ONCE` | `AT_MOST_ONCE` | [3.3.1.2](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901103){:target="_blank"} |

 {% capture tab_content %}

 Fluent
 ===

```java
client.publishWith()
        .topic("test/topic")
        .qos(MqttQos.AT_LEAST_ONCE)
        ...;
```

 ====

 Prebuilt message
 ===

```java
Mqtt5Publish publishMessage = Mqtt5Publish.builder()
        .topic("test/topic")
        .qos(MqttQos.AT_LEAST_ONCE)
        ...build();
```

 {% endcapture %}{% include tabs.html tab_group="mqtt-operation-style" %}

====

MQTT 3.1.1
===

| Property | Values | Default | MQTT Specification |
| -------- | ------ | ------- | ------------------ |
| `qos` | `AT_MOST_ONCE` <br/> `AT_LEAST_ONCE` <br/> `EXACTLY_ONCE` | `AT_MOST_ONCE` | [3.3.1.2](http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/errata01/os/mqtt-v3.1.1-errata01-os-complete.html#_Toc385349263){:target="_blank"} |

 {% capture tab_content %}

 Fluent
 ===

```java
client.publishWith()
        .topic("test/topic")
        .qos(MqttQos.AT_LEAST_ONCE)
        ...;
```

 ====

 Prebuilt message
 ===

```java
Mqtt3Publish publishMessage = Mqtt3Publish.builder()
        .topic("test/topic")
        .qos(MqttQos.AT_LEAST_ONCE)
        ...build();
```

 {% endcapture %}{% include tabs.html tab_group="mqtt-operation-style" %}

{% endcapture %}{% include tabs.html tab_group="mqtt-version" tab_no_header=true %}

***



## Retain

The retain flag indicates that the message should be stored at the broker for its topic.
New subscribers then get the last retained message on that topic even if they were not connected when it was published.

{% capture tab_content %}

MQTT 5.0
===

| Property | Values | Default | MQTT Specification |
| -------- | ------ | ------- | ------------------ |
| `retain` | `true`/`false` | `false` | [3.3.1.3](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901104){:target="_blank"} |

 {% capture tab_content %}

 Fluent
 ===

```java
client.publishWith()
        .topic("test/topic")
        .retain(true)
        ...;
```

 ====

 Prebuilt message
 ===

```java
Mqtt5Publish publishMessage = Mqtt5Publish.builder()
        .topic("test/topic")
        .retain(true)
        ...build();
```

 {% endcapture %}{% include tabs.html tab_group="mqtt-operation-style" %}

====

MQTT 3.1.1
===

| Property | Values | Default | MQTT Specification |
| -------- | ------ | ------- | ------------------ |
| `retain` | `true`/`false` | `false` | [3.3.1.3](http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/errata01/os/mqtt-v3.1.1-errata01-os-complete.html#_Toc385349265){:target="_blank"} |

 {% capture tab_content %}

 Fluent
 ===

```java
client.publishWith()
        .topic("test/topic")
        .retain(true)
        ...;
```

 ====

 Prebuilt message
 ===

```java
Mqtt5Publish publishMessage = Mqtt5Publish.builder()
        .topic("test/topic")
        .retain(true)
        ...build();
```

 {% endcapture %}{% include tabs.html tab_group="mqtt-operation-style" %}

{% endcapture %}{% include tabs.html tab_group="mqtt-version" tab_no_header=true %}




{% capture tab_content %}

MQTT 5.0
===

***

## Message Expiry Interval

The message expiry interval is the time interval (in seconds) the message will be queued for subscribers.

| Property | Values | Default | MQTT Specification |
| -------- | ------ | ------- | ------------------ |
| `messageExpiryInterval` | [`0` - `4_294_967_295`] | - | [3.3.2.3.3](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901112){:target="_blank"} |

 {% capture tab_content %}

 Fluent
 ===

```java
client.publishWith()
        .topic("test/topic")
        .messageExpiryInterval(100)
        ...;
```

 ====

 Prebuilt message
 ===

```java
Mqtt5Publish publishMessage = Mqtt5Publish.builder()
        .topic("test/topic")
        .messageExpiryInterval(100)
        ...build();
```

 {% endcapture %}{% include tabs.html tab_group="mqtt-operation-style" %}

Session expiry can be disabled (the default) by using the method `noMessageExpiry`.

 {% capture tab_content %}

 Fluent
 ===

```java
client.publishWith()
        .topic("test/topic")
        .noMessageExpiry()
        ...;
```

 ====

 Prebuilt message
 ===

```java
Mqtt5Publish publishMessage = Mqtt5Publish.builder()
        .topic("test/topic")
        .noMessageExpiry()
        ...build();
```

 {% endcapture %}{% include tabs.html tab_group="mqtt-operation-style" tab_no_header=true %}

{% include admonition.html type="tip" title="Additional Resources" content="
[MQTT 5 Essentials - Session and Message Expiry Intervals](https://www.hivemq.com/blog/mqtt5-essentials-part4-session-and-message-expiry/)
"%}

====

MQTT 3.1.1
===

{% endcapture %}{% include tabs.html tab_group="mqtt-version" tab_no_header=true %}




{% capture tab_content %}

MQTT 5.0
===

***

## Payload Format Indicator

{% include admonition.html type="tip" title="Additional Resources" content="
[MQTT 5 Essentials - Payload Format Description](https://www.hivemq.com/blog/mqtt5-essentials-part8-payload-format-description/)
"%}

====

MQTT 3.1.1
===

{% endcapture %}{% include tabs.html tab_group="mqtt-version" tab_no_header=true %}




{% capture tab_content %}

MQTT 5.0
===

***

## Content Type

{% include admonition.html type="tip" title="Additional Resources" content="
[MQTT 5 Essentials - Payload Format Description](https://www.hivemq.com/blog/mqtt5-essentials-part8-payload-format-description/)
"%}

====

MQTT 3.1.1
===

{% endcapture %}{% include tabs.html tab_group="mqtt-version" tab_no_header=true %}




{% capture tab_content %}

MQTT 5.0
===

***

## Response Topic

{% include admonition.html type="tip" title="Additional Resources" content="
[MQTT 5 Essentials - Request-Response Pattern](https://www.hivemq.com/blog/mqtt5-essentials-part9-request-response-pattern/)
"%}

====

MQTT 3.1.1
===

{% endcapture %}{% include tabs.html tab_group="mqtt-version" tab_no_header=true %}




{% capture tab_content %}

MQTT 5.0
===

***

## Correlation Data

{% include admonition.html type="tip" title="Additional Resources" content="
[MQTT 5 Essentials - Request-Response Pattern](https://www.hivemq.com/blog/mqtt5-essentials-part9-request-response-pattern/)
"%}

====

MQTT 3.1.1
===

{% endcapture %}{% include tabs.html tab_group="mqtt-version" tab_no_header=true %}




{% capture tab_content %}

MQTT 5.0
===

***

## User Properties

{% include admonition.html type="tip" title="Additional Resources" content="
[MQTT 5 Essentials - User Properties](https://www.hivemq.com/blog/mqtt5-essentials-part6-user-properties/)
"%}

====

MQTT 3.1.1
===

{% endcapture %}{% include tabs.html tab_group="mqtt-version" tab_no_header=true %}
