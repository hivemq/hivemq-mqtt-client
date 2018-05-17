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

package org.mqttbee.mqtt.codec.encoder.mqtt5;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageWithUserPropertiesEncoder.Mqtt5MessageWithReasonStringEncoder;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.auth.MqttAuth;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.*;
import static org.mqttbee.mqtt.message.auth.MqttAuthProperty.AUTHENTICATION_DATA;
import static org.mqttbee.mqtt.message.auth.MqttAuthProperty.AUTHENTICATION_METHOD;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5AuthEncoder extends Mqtt5MessageWithReasonStringEncoder<MqttAuth> {

    private static final int FIXED_HEADER = Mqtt5MessageType.AUTH.getCode() << 4;
    private static final int VARIABLE_HEADER_FIXED_LENGTH = 1; // reason code

    @Inject
    Mqtt5AuthEncoder() {
    }

    @Override
    int remainingLengthWithoutProperties(@NotNull final MqttAuth message) {
        return VARIABLE_HEADER_FIXED_LENGTH;
    }

    @Override
    int propertyLength(@NotNull final MqttAuth message) {
        int propertyLength = 0;

        propertyLength += propertyEncodedLength(message.getMethod());
        propertyLength += nullablePropertyEncodedLength(message.getRawData());
        propertyLength += omissiblePropertyLength(message);

        return propertyLength;
    }

    @Override
    protected void encode(
            @NotNull final MqttAuth message, @NotNull final ByteBuf out, final int remainingLength,
            final int propertyLength, final int omittedProperties) {

        encodeFixedHeader(out, remainingLength);
        encodeVariableHeader(message, out, propertyLength, omittedProperties);
    }

    private void encodeFixedHeader(@NotNull final ByteBuf out, final int remainingLength) {
        out.writeByte(FIXED_HEADER);
        MqttVariableByteInteger.encode(remainingLength, out);
    }

    private void encodeVariableHeader(
            @NotNull final MqttAuth message, @NotNull final ByteBuf out, final int propertyLength,
            final int omittedProperties) {

        out.writeByte(message.getReasonCode().getCode());
        encodeProperties(message, out, propertyLength, omittedProperties);
    }

    private void encodeProperties(
            @NotNull final MqttAuth message, @NotNull final ByteBuf out, final int propertyLength,
            final int omittedProperties) {

        MqttVariableByteInteger.encode(propertyLength, out);
        encodeProperty(AUTHENTICATION_METHOD, message.getMethod(), out);
        encodeNullableProperty(AUTHENTICATION_DATA, message.getRawData(), out);
        encodeOmissibleProperties(message, out, omittedProperties);
    }

}
