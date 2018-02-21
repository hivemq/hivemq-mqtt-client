package org.mqttbee.mqtt.message.subscribe.suback;

import org.mqttbee.mqtt.message.MqttProperty;

/**
 * All possible MQTT SUBACK properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface MqttSubAckProperty {

    int REASON_STRING = MqttProperty.REASON_STRING;
    int USER_PROPERTY = MqttProperty.USER_PROPERTY;

}
