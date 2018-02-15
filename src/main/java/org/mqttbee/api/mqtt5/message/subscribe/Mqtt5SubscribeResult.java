package org.mqttbee.api.mqtt5.message.subscribe;

import org.mqttbee.api.mqtt5.message.Mqtt5Message;

/**
 * Marker interface for MQTT messages sent by the server because of a Subscribe message sent by the client.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5SubscribeResult extends Mqtt5Message {
}
