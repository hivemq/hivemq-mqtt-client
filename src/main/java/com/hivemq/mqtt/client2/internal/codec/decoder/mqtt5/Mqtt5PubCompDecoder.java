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
import com.hivemq.mqtt.client2.internal.message.publish.MqttPubComp;
import com.hivemq.mqtt.client2.mqtt5.message.publish.Mqtt5PubCompReasonCode;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import static com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoderUtil.checkFixedHeaderFlags;
import static com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoderUtil.remainingLengthTooShort;
import static com.hivemq.mqtt.client2.internal.codec.decoder.mqtt5.Mqtt5MessageDecoderUtil.*;
import static com.hivemq.mqtt.client2.internal.message.publish.MqttPubComp.DEFAULT_REASON_CODE;
import static com.hivemq.mqtt.client2.internal.message.publish.MqttPubCompProperty.REASON_STRING;
import static com.hivemq.mqtt.client2.internal.message.publish.MqttPubCompProperty.USER_PROPERTY;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubCompDecoder implements MqttMessageDecoder {

    public static final @NotNull Mqtt5PubCompDecoder INSTANCE = new Mqtt5PubCompDecoder();

    private static final int FLAGS = 0b0000;
    private static final int MIN_REMAINING_LENGTH = 2;

    private Mqtt5PubCompDecoder() {}

    @Override
    public @NotNull MqttPubComp decode(
            final int flags,
            final @NotNull ByteBuf in,
            final @NotNull MqttDecoderContext context) throws MqttDecoderException {
        checkFixedHeaderFlags(FLAGS, flags);

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            throw remainingLengthTooShort();
        }

        final int packetIdentifier = in.readUnsignedShort();

        Mqtt5PubCompReasonCode reasonCode = DEFAULT_REASON_CODE;
        MqttUtf8StringImpl reasonString = null;
        ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder = null;

        if (in.isReadable()) {
            reasonCode = Mqtt5PubCompReasonCode.fromCode(in.readUnsignedByte());
            if (reasonCode == null) {
                throw wrongReasonCode();
            }

            if (in.isReadable()) {
                checkPropertyLengthNoPayload(in);

                while (in.isReadable()) {
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
            }
        }

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.build(userPropertiesBuilder);

        return new MqttPubComp(packetIdentifier, reasonCode, reasonString, userProperties);
    }
}
