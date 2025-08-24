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

package com.hivemq.mqtt.client2.internal.codec.decoder.mqtt5;

import com.hivemq.mqtt.client2.internal.codec.decoder.MqttDecoderContext;
import com.hivemq.mqtt.client2.internal.codec.decoder.MqttDecoderException;
import com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoder;
import com.hivemq.mqtt.client2.internal.collections.ImmutableList;
import com.hivemq.mqtt.client2.internal.datatypes.MqttUserPropertiesImpl;
import com.hivemq.mqtt.client2.internal.datatypes.MqttUserPropertyImpl;
import com.hivemq.mqtt.client2.internal.datatypes.MqttUtf8StringImpl;
import com.hivemq.mqtt.client2.internal.message.subscribe.MqttSubAck;
import com.hivemq.mqtt.client2.mqtt5.message.subscribe.Mqtt5SubAckReasonCode;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import static com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoderUtil.checkFixedHeaderFlags;
import static com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoderUtil.remainingLengthTooShort;
import static com.hivemq.mqtt.client2.internal.codec.decoder.mqtt5.Mqtt5MessageDecoderUtil.*;
import static com.hivemq.mqtt.client2.internal.message.subscribe.MqttSubAckProperty.REASON_STRING;
import static com.hivemq.mqtt.client2.internal.message.subscribe.MqttSubAckProperty.USER_PROPERTY;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubAckDecoder implements MqttMessageDecoder {

    public static final @NotNull Mqtt5SubAckDecoder INSTANCE = new Mqtt5SubAckDecoder();

    private static final int FLAGS = 0b0000;
    private static final int MIN_REMAINING_LENGTH = 3;

    private Mqtt5SubAckDecoder() {}

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

        final int propertyLength = decodePropertyLength(in);

        MqttUtf8StringImpl reasonString = null;
        ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder = null;

        final int propertiesStartIndex = in.readerIndex();
        int readPropertyLength;
        while ((readPropertyLength = in.readerIndex() - propertiesStartIndex) < propertyLength) {

            final int propertyIdentifier = decodePropertyIdentifier(in);

            switch (propertyIdentifier) {
                case REASON_STRING:
                    reasonString = decodeReasonStringIfRequested(reasonString, in, context);
                    break;

                case USER_PROPERTY:
                    userPropertiesBuilder = decodeUserPropertyIfRequested(userPropertiesBuilder, in, context);
                    break;

                default:
                    throw wrongProperty(propertyIdentifier);
            }
        }

        if (readPropertyLength != propertyLength) {
            throw malformedPropertyLength();
        }

        final int reasonCodeCount = in.readableBytes();
        if (reasonCodeCount == 0) {
            throw noReasonCodes();
        }

        final ImmutableList.Builder<Mqtt5SubAckReasonCode> reasonCodesBuilder = ImmutableList.builder(reasonCodeCount);
        for (int i = 0; i < reasonCodeCount; i++) {
            final Mqtt5SubAckReasonCode reasonCode = Mqtt5SubAckReasonCode.fromCode(in.readUnsignedByte());
            if (reasonCode == null) {
                throw wrongReasonCode();
            }
            reasonCodesBuilder.add(reasonCode);
        }
        final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes = reasonCodesBuilder.build();

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.build(userPropertiesBuilder);

        return new MqttSubAck(packetIdentifier, reasonCodes, reasonString, userProperties);
    }
}
