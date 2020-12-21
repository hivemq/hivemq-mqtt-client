/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.client2.mqtt.mqtt5.message;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class Mqtt5MessageTypeTest {

    @Test
    public void getCode_connect() {
        assertEquals(1, Mqtt5MessageType.CONNECT.getCode());
    }

    @Test
    public void getCode_connack() {
        assertEquals(2, Mqtt5MessageType.CONNACK.getCode());
    }

    @Test
    public void getCode_publish() {
        assertEquals(3, Mqtt5MessageType.PUBLISH.getCode());
    }

    @Test
    public void getCode_puback() {
        assertEquals(4, Mqtt5MessageType.PUBACK.getCode());
    }

    @Test
    public void getCode_pubrec() {
        assertEquals(5, Mqtt5MessageType.PUBREC.getCode());
    }

    @Test
    public void getCode_pubrel() {
        assertEquals(6, Mqtt5MessageType.PUBREL.getCode());
    }

    @Test
    public void getCode_pubcomp() {
        assertEquals(7, Mqtt5MessageType.PUBCOMP.getCode());
    }

    @Test
    public void getCode_subscribe() {
        assertEquals(8, Mqtt5MessageType.SUBSCRIBE.getCode());
    }

    @Test
    public void getCode_suback() {
        assertEquals(9, Mqtt5MessageType.SUBACK.getCode());
    }

    @Test
    public void getCode_unsubscribe() {
        assertEquals(10, Mqtt5MessageType.UNSUBSCRIBE.getCode());
    }

    @Test
    public void getCode_unsuback() {
        assertEquals(11, Mqtt5MessageType.UNSUBACK.getCode());
    }

    @Test
    public void getCode_pingreq() {
        assertEquals(12, Mqtt5MessageType.PINGREQ.getCode());
    }

    @Test
    public void getCode_pingresp() {
        assertEquals(13, Mqtt5MessageType.PINGRESP.getCode());
    }

    @Test
    public void getCode_disconnect() {
        assertEquals(14, Mqtt5MessageType.DISCONNECT.getCode());
    }

    @Test
    public void getCode_auth() {
        assertEquals(15, Mqtt5MessageType.AUTH.getCode());
    }

    @Test
    public void fromCode_0() {
        assertNull(Mqtt5MessageType.fromCode(0));
    }

    @Test
    public void fromCode_1() {
        assertSame(Mqtt5MessageType.CONNECT, Mqtt5MessageType.fromCode(1));
    }

    @Test
    public void fromCode_2() {
        assertSame(Mqtt5MessageType.CONNACK, Mqtt5MessageType.fromCode(2));
    }

    @Test
    public void fromCode_3() {
        assertSame(Mqtt5MessageType.PUBLISH, Mqtt5MessageType.fromCode(3));
    }

    @Test
    public void fromCode_4() {
        assertSame(Mqtt5MessageType.PUBACK, Mqtt5MessageType.fromCode(4));
    }

    @Test
    public void fromCode_5() {
        assertSame(Mqtt5MessageType.PUBREC, Mqtt5MessageType.fromCode(5));
    }

    @Test
    public void fromCode_6() {
        assertSame(Mqtt5MessageType.PUBREL, Mqtt5MessageType.fromCode(6));
    }

    @Test
    public void fromCode_7() {
        assertSame(Mqtt5MessageType.PUBCOMP, Mqtt5MessageType.fromCode(7));
    }

    @Test
    public void fromCode_8() {
        assertSame(Mqtt5MessageType.SUBSCRIBE, Mqtt5MessageType.fromCode(8));
    }

    @Test
    public void fromCode_9() {
        assertSame(Mqtt5MessageType.SUBACK, Mqtt5MessageType.fromCode(9));
    }

    @Test
    public void fromCode_10() {
        assertSame(Mqtt5MessageType.UNSUBSCRIBE, Mqtt5MessageType.fromCode(10));
    }

    @Test
    public void fromCode_11() {
        assertSame(Mqtt5MessageType.UNSUBACK, Mqtt5MessageType.fromCode(11));
    }

    @Test
    public void fromCode_12() {
        assertSame(Mqtt5MessageType.PINGREQ, Mqtt5MessageType.fromCode(12));
    }

    @Test
    public void fromCode_13() {
        assertSame(Mqtt5MessageType.PINGRESP, Mqtt5MessageType.fromCode(13));
    }

    @Test
    public void fromCode_14() {
        assertSame(Mqtt5MessageType.DISCONNECT, Mqtt5MessageType.fromCode(14));
    }

    @Test
    public void fromCode_15() {
        assertSame(Mqtt5MessageType.AUTH, Mqtt5MessageType.fromCode(15));
    }

    @Test
    public void fromCode_16() {
        assertNull(Mqtt5MessageType.fromCode(16));
    }

    @Test
    public void fromCode_negative() {
        assertNull(Mqtt5MessageType.fromCode(-1));
    }
}