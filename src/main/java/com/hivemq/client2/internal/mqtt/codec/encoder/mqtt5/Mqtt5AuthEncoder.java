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

package com.hivemq.client2.internal.mqtt.codec.encoder.mqtt5;

import com.hivemq.client2.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client2.internal.mqtt.message.auth.MqttAuth;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5MessageType;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.hivemq.client2.internal.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.*;
import static com.hivemq.client2.internal.mqtt.message.auth.MqttAuthProperty.AUTHENTICATION_DATA;
import static com.hivemq.client2.internal.mqtt.message.auth.MqttAuthProperty.AUTHENTICATION_METHOD;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5AuthEncoder extends Mqtt5MessageWithUserPropertiesEncoder.WithReason<MqttAuth> {

    private static final int FIXED_HEADER = Mqtt5MessageType.AUTH.getCode() << 4;
    private static final int VARIABLE_HEADER_FIXED_LENGTH = 1; // reason code

    @Inject
    Mqtt5AuthEncoder() {}

    @Override
    int remainingLengthWithoutProperties(final @NotNull MqttAuth message) {
        return VARIABLE_HEADER_FIXED_LENGTH;
    }

    @Override
    int propertyLength(final @NotNull MqttAuth message) {
        int propertyLength = 0;

        propertyLength += propertyEncodedLength(message.getMethod());
        propertyLength += nullablePropertyEncodedLength(message.getRawData());
        propertyLength += omissiblePropertyLength(message);

        return propertyLength;
    }

    @Override
    void encode(
            final @NotNull MqttAuth message,
            final @NotNull ByteBuf out,
            final int remainingLength,
            final int propertyLength,
            final int omittedProperties) {

        encodeFixedHeader(out, remainingLength);
        encodeVariableHeader(message, out, propertyLength, omittedProperties);
    }

    private void encodeFixedHeader(final @NotNull ByteBuf out, final int remainingLength) {
        out.writeByte(FIXED_HEADER);
        MqttVariableByteInteger.encode(remainingLength, out);
    }

    private void encodeVariableHeader(
            final @NotNull MqttAuth message,
            final @NotNull ByteBuf out,
            final int propertyLength,
            final int omittedProperties) {

        out.writeByte(message.getReasonCode().getCode());
        encodeProperties(message, out, propertyLength, omittedProperties);
    }

    private void encodeProperties(
            final @NotNull MqttAuth message,
            final @NotNull ByteBuf out,
            final int propertyLength,
            final int omittedProperties) {

        MqttVariableByteInteger.encode(propertyLength, out);
        encodeProperty(AUTHENTICATION_METHOD, message.getMethod(), out);
        encodeNullableProperty(AUTHENTICATION_DATA, message.getRawData(), out);
        encodeOmissibleProperties(message, out, omittedProperties);
    }
}
