package org.mqttbee.mqtt5.message.connect;

import org.mqttbee.mqtt5.message.Mqtt5Property;

/**
 * All possible MQTT CONNECT properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5ConnectProperty {

    int SESSION_EXPIRY_INTERVAL = Mqtt5Property.SESSION_EXPIRY_INTERVAL;
    int AUTHENTICATION_METHOD = Mqtt5Property.AUTHENTICATION_METHOD;
    int AUTHENTICATION_DATA = Mqtt5Property.AUTHENTICATION_DATA;
    int REQUEST_PROBLEM_INFORMATION = Mqtt5Property.REQUEST_PROBLEM_INFORMATION;
    int REQUEST_RESPONSE_INFORMATION = Mqtt5Property.REQUEST_RESPONSE_INFORMATION;
    int RECEIVE_MAXIMUM = Mqtt5Property.RECEIVE_MAXIMUM;
    int TOPIC_ALIAS_MAXIMUM = Mqtt5Property.TOPIC_ALIAS_MAXIMUM;
    int USER_PROPERTY = Mqtt5Property.USER_PROPERTY;
    int MAXIMUM_PACKET_SIZE = Mqtt5Property.MAXIMUM_PACKET_SIZE;

}
