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

package com.hivemq.client.internal.mqtt.codec.decoder.mqtt5;

import com.hivemq.client.internal.mqtt.codec.decoder.MqttDecoderContext;
import com.hivemq.client.internal.mqtt.codec.decoder.MqttDecoderException;
import com.hivemq.client.internal.mqtt.datatypes.MqttBinaryData;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertyImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

import static com.hivemq.client.internal.mqtt.codec.decoder.MqttMessageDecoderUtil.malformedUTF8String;
import static com.hivemq.client.internal.mqtt.codec.decoder.MqttMessageDecoderUtil.remainingLengthTooShort;
import static com.hivemq.client.internal.mqtt.message.connect.MqttConnAck.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;

/**
 * Util for decoders for MQTT 5 messages.
 *
 * @author Silvio Giebl
 */
final class Mqtt5MessageDecoderUtil {

    static @NotNull MqttDecoderException wrongReasonCode() {
        return new MqttDecoderException("wrong reason code");
    }

    static @NotNull MqttDecoderException malformedPropertyLength() {
        return new MqttDecoderException("malformed properties length");
    }

    static @NotNull MqttDecoderException wrongProperty(final int propertyIdentifier) {
        return new MqttDecoderException("wrong property with identifier " + propertyIdentifier);
    }

    static @NotNull MqttDecoderException moreThanOnce(final @NotNull String name) {
        return new MqttDecoderException(
                Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, name + " must not be included more than once");
    }

    static @NotNull MqttDecoderException noReasonCodes() {
        return new MqttDecoderException(
                Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "must contain at least one reason code");
    }

    static void checkPropertyLengthNoPayload(final @NotNull ByteBuf in) throws MqttDecoderException {
        final int propertyLength = MqttVariableByteInteger.decode(in);
        if (propertyLength < 0) {
            throw malformedPropertyLength();
        }
        if (in.readableBytes() != propertyLength) {
            if (in.readableBytes() < propertyLength) {
                throw remainingLengthTooShort();
            } else {
                throw new MqttDecoderException("must not have a payload");
            }
        }
    }

    static int decodePropertyLength(final @NotNull ByteBuf in) throws MqttDecoderException {
        final int propertyLength = MqttVariableByteInteger.decode(in);
        if (propertyLength < 0) {
            throw malformedPropertyLength();
        }
        if (in.readableBytes() < propertyLength) {
            throw remainingLengthTooShort();
        }
        return propertyLength;
    }

    static int decodePropertyIdentifier(final @NotNull ByteBuf in) throws MqttDecoderException {
        final int propertyIdentifier = MqttVariableByteInteger.decode(in);
        if (propertyIdentifier < 0) {
            throw new MqttDecoderException("malformed property identifier");
        }
        return propertyIdentifier;
    }

    static boolean booleanOnlyOnce(final boolean present, final @NotNull String name, final @NotNull ByteBuf in)
            throws MqttDecoderException {

        if (present) {
            throw moreThanOnce(name);
        }
        if (in.readableBytes() < 1) {
            throw malformedPropertyLength();
        }
        final byte value = in.readByte();
        if (value == 0) {
            return false;
        }
        if (value == 1) {
            return true;
        }
        throw new MqttDecoderException(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "malformed boolean for " + name);
    }

    static short unsignedByteOnlyOnce(
            final boolean present, final @NotNull String name, final @NotNull ByteBuf in) throws MqttDecoderException {

        if (present) {
            throw moreThanOnce(name);
        }
        if (in.readableBytes() < 1) {
            throw malformedPropertyLength();
        }
        return in.readUnsignedByte();
    }

    static int unsignedShortOnlyOnce(
            final boolean present, final @NotNull String name, final @NotNull ByteBuf in) throws MqttDecoderException {

        if (present) {
            throw moreThanOnce(name);
        }
        if (in.readableBytes() < 2) {
            throw malformedPropertyLength();
        }
        return in.readUnsignedShort();
    }

    static int unsignedShortOnlyOnce(
            final int current, final int notPresent, final @NotNull String name, final @NotNull ByteBuf in)
            throws MqttDecoderException {

        return unsignedShortOnlyOnce(current != notPresent, name, in);
    }

    static long unsignedIntOnlyOnce(
            final boolean present, final @NotNull String name, final @NotNull ByteBuf in) throws MqttDecoderException {

        if (present) {
            throw moreThanOnce(name);
        }
        if (in.readableBytes() < 4) {
            throw malformedPropertyLength();
        }
        return in.readUnsignedInt();
    }

    static long unsignedIntOnlyOnce(
            final long current, final long notPresent, final @NotNull String name, final @NotNull ByteBuf in)
            throws MqttDecoderException {

        return unsignedIntOnlyOnce(current != notPresent, name, in);
    }

    static @NotNull ByteBuffer decodeBinaryDataOnlyOnce(
            final @Nullable ByteBuffer current,
            final @NotNull String name,
            final @NotNull ByteBuf in,
            final boolean direct) throws MqttDecoderException {

        if (current != null) {
            throw moreThanOnce(name);
        }
        final ByteBuffer decoded = MqttBinaryData.decode(in, direct);
        if (decoded == null) {
            throw new MqttDecoderException("malformed binary data for " + name);
        }
        return decoded;
    }

    static @NotNull MqttUtf8StringImpl decodeUTF8StringOnlyOnce(
            final @Nullable MqttUtf8StringImpl current, final @NotNull String name, final @NotNull ByteBuf in)
            throws MqttDecoderException {

        if (current != null) {
            throw moreThanOnce(name);
        }
        final MqttUtf8StringImpl decoded = MqttUtf8StringImpl.decode(in);
        if (decoded == null) {
            throw malformedUTF8String(name);
        }
        return decoded;
    }

    static @NotNull MqttUtf8StringImpl decodeReasonString(
            final @Nullable MqttUtf8StringImpl current, final @NotNull ByteBuf in) throws MqttDecoderException {

        return decodeUTF8StringOnlyOnce(current, "reason string", in);
    }

    static @NotNull ImmutableList.Builder<MqttUserPropertyImpl> decodeUserProperty(
            @Nullable ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder, final @NotNull ByteBuf in)
            throws MqttDecoderException {

        final MqttUserPropertyImpl userProperty = MqttUserPropertyImpl.decode(in);
        if (userProperty == null) {
            throw new MqttDecoderException("malformed user property");
        }
        if (userPropertiesBuilder == null) {
            userPropertiesBuilder = ImmutableList.builder();
        }
        userPropertiesBuilder.add(userProperty);
        return userPropertiesBuilder;
    }

    private static void checkProblemInformationRequested(
            final @NotNull String name, final @NotNull MqttDecoderContext context) throws MqttDecoderException {

        if (!context.isProblemInformationRequested()) {
            throw new MqttDecoderException(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    name + " must not be included if problem information is not requested");
        }
    }

    static @NotNull MqttUtf8StringImpl decodeReasonStringIfRequested(
            final @Nullable MqttUtf8StringImpl current,
            final @NotNull ByteBuf in,
            final @NotNull MqttDecoderContext context) throws MqttDecoderException {

        checkProblemInformationRequested("reason string", context);
        return decodeReasonString(current, in);
    }

    static @NotNull ImmutableList.Builder<MqttUserPropertyImpl> decodeUserPropertyIfRequested(
            final @Nullable ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder,
            final @NotNull ByteBuf in,
            final @NotNull MqttDecoderContext context) throws MqttDecoderException {

        checkProblemInformationRequested("user property", context);
        return decodeUserProperty(userPropertiesBuilder, in);
    }

    static @NotNull MqttUtf8StringImpl decodeAuthMethod(
            final @Nullable MqttUtf8StringImpl current, final @NotNull ByteBuf in) throws MqttDecoderException {

        return decodeUTF8StringOnlyOnce(current, "auth method", in);
    }

    static @NotNull ByteBuffer decodeAuthData(
            final @Nullable ByteBuffer current, final @NotNull ByteBuf in, final @NotNull MqttDecoderContext context)
            throws MqttDecoderException {

        return decodeBinaryDataOnlyOnce(current, "auth data", in, context.useDirectBufferAuth());
    }

    static @NotNull MqttUtf8StringImpl decodeServerReference(
            final @Nullable MqttUtf8StringImpl current, final @NotNull ByteBuf in) throws MqttDecoderException {

        return decodeUTF8StringOnlyOnce(current, "server reference", in);
    }

    static long decodeSessionExpiryInterval(final long current, final @NotNull ByteBuf in) throws MqttDecoderException {
        return unsignedIntOnlyOnce(current, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, "session expiry interval", in);
    }

    private Mqtt5MessageDecoderUtil() {}
}
