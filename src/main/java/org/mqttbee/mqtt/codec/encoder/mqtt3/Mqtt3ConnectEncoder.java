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

package org.mqttbee.mqtt.codec.encoder.mqtt3;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider.ThreadLocalMqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.auth.MqttSimpleAuth;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.mqtt.message.connect.MqttConnectWrapper;
import org.mqttbee.mqtt.message.publish.MqttWillPublish;

import static org.mqttbee.mqtt.codec.encoder.MqttMessageEncoderUtil.*;

/**
 * @author Silvio Giebl
 */
public class Mqtt3ConnectEncoder extends Mqtt3WrappedMessageEncoder<MqttConnect, MqttConnectWrapper> {

    public static final MqttWrappedMessageEncoderProvider<MqttConnect, MqttConnectWrapper, MqttMessageEncoderProvider<MqttConnectWrapper>>
            PROVIDER = ThreadLocalMqttWrappedMessageEncoderProvider.create(Mqtt3ConnectEncoder::new);

    private static final int FIXED_HEADER = Mqtt3MessageType.CONNECT.getCode() << 4;
    private static final int VARIABLE_HEADER_FIXED_LENGTH =
            6 /* protocol name */ + 1 /* protocol version */ + 1 /* connect flags */ + 2 /* keep alive */;
    private static final byte PROTOCOL_VERSION = 4;

    @Override
    int calculateRemainingLength(@NotNull final MqttConnectWrapper message) {
        final MqttConnect wrapped = message.getWrapped();

        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        remainingLength += message.getClientIdentifier().encodedLength();

        final MqttSimpleAuth simpleAuth = wrapped.getRawSimpleAuth();
        if (simpleAuth != null) {
            remainingLength += nullableEncodedLength(simpleAuth.getRawUsername());
            remainingLength += nullableEncodedLength(simpleAuth.getRawPassword());
        }

        final MqttWillPublish willPublish = wrapped.getRawWillPublish();
        if (willPublish != null) {
            remainingLength += willPublish.getTopic().encodedLength();
            remainingLength += encodedOrEmptyLength(willPublish.getRawPayload());
        }

        return remainingLength;
    }

    @Override
    public void encode(
            @NotNull final MqttConnectWrapper message, @NotNull final ByteBuf out, final int remainingLength) {

        encodeFixedHeader(out, remainingLength);
        encodeVariableHeader(message, out);
        encodePayload(message, out);
    }

    private void encodeFixedHeader(@NotNull final ByteBuf out, final int remainingLength) {
        out.writeByte(FIXED_HEADER);
        MqttVariableByteInteger.encode(remainingLength, out);
    }

    private void encodeVariableHeader(@NotNull final MqttConnectWrapper message, @NotNull final ByteBuf out) {
        final MqttConnect wrapped = message.getWrapped();

        MqttUTF8StringImpl.PROTOCOL_NAME.to(out);
        out.writeByte(PROTOCOL_VERSION);

        int connectFlags = 0;

        final MqttSimpleAuth simpleAuth = wrapped.getRawSimpleAuth();
        if (simpleAuth != null) {
            if (simpleAuth.getRawUsername() != null) {
                connectFlags |= 0b1000_0000;
            }
            if (simpleAuth.getRawPassword() != null) {
                connectFlags |= 0b0100_0000;
            }
        }

        final MqttWillPublish willPublish = wrapped.getRawWillPublish();
        if (willPublish != null) {
            connectFlags |= 0b0000_0100;
            connectFlags |= (willPublish.getQos().getCode() << 3);
            if (willPublish.isRetain()) {
                connectFlags |= 0b0010_0000;
            }
        }

        if (wrapped.isCleanStart()) {
            connectFlags |= 0b0000_0010;
        }

        out.writeByte(connectFlags);

        out.writeShort(wrapped.getKeepAlive());
    }

    private void encodePayload(@NotNull final MqttConnectWrapper message, @NotNull final ByteBuf out) {
        message.getClientIdentifier().to(out);

        encodeWillPublish(message, out);

        final MqttSimpleAuth simpleAuth = message.getWrapped().getRawSimpleAuth();
        if (simpleAuth != null) {
            encodeNullable(simpleAuth.getRawUsername(), out);
            encodeNullable(simpleAuth.getRawPassword(), out);
        }
    }

    private void encodeWillPublish(@NotNull final MqttConnectWrapper message, @NotNull final ByteBuf out) {
        final MqttWillPublish willPublish = message.getWrapped().getRawWillPublish();
        if (willPublish != null) {
            willPublish.getTopic().to(out);
            encodeNullable(willPublish.getRawPayload(), out);
        }
    }

}
