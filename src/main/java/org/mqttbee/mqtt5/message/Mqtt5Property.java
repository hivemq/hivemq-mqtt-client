package org.mqttbee.mqtt5.message;

/**
 * All possible MQTT properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5Property {

    int PAYLOAD_FORMAT_INDICATOR = 0x01;
    int MESSAGE_EXPIRY_INTERVAL = 0x02;
    int CONTENT_TYPE = 0x03;
    int RESPONSE_TOPIC = 0x08;
    int CORRELATION_DATA = 0x09;
    int SUBSCRIPTION_IDENTIFIER = 0x0B;
    int SESSION_EXPIRY_INTERVAL = 0x11;
    int ASSIGNED_CLIENT_IDENTIFIER = 0x12;
    int SERVER_KEEP_ALIVE = 0x13;
    int AUTHENTICATION_METHOD = 0x15;
    int AUTHENTICATION_DATA = 0x16;
    int REQUEST_PROBLEM_INFORMATION = 0x17;
    int WILL_DELAY_INTERVAL = 0x18;
    int REQUEST_RESPONSE_INFORMATION = 0x19;
    int RESPONSE_INFORMATION = 0x1A;
    int SERVER_REFERENCE = 0x1C;
    int REASON_STRING = 0x1F;
    int RECEIVE_MAXIMUM = 0x21;
    int TOPIC_ALIAS_MAXIMUM = 0x22;
    int TOPIC_ALIAS = 0x23;
    int MAXIMUM_QOS = 0x24;
    int RETAIN_AVAILABLE = 0x25;
    int USER_PROPERTY = 0x26;
    int MAXIMUM_PACKET_SIZE = 0x27;
    int WILDCARD_SUBSCRIPTION_AVAILABLE = 0x28;
    int SUBSCRIPTION_IDENTIFIER_AVAILABLE = 0x29;
    int SHARED_SUBSCRIPTION_AVAILABLE = 0x2A;

}
