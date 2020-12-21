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

package com.hivemq.client2.internal.mqtt.codec.decoder.mqtt5;

import com.hivemq.client2.internal.mqtt.codec.decoder.MqttDecoderException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.MALFORMED_PACKET;
import static com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.PROTOCOL_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("NullabilityAnnotations")
class Mqtt5MessageDecoderUtilTest {

    private ByteBuf in;

    @BeforeEach
    void setUp() {
        in = Unpooled.buffer();
    }

    @AfterEach
    void tearDown() {
        in.release();
    }

    @Test
    void booleanOnlyOnce() throws MqttDecoderException {
        in.writeByte(1);
        assertEquals(true, Mqtt5MessageDecoderUtil.booleanOnlyOnce(false, "name", in));
        in.writeByte(0);
        assertEquals(false, Mqtt5MessageDecoderUtil.booleanOnlyOnce(false, "name", in));
    }

    @Test
    void booleanOnlyOnce_inputLengthTooShort() {
        final MqttDecoderException exception = assertThrows(MqttDecoderException.class,
                () -> Mqtt5MessageDecoderUtil.booleanOnlyOnce(false, "name", in));
        assertEquals(MALFORMED_PACKET, exception.getReasonCode());
    }

    @Test
    void booleanOnlyOnce_present() {
        in.writeByte(1);
        final MqttDecoderException exception = assertThrows(MqttDecoderException.class,
                () -> Mqtt5MessageDecoderUtil.booleanOnlyOnce(true, "name", in));
        assertEquals(PROTOCOL_ERROR, exception.getReasonCode());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 255})
    void unsignedByteOnlyOnce(final int value) throws MqttDecoderException {
        in.writeByte(value);
        assertEquals(value, Mqtt5MessageDecoderUtil.unsignedByteOnlyOnce(false, "name", in));
    }

    @Test
    void unsignedByteOnlyOnce_inputLengthTooShort() {
        final MqttDecoderException exception = assertThrows(MqttDecoderException.class,
                () -> Mqtt5MessageDecoderUtil.unsignedByteOnlyOnce(false, "name", in));
        assertEquals(MALFORMED_PACKET, exception.getReasonCode());
    }

    @Test
    void unsignedByteOnlyOnce_present() {
        in.writeByte(1);
        final MqttDecoderException exception = assertThrows(MqttDecoderException.class,
                () -> Mqtt5MessageDecoderUtil.unsignedByteOnlyOnce(true, "name", in));
        assertEquals(PROTOCOL_ERROR, exception.getReasonCode());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 65_535})
    void unsignedShortOnlyOnce(final int value) throws MqttDecoderException {
        in.writeShort(value);
        assertEquals(value, Mqtt5MessageDecoderUtil.unsignedShortOnlyOnce(false, "name", in));
    }

    @Test
    void unsignedShortOnlyOnce_inputLengthTooShort() {
        final MqttDecoderException exception = assertThrows(MqttDecoderException.class,
                () -> Mqtt5MessageDecoderUtil.unsignedShortOnlyOnce(false, "name", in));
        assertEquals(MALFORMED_PACKET, exception.getReasonCode());
    }

    @Test
    void unsignedShortOnlyOnce_present() {
        in.writeShort(1);
        final MqttDecoderException exception = assertThrows(MqttDecoderException.class,
                () -> Mqtt5MessageDecoderUtil.unsignedShortOnlyOnce(true, "name", in));
        assertEquals(PROTOCOL_ERROR, exception.getReasonCode());
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1, 4_294_967_295L})
    void unsignedIntOnlyOnce(final long value) throws MqttDecoderException {
        in.writeInt((int) value);
        assertEquals(value, Mqtt5MessageDecoderUtil.unsignedIntOnlyOnce(false, "name", in));
    }

    @Test
    void unsignedIntOnlyOnce_inputLengthTooShort() {
        final MqttDecoderException exception = assertThrows(MqttDecoderException.class,
                () -> Mqtt5MessageDecoderUtil.unsignedIntOnlyOnce(false, "name", in));
        assertEquals(MALFORMED_PACKET, exception.getReasonCode());
    }

    @Test
    void unsignedIntOnlyOnce_present() {
        in.writeInt(1);
        final MqttDecoderException exception = assertThrows(MqttDecoderException.class,
                () -> Mqtt5MessageDecoderUtil.unsignedIntOnlyOnce(true, "name", in));
        assertEquals(PROTOCOL_ERROR, exception.getReasonCode());
    }

}