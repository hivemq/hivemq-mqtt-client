---
layout: default_anchor_headings
title: Client Configuration
nav_order: 3
---

# Client Configuration

A MQTT client can be created and configured by using a fluent builder pattern.

```java
MqttClient.builder()
        .identifier(UUID.randomUUID().toString())
        .serverHost("broker.hivemq.com")
        .serverPort(1883)
        ...;
```

The following configuration methods are available for all MQTT clients:

| Method name | Description | Default value |
| ----------- | ----------- | ------------- |
| `identifier` | The unique identifier of the MQTT client | The server creates an identifier for the client |
| `serverHost` | The host name or IP address of the MQTT server | `localhost` |
| `serverPort` | The port of the MQTT server | `1883` <br/> `8883` for SSL/TLS <br/> `80` for WebSocket <br/> `443` for WebSocket + SSL/TLS |
| `useSsl` <br/> `useSslWithDefaultConfig` | Whether SSL/TLS is used, see [Security](security/ssl_tls.md) | - |
| `useWebSocket` <br/> `useWebSocketWithDefaultConfig` | Whether WebSocket transport is used, see [Transports](transports/websocket.md) | - |
| `executorConfig` | Configuration of used Threads, see [Thread Management](thread_management.md) | Default Netty event loop and `Schedulers.comutation()` for callbacks |

You can not build an instance of `MqttClient` directly, but a version specific `Mqtt5Client` or `Mqtt3Client`.

{% capture tab_content %}

MQTT 5.0
===

You can use the generic `MqttClientBuilder` to set the above properties and then switch to the builder for the
`Mqtt5Client` by calling `useMqttVersion5()`:

```java
MqttClientBuilder clientBuilder = MqttClient.builder()
        .identifier(UUID.randomUUID().toString())
        .serverHost("broker.hivemq.com")

Mqtt5Client client = clientBuilder.useMqttVersion5().build();
```

Alternatively you can directly use a `Mqtt5ClientBuilder`:

```java
Mqtt5Client client = Mqtt5Client.builder()
        .identifier(UUID.randomUUID().toString())
        .serverHost("broker.hivemq.com")
        .build();
```

The `Mqtt5ClientBuilder` has the following additional configuration methods:

| Method name | Description | Default value |
| ----------- | ----------- | ------------- |
| `advancedConfig` | Advanced configuration options for special requirements | Sensible default values, you do not have to care about these if you do not require any of the advanced options |

====

MQTT 3.1.1
===

You can use the generic `MqttClientBuilder` to set the above properties and then switch to the builder for the
`Mqtt3Client` by calling `useMqttVersion3()`:

```java
MqttClientBuilder clientBuilder = MqttClient.builder()
        .identifier(UUID.randomUUID().toString())
        .serverHost("broker.hivemq.com")

Mqtt3Client client = clientBuilder.useMqttVersion3().build();
```

Alternatively you can directly use a `Mqtt3ClientBuilder`:

```java
Mqtt3Client client = Mqtt3Client.builder()
        .identifier(UUID.randomUUID().toString())
        .serverHost("broker.hivemq.com")
        .build();
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-version" %}