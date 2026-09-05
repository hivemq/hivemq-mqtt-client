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

## Automatic reconnect

Automatic reconnect is disabled by default. Enable the default strategy on the client builder:

```java
Mqtt5Client client = Mqtt5Client.builder()
        .serverHost("broker.hivemq.com")
        .automaticReconnectWithDefaultConfig()
        .build();
```

The default strategy uses exponential backoff: 1 second initial delay, doubled after each failed attempt, capped at 2 minutes, plus a random delay of ±25%.

You can customize the delays:

```java
Mqtt5Client client = Mqtt5Client.builder()
        .serverHost("broker.hivemq.com")
        .automaticReconnect()
            .initialDelay(500, TimeUnit.MILLISECONDS)
            .maxDelay(3, TimeUnit.MINUTES)
            .applyAutomaticReconnect()
        .build();
```

`Mqtt3Client.builder()` has the same methods.

{% capture admonition_content %}
[HiveMQ MQTT Client Features: Reconnect Handling](https://www.hivemq.com/blog/hivemq-mqtt-client-features-reconnect-handling/){:target="_blank"}
{% endcapture %}{% include admonition.html type="tip" title="Additional Resources" content=admonition_content %}

### Stopping reconnect

`disconnect()` can only be called when the client is connected. While the client is connecting or reconnecting, `disconnect()` throws `MqttClientStateException` and automatic reconnect keeps running.

To stop reconnecting, add a disconnected listener that cancels reconnect. You can add this listener in addition to automatic reconnect; you do not need to reimplement the backoff. This works for MQTT 3 and MQTT 5.

```java
final AtomicBoolean stopReconnect = new AtomicBoolean(false);
final Mqtt5Client client = Mqtt5Client.builder()
        .automaticReconnectWithDefaultConfig()
        .addDisconnectedListener(context -> {
            if (stopReconnect.get()) {
                context.getReconnector().reconnect(false);
            }
        })
        .build();

// when you want to disconnect for good
stopReconnect.set(true);
client.toAsync().disconnect();
```

The same pattern works with `Mqtt3Client`:

```java
final AtomicBoolean stopReconnect = new AtomicBoolean(false);
final Mqtt3Client client = Mqtt3Client.builder()
        .automaticReconnectWithDefaultConfig()
        .addDisconnectedListener(context -> {
            if (stopReconnect.get()) {
                context.getReconnector().reconnect(false);
            }
        })
        .build();

stopReconnect.set(true);
client.toAsync().disconnect();
```

If the client is not connected (`disconnect()` would throw), still set `stopReconnect` to `true`. The disconnected listener runs after a failed connect or reconnect attempt and cancels further retries.

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