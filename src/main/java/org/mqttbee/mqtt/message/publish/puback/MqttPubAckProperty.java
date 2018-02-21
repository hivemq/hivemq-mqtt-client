package org.mqttbee.mqtt.message.publish.puback;

import org.mqttbee.mqtt.message.MqttProperty;

/**
 * All possible MQTT PUBACK properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface MqttPubAckProperty {

    int REASON_STRING = MqttProperty.REASON_STRING;
    int USER_PROPERTY = MqttProperty.USER_PROPERTY;

}
