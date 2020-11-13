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

package com.hivemq.client.internal.mqtt.codec.decoder.mqtt3;

import com.hivemq.client.internal.mqtt.codec.decoder.MqttDecoderContext;
import com.hivemq.client.internal.mqtt.codec.decoder.MqttDecoderException;
import com.hivemq.client.internal.mqtt.codec.decoder.MqttMessageDecoder;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnAck;
import com.hivemq.client.internal.mqtt.message.connect.mqtt3.Mqtt3ConnAckView;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnAckReturnCode;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.hivemq.client.internal.mqtt.codec.decoder.MqttMessageDecoderUtil.checkFixedHeaderFlags;
import static com.hivemq.client.internal.mqtt.codec.decoder.MqttMessageDecoderUtil.checkRemainingLength;
import static com.hivemq.client.internal.mqtt.codec.decoder.mqtt3.Mqtt3MessageDecoderUtil.wrongReturnCode;

/**
 * @author Daniel Krüger
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt3ConnAckDecoder implements MqttMessageDecoder {

    private static final int FLAGS = 0b0000;
    private static final int REMAINING_LENGTH = 2;

    @Inject
    Mqtt3ConnAckDecoder() {}

    @Override
    public @NotNull MqttConnAck decode(
            final int flags, final @NotNull ByteBuf in, final @NotNull MqttDecoderContext context)
            throws MqttDecoderException {

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
