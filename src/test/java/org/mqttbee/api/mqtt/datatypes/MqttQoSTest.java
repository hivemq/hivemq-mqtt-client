package org.mqttbee.api.mqtt.datatypes;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Silvio Giebl
 */
public class MqttQoSTest {

    @Test
    public void test_getCode_atMostOnce() {
        assertEquals(0, MqttQoS.AT_MOST_ONCE.getCode());
    }

    @Test
    public void test_getCode_atLeastOnce() {
        assertEquals(1, MqttQoS.AT_LEAST_ONCE.getCode());
    }

    @Test
    public void test_getCode_exactlyOnce() {
        assertEquals(2, MqttQoS.EXACTLY_ONCE.getCode());
    }

    @Test
    public void test_fromCode_0() {
        assertSame(MqttQoS.AT_MOST_ONCE, MqttQoS.fromCode(0));
    }

    @Test
    public void test_fromCode_1() {
        assertSame(MqttQoS.AT_LEAST_ONCE, MqttQoS.fromCode(1));
    }

    @Test
    public void test_fromCode_2() {
        assertSame(MqttQoS.EXACTLY_ONCE, MqttQoS.fromCode(2));
    }

    @Test
    public void test_fromCode_3() {
        assertNull(MqttQoS.fromCode(3));
    }

    @Test
    public void test_fromCode_negative() {
        assertNull(MqttQoS.fromCode(-1));
    }

}