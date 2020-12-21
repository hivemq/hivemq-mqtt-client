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

package com.hivemq.client2.internal.mqtt.datatypes;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class MqttUserPropertyImplTest {

    @Test
    public void test_decode() {
        final byte[] binary = {0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e'};
        final ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(binary);
        final MqttUserPropertyImpl userProperty = MqttUserPropertyImpl.decode(byteBuf);
        byteBuf.release();
        assertNotNull(userProperty);
        assertEquals("name", userProperty.getName().toString());
        assertEquals("value", userProperty.getValue().toString());
    }

    @Test
    public void test_decode_malformed_name() {
        final byte[] binary = {0, 4, 'n', 'a', 'm', 0, 5, 'v', 'a', 'l', 'u', 'e'};
        final ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(binary);
        final MqttUserPropertyImpl userProperty = MqttUserPropertyImpl.decode(byteBuf);
        byteBuf.release();
        assertNull(userProperty);
    }

    @Test
    public void test_decode_malformed_value() {
        final byte[] binary = {0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u'};
        final ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(binary);
        final MqttUserPropertyImpl userProperty = MqttUserPropertyImpl.decode(byteBuf);
        byteBuf.release();
        assertNull(userProperty);
    }

    @Test
    public void test_equals() {
        EqualsVerifier.forClass(MqttUserPropertyImpl.class).suppress(Warning.STRICT_INHERITANCE).verify();
    }

}