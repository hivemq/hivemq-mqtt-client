package org.mqttbee.mqtt.message.auth;

import org.mqttbee.mqtt.message.MqttProperty;

/**
 * All possible MQTT AUTH properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface MqttAuthProperty {

    int AUTHENTICATION_METHOD = MqttProperty.AUTHENTICATION_METHOD;
    int AUTHENTICATION_DATA = MqttProperty.AUTHENTICATION_DATA;
    int REASON_STRING = MqttProperty.REASON_STRING;
    int USER_PROPERTY = MqttProperty.USER_PROPERTY;

}
