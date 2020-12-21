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

package com.hivemq.client2.mqtt.mqtt3.message;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class Mqtt3MessageTypeTest {

    @Test
    public void getCode_connect() {
        assertEquals(1, Mqtt3MessageType.CONNECT.getCode());
    }

    @Test
    public void getCode_connack() {
        assertEquals(2, Mqtt3MessageType.CONNACK.getCode());
    }

    @Test
    public void getCode_publish() {
        assertEquals(3, Mqtt3MessageType.PUBLISH.getCode());
    }

    @Test
    public void getCode_puback() {
        assertEquals(4, Mqtt3MessageType.PUBACK.getCode());
    }

    @Test
    public void getCode_pubrec() {
        assertEquals(5, Mqtt3MessageType.PUBREC.getCode());
    }

    @Test
    public void getCode_pubrel() {
        assertEquals(6, Mqtt3MessageType.PUBREL.getCode());
    }

    @Test
    public void getCode_pubcomp() {
        assertEquals(7, Mqtt3MessageType.PUBCOMP.getCode());
    }

    @Test
    public void getCode_subscribe() {
        assertEquals(8, Mqtt3MessageType.SUBSCRIBE.getCode());
    }

    @Test
    public void getCode_suback() {
        assertEquals(9, Mqtt3MessageType.SUBACK.getCode());
    }

    @Test
    public void getCode_unsubscribe() {
        assertEquals(10, Mqtt3MessageType.UNSUBSCRIBE.getCode());
    }

    @Test
    public void getCode_unsuback() {
        assertEquals(11, Mqtt3MessageType.UNSUBACK.getCode());
    }

    @Test
    public void getCode_pingreq() {
        assertEquals(12, Mqtt3MessageType.PINGREQ.getCode());
    }

    @Test
    public void getCode_pingresp() {
        assertEquals(13, Mqtt3MessageType.PINGRESP.getCode());
    }

    @Test
    public void getCode_disconnect() {
        assertEquals(14, Mqtt3MessageType.DISCONNECT.getCode());
    }

    @Test
    public void fromCode_0() {
        assertNull(Mqtt3MessageType.fromCode(0));
    }

    @Test
    public void fromCode_1() {
        assertSame(Mqtt3MessageType.CONNECT, Mqtt3MessageType.fromCode(1));
    }

    @Test
    public void fromCode_2() {
        assertSame(Mqtt3MessageType.CONNACK, Mqtt3MessageType.fromCode(2));
    }

    @Test
    public void fromCode_3() {
        assertSame(Mqtt3MessageType.PUBLISH, Mqtt3MessageType.fromCode(3));
    }

    @Test
    public void fromCode_4() {
        assertSame(Mqtt3MessageType.PUBACK, Mqtt3MessageType.fromCode(4));
    }

    @Test
    public void fromCode_5() {
        assertSame(Mqtt3MessageType.PUBREC, Mqtt3MessageType.fromCode(5));
    }

    @Test
    public void fromCode_6() {
        assertSame(Mqtt3MessageType.PUBREL, Mqtt3MessageType.fromCode(6));
    }

    @Test
    public void fromCode_7() {
        assertSame(Mqtt3MessageType.PUBCOMP, Mqtt3MessageType.fromCode(7));
    }

    @Test
    public void fromCode_8() {
        assertSame(Mqtt3MessageType.SUBSCRIBE, Mqtt3MessageType.fromCode(8));
    }

    @Test
    public void fromCode_9() {
        assertSame(Mqtt3MessageType.SUBACK, Mqtt3MessageType.fromCode(9));
    }

    @Test
    public void fromCode_10() {
        assertSame(Mqtt3MessageType.UNSUBSCRIBE, Mqtt3MessageType.fromCode(10));
    }

    @Test
    public void fromCode_11() {
        assertSame(Mqtt3MessageType.UNSUBACK, Mqtt3MessageType.fromCode(11));
    }

    @Test
    public void fromCode_12() {
        assertSame(Mqtt3MessageType.PINGREQ, Mqtt3MessageType.fromCode(12));
    }

    @Test
    public void fromCode_13() {
        assertSame(Mqtt3MessageType.PINGRESP, Mqtt3MessageType.fromCode(13));
    }

    @Test
    public void fromCode_14() {
        assertSame(Mqtt3MessageType.DISCONNECT, Mqtt3MessageType.fromCode(14));
    }

    @Test
    public void fromCode_15() {
        assertNull(Mqtt3MessageType.fromCode(15));
    }

    @Test
    public void fromCode_16() {
        assertNull(Mqtt3MessageType.fromCode(16));
    }

    @Test
    public void fromCode_negative() {
        assertNull(Mqtt3MessageType.fromCode(-1));
    }
}