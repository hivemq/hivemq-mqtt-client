package org.mqttbee.mqtt.message;

import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Silvio Giebl
 */
public class MqttMessageTypeTest {

    @Test
    public void test_getCode_connect() {
        assertEquals(1, MqttMessageType.CONNECT.getCode());
    }

    @Test
    public void test_getCode_connack() {
        assertEquals(2, MqttMessageType.CONNACK.getCode());
    }

    @Test
    public void test_getCode_publish() {
        assertEquals(3, MqttMessageType.PUBLISH.getCode());
    }

    @Test
    public void test_getCode_puback() {
        assertEquals(4, MqttMessageType.PUBACK.getCode());
    }

    @Test
    public void test_getCode_pubrec() {
        assertEquals(5, MqttMessageType.PUBREC.getCode());
    }

    @Test
    public void test_getCode_pubrel() {
        assertEquals(6, MqttMessageType.PUBREL.getCode());
    }

    @Test
    public void test_getCode_pubcomp() {
        assertEquals(7, MqttMessageType.PUBCOMP.getCode());
    }

    @Test
    public void test_getCode_subscribe() {
        assertEquals(8, MqttMessageType.SUBSCRIBE.getCode());
    }

    @Test
    public void test_getCode_suback() {
        assertEquals(9, MqttMessageType.SUBACK.getCode());
    }

    @Test
    public void test_getCode_unsubscribe() {
        assertEquals(10, MqttMessageType.UNSUBSCRIBE.getCode());
    }

    @Test
    public void test_getCode_unsuback() {
        assertEquals(11, MqttMessageType.UNSUBACK.getCode());
    }

    @Test
    public void test_getCode_pingreq() {
        assertEquals(12, MqttMessageType.PINGREQ.getCode());
    }

    @Test
    public void test_getCode_pingresp() {
        assertEquals(13, MqttMessageType.PINGRESP.getCode());
    }

    @Test
    public void test_getCode_disconnect() {
        assertEquals(14, MqttMessageType.DISCONNECT.getCode());
    }

    @Test
    public void test_getCode_auth() {
        assertEquals(15, MqttMessageType.AUTH.getCode());
    }

    @Test
    public void test_fromCode_0() {
        assertSame(MqttMessageType.RESERVED_ZERO, MqttMessageType.fromCode(0));
    }

    @Test
    public void test_fromCode_1() {
        assertSame(MqttMessageType.CONNECT, MqttMessageType.fromCode(1));
    }

    @Test
    public void test_fromCode_2() {
        assertSame(MqttMessageType.CONNACK, MqttMessageType.fromCode(2));
    }

    @Test
    public void test_fromCode_3() {
        assertSame(MqttMessageType.PUBLISH, MqttMessageType.fromCode(3));
    }

    @Test
    public void test_fromCode_4() {
        assertSame(MqttMessageType.PUBACK, MqttMessageType.fromCode(4));
    }

    @Test
    public void test_fromCode_5() {
        assertSame(MqttMessageType.PUBREC, MqttMessageType.fromCode(5));
    }

    @Test
    public void test_fromCode_6() {
        assertSame(MqttMessageType.PUBREL, MqttMessageType.fromCode(6));
    }

    @Test
    public void test_fromCode_7() {
        assertSame(MqttMessageType.PUBCOMP, MqttMessageType.fromCode(7));
    }

    @Test
    public void test_fromCode_8() {
        assertSame(MqttMessageType.SUBSCRIBE, MqttMessageType.fromCode(8));
    }

    @Test
    public void test_fromCode_9() {
        assertSame(MqttMessageType.SUBACK, MqttMessageType.fromCode(9));
    }

    @Test
    public void test_fromCode_10() {
        assertSame(MqttMessageType.UNSUBSCRIBE, MqttMessageType.fromCode(10));
    }

    @Test
    public void test_fromCode_11() {
        assertSame(MqttMessageType.UNSUBACK, MqttMessageType.fromCode(11));
    }

    @Test
    public void test_fromCode_12() {
        assertSame(MqttMessageType.PINGREQ, MqttMessageType.fromCode(12));
    }

    @Test
    public void test_fromCode_13() {
        assertSame(MqttMessageType.PINGRESP, MqttMessageType.fromCode(13));
    }

    @Test
    public void test_fromCode_14() {
        assertSame(MqttMessageType.DISCONNECT, MqttMessageType.fromCode(14));
    }

    @Test
    public void test_fromCode_15() {
        assertSame(MqttMessageType.AUTH, MqttMessageType.fromCode(15));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_fromCode_16() {
        MqttMessageType.fromCode(16);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_fromCode_negative() {
        MqttMessageType.fromCode(-1);
    }

}