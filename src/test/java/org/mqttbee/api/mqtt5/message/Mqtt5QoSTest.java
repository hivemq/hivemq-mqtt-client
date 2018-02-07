package org.mqttbee.api.mqtt5.message;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Silvio Giebl
 */
public class Mqtt5QoSTest {

    @Test
    public void test_getCode_atMostOnce() {
        assertEquals(0, Mqtt5QoS.AT_MOST_ONCE.getCode());
    }

    @Test
    public void test_getCode_atLeastOnce() {
        assertEquals(1, Mqtt5QoS.AT_LEAST_ONCE.getCode());
    }

    @Test
    public void test_getCode_exactlyOnce() {
        assertEquals(2, Mqtt5QoS.EXACTLY_ONCE.getCode());
    }

    @Test
    public void test_fromCode_0() {
        assertSame(Mqtt5QoS.AT_MOST_ONCE, Mqtt5QoS.fromCode(0));
    }

    @Test
    public void test_fromCode_1() {
        assertSame(Mqtt5QoS.AT_LEAST_ONCE, Mqtt5QoS.fromCode(1));
    }

    @Test
    public void test_fromCode_2() {
        assertSame(Mqtt5QoS.EXACTLY_ONCE, Mqtt5QoS.fromCode(2));
    }

    @Test
    public void test_fromCode_3() {
        assertNull(Mqtt5QoS.fromCode(3));
    }

    @Test
    public void test_fromCode_negative() {
        assertNull(Mqtt5QoS.fromCode(-1));
    }

}