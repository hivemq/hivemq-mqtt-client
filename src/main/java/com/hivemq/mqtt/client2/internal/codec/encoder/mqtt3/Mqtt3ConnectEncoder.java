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

package com.hivemq.mqtt.client2.internal.codec.encoder.mqtt3;

import com.hivemq.mqtt.client2.internal.datatypes.MqttBinaryData;
import com.hivemq.mqtt.client2.internal.datatypes.MqttUtf8StringImpl;
import com.hivemq.mqtt.client2.internal.datatypes.MqttVariableByteInteger;
import com.hivemq.mqtt.client2.internal.message.auth.MqttSimpleAuth;
import com.hivemq.mqtt.client2.internal.message.connect.MqttConnect;
import com.hivemq.mqtt.client2.internal.message.connect.MqttStatefulConnect;
import com.hivemq.mqtt.client2.internal.message.publish.MqttWillPublish;
import com.hivemq.mqtt.client2.mqtt3.message.Mqtt3MessageType;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.hivemq.mqtt.client2.internal.codec.encoder.MqttMessageEncoderUtil.encodeNullable;
import static com.hivemq.mqtt.client2.internal.codec.encoder.MqttMessageEncoderUtil.nullableEncodedLength;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt3ConnectEncoder extends Mqtt3MessageEncoder<MqttStatefulConnect> {

    private static final int FIXED_HEADER = Mqtt3MessageType.CONNECT.getCode() << 4;
    private static final int VARIABLE_HEADER_FIXED_LENGTH =
            6 /* protocol name */ + 1 /* protocol version */ + 1 /* connect flags */ + 2 /* keep alive */;
    private static final byte PROTOCOL_VERSION = 4;

    @Inject
    Mqtt3ConnectEncoder() {}

    @Override
    int remainingLength(final @NotNull MqttStatefulConnect message) {
        final MqttConnect stateless = message.stateless();

        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        remainingLength += message.getClientIdentifier().encodedLength();

        final MqttSimpleAuth simpleAuth = stateless.getRawSimpleAuth();
        if (simpleAuth != null) {
            remainingLength += nullableEncodedLength(simpleAuth.getRawUsername());
            remainingLength += nullableEncodedLength(simpleAuth.getRawPassword());
        }

        final MqttWillPublish willPublish = stateless.getRawWillPublish();
        if (willPublish != null) {
            remainingLength += willPublish.getTopic().encodedLength();
            remainingLength += MqttBinaryData.encodedLength(willPublish.getRawPayload());
        }

        return remainingLength;
    }

    @Override
    void encode(final @NotNull MqttStatefulConnect message, final @NotNull ByteBuf out, final int remainingLength) {
        encodeFixedHeader(out, remainingLength);
        encodeVariableHeader(message, out);
        encodePayload(message, out);
    }

    private void encodeFixedHeader(final @NotNull ByteBuf out, final int remainingLength) {
        out.writeByte(FIXED_HEADER);
        MqttVariableByteInteger.encode(remainingLength, out);
    }

    private void encodeVariableHeader(final @NotNull MqttStatefulConnect message, final @NotNull ByteBuf out) {
        final MqttConnect stateless = message.stateless();

        MqttUtf8StringImpl.PROTOCOL_NAME.encode(out);
        out.writeByte(PROTOCOL_VERSION);

        int connectFlags = 0;

        final MqttSimpleAuth simpleAuth = stateless.getRawSimpleAuth();
        if (simpleAuth != null) {
            if (simpleAuth.getRawUsername() != null) {
                connectFlags |= 0b1000_0000;
            }
            if (simpleAuth.getRawPassword() != null) {
                connectFlags |= 0b0100_0000;
            }
        }

        final MqttWillPublish willPublish = stateless.getRawWillPublish();
        if (willPublish != null) {
            connectFlags |= 0b0000_0100;
            connectFlags |= (willPublish.getQos().getCode() << 3);
            if (willPublish.isRetain()) {
                connectFlags |= 0b0010_0000;
            }
        }

        if (stateless.isCleanStart()) {
            connectFlags |= 0b0000_0010;
        }

        out.writeByte(connectFlags);

        out.writeShort(stateless.getKeepAlive());
    }

    private void encodePayload(final @NotNull MqttStatefulConnect message, final @NotNull ByteBuf out) {
        message.getClientIdentifier().encode(out);

        encodeWillPublish(message, out);

        final MqttSimpleAuth simpleAuth = message.stateless().getRawSimpleAuth();
        if (simpleAuth != null) {
            encodeNullable(simpleAuth.getRawUsername(), out);
            encodeNullable(simpleAuth.getRawPassword(), out);
        }
    }

    private void encodeWillPublish(final @NotNull MqttStatefulConnect message, final @NotNull ByteBuf out) {
        final MqttWillPublish willPublish = message.stateless().getRawWillPublish();
        if (willPublish != null) {
            willPublish.getTopic().encode(out);
            MqttBinaryData.encode(willPublish.getRawPayload(), out);
        }
    }
}
