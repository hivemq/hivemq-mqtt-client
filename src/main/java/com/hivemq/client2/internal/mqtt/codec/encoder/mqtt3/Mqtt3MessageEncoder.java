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

package com.hivemq.client2.internal.mqtt.codec.encoder.mqtt3;

import com.hivemq.client2.internal.mqtt.codec.encoder.MqttEncoderContext;
import com.hivemq.client2.internal.mqtt.codec.encoder.MqttMessageEncoder;
import com.hivemq.client2.internal.mqtt.message.MqttMessage;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import static com.hivemq.client2.internal.mqtt.codec.encoder.MqttMessageEncoderUtil.encodedPacketLength;
import static com.hivemq.client2.internal.mqtt.codec.encoder.MqttMessageEncoderUtil.maximumPacketSizeExceeded;

/**
 * Base class of encoders for MQTT 3 messages.
 *
 * @param <M> the type of the MQTT message.
 * @author Silvio Giebl
 */
abstract class Mqtt3MessageEncoder<M extends MqttMessage> extends MqttMessageEncoder<M> {

    @Override
    protected @NotNull ByteBuf encode(final @NotNull M message, final @NotNull MqttEncoderContext context) {
        final int remainingLength = remainingLength(message);
        final int encodedLength = encodedPacketLength(remainingLength);
        if (encodedLength > context.getMaximumPacketSize()) {
            throw maximumPacketSizeExceeded(message, encodedLength, context.getMaximumPacketSize());
        }
        return encode(message, context, encodedLength, remainingLength);
    }

    @NotNull ByteBuf encode(
            final @NotNull M message,
            final @NotNull MqttEncoderContext context,
            final int encodedLength,
            final int remainingLength) {

        final ByteBuf out = context.getAllocator().ioBuffer(encodedLength, encodedLength);
        encode(message, out, remainingLength);
        return out;
    }

    abstract void encode(final @NotNull M message, final @NotNull ByteBuf out, final int remainingLength);

    abstract int remainingLength(final @NotNull M message);

    /**
     * Base class of encoders for MQTT 3 messages with only a Packet Identifier.
     *
     * @param <M> the type of the MQTT message.
     */
    static abstract class Mqtt3MessageWithIdEncoder<M extends MqttMessage.WithId> extends MqttMessageEncoder<M> {

        private static final int REMAINING_LENGTH = 2;
        private static final int ENCODED_LENGTH = 2 + REMAINING_LENGTH;

        @Override
        protected @NotNull ByteBuf encode(final @NotNull M message, final @NotNull MqttEncoderContext context) {
            if (ENCODED_LENGTH > context.getMaximumPacketSize()) {
                throw maximumPacketSizeExceeded(message, ENCODED_LENGTH, context.getMaximumPacketSize());
            }
            final ByteBuf out = context.getAllocator().ioBuffer(ENCODED_LENGTH, ENCODED_LENGTH);
            encode(message, out);
            return out;
        }

        private void encode(final @NotNull M message, final @NotNull ByteBuf out) {
            encodeFixedHeader(out);
            encodeVariableHeader(message, out);
        }

        private void encodeFixedHeader(final @NotNull ByteBuf out) {
            out.writeByte(getFixedHeader());
            out.writeByte(REMAINING_LENGTH);
        }

        private void encodeVariableHeader(final @NotNull M message, final @NotNull ByteBuf out) {
            out.writeShort(message.getPacketIdentifier());
        }

        abstract int getFixedHeader();
    }
}
