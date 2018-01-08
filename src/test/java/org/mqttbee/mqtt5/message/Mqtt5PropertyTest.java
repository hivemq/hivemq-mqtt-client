package org.mqttbee.mqtt5.message;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PropertyTest {

    @Test
    public void test_all_properties() {
        assertEquals(0x01, Mqtt5Property.PAYLOAD_FORMAT_INDICATOR);
        assertEquals(0x02, Mqtt5Property.MESSAGE_EXPIRY_INTERVAL);
        assertEquals(0x03, Mqtt5Property.CONTENT_TYPE);
        assertEquals(0x08, Mqtt5Property.RESPONSE_TOPIC);
        assertEquals(0x09, Mqtt5Property.CORRELATION_DATA);
        assertEquals(0x0B, Mqtt5Property.SUBSCRIPTION_IDENTIFIER);
        assertEquals(0x11, Mqtt5Property.SESSION_EXPIRY_INTERVAL);
        assertEquals(0x12, Mqtt5Property.ASSIGNED_CLIENT_IDENTIFIER);
        assertEquals(0x13, Mqtt5Property.SERVER_KEEP_ALIVE);
        assertEquals(0x15, Mqtt5Property.AUTHENTICATION_METHOD);
        assertEquals(0x16, Mqtt5Property.AUTHENTICATION_DATA);
        assertEquals(0x17, Mqtt5Property.REQUEST_PROBLEM_INFORMATION);
        assertEquals(0x18, Mqtt5Property.WILL_DELAY_INTERVAL);
        assertEquals(0x19, Mqtt5Property.REQUEST_RESPONSE_INFORMATION);
        assertEquals(0x1A, Mqtt5Property.RESPONSE_INFORMATION);
        assertEquals(0x1C, Mqtt5Property.SERVER_REFERENCE);
        assertEquals(0x1F, Mqtt5Property.REASON_STRING);
        assertEquals(0x21, Mqtt5Property.RECEIVE_MAXIMUM);
        assertEquals(0x22, Mqtt5Property.TOPIC_ALIAS_MAXIMUM);
        assertEquals(0x23, Mqtt5Property.TOPIC_ALIAS);
        assertEquals(0x24, Mqtt5Property.MAXIMUM_QOS);
        assertEquals(0x25, Mqtt5Property.RETAIN_AVAILABLE);
        assertEquals(0x26, Mqtt5Property.USER_PROPERTY);
        assertEquals(0x27, Mqtt5Property.MAXIMUM_PACKET_SIZE);
        assertEquals(0x28, Mqtt5Property.WILDCARD_SUBSCRIPTION_AVAILABLE);
        assertEquals(0x29, Mqtt5Property.SUBSCRIPTION_IDENTIFIER_AVAILABLE);
        assertEquals(0x2A, Mqtt5Property.SHARED_SUBSCRIPTION_AVAILABLE);
    }

}