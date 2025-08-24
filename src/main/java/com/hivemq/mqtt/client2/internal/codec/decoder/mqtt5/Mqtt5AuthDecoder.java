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
import com.hivemq.mqtt.client2.internal.message.auth.MqttAuth;
import com.hivemq.mqtt.client2.mqtt5.message.auth.Mqtt5AuthReasonCode;
import com.hivemq.mqtt.client2.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

import static com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoderUtil.checkFixedHeaderFlags;
import static com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoderUtil.remainingLengthTooShort;
import static com.hivemq.mqtt.client2.internal.codec.decoder.mqtt5.Mqtt5MessageDecoderUtil.*;
import static com.hivemq.mqtt.client2.internal.message.auth.MqttAuthProperty.*;

/**
 * @author Silvio Giebl
 */
public class Mqtt5AuthDecoder implements MqttMessageDecoder {

    public static final @NotNull Mqtt5AuthDecoder INSTANCE = new Mqtt5AuthDecoder();

    private static final int FLAGS = 0b0000;
    private static final int MIN_REMAINING_LENGTH = 2; // reason code (1) + property length (min 1)

    private Mqtt5AuthDecoder() {}

    @Override
    public @NotNull MqttAuth decode(
            final int flags,
            final @NotNull ByteBuf in,
            final @NotNull MqttDecoderContext context) throws MqttDecoderException {
        checkFixedHeaderFlags(FLAGS, flags);

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            throw remainingLengthTooShort();
        }

        final Mqtt5AuthReasonCode reasonCode = Mqtt5AuthReasonCode.fromCode(in.readUnsignedByte());
        if (reasonCode == null) {
            throw wrongReasonCode();
        }

        checkPropertyLengthNoPayload(in);

        MqttUtf8StringImpl method = null;
        ByteBuffer data = null;
        MqttUtf8StringImpl reasonString = null;
        ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder = null;

        while (in.isReadable()) {
            final int propertyIdentifier = decodePropertyIdentifier(in);

            switch (propertyIdentifier) {
                case AUTHENTICATION_METHOD:
                    method = decodeAuthMethod(method, in);
                    break;

                case AUTHENTICATION_DATA:
                    data = decodeAuthData(data, in, context);
                    break;

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

        if (method == null) {
            throw new MqttDecoderException(
                    Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "must not omit authentication method");
        }

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.build(userPropertiesBuilder);

        return new MqttAuth(reasonCode, method, data, reasonString, userProperties);
    }
}
