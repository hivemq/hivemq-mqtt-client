---
layout: default_anchor_headings
title: Connect
parent: MQTT Operations
nav_order: 1
---

# Connect

You can connect a MQTT client without the need to provide arguments.
This will use the default parameters as defined in the MQTT specification or reasonable defaults if not defined there.

```java
client.connect();
```

The return type depends on the used MQTT version and API flavour.

{% capture tab_content %}

MQTT 5.0
===

{% capture tab_content %}

Blocking
===

The blocking API directly returns a `Mqtt5ConnAck` message (which contains data from the broker) if the connect was 
successful.

```java
Mqtt5ConnAck connAckMessage = client.connect();
```

====

Async
===

The asynchronous API returns a `CompletableFuture` which succeeds with a `Mqtt5ConnAck` message (which contains data 
from the broker) if the connect was successful.

```java
CompletableFuture<Mqtt5ConnAck> connAckFuture = client.connect();
```

====

Reactive
===

The reactive API returns a `Single` which succeeds with a `Mqtt5ConnAck` message (which contains data 
from the broker) if the connect was successful.
As the `Single` is a reactive type the following line does not connect immediately but only after you subscribe to it 
(in terms of Reactive Streams).

```java
Single<Mqtt5ConnAck> connAckSingle = client.connect();
```

{% endcapture %}
{% include tabs.html tab_group="api-flavour" %}

***

The rest of this section describes all possible properties of a `Mqtt5Connect` message.
They can be set via a fluent builder API.

{% capture tab_content %}

Blocking
===

{% capture tab_content %}

Fluent
===

```java
Mqtt5ConnAck connAckMessage = client.connectWith()
        ... // here you can specify multiple properties which are described below
        .send();
```

====

Prebuilt message
===

```java
Mqtt5Connect connectMessage = Mqtt5ConnectMessage.builder()
        ... // here you can specify multiple properties which are described below
        .build();

Mqtt5ConnAck connAckMessage = client.connect(connectMessage);
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-operation-style" %}

====


Async
===

{% capture tab_content %}

Fluent
===

```java
CompletableFuture<Mqtt5ConnAck> connAckFuture = client.connectWith()
        ... // here you can specify multiple properties described below
        .send();
```

====

Prebuilt message
===

```java
Mqtt5Connect connectMessage = Mqtt5ConnectMessage.builder()
        ... // here you can specify multiple properties described below
        .build();

CompletableFuture<Mqtt5ConnAck> connAckFuture = client.connect(connectMessage);
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-operation-style" %}

====


Reactive
===

{% capture tab_content %}

Fluent
===

```java
Single<Mqtt5ConnAck> connAckSingle = client.connectWith()
        ... // here you can specify multiple properties described below
        .applyConnect();
```

====

Prebuilt message
===

```java
Mqtt5Connect connectMessage = Mqtt5ConnectMessage.builder()
        ... // here you can specify multiple properties described below
        .build();

Single<Mqtt5ConnAck> connAckSingle = client.connect(connectMessage);
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-operation-style" %}

{% endcapture %}
{% include tabs.html tab_group="api-flavour" tab_merge=true %}

====


MQTT 3.1.1
===

{% capture tab_content %}

Blocking
===

The blocking API directly returns a `Mqtt3ConnAck` message (which contains data from the broker) if the connect was 
successful.

```java
Mqtt3ConnAck connAckMessage = client.connect();
```

====

Async
===

The asynchronous API returns a `CompletableFuture` which succeeds with a `Mqtt3ConnAck` message (which contains data 
from the broker) if the connect was successful.

```java
CompletableFuture<Mqtt3ConnAck> connAckFuture = client.connect();
```

====

Reactive
===

The reactive API returns a `Single` which succeeds with a `Mqtt3ConnAck` message (which contains data 
from the broker) if the connect was successful.
As the `Single` is a reactive type the following line does not connect immediately but only after you subscribe to it 
(in terms of Reactive Streams).

```java
Single<Mqtt3ConnAck> connAckSingle = client.connect();
```

{% endcapture %}
{% include tabs.html tab_group="api-flavour" %}

***

The rest of this section describes all possible properties of a `Mqtt3Connect` message.
They can be set via a fluent builder API.

{% capture tab_content %}

Blocking
===

{% capture tab_content %}

Fluent
===

```java
Mqtt3ConnAck connAckMessage = client.connectWith()
        ... // here you can specify multiple properties which are described below
        .send();
```

====

Prebuilt message
===

```java
Mqtt3Connect connectMessage = Mqtt3Connect.builder()
        ... // here you can specify multiple properties which are described below
        .build();

Mqtt3ConnAck connAckMessage = client.connect(connectMessage);
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-operation-style" %}

====


Async
===

{% capture tab_content %}

Fluent
===

```java
CompletableFuture<Mqtt3ConnAck> connAckFuture = client.connectWith()
        ... // here you can specify multiple properties described below
        .send();
```

====

Prebuilt message
===

```java
Mqtt3Connect connectMessage = Mqtt3Connect.builder()
        ... // here you can specify multiple properties described below
        .build();

CompletableFuture<Mqtt3ConnAck> connAckFuture = client.connect(connectMessage);
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-operation-style" %}

====


Reactive
===

{% capture tab_content %}

Fluent
===

```java
Single<Mqtt3ConnAck> connAckSingle = client.connectWith()
        ... // here you can specify multiple properties described below
        .applyConnect();
```

====

Prebuilt message
===

```java
Mqtt3Connect connectMessage = Mqtt3Connect.builder()
        ... // here you can specify multiple properties described below
        .build();

Single<Mqtt3ConnAck> connAckSingle = client.connect(connectMessage);
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-operation-style" %}

{% endcapture %}
{% include tabs.html tab_group="api-flavour" tab_merge=true %}

{% endcapture %}
{% include tabs.html tab_group="mqtt-version" %}
{: .mb-5 }


#### List of connect properties

{% capture tab_content %}

MQTT 5.0
===

* [Clean start](#clean-start)
* [Session expiry interval](#session-expiry-interval)
* [Keep alive](#keep-alive)
* [Authentication/Authorization](#authenticationauthorization)
* [Will](#will)
* [Restrictions](#restrictions)
* [User properties](#user-properties)

====

MQTT 3.1.1
===

* [Clean session](#clean-session)
* [Keep alive](#keep-alive)
* [Authentication/Authorization](#authenticationauthorization)
* [Will](#will)

{% endcapture %}
{% include tabs.html tab_group="mqtt-version" tab_no_header=true %}


{% capture tab_content %}

MQTT 5.0
===

## Clean start

Clean start determines if the client wants to start a new "clean" session (`true`) or wants to resume a previous session 
if present (`false`).

| Property | Values | Default | MQTT Specification |
| -------- | ------ | ------------- | ------------------ |
| `cleanStart` | `true`/`false` | `true` | [3.1.2.4](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901039) |

{% capture tab_content %}

Fluent
===

```java
client.connectWith().cleanStart(false)...;
```

====

Prebuilt message
===

```java
Mqtt5Connect connectMessage = Mqtt5ConnectMessage.builder().cleanStart(false)...build();
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-operation-style" %}


## Session expiry interval

The session expiry interval is the time interval (in seconds) the session will persist when the client is disconnected.

| Property | Values | Default | MQTT Specification |
| -------- | ------ | ------------- | ------------------ |
| `sessionExpiryInterval` | [`0` - `4_294_967_295`] | `0` | [3.1.2.11.2](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901048) |

{% capture tab_content %}

Fluent
===

```java
client.connectWith().sessionExpiryInterval(100)...;
```

====

Prebuilt message
===

```java
Mqtt5Connect connectMessage = Mqtt5ConnectMessage.builder().sessionExpiryInterval(100)...build();
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-operation-style" %}

Session expiry can be disabled by setting it to `4_294_967_295` or using the method `noSessionExpiry`.

{% capture tab_content %}

Fluent
===

```java
client.connectWith().noSessionExpiry()...;
```

====

Prebuilt message
===

```java
Mqtt5Connect connectMessage = Mqtt5ConnectMessage.builder().noSessionExpiry()...build();
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-operation-style" tab_no_header=true %}

====


MQTT 3.1.1
===

## Clean session

Clean session determines if the client wants to start a new "clean" session (`true`) or wants to resume a previous 
session which will persist when the client is disconnected (`false`).

| Property | Values | Default | MQTT Specification |
| -------- | ------ | ------------- | ------------------ |
| `cleanSession` | `true`/`false` | `true` | [3.1.2.4](http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/errata01/os/mqtt-v3.1.1-errata01-os-complete.html#_Toc442180843) |

{% capture tab_content %}

Fluent
===

```java
client.connectWith().cleanSession(false)...;
```

====

Prebuilt message
===

```java
Mqtt3Connect connectMessage = Mqtt3Connect.builder().cleanSession(false)...build();
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-operation-style" %}

{% endcapture %}
{% include tabs.html tab_group="mqtt-version" tab_no_header=true %}


## Keep alive

The keep alive is the time interval (in seconds) in which the client sends a ping to the broker if no other MQTT packets 
are sent during this period of time.
It is used to determine if the connection is still up.

{% capture tab_content %}

MQTT 5.0
===

| Property | Values | Default | MQTT Specification |
| -------- | ------ | ------------- | ------------------ |
| `keepAlive` | [`0` - `65_535`] | `60` | [3.1.2.10](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901045) |

{% capture tab_content %}

Fluent
===

```java
client.connectWith().keepAlive(30)...;
```

====

Prebuilt message
===

```java
Mqtt5Connect connectMessage = Mqtt5ConnectMessage.builder().keepAlive(30)...build();
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-operation-style" %}

Keep alive can be disabled by setting it to `0` or using the method `noKeepAlive`.

{% capture tab_content %}

Fluent
===

```java
client.connectWith().noKeepAlive()...;
```

====

Prebuilt message
===

```java
Mqtt5Connect connectMessage = Mqtt5ConnectMessage.builder().noKeepAlive()...build();
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-operation-style" tab_no_header=true %}

====


MQTT 3.1.1
===

| Property | Values | Default | MQTT Specification |
| -------- | ------ | ------------- | ------------------ |
| `keepAlive` | [`0` - `65_535`] | `60` | [3.1.2.10](http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/errata01/os/mqtt-v3.1.1-errata01-os-complete.html#_Toc442180843) |

{% capture tab_content %}

Fluent
===

```java
client.connectWith().keepAlive(30)...;
```

====

Prebuilt message
===

```java
Mqtt3Connect connectMessage = Mqtt3Connect.builder().keepAlive(30)...build();
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-operation-style" %}

Keep alive can be disabled by setting it to `0` or using the method `noKeepAlive`.

{% capture tab_content %}

Fluent
===

```java
client.connectWith().noKeepAlive()...;
```

====

Prebuilt message
===

```java
Mqtt3Connect connectMessage = Mqtt3Connect.builder().noKeepAlive()...build();
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-operation-style" tab_no_header=true %}

{% endcapture %}
{% include tabs.html tab_group="mqtt-version" tab_no_header=true %}


## Authentication/authorization

### Simple auth (username/password)

{% capture tab_content %}

MQTT 5.0
===

| Property | Values | Default | MQTT Specification |
| -------- | ------ | ------------- | ------------------ |
| `simpleAuth.username` | `String`/`MqttUtf8String` | - | [3.1.3.5](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901071) |
| `simpleAuth.password` | `byte[]`/`ByteBuffer` | - | [3.1.3.6](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901072) |

{% capture tab_content %}

Fluent
===

```java
client.connectWith()
        .simpleAuth()
            .username("username")
            .password("password".getBytes())
            .applySimpleAuth()
        ...;
```

====

Prebuilt message
===

```java
Mqtt5Connect connectMessage = Mqtt5ConnectMessage.builder()
        .simpleAuth()
            .username("username")
            .password("password".getBytes())
            .applySimpleAuth()
        ...
        .build();
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-operation-style" %}


### Enhanced auth

You need to implement an `Mqtt5EnhancedAuthMechanism` for enhanced auth.
See the [Enhanced Auth](../security/auth.md) section for more details.

Simple and enhanced auth can be used both at the same time.

| Property | Values | Default | MQTT Specification |
| -------- | ------ | ------------- | ------------------ |
| `enhancedAuth` | `Mqtt5EnhancedAuthMechanism` | - | [3.1.2.11.9/10](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901055) |

{% capture tab_content %}

Fluent
===

```java
Mqtt5EnhancedAuthMechanism myEnhancedAuthMechanism = ...

client.connectWith()
        .enhancedAuth(myEnhancedAuthMechanism)
        ...;
```

====

Prebuilt message
===

```java
Mqtt5EnhancedAuthMechanism myEnhancedAuthMechanism = ...

Mqtt5Connect connectMessage = Mqtt5ConnectMessage.builder()
        .enhancedAuth(myEnhancedAuthMechanism)
        ...
        .build();
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-operation-style" %}

====


MQTT 3.1.1
===

| Property | Values | Default | MQTT Specification |
| -------- | ------ | ------------- | ------------------ |
| `simpleAuth.username` | `String`/`MqttUtf8String` | - | [3.1.3.4](http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/errata01/os/mqtt-v3.1.1-errata01-os-complete.html#_Toc442180844) |
| `simpleAuth.password` | `byte[]`/`ByteBuffer` | - | [3.1.3.5](http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/errata01/os/mqtt-v3.1.1-errata01-os-complete.html#_Toc442180844) |

{% capture tab_content %}

Fluent
===

```java
client.connectWith()
        .simpleAuth()
            .username("username")
            .password("password".getBytes())
            .applySimpleAuth()
        ...;
```

====

Prebuilt message
===

```java
Mqtt3Connect connectMessage = Mqtt3Connect.builder()
        .simpleAuth()
            .username("username")
            .password("password".getBytes())
            .applySimpleAuth()
        ...
        .build();
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-operation-style" %}

{% endcapture %}
{% include tabs.html tab_group="mqtt-version" tab_no_header=true %}


## Will

{% capture tab_content %}

MQTT 5.0
===

The Will publish message is also known as Last Will and Testament (LWT).
It is the message that is published by the broker if the client disconnected ungracefully or with the reason code 
`DISCONNECT_WITH_WILL_MESSAGE`.

`topic` is the only mandatory property for a Will publish message, all others have defaults or are optional.

| Property | Values | Default | MQTT Specification |
| -------- | ------ | ------------- | ------------------ |
| `willPublish.topic` | `String`/`MqttTopic` | mandatory | [3.1.3.3](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901069) |
| `willPublish.qos` | `MqttQos` | `AT_MOST_ONCE` | [3.1.2.6](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901041) |
| `willPublish.payload` | `byte[]`/`ByteBuffer` | - | [3.1.3.4](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901070) |
| `willPublish.retain` | `true`/`false` | `false` | [3.1.2.7](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901042) |
| `willPublish.messageExpiryInterval` | [`0` - `4_294_967_295`] | `4_294_967_295` | [3.1.3.2.4](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901064) |
| `willPublish.delayInterval` | [`0` - `4_294_967_295`] | `0` | [3.1.3.2.2](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901062) |
| `willPublish.payloadFormatIndicator` | `Mqtt5PayloadFormatIndicator` | - | [3.1.3.2.3](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901063) |
| `willPublish.contentType` | `String`/`MqttUtf8String` | - | [3.1.3.2.5](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901065) |
| `willPublish.responseTopic` | `String`/`MqttTopic` | - | [3.1.3.2.6](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901066) |
| `willPublish.correlationData` | `byte[]`/`ByteBuffer` | - | [3.1.3.2.7](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901067) |
| `willPublish.userProperties` | `Mqtt5UserProperties` | - | [3.1.3.2.8](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901068) |

{% capture tab_content %}

Fluent
===

```java
client.connectWith()
        .willPublish()
            .topic("test/topic")
            .qos(MqttQos.AT_LEAST_ONCE)
            .payload("payload".getBytes())
            .retain(true)
            .messageExpiryInterval(100)
            .delayInterval(10)
            .payloadFormatIndicator(Mqtt5PayloadFormatIndicator.UTF_8)
            .contentType("text/plain")
            .responseTopic("response/topic")
            .correlationData("correlationData".getBytes())
            .userProperties()
                .add("key1", "value1")
                .add("key2", "value2")
                .applyUserProperties()
            .applyWillPublish()
        ...
```

====

Prebuilt message
===

```java
Mqtt5Connect connectMessage = Mqtt5ConnectMessage.builder()
        .willPublish()
            .topic("test/topic")
            .qos(MqttQos.AT_LEAST_ONCE)
            .payload("payload".getBytes())
            .retain(true)
            .messageExpiryInterval(100)
            .delayInterval(10)
            .payloadFormatIndicator(Mqtt5PayloadFormatIndicator.UTF_8)
            .contentType("text/plain")
            .responseTopic("response/topic")
            .correlationData("correlationData".getBytes())
            .userProperties()
                .add("key1", "value1")
                .add("key2", "value2")
                .applyUserProperties()
            .applyWillPublish()
        ...
        .build();
```

You can also prebuild the `Mqtt5WillPublish`.

```java
Mqtt5WillPublish willPublishMessage = Mqtt5WillPublish.builder()
        .topic("test/topic")
        .qos(MqttQos.AT_LEAST_ONCE)
        .payload("payload".getBytes())
        .retain(true)
        .messageExpiryInterval(100)
        .delayInterval(10)
        .payloadFormatIndicator(Mqtt5PayloadFormatIndicator.UTF_8)
        .contentType("text/plain")
        .responseTopic("response/topic")
        .correlationData("correlationData".getBytes())
        .userProperties()
            .add("key1", "value1")
            .add("key2", "value2")
            .applyUserProperties()
        .build()

Mqtt5Connect connectMessage = Mqtt5ConnectMessage.builder()
        .willPublish(willPublishMessage)
        ...
        .build();
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-operation-style" %}
{: .mb-5 }

Message expiry can be disabled by setting it to `4_294_967_295` (default) or using the method `noMessageExpiry`.

All properties of a Will publish message are the same as of a normal [`Mqtt5Publish` message](publish.md) with the 
addition of the `delayInterval`.

====


MQTT 3.1.1
===

The Will publish message is also known as Last Will and Testament (LWT).
It is the message that is published by the broker if the client disconnected ungracefully.

`topic` is the only mandatory property for a Will publish message, all others have defaults or are optional.

| Property | Values | Default | MQTT Specification |
| -------- | ------ | ------------- | ------------------ |
| `willPublish.topic` | `String`/`MqttTopic` | mandatory | [3.1.3.2](http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/errata01/os/mqtt-v3.1.1-errata01-os-complete.html#_Toc442180844) |
| `willPublish.qos` | `MqttQos` | `AT_MOST_ONCE` | [3.1.2.6](http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/errata01/os/mqtt-v3.1.1-errata01-os-complete.html#_Toc442180843) |
| `willPublish.payload` | `byte[]`/`ByteBuffer` | - | [3.1.3.3](http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/errata01/os/mqtt-v3.1.1-errata01-os-complete.html#_Toc442180844) |
| `willPublish.retain` | `true`/`false` | `false` | [3.1.2.7](http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/errata01/os/mqtt-v3.1.1-errata01-os-complete.html#_Toc442180843) |

{% capture tab_content %}

Fluent
===

```java
client.connectWith()
        .willPublish()
            .topic("test/topic")
            .qos(MqttQos.AT_LEAST_ONCE)
            .payload("payload".getBytes())
            .retain(true)
            .applyWillPublish()
        ...
```

====

Prebuilt message
===

```java
Mqtt3Connect connectMessage = Mqtt3Connect.builder()
        .willPublish()
            .topic("test/topic")
            .qos(MqttQos.AT_LEAST_ONCE)
            .payload("payload".getBytes())
            .retain(true)
            .applyWillPublish()
        ...
        .build();
```

You can also prebuild the `Mqtt5WillPublish`.

```java
Mqtt3Publish willPublishMessage = Mqtt3Publish.builder()
        .topic("test/topic")
        .qos(MqttQos.AT_LEAST_ONCE)
        .payload("payload".getBytes())
        .retain(true)
        .build()

Mqtt3Connect connectMessage = Mqtt3Connect.builder()
        .willPublish(willPublishMessage)
        ...
        .build();
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-operation-style" %}
{: .mb-5 }

All properties of a Will publish message are the same as of a normal [`Mqtt3Publish` message](publish.md).

{% endcapture %}
{% include tabs.html tab_group="mqtt-version" tab_no_header=true %}


{% capture tab_content %}

MQTT 5.0
===

## Restrictions

You can specify broker side and client side restrictions.
The ones for messages received from the broker are sent with the `Mqtt5Connect` message.
The others for messages the client sends itself are used in conjunction with the restrictions the broker specifies in 
the `Mqtt5ConnAck` message to determine the actual client side restrictions.

| Property | Values | Default | MQTT Specification |
| -------- | ------ | ------------- | ------------------ |
| `restrictions.receiveMaximum` | [`1` - `65_535`] | `65_535` | [3.1.2.11.3](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901049) |
| `restrictions.sendMaximum` | [`1` - `65_535`] | `65_535` | - |
| `restrictions.maximumPacketSize` | [`1` - `268_435_460`] | `268_435_460` | [3.1.2.11.4](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901050) |
| `restrictions.sendMaximumPacketSize` | [`1` - `268_435_460`] | `268_435_460` | - |
| `restrictions.topicAliasMaximum` | [`0` - `65_535`] | `0` | [3.1.2.11.5](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901051) |
| `restrictions.sendTopicAliasMaximum` | [`0` - `65_535`] | `16` | - |
| `restrictions.requestProblemInformation` | `true`/`false` | `true` | [3.1.2.11.7](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901053) |
| `restrictions.requestResponseInformation` | `true`/`false` | `false` | [3.1.2.11.6](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901052) |

{% capture tab_content %}

Fluent
===

```java
client.connectWith()
        .restrictions()
            .receiveMaximum(16)
            .sendMaximum(32)
            .maximumPacketSize(2048)
            .sendMaximumPacketSize(1024)
            .topicAliasMaximum(16)
            .sendTopicAliasMaximum(8)
            .requestProblemInformation(false)
            .requestResponseInformation(true)
            .applyRestrictions()
        ...
```

====

Prebuilt message
===

```java
Mqtt5Connect connectMessage = Mqtt5ConnectMessage.builder()
        .restrictions()
            .receiveMaximum(16)
            .sendMaximum(32)
            .maximumPacketSize(2048)
            .sendMaximumPacketSize(1024)
            .topicAliasMaximum(16)
            .sendTopicAliasMaximum(8)
            .requestProblemInformation(false)
            .requestResponseInformation(true)
            .applyRestrictions()
        ...
        .build();
```

You can also prebuild the `Mqtt5ConnectRestrictions`.

```java
Mqtt5ConnectRestrictions restrictions = Mqtt5ConnectRestrictions.builder()
        .receiveMaximum(16)
        .sendMaximum(32)
        .maximumPacketSize(2048)
        .sendMaximumPacketSize(1024)
        .topicAliasMaximum(16)
        .sendTopicAliasMaximum(8)
        .requestProblemInformation(false)
        .requestResponseInformation(true)
        .build();

Mqtt5Connect connectMessage = Mqtt5ConnectMessage.builder()
        .restrictions(restrictions)
        ...
        .build();
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-operation-style" %}

====


MQTT 3.1.1
===

{% endcapture %}
{% include tabs.html tab_group="mqtt-version" tab_no_header=true %}


{% capture tab_content %}

MQTT 5.0
===

## User properties

User Properties are user defined name and value pairs which are sent with the `Mqtt5Connect` message.

| Method | Values | MQTT Specification |
| ------ | ------ | ------------------ |
| `userProperties.add` | `String, String`<br/>`MqttUtf8String, MqttUtf8String`<br/>`Mqtt5UserProperty` | [3.1.2.11.8](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901054) |

{% capture tab_content %}

Fluent
===

```java
client.connectWith()
        .userProperties()
            .add("key1", "value1")
            .add(Mqtt5UserProperty.of("key2", "value2"))
            .applyUserProperties()
        ...
```

====

Prebuilt message
===

```java
Mqtt5Connect connectMessage = Mqtt5ConnectMessage.builder()
        .userProperties()
            .add("key1", "value1")
            .add(Mqtt5UserProperty.of("key2", "value2"))
            .applyUserProperties()
        ...
        .build();
```

You can also prebuild the `Mqtt5UserProperties`.

```java
Mqtt5UserProperties connectUserProperties = Mqtt5UserProperties.builder()
        .add("key1", "value1")
        .add(Mqtt5UserProperty.of("key2", "value2"))
        .build();

Mqtt5Connect connectMessage = Mqtt5ConnectMessage.builder()
        .userProperties(connectUserProperties)
        ...
        .build();
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-operation-style" %}

====


MQTT 3.1.1
===

{% endcapture %}
{% include tabs.html tab_group="mqtt-version" tab_no_header=true %}