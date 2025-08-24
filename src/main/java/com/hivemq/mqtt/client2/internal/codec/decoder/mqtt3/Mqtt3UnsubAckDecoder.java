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

import com.hivemq.mqtt.client2.internal.codec.decoder.MqttDecoderContext;
import com.hivemq.mqtt.client2.internal.codec.decoder.MqttDecoderException;
import com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoder;
import com.hivemq.mqtt.client2.internal.message.unsubscribe.MqttUnsubAck;
import com.hivemq.mqtt.client2.internal.message.unsubscribe.mqtt3.Mqtt3UnsubAckView;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import static com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoderUtil.checkFixedHeaderFlags;
import static com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoderUtil.checkRemainingLength;

/**
 * @author Daniel Krüger
 * @author Silvio Giebl
 */
public class Mqtt3UnsubAckDecoder implements MqttMessageDecoder {

    public static final @NotNull Mqtt3UnsubAckDecoder INSTANCE = new Mqtt3UnsubAckDecoder();

    private static final int FLAGS = 0b0000;
    private static final int REMAINING_LENGTH = 2; // 2 for the packetId

    private Mqtt3UnsubAckDecoder() {}

    @Override
    public @NotNull MqttUnsubAck decode(
            final int flags,
            final @NotNull ByteBuf in,
            final @NotNull MqttDecoderContext context) throws MqttDecoderException {
        checkFixedHeaderFlags(FLAGS, flags);
        checkRemainingLength(REMAINING_LENGTH, in.readableBytes());

        final int packetIdentifier = in.readUnsignedShort();

        return Mqtt3UnsubAckView.delegate(packetIdentifier);
    }
}
