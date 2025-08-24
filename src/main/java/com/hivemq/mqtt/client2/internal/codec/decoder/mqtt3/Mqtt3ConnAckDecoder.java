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
import com.hivemq.mqtt.client2.internal.message.connect.MqttConnAck;
import com.hivemq.mqtt.client2.internal.message.connect.mqtt3.Mqtt3ConnAckView;
import com.hivemq.mqtt.client2.mqtt3.message.connect.Mqtt3ConnAckReturnCode;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import static com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoderUtil.checkFixedHeaderFlags;
import static com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoderUtil.checkRemainingLength;
import static com.hivemq.mqtt.client2.internal.codec.decoder.mqtt3.Mqtt3MessageDecoderUtil.wrongReturnCode;

/**
 * @author Daniel Krüger
 * @author Silvio Giebl
 */
public class Mqtt3ConnAckDecoder implements MqttMessageDecoder {

    public static final @NotNull Mqtt3ConnAckDecoder INSTANCE = new Mqtt3ConnAckDecoder();

    private static final int FLAGS = 0b0000;
    private static final int REMAINING_LENGTH = 2;

    private Mqtt3ConnAckDecoder() {}

    @Override
    public @NotNull MqttConnAck decode(
            final int flags,
            final @NotNull ByteBuf in,
            final @NotNull MqttDecoderContext context) throws MqttDecoderException {
        checkFixedHeaderFlags(FLAGS, flags);
        checkRemainingLength(REMAINING_LENGTH, in.readableBytes());

        final byte connAckFlags = in.readByte();

        if ((connAckFlags & 0xfe) != 0) {
            throw new MqttDecoderException("wrong CONNACK flags, bits 7-1 must be 0");
        }

        final boolean sessionPresent = (connAckFlags & 0b1) == 1;

        final Mqtt3ConnAckReturnCode returnCode = Mqtt3ConnAckReturnCode.fromCode(in.readUnsignedByte());
        if (returnCode == null) {
            throw wrongReturnCode();
        }

        if ((returnCode != Mqtt3ConnAckReturnCode.SUCCESS) && sessionPresent) {
            throw new MqttDecoderException("session present must be 0 if return code is not SUCCESS");
        }

        return Mqtt3ConnAckView.delegate(returnCode, sessionPresent);
    }
}
