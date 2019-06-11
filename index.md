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

{% include news.md %}
