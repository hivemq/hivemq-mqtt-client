---
nav_order: 7
---

# Advanced Usage

## Manual Message Acknowledgement

Manual message acknowledgment is a technique used in MQTT (Message Queuing Telemetry Transport) protocol to ensure message delivery reliability between clients and brokers. When a QoS level 1 or 2 message is published to a topic, the sender expects confirmation (acknowledgment) from the recipient that the message has been received and processed successfully.   By default this is an automatic operation that happens in the background.

As an alternative, the HiveMQ Java MQTT client allows for conditionally acknowledge received messages.  In this case, the recipient (subscriber) explicitly acknowledges the message after processing it, providing an assurance to the sender.

With the HiveMQ Java MQTT client you can:

* Selectively enable manual acknowledgment for specific streams

* Acknowledge messages that are emitted to multiple streams independently per stream (the client aggregates the acknowledgments before sending MQTT acknowledgments)

* Order of manual acknowledgment does not matter (the client automatically ensures the order of MQTT acknowledgments for 100% compatibility with the MQTT specification)

### Example

```java
final Mqtt5SubAck subAck = client.subscribeWith()
    .topicFilter("demo/topic/a")
    .manualAcknowledgement(true) // Enable manual acknowledgement for this subscription
    .callback(publish -> {
        boolean success = false;

        // Some logic & conditions

        if (success) {
            publish.acknowledge();  // Conditionally acknowledge the message
        }
    })
    .send().join();
```

