---
layout: default
title: Home
nav_exclude: true
---

![Logo]({{ site.logo }})
{: .banner }

# What is MQTT Bee?

MQTT Bee is a MQTT 5.0 and MQTT 3.1.1 compatible and feature-rich high-performance Java client library with different 
API flavours and backpressure support.

MQTT Bee is an Open Source project backed by dc-square (the company behind the MQTT Broker 
[HiveMQ](https://www.hivemq.com/)) and BMW CarIT.

[View it on GitHub](https://github.com/mqtt-bee/mqtt-bee){: .btn .btn-yellow }

# Features

- **All MQTT 3.1.1 and MQTT 5.0 features**
- API flavors:
  - **Reactive**, **Async** and **Blocking**
  - Flexible switching
  - Consistent and clearly separated
- **Backpressure support**:
  - QoS 1 and 2
  - QoS 0 (dropping incoming messages if necessary)
- **SSL/TLS**
- WebSocket transport
- Automatic and configurable **thread management**
- MQTT 5 specific:
  - Pluggable Enhanced Auth support
    - Additional to MQTT specification: server-triggered reauth
  - Automatic Topic Alias mapping
  - Interceptors for QoS flows

{% include news.md %}
