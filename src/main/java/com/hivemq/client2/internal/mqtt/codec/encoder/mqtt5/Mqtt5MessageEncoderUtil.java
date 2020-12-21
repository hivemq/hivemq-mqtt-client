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

package com.hivemq.client2.internal.mqtt.codec.encoder.mqtt5;

import com.hivemq.client2.internal.mqtt.datatypes.MqttBinaryData;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client2.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

/**
 * Util for decoders for MQTT 5 messages.
 *
 * @author Silvio Giebl
 */
final class Mqtt5MessageEncoderUtil {

    static int propertyEncodedLength(final @NotNull MqttUtf8StringImpl string) {
        return 1 + string.encodedLength();
    }

    static int nullablePropertyEncodedLength(final @Nullable MqttUtf8StringImpl string) {
        return (string == null) ? 0 : propertyEncodedLength(string);
    }

    static int nullablePropertyEncodedLength(final @Nullable ByteBuffer byteBuffer) {
        return (byteBuffer == null) ? 0 : 1 + MqttBinaryData.encodedLength(byteBuffer);
    }

    static int nullablePropertyEncodedLength(final @Nullable Mqtt5PayloadFormatIndicator payloadFormatIndicator) {
        return (payloadFormatIndicator == null) ? 0 : 2;
    }

    static int booleanPropertyEncodedLength(final boolean value, final boolean defaultValue) {
        return (value == defaultValue) ? 0 : 2;
    }

    static int shortPropertyEncodedLength(final int value, final int defaultValue) {
        return (value == defaultValue) ? 0 : 3;
    }

    static int intPropertyEncodedLength(final long value, final long defaultValue) {
        return (value == defaultValue) ? 0 : 5;
    }

    static int variableByteIntegerPropertyEncodedLength(final int value) {
        return 1 + MqttVariableByteInteger.encodedLength(value);
    }

    static int variableByteIntegerPropertyEncodedLength(final int value, final int defaultValue) {
        return (value == defaultValue) ? 0 : variableByteIntegerPropertyEncodedLength(value);
    }

    static void encodeProperty(
            final int propertyIdentifier, final @NotNull MqttUtf8StringImpl string, final @NotNull ByteBuf out) {

        out.writeByte(propertyIdentifier);
        string.encode(out);
    }

    static void encodeNullableProperty(
            final int propertyIdentifier, final @Nullable MqttUtf8StringImpl string, final @NotNull ByteBuf out) {

        if (string != null) {
            encodeProperty(propertyIdentifier, string, out);
        }
    }

    static void encodeNullableProperty(
            final int propertyIdentifier, final @Nullable ByteBuffer byteBuffer, final @NotNull ByteBuf out) {

        if (byteBuffer != null) {
            out.writeByte(propertyIdentifier);
            MqttBinaryData.encode(byteBuffer, out);
        }
    }

    static void encodeNullableProperty(
            final int propertyIdentifier,
            final @Nullable Mqtt5PayloadFormatIndicator payloadFormatIndicator,
            final @NotNull ByteBuf out) {

        if (payloadFormatIndicator != null) {
            out.writeByte(propertyIdentifier);
            out.writeByte(payloadFormatIndicator.getCode());
        }
    }

    static void encodeBooleanProperty(
            final int propertyIdentifier, final boolean value, final boolean defaultValue, final @NotNull ByteBuf out) {

        if (value != defaultValue) {
            out.writeByte(propertyIdentifier);
            out.writeByte(value ? 1 : 0);
        }
    }

    static void encodeShortProperty(
            final int propertyIdentifier, final int value, final int defaultValue, final @NotNull ByteBuf out) {

        if (value != defaultValue) {
            out.writeByte(propertyIdentifier);
            out.writeShort(value);
        }
    }

    static void encodeIntProperty(
            final int propertyIdentifier, final long value, final long defaultValue, final @NotNull ByteBuf out) {

        if (value != defaultValue) {
            out.writeByte(propertyIdentifier);
            out.writeInt((int) value);
        }
    }

    static void encodeVariableByteIntegerProperty(
            final int propertyIdentifier, final int value, final @NotNull ByteBuf out) {

        out.writeByte(propertyIdentifier);
        MqttVariableByteInteger.encode(value, out);
    }

    static void encodeVariableByteIntegerProperty(
            final int propertyIdentifier, final int value, final long defaultValue, final @NotNull ByteBuf out) {

        if (value != defaultValue) {
            encodeVariableByteIntegerProperty(propertyIdentifier, value, out);
        }
    }

    private Mqtt5MessageEncoderUtil() {}
}
