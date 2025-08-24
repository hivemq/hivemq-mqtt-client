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
import com.hivemq.mqtt.client2.internal.collections.ImmutableList;
import com.hivemq.mqtt.client2.internal.message.subscribe.MqttSubAck;
import com.hivemq.mqtt.client2.internal.message.subscribe.mqtt3.Mqtt3SubAckView;
import com.hivemq.mqtt.client2.mqtt3.message.subscribe.Mqtt3SubAckReturnCode;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import static com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoderUtil.checkFixedHeaderFlags;
import static com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoderUtil.remainingLengthTooShort;
import static com.hivemq.mqtt.client2.internal.codec.decoder.mqtt3.Mqtt3MessageDecoderUtil.wrongReturnCode;

/**
 * @author Daniel Krüger
 * @author Silvio Giebl
 */
public class Mqtt3SubAckDecoder implements MqttMessageDecoder {

    public static final @NotNull Mqtt3SubAckDecoder INSTANCE = new Mqtt3SubAckDecoder();

    private static final int FLAGS = 0b0000;
    private static final int MIN_REMAINING_LENGTH = 3; // 2 for the packetId + 1 for at least one Subscription

    private Mqtt3SubAckDecoder() {}

    @Override
    public @NotNull MqttSubAck decode(
            final int flags,
            final @NotNull ByteBuf in,
            final @NotNull MqttDecoderContext context) throws MqttDecoderException {
        checkFixedHeaderFlags(FLAGS, flags);

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            throw remainingLengthTooShort();
        }

        final int packetIdentifier = in.readUnsignedShort();

        final int returnCodeCount = in.readableBytes();
        final ImmutableList.Builder<Mqtt3SubAckReturnCode> returnCodesBuilder = ImmutableList.builder(returnCodeCount);
        for (int i = 0; i < returnCodeCount; i++) {
            final Mqtt3SubAckReturnCode returnCode = Mqtt3SubAckReturnCode.fromCode(in.readUnsignedByte());
            if (returnCode == null) {
                throw wrongReturnCode();
            }
            returnCodesBuilder.add(returnCode);
        }

        return Mqtt3SubAckView.delegate(packetIdentifier, returnCodesBuilder.build());
    }
}
