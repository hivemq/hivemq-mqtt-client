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

package com.hivemq.mqtt.client2.internal.codec.decoder.mqtt3;

import com.hivemq.mqtt.client2.datatypes.MqttQos;
import com.hivemq.mqtt.client2.internal.codec.decoder.MqttDecoderContext;
import com.hivemq.mqtt.client2.internal.codec.decoder.MqttDecoderException;
import com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoder;
import com.hivemq.mqtt.client2.internal.datatypes.MqttTopicImpl;
import com.hivemq.mqtt.client2.internal.message.publish.MqttPublish;
import com.hivemq.mqtt.client2.internal.message.publish.MqttStatefulPublish;
import com.hivemq.mqtt.client2.internal.message.publish.mqtt3.Mqtt3PublishView;
import com.hivemq.mqtt.client2.internal.util.ByteBufferUtil;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

import static com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoderUtil.*;

/**
 * @author Daniel Krüger
 * @author Silvio Giebl
 */
public class Mqtt3PublishDecoder implements MqttMessageDecoder {

    public static final @NotNull Mqtt3PublishDecoder INSTANCE = new Mqtt3PublishDecoder();

    private static final int MIN_REMAINING_LENGTH = 2; // 2 for the packetIdentifier

    private Mqtt3PublishDecoder() {}

    @Override
    public @NotNull MqttStatefulPublish decode(
            final int flags, final @NotNull ByteBuf in, final @NotNull MqttDecoderContext context)
            throws MqttDecoderException {

        final boolean dup = (flags & 0b1000) != 0;
        final MqttQos qos = decodePublishQos(flags, dup);
        final boolean retain = (flags & 0b0001) != 0;

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            throw remainingLengthTooShort();
        }

        final MqttTopicImpl topic = MqttTopicImpl.decode(in);
        if (topic == null) {
            throw malformedTopic();
        }

        final int packetIdentifier = decodePublishPacketIdentifier(qos, in);

        final int payloadLength = in.readableBytes();
        ByteBuffer payload = ByteBufferUtil.EMPTY_BYTE_BUFFER;
        if (payloadLength > 0) {
            payload = ByteBufferUtil.allocate(payloadLength, context.useDirectBufferPayload());
            in.readBytes(payload);
            payload.position(0);
        }

        final MqttPublish publish = Mqtt3PublishView.delegate(topic, payload, qos, retain);

        return Mqtt3PublishView.statefulDelegate(publish, packetIdentifier, dup);
    }
}
