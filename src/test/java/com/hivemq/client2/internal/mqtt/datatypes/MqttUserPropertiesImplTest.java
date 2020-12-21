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

import com.hivemq.client2.internal.mqtt.message.MqttProperty;
import com.hivemq.client2.internal.util.collections.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class MqttUserPropertiesImplTest {

    @Test
    void test_of() {
        final MqttUtf8StringImpl name = MqttUtf8StringImpl.of("name");
        final MqttUtf8StringImpl value = MqttUtf8StringImpl.of("value");
        final MqttUserPropertyImpl userProperty = new MqttUserPropertyImpl(name, value);
        final ImmutableList<MqttUserPropertyImpl> userPropertiesList = ImmutableList.of(userProperty);
        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.of(userPropertiesList);
        assertSame(userPropertiesList, userProperties.asList());
    }

    @Test
    void test_build_not_null() {
        final ImmutableList.Builder<MqttUserPropertyImpl> builder = ImmutableList.builder();
        final MqttUtf8StringImpl name = MqttUtf8StringImpl.of("name");
        final MqttUtf8StringImpl value = MqttUtf8StringImpl.of("value");
        builder.add(new MqttUserPropertyImpl(name, value));
        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.build(builder);
        final ImmutableList<MqttUserPropertyImpl> userPropertiesList = userProperties.asList();
        assertEquals(1, userPropertiesList.size());
        assertEquals(name, userPropertiesList.get(0).getName());
        assertEquals(value, userPropertiesList.get(0).getValue());
    }

    @Test
    void test_build_null() {
        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.build(null);
        assertEquals(MqttUserPropertiesImpl.NO_USER_PROPERTIES, userProperties);
        assertEquals(0, userProperties.asList().size());
    }

    @Test
    void test_encode() {
        final byte[] expected = {
                MqttProperty.USER_PROPERTY, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e',
                MqttProperty.USER_PROPERTY, 0, 4, 'n', 'a', 'm', 'e', 0, 4, 't', 'e', 's', 't'
        };
        final MqttUtf8StringImpl name = MqttUtf8StringImpl.of("name");
        final MqttUtf8StringImpl value = MqttUtf8StringImpl.of("value");
        final MqttUtf8StringImpl value2 = MqttUtf8StringImpl.of("test");
        final MqttUserPropertyImpl userProperty1 = new MqttUserPropertyImpl(name, value);
        final MqttUserPropertyImpl userProperty2 = new MqttUserPropertyImpl(name, value2);
        final MqttUserPropertiesImpl userProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(userProperty1, userProperty2));

        final ByteBuf byteBuf = Unpooled.buffer();
        userProperties.encode(byteBuf);
        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();

        assertArrayEquals(expected, actual);
    }

    @Test
    void test_encodedLength() {
        final MqttUtf8StringImpl name = MqttUtf8StringImpl.of("name");
        final MqttUtf8StringImpl value = MqttUtf8StringImpl.of("value");
        final MqttUtf8StringImpl value2 = MqttUtf8StringImpl.of("test");
        final MqttUserPropertyImpl userProperty1 = new MqttUserPropertyImpl(name, value);
        final MqttUserPropertyImpl userProperty2 = new MqttUserPropertyImpl(name, value2);
        final MqttUserPropertiesImpl userProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(userProperty1, userProperty2));

        assertEquals(27, userProperties.encodedLength());
    }
}