---
layout: default_anchor_headings
title: API Flavours
nav_order: 4
---

# API Flavours

HiveMQ MQTT Client offers 3 different flavours of API:

- Blocking
- Asynchronous
- Reactive

Each of them has its own interface, so they are clearly separated and their methods can not be confused.
Nevertheless it is possible to switch the API flavour at any time and use different API flavours of the same client 
concurrently.

For switching the API flavour simply call one of the `to...` methods as listed in the table below.
Alternatively you can directly specify the API flavour when building the client by calling a specific `build...` method.

| API flavour  | Switch method  | Builder method    |
| ------------ | -------------- | ----------------- |
| Blocking     | `toBlocking()` | `buildBlocking()` |
| Asynchronous | `toAsync()`    | `buildAsync()`    |
| Reactive     | `toRx()`       | `buildRx()`       |


{% capture tab_content %}

Blocking
===

The methods of the blocking API flavour directly return the results and block the current thread until those are 
available.

{% capture tab_content %}

MQTT 5.0
===

The interface of the blocking API flavour of a MQTT 5 Client is `Mqtt5BlockingClient`.

Switching to the blocking API flavour:

```java
Mqtt5Client client = ...; // could be Mqtt5BlockingClient, Mqtt5AsyncClient or Mqtt5RxClient
Mqtt5BlockingClient blockingClient = client.toBlocking();
```

Building it directly:

```java
Mqtt5BlockingClient blockingClient = Mqtt5Client.builder().buildBlocking();
```

====

MQTT 3.1.1
===

The interface of the blocking API flavour of a MQTT 3 Client is `Mqtt3BlockingClient`.

Switching to the blocking API flavour:

```java
Mqtt3Client client = ...; // could be Mqtt3BlockingClient, Mqtt3AsyncClient or Mqtt3RxClient
Mqtt3BlockingClient blockingClient = client.toBlocking();
```

Building it directly:

```java
Mqtt3BlockingClient blockingClient = Mqtt3Client.builder().buildBlocking();
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-version" %}

====


Asynchronous
===

The methods of the asynchronous API flavour have in common that they return `CompletableFuture` instead of waiting for 
the result. Additionally callbacks are used for the streams of incoming Publish messages.

{% capture tab_content %}

MQTT 5.0
===

The interface of the asynchronous API flavour of a MQTT 5 Client is `Mqtt5AsyncClient`.

Switching to the asynchronous API flavour:

```java
Mqtt5Client client = ...; // could be Mqtt5BlockingClient, Mqtt5AsyncClient or Mqtt5RxClient
Mqtt5AsyncClient asyncClient = client.toAsync();
```

Building it directly:

```java
Mqtt5AsyncClient asyncClient = Mqtt5Client.builder().buildAsync();
```

====

MQTT 3.1.1
===

The interface of the asynchronous API flavour of a MQTT 3 Client is `Mqtt3AsyncClient`.

Switching to the asynchronous API flavour:

```java
Mqtt3Client client = ...; // could be Mqtt3BlockingClient, Mqtt3AsyncClient or Mqtt3RxClient
Mqtt3AsyncClient asyncClient = client.toAsync();
```

Building it directly:

```java
Mqtt3AsyncClient asyncClient = Mqtt3Client.builder().buildAsync();
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-version" %}

====


Reactive
===

The reactive API flavour uses [RxJava](https://github.com/ReactiveX/RxJava) which is an implementation of 
[Reactive Streams](http://www.reactive-streams.org) and the [Reactive Extensions](http://reactivex.io).

As RxJava is compliant to the Reactive Streams specification it is easily interoperable with other reactive libraries 
such as Reactor Core.

The reactive API flavour uses the RxJava data types `Flowable`, `Single` and `Completable` to expressively declare 
return types.

{% capture tab_content %}

MQTT 5.0
===

The interface of the reactive API flavour of a MQTT 5 Client is `Mqtt5RxClient`.

Switching to the reactive API flavour:

```java
Mqtt5Client client = ...; // could be Mqtt5BlockingClient, Mqtt5AsyncClient or Mqtt5RxClient
Mqtt5RxClient rxClient = client.toRx();
```

Building it directly:

```java
Mqtt5RxClient rxClient = Mqtt5Client.builder().buildRx();
```

====

MQTT 3.1.1
===

The interface of the reactive API flavour of a MQTT 3 Client is `Mqtt3RxClient`.

Switching to the reactive API flavour:

```java
Mqtt3Client client = ...; // could be Mqtt3BlockingClient, Mqtt3AsyncClient or Mqtt3RxClient
Mqtt3RxClient rxClient = client.toRx();
```

Building it directly:

```java
Mqtt3RxClient rxClient = Mqtt3Client.builder().buildRx();
```

{% endcapture %}
{% include tabs.html tab_group="mqtt-version" %}

{% endcapture %}
{% include tabs.html tab_group="api-flavour" %}