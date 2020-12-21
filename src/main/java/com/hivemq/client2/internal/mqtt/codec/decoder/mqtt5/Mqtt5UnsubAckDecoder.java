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

package com.hivemq.client2.internal.mqtt.codec.decoder.mqtt5;

import com.hivemq.client2.internal.mqtt.codec.decoder.MqttDecoderContext;
import com.hivemq.client2.internal.mqtt.codec.decoder.MqttDecoderException;
import com.hivemq.client2.internal.mqtt.codec.decoder.MqttMessageDecoder;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUserPropertyImpl;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client2.internal.mqtt.message.unsubscribe.MqttUnsubAck;
import com.hivemq.client2.internal.util.collections.ImmutableList;
import com.hivemq.client2.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubAckReasonCode;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.hivemq.client2.internal.mqtt.codec.decoder.MqttMessageDecoderUtil.checkFixedHeaderFlags;
import static com.hivemq.client2.internal.mqtt.codec.decoder.MqttMessageDecoderUtil.remainingLengthTooShort;
import static com.hivemq.client2.internal.mqtt.codec.decoder.mqtt5.Mqtt5MessageDecoderUtil.*;
import static com.hivemq.client2.internal.mqtt.message.unsubscribe.MqttUnsubAckProperty.REASON_STRING;
import static com.hivemq.client2.internal.mqtt.message.unsubscribe.MqttUnsubAckProperty.USER_PROPERTY;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5UnsubAckDecoder implements MqttMessageDecoder {

    private static final int FLAGS = 0b0000;
    private static final int MIN_REMAINING_LENGTH = 3;

    @Inject
    Mqtt5UnsubAckDecoder() {}

    @Override
    public @NotNull MqttUnsubAck decode(
            final int flags, final @NotNull ByteBuf in, final @NotNull MqttDecoderContext context)
            throws MqttDecoderException {

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

        final ImmutableList.Builder<Mqtt5UnsubAckReasonCode> reasonCodesBuilder =
                ImmutableList.builder(reasonCodeCount);
        for (int i = 0; i < reasonCodeCount; i++) {
            final Mqtt5UnsubAckReasonCode reasonCode = Mqtt5UnsubAckReasonCode.fromCode(in.readUnsignedByte());
            if (reasonCode == null) {
                throw wrongReasonCode();
            }
            reasonCodesBuilder.add(reasonCode);
        }
        final ImmutableList<Mqtt5UnsubAckReasonCode> reasonCodes = reasonCodesBuilder.build();

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.build(userPropertiesBuilder);

        return new MqttUnsubAck(packetIdentifier, reasonCodes, reasonString, userProperties);
    }
}
