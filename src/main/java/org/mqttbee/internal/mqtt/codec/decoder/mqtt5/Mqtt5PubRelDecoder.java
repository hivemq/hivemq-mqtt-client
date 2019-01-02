/*
 * Copyright 2018 The MQTT Bee project
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
 *
 */

package org.mqttbee.internal.mqtt.codec.decoder.mqtt5;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.internal.mqtt.codec.decoder.MqttDecoderContext;
import org.mqttbee.internal.mqtt.codec.decoder.MqttDecoderException;
import org.mqttbee.internal.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.internal.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.internal.mqtt.datatypes.MqttUtf8StringImpl;
import org.mqttbee.internal.mqtt.message.publish.pubrel.MqttPubRel;
import org.mqttbee.internal.util.collections.ImmutableList;
import org.mqttbee.mqtt.mqtt5.message.publish.pubrel.Mqtt5PubRelReasonCode;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.mqttbee.internal.mqtt.codec.decoder.MqttMessageDecoderUtil.checkFixedHeaderFlags;
import static org.mqttbee.internal.mqtt.codec.decoder.MqttMessageDecoderUtil.remainingLengthTooShort;
import static org.mqttbee.internal.mqtt.codec.decoder.mqtt5.Mqtt5MessageDecoderUtil.*;
import static org.mqttbee.internal.mqtt.message.publish.pubrel.MqttPubRel.DEFAULT_REASON_CODE;
import static org.mqttbee.internal.mqtt.message.publish.pubrel.MqttPubRelProperty.REASON_STRING;
import static org.mqttbee.internal.mqtt.message.publish.pubrel.MqttPubRelProperty.USER_PROPERTY;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubRelDecoder implements MqttMessageDecoder {

    private static final int FLAGS = 0b0010;
    private static final int MIN_REMAINING_LENGTH = 2;

    @Inject
    Mqtt5PubRelDecoder() {}

    @Override
    public @NotNull MqttPubRel decode(
            final int flags, final @NotNull ByteBuf in, final @NotNull MqttDecoderContext context)
            throws MqttDecoderException {

        checkFixedHeaderFlags(FLAGS, flags);

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            throw remainingLengthTooShort();
        }

        final int packetIdentifier = in.readUnsignedShort();

        Mqtt5PubRelReasonCode reasonCode = DEFAULT_REASON_CODE;
        MqttUtf8StringImpl reasonString = null;
        ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder = null;

        if (in.isReadable()) {
            reasonCode = Mqtt5PubRelReasonCode.fromCode(in.readUnsignedByte());
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

        return new MqttPubRel(packetIdentifier, reasonCode, reasonString, userProperties);
    }
}
