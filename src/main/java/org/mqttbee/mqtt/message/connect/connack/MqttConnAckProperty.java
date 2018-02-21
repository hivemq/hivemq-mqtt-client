package org.mqttbee.mqtt.message.connect.connack;

import org.mqttbee.mqtt.message.MqttProperty;

/**
 * All possible MQTT CONNACK properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface MqttConnAckProperty {

    int SESSION_EXPIRY_INTERVAL = MqttProperty.SESSION_EXPIRY_INTERVAL;
    int ASSIGNED_CLIENT_IDENTIFIER = MqttProperty.ASSIGNED_CLIENT_IDENTIFIER;
    int SERVER_KEEP_ALIVE = MqttProperty.SERVER_KEEP_ALIVE;
    int AUTHENTICATION_METHOD = MqttProperty.AUTHENTICATION_METHOD;
    int AUTHENTICATION_DATA = MqttProperty.AUTHENTICATION_DATA;
    int RESPONSE_INFORMATION = MqttProperty.RESPONSE_INFORMATION;
    int SERVER_REFERENCE = MqttProperty.SERVER_REFERENCE;
    int REASON_STRING = MqttProperty.REASON_STRING;
    int RECEIVE_MAXIMUM = MqttProperty.RECEIVE_MAXIMUM;
    int TOPIC_ALIAS_MAXIMUM = MqttProperty.TOPIC_ALIAS_MAXIMUM;
    int MAXIMUM_QOS = MqttProperty.MAXIMUM_QOS;
    int RETAIN_AVAILABLE = MqttProperty.RETAIN_AVAILABLE;
    int USER_PROPERTY = MqttProperty.USER_PROPERTY;
    int MAXIMUM_PACKET_SIZE = MqttProperty.MAXIMUM_PACKET_SIZE;
    int WILDCARD_SUBSCRIPTION_AVAILABLE = MqttProperty.WILDCARD_SUBSCRIPTION_AVAILABLE;
    int SUBSCRIPTION_IDENTIFIER_AVAILABLE = MqttProperty.SUBSCRIPTION_IDENTIFIER_AVAILABLE;
    int SHARED_SUBSCRIPTION_AVAILABLE = MqttProperty.SHARED_SUBSCRIPTION_AVAILABLE;

}
