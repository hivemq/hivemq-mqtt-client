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

package org.mqttbee.mqtt.codec.decoder.mqtt5;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.codec.decoder.MqttDecoderException;
import org.mqttbee.mqtt.datatypes.MqttBinaryData;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.netty.ChannelAttributes;

import java.nio.ByteBuffer;

import static org.mqttbee.mqtt.codec.decoder.MqttMessageDecoderUtil.malformedUTF8String;
import static org.mqttbee.mqtt.codec.decoder.MqttMessageDecoderUtil.remainingLengthTooShort;
import static org.mqttbee.mqtt.message.connect.connack.MqttConnAck.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;

/**
 * Util for decoders for MQTT 5 messages.
 *
 * @author Silvio Giebl
 */
class Mqtt5MessageDecoderUtil {

    private Mqtt5MessageDecoderUtil() {
    }

    @NotNull
    static MqttDecoderException wrongReasonCode() {
        return new MqttDecoderException("wrong reason code");
    }

    @NotNull
    static MqttDecoderException malformedPropertyLength() {
        return new MqttDecoderException("malformed properties length");
    }

    @NotNull
    static MqttDecoderException wrongProperty(final int propertyIdentifier) {
        return new MqttDecoderException("wrong property with identifier " + propertyIdentifier);
    }

    @NotNull
    static MqttDecoderException moreThanOnce(@NotNull final String name) {
        return new MqttDecoderException(
                Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, name + " must not be included more than once");
    }

    @NotNull
    static MqttDecoderException noReasonCodes() {
        return new MqttDecoderException(
                Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "must contain at least one reason code");
    }

    static void checkPropertyLengthNoPayload(@NotNull final ByteBuf in) throws MqttDecoderException {
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

    static int decodePropertyLength(@NotNull final ByteBuf in) throws MqttDecoderException {
        final int propertyLength = MqttVariableByteInteger.decode(in);
        if (propertyLength < 0) {
            throw malformedPropertyLength();
        }
        if (in.readableBytes() < propertyLength) {
            throw remainingLengthTooShort();
        }
        return propertyLength;
    }

    static int decodePropertyIdentifier(@NotNull final ByteBuf in) throws MqttDecoderException {
        final int propertyIdentifier = MqttVariableByteInteger.decode(in);
        if (propertyIdentifier < 0) {
            throw new MqttDecoderException("malformed property identifier");
        }
        return propertyIdentifier;
    }

    static boolean booleanOnlyOnce(final boolean present, @NotNull final String name, @NotNull final ByteBuf in)
            throws MqttDecoderException {

        if (present) {
            throw moreThanOnce(name);
        }
        if (in.readableBytes() < 1) {
            throw new MqttDecoderException("malformed properties length");
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
            final boolean present, @NotNull final String name, @NotNull final ByteBuf in) throws MqttDecoderException {

        if (present) {
            throw moreThanOnce(name);
        }
        if (in.readableBytes() < 1) {
            throw new MqttDecoderException("malformed properties length");
        }
        return in.readUnsignedByte();
    }

    static int unsignedShortOnlyOnce(
            final boolean present, @NotNull final String name, @NotNull final ByteBuf in) throws MqttDecoderException {

        if (present) {
            throw moreThanOnce(name);
        }
        if (in.readableBytes() < 2) {
            throw new MqttDecoderException("malformed properties length");
        }
        return in.readUnsignedShort();
    }

    static int unsignedShortOnlyOnce(
            final int current, final int notPresent, @NotNull final String name, @NotNull final ByteBuf in)
            throws MqttDecoderException {

        return unsignedShortOnlyOnce(current != notPresent, name, in);
    }

    static long unsignedIntOnlyOnce(
            final boolean present, @NotNull final String name, @NotNull final ByteBuf in) throws MqttDecoderException {

        if (present) {
            throw moreThanOnce(name);
        }
        if (in.readableBytes() < 4) {
            throw new MqttDecoderException("malformed properties length");
        }
        return in.readUnsignedInt();
    }

    static long unsignedIntOnlyOnce(
            final long current, final long notPresent, @NotNull final String name, @NotNull final ByteBuf in)
            throws MqttDecoderException {

        return unsignedIntOnlyOnce(current != notPresent, name, in);
    }

    @NotNull
    static ByteBuffer decodeBinaryDataOnlyOnce(
            @Nullable final ByteBuffer current, @NotNull final String name, @NotNull final ByteBuf in,
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

    @NotNull
    static MqttUTF8StringImpl decodeUTF8StringOnlyOnce(
            @Nullable final MqttUTF8StringImpl current, @NotNull final String name, @NotNull final ByteBuf in)
            throws MqttDecoderException {

        if (current != null) {
            throw moreThanOnce(name);
        }
        final MqttUTF8StringImpl decoded = MqttUTF8StringImpl.from(in);
        if (decoded == null) {
            throw malformedUTF8String(name);
        }
        return decoded;
    }

    @NotNull
    static MqttUTF8StringImpl decodeReasonString(
            @Nullable final MqttUTF8StringImpl current, @NotNull final ByteBuf in) throws MqttDecoderException {

        return decodeUTF8StringOnlyOnce(current, "reason string", in);
    }

    @NotNull
    static ImmutableList.Builder<MqttUserPropertyImpl> decodeUserProperty(
            @Nullable ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder, @NotNull final ByteBuf in)
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

    private static MqttDecoderException checkProblemInformationRequested(@NotNull final String name) {
        return new MqttDecoderException(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                name + " must not be included if problem information is not requested");
    }

    @NotNull
    static MqttUTF8StringImpl decodeReasonStringIfRequested(
            @Nullable final MqttUTF8StringImpl current, @NotNull final MqttClientConnectionData clientConnectionData,
            @NotNull final ByteBuf in)
            throws MqttDecoderException {

        if (!clientConnectionData.isProblemInformationRequested()) {
            throw checkProblemInformationRequested("reason string");
        }
        return decodeReasonString(current, in);
    }

    @NotNull
    static ImmutableList.Builder<MqttUserPropertyImpl> decodeUserPropertyIfRequested(
            @Nullable final ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder,
            @NotNull final MqttClientConnectionData clientConnectionData, @NotNull final ByteBuf in)
            throws MqttDecoderException {

        if ((userPropertiesBuilder == null) && !clientConnectionData.isProblemInformationRequested()) {
            throw checkProblemInformationRequested("user property");
        }
        return decodeUserProperty(userPropertiesBuilder, in);
    }

    @NotNull
    static MqttUTF8StringImpl decodeAuthMethod(
            @Nullable final MqttUTF8StringImpl current, @NotNull final ByteBuf in) throws MqttDecoderException {

        return decodeUTF8StringOnlyOnce(current, "auth method", in);
    }

    @NotNull
    static ByteBuffer decodeAuthData(
            @Nullable final ByteBuffer current, @NotNull final ByteBuf in, @NotNull final Channel channel)
            throws MqttDecoderException {

        return decodeBinaryDataOnlyOnce(current, "auth data", in, ChannelAttributes.useDirectBufferForAuth(channel));
    }

    @NotNull
    static MqttUTF8StringImpl decodeServerReference(
            @Nullable final MqttUTF8StringImpl current, @NotNull final ByteBuf in) throws MqttDecoderException {

        return decodeUTF8StringOnlyOnce(current, "server reference", in);
    }

    static long decodeSessionExpiryInterval(final long current, @NotNull final ByteBuf in) throws MqttDecoderException {
        return unsignedIntOnlyOnce(current, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, "session expiry interval", in);
    }

}
