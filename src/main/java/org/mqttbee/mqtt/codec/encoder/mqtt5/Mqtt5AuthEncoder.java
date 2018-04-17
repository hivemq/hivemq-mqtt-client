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
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt.MqttServerConnectionData;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageWithUserPropertiesEncoder.Mqtt5MessageWithReasonStringEncoder;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider.ThreadLocalMqttMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.auth.MqttAuth;

import static org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.*;
import static org.mqttbee.mqtt.message.auth.MqttAuthProperty.AUTHENTICATION_DATA;
import static org.mqttbee.mqtt.message.auth.MqttAuthProperty.AUTHENTICATION_METHOD;

/**
 * @author Silvio Giebl
 */
public class Mqtt5AuthEncoder
        extends Mqtt5MessageWithReasonStringEncoder<MqttAuth, MqttMessageEncoderProvider<MqttAuth>> {

    public static final MqttMessageEncoderProvider<MqttAuth> PROVIDER =
            new ThreadLocalMqttMessageEncoderProvider<>(Mqtt5AuthEncoder::new);

    private static final int FIXED_HEADER = Mqtt5MessageType.AUTH.getCode() << 4;
    private static final int VARIABLE_HEADER_FIXED_LENGTH = 1; // reason code

    @Override
    int calculateRemainingLength() {
        return VARIABLE_HEADER_FIXED_LENGTH;
    }

    @Override
    int calculatePropertyLength() {
        int propertyLength = 0;

        propertyLength += propertyEncodedLength(message.getMethod());
        propertyLength += nullablePropertyEncodedLength(message.getRawData());
        propertyLength += omissiblePropertiesLength();

        return propertyLength;
    }

    @Override
    public void encode(@NotNull final ByteBuf out, @NotNull final Channel channel) {
        final int maximumPacketSize = MqttServerConnectionData.getMaximumPacketSize(channel);

        encodeFixedHeader(out, maximumPacketSize);
        encodeVariableHeader(out, maximumPacketSize);
    }

    private void encodeFixedHeader(@NotNull final ByteBuf out, final int maximumPacketSize) {
        out.writeByte(FIXED_HEADER);
        MqttVariableByteInteger.encode(remainingLength(maximumPacketSize), out);
    }

    private void encodeVariableHeader(@NotNull final ByteBuf out, final int maximumPacketSize) {
        out.writeByte(message.getReasonCode().getCode());
        encodeProperties(out, maximumPacketSize);
    }

    private void encodeProperties(@NotNull final ByteBuf out, final int maximumPacketSize) {
        MqttVariableByteInteger.encode(propertyLength(maximumPacketSize), out);
        encodeProperty(AUTHENTICATION_METHOD, message.getMethod(), out);
        encodeNullableProperty(AUTHENTICATION_DATA, message.getRawData(), out);
        encodeOmissibleProperties(maximumPacketSize, out);
    }

}
