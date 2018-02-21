package org.mqttbee.mqtt.message.publish.pubcomp;

import org.mqttbee.mqtt.message.MqttProperty;

/**
 * All possible MQTT PUBCOMP properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface MqttPubCompProperty {

    int REASON_STRING = MqttProperty.REASON_STRING;
    int USER_PROPERTY = MqttProperty.USER_PROPERTY;

}
