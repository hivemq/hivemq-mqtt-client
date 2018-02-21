package org.mqttbee.mqtt.message.connect;

import org.mqttbee.mqtt.message.MqttProperty;

/**
 * All possible MQTT CONNECT properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface MqttConnectProperty {

    int SESSION_EXPIRY_INTERVAL = MqttProperty.SESSION_EXPIRY_INTERVAL;
    int AUTHENTICATION_METHOD = MqttProperty.AUTHENTICATION_METHOD;
    int AUTHENTICATION_DATA = MqttProperty.AUTHENTICATION_DATA;
    int REQUEST_PROBLEM_INFORMATION = MqttProperty.REQUEST_PROBLEM_INFORMATION;
    int REQUEST_RESPONSE_INFORMATION = MqttProperty.REQUEST_RESPONSE_INFORMATION;
    int RECEIVE_MAXIMUM = MqttProperty.RECEIVE_MAXIMUM;
    int TOPIC_ALIAS_MAXIMUM = MqttProperty.TOPIC_ALIAS_MAXIMUM;
    int USER_PROPERTY = MqttProperty.USER_PROPERTY;
    int MAXIMUM_PACKET_SIZE = MqttProperty.MAXIMUM_PACKET_SIZE;

}
