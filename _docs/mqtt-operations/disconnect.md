---
nav_order: 7
redirect_from: /docs/mqtt_operations/disconnect.html
---

# Disconnect

You can disconnect a MQTT client without the need to provide arguments.
This will use the default parameters as defined in the MQTT specification or reasonable defaults if not defined there.

```java
client.disconnect();
```

The return type depends on the used MQTT version and API flavour.

{% capture tab_content %}

MQTT 5.0
===

 {% capture tab_content %}

 Blocking
 ===

The blocking API for `disconnect` has a `Void` return value since disconnects in MQTT are sent only with no response defined.

```java
client.disconnect();
```

 ====

 Async
 ===

The asynchronous API returns a `CompletableFuture` which completes with a `Void` return value.

```java
CompletableFuture<Void> disconnectFuture = client.disconnect();
```

 ====

 Reactive
 ===

The reactive API returns a `Single` which succeeds with a `Void` return since no response is expected.
As the `Single` is a reactive type, the following line does not disconnect immediately but only after you subscribe to it 
(in terms of Reactive Streams).

```java
Single<Void> disconnectSingle = client.connect();
```

 {% endcapture %}{% include tabs.html group="api-flavour" content=tab_content %}

====


MQTT 3.1.1
===

 {% capture tab_content %}

 Blocking
 ===

The blocking API for `disconnect` has a `Void` return value since disconnects in MQTT are sent only with no response defined.

```java
client.disconnect();
```

 ====

 Async
 ===

The asynchronous API returns a `CompletableFuture` which completes with a `Void` return value.

```java
CompletableFuture<Void> disconnectFuture = client.disconnect();
```

 ====

 Reactive
 ===

The reactive API returns a `Single` which succeeds with a `Void` return since no response is expected.
As the `Single` is a reactive type, the following line does not disconnect immediately but only after you subscribe to it 
(in terms of Reactive Streams).

```java
Single<Void> disconnectSingle = client.connect();
```

 {% endcapture %}{% include tabs.html group="api-flavour" content=tab_content %}

{% endcapture %}{% include tabs.html group="mqtt-version" content=tab_content %}

***



{% capture tab_content %}

MQTT 5.0
===

The rest of this section describes all possible properties of a `Mqtt5Disconnect` message.
They can be set via a fluent builder API.

 {% capture tab_content %}

 Blocking
 ===

  {% capture tab_content %}

  Fluent
  ===

```java
Mqtt5Disconnect disconnectMessage = Mqtt5Disconnect.builder()
        ... // here you can specify multiple properties described below
        .send();
```

  ====

  Prebuilt message
  ===

```java
Mqtt5Disconnect disconnectMessage = Mqtt5Disconnect.builder()
        ... // here you can specify multiple properties described below
        .build();

client.disconnect(disconnectMessage);
```

  {% endcapture %}{% include tabs.html group="mqtt-operation-style" content=tab_content %}

 ====

 Async
 ===

  {% capture tab_content %}

  Fluent
  ===

```java
Mqtt5Disconnect disconnectMessage = Mqtt5Disconnect.builder()
        ... // here you can specify multiple properties described below
        .send();
```

  ====

  Prebuilt message
  ===

```java
Mqtt5Disconnect disconnectMessage = Mqtt5Disconnect.builder()
        ... // here you can specify multiple properties described below
        .build();

CompletableFuture<Void> disconnectFuture = client.disconnect(disconnectMessage);
```

  {% endcapture %}{% include tabs.html group="mqtt-operation-style" content=tab_content %}

 ====

 Reactive
 ===

  {% capture tab_content %}

  Fluent
  ===

```java
Mqtt5Disconnect disconnectMessage = Mqtt5Disconnect.builder()
        ... // here you can specify multiple properties described below
        .applyDisconnect();
```

  ====

  Prebuilt message
  ===

```java
Mqtt5Disconnect disconnectMessage = Mqtt5Disconnect.builder()
        ... // here you can specify multiple properties described below
        .build();

Single<Void> disconnectSingle = client.disconnect(disconnectMessage);
```

  {% endcapture %}{% include tabs.html group="mqtt-operation-style" content=tab_content %}

 {% endcapture %}{% include tabs.html group="api-flavour" content=tab_content merge=true %}

====


MQTT 3.1.1
===

The Disconnect call for MQTT 3.1.1 does not support any properties.


{% endcapture %}{% include tabs.html group="mqtt-version" content=tab_content no_header=true %}



{% capture tab_content %}

MQTT 5.0
===

***

### Disconnect Properties

- [Reason code](#reason-code)
- [Session Expiry Interval](#session-expiry-interval)
- [User Properties](#user-properties)

***


## Reason Code

The reason code of a disconnect message indicates to the broker the reason for the disconnect.  This can be used to trigger broker behavior.

| Property | Values | Default | MQTT Specification |
| -------- | ------ | ------- | ------------------ |
| `reasonCode` | See [Mqtt5DisconnectReasonCode](https://github.com/hivemq/hivemq-mqtt-client/blob/d709dc3aeea8a6837ecc21413988de154a66be1c/src/main/java/com/hivemq/client/mqtt/mqtt5/message/disconnect/Mqtt5DisconnectReasonCode.java#L32) | `NORMAL_DISCONNECTION` | [3.14.2.1](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Disconnect_Reason_Code){:target="_blank"} |

 {% capture tab_content %}

 Fluent
 ===

```java
client.disconnectWith().reasonCode(Mqtt5DisconnectReasonCode.DISCONNECT_WITH_WILL_MESSAGE)...;
```

 ====

 Prebuilt message
 ===

```java
Mqtt5Disconnect disconnectMessage = Mqtt5Disconnect.builder()
    .reasonCode(Mqtt5DisconnectReasonCode.DISCONNECT_WITH_WILL_MESSAGE)...build();
```

 {% endcapture %}{% include tabs.html group="mqtt-operation-style" content=tab_content %}

***



## Session Expiry Interval

The session expiry interval is the time interval (in seconds) the session will persist when the client is disconnected.

| Property | Values | Default | MQTT Specification |
| -------- | ------ | ------- | ------------------ |
| `sessionExpiryInterval` | [`0` - `4_294_967_295`] | N/A | [3.14.2.2.2](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901211){:target="_blank"} |

_If the Session Expiry Interval is absent, the Session Expiry Interval in the CONNECT packet is used._

 {% capture tab_content %}

 Fluent
 ===

```java
client.disconnectWith().sessionExpiryInterval(100)...;
```

 ====

 Prebuilt message
 ===

```java
Mqtt5Disconnect disconnectMessage = Mqtt5Disconnect.builder().sessionExpiryInterval(100)...build();
```

 {% endcapture %}{% include tabs.html group="mqtt-operation-style" content=tab_content %}

Session expiry can be disabled by setting it to `4_294_967_295` or using the method `noSessionExpiry`.

 {% capture tab_content %}

 Fluent
 ===

```java
client.disconnectWith().noSessionExpiry()...;
```

 ====

 Prebuilt message
 ===

```java
Mqtt5Disconnect disconnectMessage = Mqtt5Disconnect.builder().noSessionExpiry()...build();
```

 {% endcapture %}{% include tabs.html group="mqtt-operation-style" content=tab_content no_header=true %}

{% capture admonition_content %}
[MQTT 5 Essentials - Session and Message Expiry Intervals](https://www.hivemq.com/blog/mqtt5-essentials-part4-session-and-message-expiry/){:target="_blank"}
{% endcapture %}{% include admonition.html type="tip" title="Additional Resources" content=admonition_content %}

====


{% endcapture %}{% include tabs.html group="mqtt-version" content=tab_content no_header=true %}

***


{% capture tab_content %}

MQTT 5.0
===

***

## User Properties

User Properties are user defined name and value pairs which are sent with the `Mqtt5Disconnect` message.

| Method | Values | MQTT Specification |
| ------ | ------ | ------------------ |
| `userProperties.add` | `String, String`<br/>`MqttUtf8String, MqttUtf8String`<br/>`Mqtt5UserProperty` | [3.1.2.11.8](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901054){:target="_blank"} |

 {% capture tab_content %}

 Fluent
 ===

```java
client.disconnectWith()
        .userProperties()
            .add("name1", "value1")
            .add(Mqtt5UserProperty.of("name2", "value2"))
            .applyUserProperties()
        ...
```

 ====

 Prebuilt message
 ===

```java
Mqtt5Disconnect disconnectMessage = Mqtt5Disconnect.builder()
        .userProperties()
            .add("name1", "value1")
            .add(Mqtt5UserProperty.of("name2", "value2"))
            .applyUserProperties()
        ...
        .build();
```

You can also prebuild the `Mqtt5UserProperties`.

```java
Mqtt5UserProperties connectUserProperties = Mqtt5UserProperties.builder()
        .add("name1", "value1")
        .add(Mqtt5UserProperty.of("name2", "value2"))
        .build();

Mqtt5Disconnect disconnectMessage = Mqtt5Disconnect.builder()
        .userProperties(connectUserProperties)
        ...
        .build();
```

 {% endcapture %}{% include tabs.html group="mqtt-operation-style" content=tab_content %}

{% capture admonition_content %}
[MQTT 5 Essentials - User Properties](https://www.hivemq.com/blog/mqtt5-essentials-part6-user-properties/){:target="_blank"}
{% endcapture %}{% include admonition.html type="tip" title="Additional Resources" content=admonition_content %}

====


MQTT 3.1.1
===

{% endcapture %}{% include tabs.html group="mqtt-version" content=tab_content no_header=true %}