package org.mqttbee.mqtt5.message;


import org.junit.jupiter.api.Test;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5RetainHandling;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author David Katz
 */
class Mqtt5RetainHandlingTest {

    @Test
    void test_getCode_send() {
        assertEquals(0x00, Mqtt5RetainHandling.SEND.getCode());
        assertEquals(Mqtt5RetainHandling.SEND, Mqtt5RetainHandling.fromCode(0x00));
    }

    @Test
    void test_getCode_sendIfSubscriptionDoesNotExist() {
        assertEquals(0x01, Mqtt5RetainHandling.SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST.getCode());
        assertEquals(Mqtt5RetainHandling.SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST, Mqtt5RetainHandling.fromCode(0x01));
    }

    @Test
    void test_getCode_doNotSend() {
        assertEquals(0x02, Mqtt5RetainHandling.DO_NOT_SEND.getCode());
        assertEquals(Mqtt5RetainHandling.DO_NOT_SEND, Mqtt5RetainHandling.fromCode(0x02));
    }

    @Test
    void test_invalidCode() {
        assertNull(Mqtt5RetainHandling.fromCode(0xFF));
    }
}
