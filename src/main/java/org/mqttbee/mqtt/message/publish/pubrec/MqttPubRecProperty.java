package org.mqttbee.mqtt.message.publish.pubrec;

import org.mqttbee.mqtt.message.MqttProperty;

/**
 * All possible MQTT PUBREC properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface MqttPubRecProperty {

    int REASON_STRING = MqttProperty.REASON_STRING;
    int USER_PROPERTY = MqttProperty.USER_PROPERTY;

}
