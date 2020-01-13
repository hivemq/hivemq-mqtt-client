---
layout: default
title: Home
nav_exclude: true
---

![Logo]({{ site.logo | absolute_url }})
{: .banner }


# What is HiveMQ MQTT Client?

HiveMQ MQTT Client is a MQTT 5.0 and MQTT 3.1.1 compatible and feature-rich high-performance Java client library with 
different API flavours and backpressure support.

HiveMQ MQTT Client is an Open Source project backed by [HiveMQ](https://www.hivemq.com/) and BMW CarIT.

[View it on GitHub](https://github.com/hivemq/hivemq-mqtt-client){: .btn .btn-yellow }


# Features

- **All MQTT 3.1.1 and MQTT 5.0 features**
- API flavors:
  - **Reactive**, **Asynchronous** and **Blocking**
  - Flexible switching
  - Consistent and clearly separated
- **Backpressure support**:
  - QoS 1 and 2
  - QoS 0 (dropping incoming messages, if necessary)
- Transports:
  - TCP
  - **SSL/TLS**
  - WebSocket, Secure WebSocket
- Automatic and configurable **thread management**
- Automatic and configurable **reconnect handling** and message redelivery
- Lifecycle listeners (connected, disconnected)
- MQTT 5 specific:
  - Pluggable Enhanced Auth support (additional to MQTT specification: server-triggered reauth)
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
  max-height: 100px;
  padding: 0.5rem;
  border-radius: 4px;
  background-color: white;
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

</div>

If you use the HiveMQ MQTT Client in a project that is not listed here, feel free to open an issue or pull request.


# Contributors

<ul class="list-style-none">
{% for contributor in site.github.contributors %}
  <li class="d-inline-block mr-1">
     <a href="{{ contributor.html_url }}"><img src="{{ contributor.avatar_url }}" width="32" height="32" alt="{{ contributor.login }}"/></a>
  </li>
{% endfor %}
</ul>


{% include news.md %}
