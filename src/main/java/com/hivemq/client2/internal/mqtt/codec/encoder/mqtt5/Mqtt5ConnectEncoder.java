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

import com.hivemq.client2.internal.mqtt.codec.encoder.MqttEncoderContext;
import com.hivemq.client2.internal.mqtt.codec.encoder.MqttMessageEncoder;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client2.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client2.internal.mqtt.message.auth.MqttEnhancedAuth;
import com.hivemq.client2.internal.mqtt.message.auth.MqttSimpleAuth;
import com.hivemq.client2.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client2.internal.mqtt.message.connect.MqttConnectRestrictions;
import com.hivemq.client2.internal.mqtt.message.connect.MqttStatefulConnect;
import com.hivemq.client2.internal.mqtt.message.publish.MqttWillPublish;
import com.hivemq.client2.internal.mqtt.message.publish.MqttWillPublishProperty;
import com.hivemq.client2.mqtt.exceptions.MqttEncodeException;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5MessageType;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.hivemq.client2.internal.mqtt.codec.encoder.MqttMessageEncoderUtil.*;
import static com.hivemq.client2.internal.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.*;
import static com.hivemq.client2.internal.mqtt.message.connect.MqttConnect.DEFAULT_SESSION_EXPIRY_INTERVAL;
import static com.hivemq.client2.internal.mqtt.message.connect.MqttConnectProperty.*;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5ConnectEncoder extends MqttMessageEncoder<MqttStatefulConnect> {

    private static final int FIXED_HEADER = Mqtt5MessageType.CONNECT.getCode() << 4;
    private static final byte PROTOCOL_VERSION = 5;
    private static final int VARIABLE_HEADER_FIXED_LENGTH =
            6 /* protocol name */ + 1 /* protocol version */ + 1 /* connect flags */ + 2 /* keep alive */;

    private final @NotNull Mqtt5PublishEncoder publishEncoder;

    @Inject
    Mqtt5ConnectEncoder(final @NotNull Mqtt5PublishEncoder publishEncoder) {
        this.publishEncoder = publishEncoder;
    }

    @Override
    protected @NotNull ByteBuf encode(
            final @NotNull MqttStatefulConnect message, final @NotNull MqttEncoderContext context) {

        int propertyLength = propertyLength(message);
        final int willPropertyLength = willPropertyLength(message);
        final int remainingLengthWithoutProperties = remainingLengthWithoutProperties(message);
        int remainingLength = remainingLength(remainingLengthWithoutProperties, propertyLength, willPropertyLength);
        int encodedLength = encodedPacketLength(remainingLength);
        int omittedProperties = 0;
        while (encodedLength > context.getMaximumPacketSize()) {
            omittedProperties++;
            propertyLength = propertyLength(message, propertyLength, omittedProperties);
            if (propertyLength < 0) {
                throw maximumPacketSizeExceeded(message, encodedLength, context.getMaximumPacketSize());
            }
            remainingLength = remainingLength(remainingLengthWithoutProperties, propertyLength, willPropertyLength);
            encodedLength = encodedPacketLength(remainingLength);
        }
        return encode(message, context, encodedLength, remainingLength, propertyLength, willPropertyLength,
                omittedProperties);
    }

    private @NotNull ByteBuf encode(
            final @NotNull MqttStatefulConnect message,
            final @NotNull MqttEncoderContext context,
            final int encodedLength,
            final int remainingLength,
            final int propertyLength,
            final int willPropertyLength,
            final int omittedProperties) {

        final ByteBuf out = context.getAllocator().ioBuffer(encodedLength, encodedLength);
        encode(message, out, remainingLength, propertyLength, willPropertyLength, omittedProperties);
        return out;
    }

    private int remainingLength(
            final int remainingLengthWithoutProperties, final int propertyLength, final int willPropertyLength) {

        return remainingLengthWithoutProperties + encodedLengthWithHeader(propertyLength) +
                ((willPropertyLength == -1) ? 0 : encodedLengthWithHeader(willPropertyLength));
    }

    private int remainingLengthWithoutProperties(final @NotNull MqttStatefulConnect message) {
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
            remainingLength += encodedOrEmptyLength(willPublish.getRawPayload());
        }

        return remainingLength;
    }

    private int propertyLength(final @NotNull MqttStatefulConnect message) {
        final MqttConnect stateless = message.stateless();

        int propertyLength = 0;

        propertyLength +=
                intPropertyEncodedLength(stateless.getSessionExpiryInterval(), DEFAULT_SESSION_EXPIRY_INTERVAL);

        final MqttConnectRestrictions restrictions = stateless.getRestrictions();
        if (restrictions != MqttConnectRestrictions.DEFAULT) {
            propertyLength += shortPropertyEncodedLength(restrictions.getReceiveMaximum(),
                    MqttConnectRestrictions.DEFAULT_RECEIVE_MAXIMUM);
            propertyLength += intPropertyEncodedLength(restrictions.getMaximumPacketSize(),
                    MqttConnectRestrictions.DEFAULT_MAXIMUM_PACKET_SIZE);
            propertyLength += shortPropertyEncodedLength(restrictions.getTopicAliasMaximum(),
                    MqttConnectRestrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM);
            propertyLength += booleanPropertyEncodedLength(restrictions.isRequestResponseInformation(),
                    MqttConnectRestrictions.DEFAULT_REQUEST_RESPONSE_INFORMATION);
            propertyLength += booleanPropertyEncodedLength(restrictions.isRequestProblemInformation(),
                    MqttConnectRestrictions.DEFAULT_REQUEST_PROBLEM_INFORMATION);
        }

        propertyLength += message.getUserProperties().encodedLength();

        final MqttEnhancedAuth enhancedAuth = message.getEnhancedAuth();
        if (enhancedAuth != null) {
            propertyLength += propertyEncodedLength(enhancedAuth.getMethod());
            propertyLength += nullablePropertyEncodedLength(enhancedAuth.getRawData());
        }

        return propertyLength;
    }

    private int propertyLength(
            final @NotNull MqttStatefulConnect message, final int propertyLength, final int omittedProperties) {

        switch (omittedProperties) {
            case 0:
                return propertyLength;
            case 1:
                return propertyLength - message.getUserProperties().encodedLength();
            default:
                return -1;
        }
    }

    private int willPropertyLength(final @NotNull MqttStatefulConnect message) {
        final MqttWillPublish willPublish = message.stateless().getRawWillPublish();
        if (willPublish == null) {
            return -1;
        }

        int willPropertyLength = publishEncoder.fixedPropertyLength(willPublish);
        willPropertyLength += willPublish.getUserProperties().encodedLength();
        willPropertyLength +=
                intPropertyEncodedLength(willPublish.getDelayInterval(), MqttWillPublish.DEFAULT_DELAY_INTERVAL);

        if (!MqttVariableByteInteger.isInRange(willPropertyLength)) {
            throw new MqttEncodeException("Will properties exceeded maximum length.");
        }

        return willPropertyLength;
    }

    private void encode(
            final @NotNull MqttStatefulConnect message,
            final @NotNull ByteBuf out,
            final int remainingLength,
            final int propertyLength,
            final int willPropertyLength,
            final int omittedProperties) {

        encodeFixedHeader(out, remainingLength);
        encodeVariableHeader(message, out, propertyLength, omittedProperties);
        encodePayload(message, out, willPropertyLength);
    }

    private void encodeFixedHeader(final @NotNull ByteBuf out, final int remainingLength) {
        out.writeByte(FIXED_HEADER);
        MqttVariableByteInteger.encode(remainingLength, out);
    }

    private void encodeVariableHeader(
            final @NotNull MqttStatefulConnect message,
            final @NotNull ByteBuf out,
            final int propertyLength,
            final int omittedProperties) {

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
            if (willPublish.isRetain()) {
                connectFlags |= 0b0010_0000;
            }
            connectFlags |= willPublish.getQos().getCode() << 3;
            connectFlags |= 0b0000_0100;
        }

        if (stateless.isCleanStart()) {
            connectFlags |= 0b0000_0010;
        }

        out.writeByte(connectFlags);

        out.writeShort(stateless.getKeepAlive());

        encodeProperties(message, out, propertyLength, omittedProperties);
    }

    private void encodeProperties(
            final @NotNull MqttStatefulConnect message,
            final @NotNull ByteBuf out,
            final int propertyLength,
            final int omittedProperties) {

        final MqttConnect stateless = message.stateless();

        MqttVariableByteInteger.encode(propertyLength, out);

        encodeIntProperty(
                SESSION_EXPIRY_INTERVAL, stateless.getSessionExpiryInterval(), DEFAULT_SESSION_EXPIRY_INTERVAL, out);

        final MqttEnhancedAuth enhancedAuth = message.getEnhancedAuth();
        if (enhancedAuth != null) {
            encodeProperty(AUTHENTICATION_METHOD, enhancedAuth.getMethod(), out);
            encodeNullableProperty(AUTHENTICATION_DATA, enhancedAuth.getRawData(), out);
        }

        final MqttConnectRestrictions restrictions = stateless.getRestrictions();
        if (restrictions != MqttConnectRestrictions.DEFAULT) {
            encodeShortProperty(RECEIVE_MAXIMUM, restrictions.getReceiveMaximum(),
                    MqttConnectRestrictions.DEFAULT_RECEIVE_MAXIMUM, out);
            encodeIntProperty(MAXIMUM_PACKET_SIZE, restrictions.getMaximumPacketSize(),
                    MqttConnectRestrictions.DEFAULT_MAXIMUM_PACKET_SIZE, out);
            encodeShortProperty(TOPIC_ALIAS_MAXIMUM, restrictions.getTopicAliasMaximum(),
                    MqttConnectRestrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM, out);
            encodeBooleanProperty(REQUEST_RESPONSE_INFORMATION, restrictions.isRequestResponseInformation(),
                    MqttConnectRestrictions.DEFAULT_REQUEST_RESPONSE_INFORMATION, out);
            encodeBooleanProperty(REQUEST_PROBLEM_INFORMATION, restrictions.isRequestProblemInformation(),
                    MqttConnectRestrictions.DEFAULT_REQUEST_PROBLEM_INFORMATION, out);
        }

        if (omittedProperties == 0) {
            message.getUserProperties().encode(out);
        }
    }

    private void encodePayload(
            final @NotNull MqttStatefulConnect message, final @NotNull ByteBuf out, final int willPropertyLength) {

        final MqttConnect stateless = message.stateless();

        message.getClientIdentifier().encode(out);

        encodeWillPublish(stateless, out, willPropertyLength);

        final MqttSimpleAuth simpleAuth = stateless.getRawSimpleAuth();
        if (simpleAuth != null) {
            encodeNullable(simpleAuth.getRawUsername(), out);
            encodeNullable(simpleAuth.getRawPassword(), out);
        }
    }

    private void encodeWillPublish(
            final @NotNull MqttConnect message, final @NotNull ByteBuf out, final int willPropertyLength) {

        final MqttWillPublish willPublish = message.getRawWillPublish();
        if (willPublish != null) {
            MqttVariableByteInteger.encode(willPropertyLength, out);

            publishEncoder.encodeFixedProperties(willPublish, out);
            willPublish.getUserProperties().encode(out);
            encodeIntProperty(MqttWillPublishProperty.WILL_DELAY_INTERVAL, willPublish.getDelayInterval(),
                    MqttWillPublish.DEFAULT_DELAY_INTERVAL, out);

            willPublish.getTopic().encode(out);
            encodeOrEmpty(willPublish.getRawPayload(), out);
        }
    }
}
