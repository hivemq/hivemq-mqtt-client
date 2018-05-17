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
import io.netty.buffer.ByteBufAllocator;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.exceptions.MqttMaximumPacketSizeExceededException;
import org.mqttbee.api.mqtt.exceptions.MqttVariableByteIntegerExceededException;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.auth.MqttEnhancedAuth;
import org.mqttbee.mqtt.message.auth.MqttSimpleAuth;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.mqtt.message.connect.MqttConnectRestrictions;
import org.mqttbee.mqtt.message.connect.MqttConnectWrapper;
import org.mqttbee.mqtt.message.publish.MqttWillPublish;
import org.mqttbee.mqtt.message.publish.MqttWillPublishProperty;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.mqttbee.mqtt.codec.encoder.MqttMessageEncoderUtil.*;
import static org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.*;
import static org.mqttbee.mqtt.message.connect.MqttConnect.*;
import static org.mqttbee.mqtt.message.connect.MqttConnectProperty.*;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5ConnectEncoder extends MqttMessageEncoder<MqttConnectWrapper> {

    private static final int FIXED_HEADER = Mqtt5MessageType.CONNECT.getCode() << 4;
    private static final byte PROTOCOL_VERSION = 5;
    private static final int VARIABLE_HEADER_FIXED_LENGTH =
            6 /* protocol name */ + 1 /* protocol version */ + 1 /* connect flags */ + 2 /* keep alive */;

    private final Mqtt5PublishEncoder publishEncoder;

    @Inject
    Mqtt5ConnectEncoder(final Mqtt5PublishEncoder publishEncoder) {
        this.publishEncoder = publishEncoder;
    }

    @NotNull
    @Override
    protected ByteBuf encode(
            @NotNull final MqttConnectWrapper message, @NotNull final ByteBufAllocator allocator,
            final int maximumPacketSize) {

        int propertyLength = propertyLength(message);
        final int willPropertyLength = willPropertyLength(message);
        final int remainingLengthWithoutProperties = remainingLengthWithoutProperties(message);
        int remainingLength = remainingLength(remainingLengthWithoutProperties, propertyLength, willPropertyLength);
        int encodedLength = encodedPacketLength(remainingLength);
        int omittedProperties = 0;
        while (encodedLength > maximumPacketSize) {
            omittedProperties++;
            propertyLength = propertyLength(message, propertyLength, omittedProperties);
            if (propertyLength < 0) {
                throw new MqttMaximumPacketSizeExceededException(message, encodedLength, maximumPacketSize);
            }
            remainingLength = remainingLength(remainingLengthWithoutProperties, propertyLength, willPropertyLength);
            encodedLength = encodedPacketLength(remainingLength);
        }
        return encode(message, allocator, encodedLength, remainingLength, propertyLength, willPropertyLength,
                omittedProperties);
    }

    @NotNull
    ByteBuf encode(
            @NotNull final MqttConnectWrapper message, @NotNull final ByteBufAllocator allocator,
            final int encodedLength, final int remainingLength, final int propertyLength, final int willPropertyLength,
            final int omittedProperties) {

        final ByteBuf out = allocator.ioBuffer(encodedLength, encodedLength);
        encode(message, out, remainingLength, propertyLength, willPropertyLength, omittedProperties);
        return out;
    }

    private int remainingLength(
            final int remainingLengthWithoutProperties, final int propertyLength, final int willPropertyLength) {

        return remainingLengthWithoutProperties + encodedLengthWithHeader(propertyLength) +
                ((willPropertyLength == -1) ? 0 : encodedLengthWithHeader(willPropertyLength));
    }

    private int remainingLengthWithoutProperties(@NotNull final MqttConnectWrapper message) {
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

    private int propertyLength(@NotNull final MqttConnectWrapper message) {
        final MqttConnect wrapped = message.getWrapped();

        int propertyLength = 0;

        propertyLength += intPropertyEncodedLength(wrapped.getSessionExpiryInterval(), DEFAULT_SESSION_EXPIRY_INTERVAL);
        propertyLength += booleanPropertyEncodedLength(
                wrapped.isResponseInformationRequested(),
                DEFAULT_RESPONSE_INFORMATION_REQUESTED);
        propertyLength += booleanPropertyEncodedLength(
                wrapped.isProblemInformationRequested(),
                DEFAULT_PROBLEM_INFORMATION_REQUESTED);

        final MqttConnectRestrictions restrictions = wrapped.getRestrictions();
        if (restrictions != MqttConnectRestrictions.DEFAULT) {
            propertyLength += shortPropertyEncodedLength(restrictions.getReceiveMaximum(),
                    MqttConnectRestrictions.DEFAULT_RECEIVE_MAXIMUM);
            propertyLength += shortPropertyEncodedLength(restrictions.getTopicAliasMaximum(),
                    MqttConnectRestrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM);
            propertyLength += intPropertyEncodedLength(restrictions.getMaximumPacketSize(),
                    MqttConnectRestrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT);
        }

        propertyLength += wrapped.getUserProperties().encodedLength();

        final MqttEnhancedAuth enhancedAuth = message.getEnhancedAuth();
        if (enhancedAuth != null) {
            propertyLength += propertyEncodedLength(enhancedAuth.getMethod());
            propertyLength += nullablePropertyEncodedLength(enhancedAuth.getRawData());
        }

        return propertyLength;
    }

    private int propertyLength(
            @NotNull final MqttConnectWrapper message, final int propertyLength, final int omittedProperties) {

        switch (omittedProperties) {
            case 0:
                return propertyLength;
            case 1:
                return propertyLength - message.getWrapped().getUserProperties().encodedLength();
            default:
                return -1;
        }
    }

    private int willPropertyLength(@NotNull final MqttConnectWrapper message) {
        final MqttWillPublish willPublish = message.getWrapped().getRawWillPublish();
        if (willPublish == null) {
            return -1;
        }

        int willPropertyLength = publishEncoder.fixedPropertyLength(willPublish);
        willPropertyLength += willPublish.getUserProperties().encodedLength();
        willPropertyLength +=
                intPropertyEncodedLength(willPublish.getDelayInterval(), MqttWillPublish.DEFAULT_DELAY_INTERVAL);

        if (!MqttVariableByteInteger.isInRange(willPropertyLength)) {
            throw new MqttVariableByteIntegerExceededException("will properties length"); // TODO
        }

        return willPropertyLength;
    }

    protected void encode(
            @NotNull final MqttConnectWrapper message, @NotNull final ByteBuf out, final int remainingLength,
            final int propertyLength, final int willPropertyLength, final int omittedProperties) {

        encodeFixedHeader(out, remainingLength);
        encodeVariableHeader(message, out, propertyLength, omittedProperties);
        encodePayload(message, out, willPropertyLength);
    }

    private void encodeFixedHeader(@NotNull final ByteBuf out, final int remainingLength) {
        out.writeByte(FIXED_HEADER);
        MqttVariableByteInteger.encode(remainingLength, out);
    }

    private void encodeVariableHeader(
            @NotNull final MqttConnectWrapper message, @NotNull final ByteBuf out, final int propertyLength,
            final int omittedProperties) {

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
            if (willPublish.isRetain()) {
                connectFlags |= 0b0010_0000;
            }
            connectFlags |= willPublish.getQos().getCode() << 3;
            connectFlags |= 0b0000_0100;
        }

        if (wrapped.isCleanStart()) {
            connectFlags |= 0b0000_0010;
        }

        out.writeByte(connectFlags);

        out.writeShort(wrapped.getKeepAlive());

        encodeProperties(message, out, propertyLength, omittedProperties);
    }

    private void encodeProperties(
            @NotNull final MqttConnectWrapper message, @NotNull final ByteBuf out, final int propertyLength,
            final int omittedProperties) {

        final MqttConnect wrapped = message.getWrapped();

        MqttVariableByteInteger.encode(propertyLength, out);

        encodeIntProperty(
                SESSION_EXPIRY_INTERVAL, wrapped.getSessionExpiryInterval(), DEFAULT_SESSION_EXPIRY_INTERVAL, out);
        encodeBooleanProperty(REQUEST_RESPONSE_INFORMATION, wrapped.isResponseInformationRequested(),
                DEFAULT_RESPONSE_INFORMATION_REQUESTED, out);
        encodeBooleanProperty(REQUEST_PROBLEM_INFORMATION, wrapped.isProblemInformationRequested(),
                DEFAULT_PROBLEM_INFORMATION_REQUESTED, out);

        final MqttEnhancedAuth enhancedAuth = message.getEnhancedAuth();
        if (enhancedAuth != null) {
            encodeProperty(AUTHENTICATION_METHOD, enhancedAuth.getMethod(), out);
            encodeNullableProperty(AUTHENTICATION_DATA, enhancedAuth.getRawData(), out);
        }

        final MqttConnectRestrictions restrictions = wrapped.getRestrictions();
        if (restrictions != MqttConnectRestrictions.DEFAULT) {
            encodeShortProperty(RECEIVE_MAXIMUM, restrictions.getReceiveMaximum(),
                    MqttConnectRestrictions.DEFAULT_RECEIVE_MAXIMUM, out);
            encodeShortProperty(TOPIC_ALIAS_MAXIMUM, restrictions.getTopicAliasMaximum(),
                    MqttConnectRestrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM, out);
            encodeIntProperty(MAXIMUM_PACKET_SIZE, restrictions.getMaximumPacketSize(),
                    MqttConnectRestrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT, out);
        }

        if (omittedProperties == 0) {
            wrapped.getUserProperties().encode(out);
        }
    }

    private void encodePayload(
            @NotNull final MqttConnectWrapper message, @NotNull final ByteBuf out, final int willPropertyLength) {

        final MqttConnect wrapped = message.getWrapped();

        message.getClientIdentifier().to(out);

        encodeWillPublish(wrapped, out, willPropertyLength);

        final MqttSimpleAuth simpleAuth = wrapped.getRawSimpleAuth();
        if (simpleAuth != null) {
            encodeNullable(simpleAuth.getRawUsername(), out);
            encodeNullable(simpleAuth.getRawPassword(), out);
        }
    }

    private void encodeWillPublish(
            @NotNull final MqttConnect message, @NotNull final ByteBuf out, final int willPropertyLength) {

        final MqttWillPublish willPublish = message.getRawWillPublish();
        if (willPublish != null) {
            MqttVariableByteInteger.encode(willPropertyLength, out);

            publishEncoder.encodeFixedProperties(willPublish, out);
            willPublish.getUserProperties().encode(out);
            encodeIntProperty(MqttWillPublishProperty.WILL_DELAY_INTERVAL, willPublish.getDelayInterval(),
                    MqttWillPublish.DEFAULT_DELAY_INTERVAL, out);

            willPublish.getTopic().to(out);
            encodeOrEmpty(willPublish.getRawPayload(), out);
        }
    }

}
