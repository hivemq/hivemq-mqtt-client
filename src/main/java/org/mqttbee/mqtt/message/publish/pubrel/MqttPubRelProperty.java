package org.mqttbee.mqtt.message.publish.pubrel;

import org.mqttbee.mqtt.message.MqttProperty;

/**
 * All possible MQTT PUBREL properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface MqttPubRelProperty {

    int REASON_STRING = MqttProperty.REASON_STRING;
    int USER_PROPERTY = MqttProperty.USER_PROPERTY;

}
