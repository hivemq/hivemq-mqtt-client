---
layout: default
title: Home
nav_exclude: true
---

![Logo]({{ site.logo | absolute_url }})
{: .banner }


# What is HiveMQ MQTT Client?

HiveMQ MQTT Client is an MQTT 5.0 and MQTT 3.1.1 compatible and feature-rich high-performance Java client library with 
different API flavours and backpressure support.

HiveMQ MQTT Client is an Open Source project backed by [HiveMQ](https://www.hivemq.com/) and BMW CarIT.

[View it on GitHub](https://github.com/hivemq/hivemq-mqtt-client){: .btn .btn-yellow }


# Features

- **All [MQTT 3.1.1](http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/errata01/os/mqtt-v3.1.1-errata01-os-complete.html) and 
  [MQTT 5.0](https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html) features**
- API flavors:
  - **Reactive**: [Reactive Streams](https://www.reactive-streams.org/) compatible, 
    [RxJava](https://github.com/ReactiveX/RxJava) and
    [Reactor](https://github.com/reactor/reactor-core) APIs are available
  - **Asynchronous API**: futures and callbacks
  - **Blocking API**: for quick start and testing
  - Switch flexibly between flavours and use them concurrently
  - Flavours are clearly separated but have a consistent API style
- **Backpressure support**:
  - QoS 1 and 2
  - QoS 0 (dropping incoming messages, if necessary)
  - Bringing MQTT flow control and reactive pull backpressure together
- Transports:
  - TCP
  - **SSL/TLS**
    - All TLS versions up to TLS 1.3 are supported
    - TLS mutual authentication
    - TLS Server Name Indication (SNI)
    - TLS Session Resumption
    - Default and customizable hostname verification
  - **WebSocket**, Secure WebSocket
  - **Proxy**: SOCKS4, SOCKS5, HTTP CONNECT
  - All possible combinations
- Automatic and configurable **thread management**
- Automatic and configurable **reconnect handling and message redelivery**
- Automatic and configurable **resubscribe if the session expired**
- **Manual message acknowledgment**
  - Selectively enable manual acknowledgment for specific streams
  - Acknowledge messages that are emitted to multiple streams independently per stream
    (the client aggregates the acknowledgments before sending MQTT acknowledgments)
  - Order of manual acknowledgment does not matter
    (the client automatically ensures the order of MQTT acknowledgments for 100% compatibility with the MQTT specification)
- Lifecycle listeners
  - When connected
  - When disconnected or connection failed
- MQTT 5 specific:
  - Pluggable Enhanced Authentication support (additional to MQTT specification: server-triggered re-authentication)
  - Automatic Topic Alias mapping
  - Interceptors for QoS flows


# Users

<style>
.users {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
}

.user-link {
  padding: 0.5rem;
}

.user-img {
  display: block;
  max-height: 6rem;
  padding: 0.5rem;
  border-radius: 0.5rem;
  background-color: white;
  transition: transform 200ms ease;
}

.user-link:hover {
  overflow: visible;
}

.user-link:hover .user-img {
  transform: scale(1.05);
}
</style>

<div class="users">

  <a href="https://github.com/bmwcarit" class="user-link">
    <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/BMW.svg/300px-BMW.svg.png" alt="BMW CarIT" class="user-img"/>
  </a>

  <a href="https://github.com/bmwcarit/joynr" class="user-link">
    <img src="https://github.com/bmwcarit/joynr/raw/master/graphics/joynr-logo.png" alt="joynr" class="user-img"/>
  </a>

  <a href="https://www.openhab.org/" class="user-link">
    <img src="https://www.openhab.org/openhab-logo.png" alt="openHAB" class="user-img"/>
  </a>

  <a href="https://github.com/eclipse/ditto" class="user-link">
    <img src="https://eclipse.org/ditto/images/ditto.svg" alt="Eclipse Ditto" class="user-img"/>
  </a>

  <a href="https://github.com/OSGP/open-smart-grid-platform" class="user-link">
    <img src="https://avatars3.githubusercontent.com/u/11352045?s=200&v=4" alt="Open Smart Grid Platform" class="user-img"/>
  </a>

  <a href="https://github.com/EXXETA/correomqtt" class="user-link">
    <img src="https://raw.githubusercontent.com/EXXETA/correomqtt/develop/icon/ico/Icon_128x128.png" alt="CorreoMQTT" class="user-img"/>
  </a>

</div>

If you use the HiveMQ MQTT Client in a project that is not listed here, feel free to open an issue or pull request.


# Contributors

<style>
.contributors {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
}

.contributor-link {
  padding: 0.25rem;
}

.contributor-img {
  display: block;
  height: 3rem;
  width: 3rem;
  border-radius: 50%;
  transition: transform 200ms ease;
}

.contributor-link:hover {
  overflow: visible;
}

.contributor-link:hover .contributor-img {
  transform: scale(1.1);
}
</style>

<div class="contributors">
{% for contributor in site.github.contributors %}
  <a href="{{ contributor.html_url }}" class="contributor-link">
    <img src="{{ contributor.avatar_url }}" alt="{{ contributor.login }}" class="contributor-img"/>
  </a>
{% endfor %}
</div>


{% include news.md %}
