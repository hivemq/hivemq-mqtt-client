---
nav_order: 3
redirect_from: /docs/client_configuration.html
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

## General configuration

| Method name | Description | Default value |
| ----------- | ----------- | ------------- |
| `identifier` | The unique identifier of the MQTT client | The server creates an identifier for the client |
| `executorConfig` | Configuration of used Threads | Default Netty event loop and `Schedulers.comutation()` for callbacks |

## Transport configuration

| Method name | Description | Default value |
| ----------- | ----------- | ------------- |
| `serverAddress` | The address (host + port) of the MQTT server | See `serverHost` and `serverPort` |
| `serverHost` | The host name or IP address of the MQTT server | `localhost` |
| `serverPort` | The port of the MQTT server | - `1883` <br/> - `8883` (SSL/TLS) <br/> - `80` (WebSocket) <br/> - `443` (Secure WebSocket) |
| `sslConfig` <br/> `sslWithDefaultConfig` | Secure transport configuration (SSL/TLS) | none |
| `webSocketConfig` <br/> `webSocketWithDefaultConfig` | WebSocket transport configuration | none |
| `transportConfig` | Transport configuration which combines: <br/> - server address (host + port) <br/> - secure transport configuration <br/> - WebSocket transport configuration | See <br/> - `serverAddress` /`Host` /`Port` <br/> - `sslConfig` <br/> - `webSocketConfig` |

## Lifecycle configuration

| Method name | Description | Default value |
| ----------- | ----------- | ------------- |
| `automaticReconnect` <br/> `automaticReconnectWithDefaultConfig` | Automatic reconnect configuration | none |
| `addConnectedListener` | Adds a listener that is notified when the client is connected | none |
| `addDisconnectedListener` | Adds a listener that is notified when the client is disconnected | none |

---

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

| Method name | Description | Default value |
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
{% include tabs.html group="mqtt-version" content=tab_content %}