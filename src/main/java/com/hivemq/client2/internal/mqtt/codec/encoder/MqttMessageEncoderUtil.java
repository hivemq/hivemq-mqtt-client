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

package com.hivemq.client2.internal.mqtt.codec.encoder;

import com.hivemq.client2.internal.mqtt.datatypes.MqttBinaryData;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client2.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client2.internal.mqtt.message.MqttMessage;
import com.hivemq.client2.mqtt.exceptions.MqttEncodeException;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

/**
 * Util for encoders for MQTT messages of different versions.
 *
 * @author Silvio Giebl
 */
public final class MqttMessageEncoderUtil {

    /**
     * Calculates the encoded length of a MQTT message with the given remaining length.
     *
     * @param remainingLength the remaining length of the MQTT message.
     * @return the encoded length of the MQTT message.
     */
    public static int encodedPacketLength(final int remainingLength) {
        return 1 + encodedLengthWithHeader(remainingLength);
    }

    /**
     * Calculates the encoded length with a prefixed header.
     *
     * @param encodedLength the encoded length.
     * @return the encoded length with a prefixed header.
     */
    public static int encodedLengthWithHeader(final int encodedLength) {
        return MqttVariableByteInteger.encodedLength(encodedLength) + encodedLength;
    }

    public static int nullableEncodedLength(final @Nullable MqttUtf8StringImpl string) {
        return (string == null) ? 0 : string.encodedLength();
    }

    public static int nullableEncodedLength(final @Nullable ByteBuffer byteBuffer) {
        return (byteBuffer == null) ? 0 : MqttBinaryData.encodedLength(byteBuffer);
    }

    public static int encodedOrEmptyLength(final @Nullable ByteBuffer byteBuffer) {
        return (byteBuffer == null) ? MqttBinaryData.EMPTY_LENGTH : MqttBinaryData.encodedLength(byteBuffer);
    }

    public static void encodeNullable(final @Nullable MqttUtf8StringImpl string, final @NotNull ByteBuf out) {
        if (string != null) {
            string.encode(out);
        }
    }

    public static void encodeNullable(final @Nullable ByteBuffer byteBuffer, final @NotNull ByteBuf out) {
        if (byteBuffer != null) {
            MqttBinaryData.encode(byteBuffer, out);
        }
    }

    public static void encodeOrEmpty(final @Nullable ByteBuffer byteBuffer, final @NotNull ByteBuf out) {
        if (byteBuffer != null) {
            MqttBinaryData.encode(byteBuffer, out);
        } else {
            MqttBinaryData.encodeEmpty(out);
        }
    }

    public static @NotNull MqttEncodeException maximumPacketSizeExceeded(
            final @NotNull MqttMessage message, final int encodedLength, final int maxPacketSize) {

        return new MqttEncodeException(
                message.getType() + " exceeded maximum packet size, minimal possible encoded length: " + encodedLength +
                        ", maximum: " + maxPacketSize + ".");
    }

    private MqttMessageEncoderUtil() {}
}
