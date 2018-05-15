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
import org.mqttbee.api.mqtt.exceptions.MqttVariableByteIntegerExceededException;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageWrapperEncoderApplier;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider.NewMqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.auth.MqttEnhancedAuth;
import org.mqttbee.mqtt.message.auth.MqttSimpleAuth;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.mqtt.message.connect.MqttConnectRestrictions;
import org.mqttbee.mqtt.message.connect.MqttConnectWrapper;
import org.mqttbee.mqtt.message.publish.MqttWillPublish;
import org.mqttbee.mqtt.message.publish.MqttWillPublishProperty;

import static org.mqttbee.mqtt.codec.encoder.MqttMessageEncoderUtil.*;
import static org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.*;
import static org.mqttbee.mqtt.message.connect.MqttConnect.*;
import static org.mqttbee.mqtt.message.connect.MqttConnectProperty.*;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ConnectEncoder extends Mqtt5WrappedMessageEncoder<MqttConnect, MqttConnectWrapper> {

    public static final MqttWrappedMessageEncoderProvider<MqttConnect, MqttConnectWrapper, MqttMessageEncoderProvider<MqttConnectWrapper>>
            PROVIDER = NewMqttWrappedMessageEncoderProvider.create(Mqtt5ConnectEncoder::new);

    private static final int VARIABLE_HEADER_FIXED_LENGTH =
            6 /* protocol name */ + 1 /* protocol version */ + 1 /* connect flags */ + 2 /* keep alive */;

    private int willPropertyLength = -1;

    @Override
    int calculateRemainingLengthWithoutProperties() {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        final MqttSimpleAuth simpleAuth = message.getRawSimpleAuth();
        if (simpleAuth != null) {
            remainingLength += nullableEncodedLength(simpleAuth.getRawUsername());
            remainingLength += nullableEncodedLength(simpleAuth.getRawPassword());
        }

        final MqttWillPublish willPublish = message.getRawWillPublish();
        if (willPublish != null) {
            remainingLength += encodedLengthWithHeader(willPropertyLength());
            remainingLength += willPublish.getTopic().encodedLength();
            remainingLength += encodedOrEmptyLength(willPublish.getRawPayload());
        }

        return remainingLength;
    }

    @Override
    int calculatePropertyLength() {
        int propertyLength = 0;

        propertyLength += intPropertyEncodedLength(message.getSessionExpiryInterval(), DEFAULT_SESSION_EXPIRY_INTERVAL);
        propertyLength += booleanPropertyEncodedLength(message.isResponseInformationRequested(),
                DEFAULT_RESPONSE_INFORMATION_REQUESTED);
        propertyLength += booleanPropertyEncodedLength(message.isProblemInformationRequested(),
                DEFAULT_PROBLEM_INFORMATION_REQUESTED);

        final MqttConnectRestrictions restrictions = message.getRestrictions();
        if (restrictions != MqttConnectRestrictions.DEFAULT) {
            propertyLength += shortPropertyEncodedLength(restrictions.getReceiveMaximum(),
                    Mqtt5ConnectRestrictions.DEFAULT_RECEIVE_MAXIMUM);
            propertyLength += shortPropertyEncodedLength(restrictions.getTopicAliasMaximum(),
                    Mqtt5ConnectRestrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM);
            propertyLength += intPropertyEncodedLength(restrictions.getMaximumPacketSize(),
                    Mqtt5ConnectRestrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT);
        }

        propertyLength += message.getUserProperties().encodedLength();

        return propertyLength;
    }

    private int willPropertyLength() {
        if (willPropertyLength == -1) {
            willPropertyLength = calculateWillPropertyLength();
        }
        return willPropertyLength;
    }

    private int calculateWillPropertyLength() {
        int willPropertyLength = 0;

        final MqttWillPublish willPublish = message.getRawWillPublish();
        if (willPublish != null) {
            willPropertyLength = ((Mqtt5PublishEncoder) willPublish.getEncoder()).propertyLength() +
                    intPropertyEncodedLength(willPublish.getDelayInterval(), Mqtt5WillPublish.DEFAULT_DELAY_INTERVAL);

            if (!MqttVariableByteInteger.isInRange(willPropertyLength)) {
                throw new MqttVariableByteIntegerExceededException("will properties length"); // TODO
            }
        }

        return willPropertyLength;
    }

    @NotNull
    @Override
    public MqttMessageEncoder wrap(@NotNull final MqttConnectWrapper wrapper) {
        return Mqtt5ConnectWrapperEncoder.APPLIER.apply(wrapper, this);
    }


    public static class Mqtt5ConnectWrapperEncoder extends
            Mqtt5MessageWrapperEncoder<MqttConnectWrapper, MqttConnect, MqttMessageEncoderProvider<MqttConnectWrapper>, Mqtt5ConnectEncoder> {

        private static final MqttMessageWrapperEncoderApplier<MqttConnectWrapper, MqttConnect, Mqtt5ConnectEncoder>
                APPLIER = new ThreadLocalMqttMessageWrapperEncoderApplier<>(Mqtt5ConnectWrapperEncoder::new);

        private static final int FIXED_HEADER = Mqtt5MessageType.CONNECT.getCode() << 4;
        private static final byte PROTOCOL_VERSION = 5;

        @Override
        int additionalRemainingLength(@NotNull final MqttConnectWrapper message) {
            return message.getClientIdentifier().encodedLength();
        }

        @Override
        int additionalPropertyLength(@NotNull final MqttConnectWrapper message) {
            int additionalPropertyLength = 0;

            final MqttEnhancedAuth enhancedAuth = message.getEnhancedAuth();
            if (enhancedAuth != null) {
                additionalPropertyLength += propertyEncodedLength(enhancedAuth.getMethod());
                additionalPropertyLength += nullablePropertyEncodedLength(enhancedAuth.getRawData());
            }

            return additionalPropertyLength;
        }

        @Override
        protected void encode(
                @NotNull final MqttConnectWrapper message, @NotNull final ByteBuf out, final int remainingLength,
                final int propertyLength, final int omittedProperties) {

            encodeFixedHeader(out, remainingLength);
            encodeVariableHeader(message, out, propertyLength, omittedProperties);
            encodePayload(message, out);
        }

        private void encodeFixedHeader(@NotNull final ByteBuf out, final int remainingLength) {
            out.writeByte(FIXED_HEADER);
            MqttVariableByteInteger.encode(remainingLength, out);
        }

        private void encodeVariableHeader(
                @NotNull final MqttConnectWrapper message, @NotNull final ByteBuf out, final int propertyLength,
                final int omittedProperties) {

            final MqttConnect connect = message.getWrapped();

            MqttUTF8StringImpl.PROTOCOL_NAME.to(out);
            out.writeByte(PROTOCOL_VERSION);

            int connectFlags = 0;

            final MqttSimpleAuth simpleAuth = connect.getRawSimpleAuth();
            if (simpleAuth != null) {
                if (simpleAuth.getRawUsername() != null) {
                    connectFlags |= 0b1000_0000;
                }
                if (simpleAuth.getRawPassword() != null) {
                    connectFlags |= 0b0100_0000;
                }
            }

            final MqttWillPublish willPublish = connect.getRawWillPublish();
            if (willPublish != null) {
                if (willPublish.isRetain()) {
                    connectFlags |= 0b0010_0000;
                }
                connectFlags |= willPublish.getQos().getCode() << 3;
                connectFlags |= 0b0000_0100;
            }

            if (connect.isCleanStart()) {
                connectFlags |= 0b0000_0010;
            }

            out.writeByte(connectFlags);

            out.writeShort(connect.getKeepAlive());

            encodeProperties(message, out, propertyLength, omittedProperties);
        }

        private void encodeProperties(
                @NotNull final MqttConnectWrapper message, @NotNull final ByteBuf out, final int propertyLength,
                final int omittedProperties) {

            final MqttConnect connect = message.getWrapped();

            MqttVariableByteInteger.encode(propertyLength, out);

            encodeIntProperty(
                    SESSION_EXPIRY_INTERVAL, connect.getSessionExpiryInterval(), DEFAULT_SESSION_EXPIRY_INTERVAL, out);
            encodeBooleanProperty(REQUEST_RESPONSE_INFORMATION, connect.isResponseInformationRequested(),
                    DEFAULT_RESPONSE_INFORMATION_REQUESTED, out);
            encodeBooleanProperty(REQUEST_PROBLEM_INFORMATION, connect.isProblemInformationRequested(),
                    DEFAULT_PROBLEM_INFORMATION_REQUESTED, out);

            final MqttEnhancedAuth enhancedAuth = message.getEnhancedAuth();
            if (enhancedAuth != null) {
                encodeProperty(AUTHENTICATION_METHOD, enhancedAuth.getMethod(), out);
                encodeNullableProperty(AUTHENTICATION_DATA, enhancedAuth.getRawData(), out);
            }

            final MqttConnectRestrictions restrictions = connect.getRestrictions();
            if (restrictions != MqttConnectRestrictions.DEFAULT) {
                encodeShortProperty(RECEIVE_MAXIMUM, restrictions.getReceiveMaximum(),
                        Mqtt5ConnectRestrictions.DEFAULT_RECEIVE_MAXIMUM, out);
                encodeShortProperty(TOPIC_ALIAS_MAXIMUM, restrictions.getTopicAliasMaximum(),
                        Mqtt5ConnectRestrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM, out);
                encodeIntProperty(MAXIMUM_PACKET_SIZE, restrictions.getMaximumPacketSize(),
                        Mqtt5ConnectRestrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT, out);
            }

            encodeOmissibleProperties(message, out, omittedProperties);
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
            final MqttConnect connect = message.getWrapped();

            final MqttWillPublish willPublish = connect.getRawWillPublish();
            if (willPublish != null) {
                final int willPropertyLength = wrappedEncoder.willPropertyLength();
                MqttVariableByteInteger.encode(willPropertyLength, out);

                ((Mqtt5PublishEncoder) willPublish.getEncoder()).encodeFixedProperties(out);
                willPublish.getUserProperties().encode(out);
                encodeIntProperty(MqttWillPublishProperty.WILL_DELAY_INTERVAL, willPublish.getDelayInterval(),
                        Mqtt5WillPublish.DEFAULT_DELAY_INTERVAL, out);

                willPublish.getTopic().to(out);
                encodeOrEmpty(willPublish.getRawPayload(), out);
            }
        }

    }

}
