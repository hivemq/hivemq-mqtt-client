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
import com.hivemq.client2.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client2.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client2.internal.mqtt.message.publish.MqttStatefulPublish;
import com.hivemq.client2.mqtt.datatypes.MqttQos;
import com.hivemq.client2.mqtt.mqtt3.message.Mqtt3MessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt3PublishEncoder extends Mqtt3MessageEncoder<MqttStatefulPublish> {

    private static final int FIXED_HEADER = Mqtt3MessageType.PUBLISH.getCode() << 4;

    @Inject
    Mqtt3PublishEncoder() {}

    @Override
    int remainingLength(final @NotNull MqttStatefulPublish message) {
        final MqttPublish stateless = message.stateless();

        int remainingLength = 0;

        remainingLength += stateless.getTopic().encodedLength();

        if (stateless.getQos() != MqttQos.AT_MOST_ONCE) {
            remainingLength += 2;
        }

        final ByteBuffer payload = stateless.getRawPayload();
        if (payload != null) {
            remainingLength += payload.remaining();
        }

        return remainingLength;
    }

    @Override
    @NotNull ByteBuf encode(
            final @NotNull MqttStatefulPublish message,
            final @NotNull MqttEncoderContext context,
            final int encodedLength,
            final int remainingLength) {

        final ByteBuffer payload = message.stateless().getRawPayload();
        if ((payload != null) && payload.isDirect()) {
            final int encodedLengthWithoutPayload = encodedLength - payload.remaining();
            final ByteBuf out =
                    context.getAllocator().ioBuffer(encodedLengthWithoutPayload, encodedLengthWithoutPayload);
            encode(message, out, remainingLength);
            return Unpooled.wrappedUnmodifiableBuffer(out, Unpooled.wrappedBuffer(payload));
        }
        final ByteBuf out = context.getAllocator().ioBuffer(encodedLength, encodedLength);
        encode(message, out, remainingLength);
        return out;
    }

    @Override
    void encode(final @NotNull MqttStatefulPublish message, final @NotNull ByteBuf out, final int remainingLength) {
        encodeFixedHeader(message, out, remainingLength);
        encodeVariableHeader(message, out);
        encodePayload(message, out);
    }

    private void encodeFixedHeader(
            final @NotNull MqttStatefulPublish message, final @NotNull ByteBuf out, final int remainingLength) {

        final MqttPublish stateless = message.stateless();

        int flags = 0;
        if (message.isDup()) {
            flags |= 0b1000;
        }
        flags |= stateless.getQos().getCode() << 1;
        if (stateless.isRetain()) {
            flags |= 0b0001;
        }

        out.writeByte(FIXED_HEADER | flags);

        MqttVariableByteInteger.encode(remainingLength, out);
    }

    private void encodeVariableHeader(final @NotNull MqttStatefulPublish message, final @NotNull ByteBuf out) {
        final MqttPublish stateless = message.stateless();

        stateless.getTopic().encode(out);

        if (stateless.getQos() != MqttQos.AT_MOST_ONCE) {
            out.writeShort(message.getPacketIdentifier());
        }
    }

    private void encodePayload(final @NotNull MqttStatefulPublish message, final @NotNull ByteBuf out) {
        final ByteBuffer payload = message.stateless().getRawPayload();
        if ((payload != null) && !payload.isDirect()) {
            out.writeBytes(payload.duplicate());
        }
    }
}
