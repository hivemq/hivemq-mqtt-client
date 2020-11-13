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

package com.hivemq.client.internal.mqtt.codec.encoder.mqtt5;

import com.hivemq.client.internal.mqtt.codec.encoder.MqttMessageEncoders;
import com.hivemq.client.internal.mqtt.datatypes.*;
import com.hivemq.client.internal.mqtt.message.auth.MqttEnhancedAuth;
import com.hivemq.client.internal.mqtt.message.auth.MqttSimpleAuth;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnectRestrictions;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnectRestrictionsBuilder;
import com.hivemq.client.internal.mqtt.message.connect.MqttStatefulConnect;
import com.hivemq.client.internal.mqtt.message.publish.MqttWillPublish;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.exceptions.MqttEncodeException;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConfig;
import com.hivemq.client.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5MessageType;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5Auth;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5AuthBuilder;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuthBuilder;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import io.netty.handler.codec.EncoderException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static com.hivemq.client.internal.mqtt.message.connect.MqttConnectRestrictions.DEFAULT;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Silvio Giebl
 * @author David Katz
 */
class Mqtt5ConnectEncoderTest extends AbstractMqtt5EncoderTest {

    private static final int VARIABLE_BYTE_INTEGER_VALUE_BITS = 7;
    private static final int VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE =
            (1 << (VARIABLE_BYTE_INTEGER_VALUE_BITS * 4)) - 1;

    Mqtt5ConnectEncoderTest() {
        super(new MqttMessageEncoders() {{
            encoders[Mqtt5MessageType.CONNECT.getCode()] = new Mqtt5ConnectEncoder(new Mqtt5PublishEncoder());
        }}, false);
    }

    @Test
    void encode_allProperties() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0001_0000,
                // remaining length (223)
                (byte) (128 + 95), 1,
                // variable header
                //   protocol name
                0, 4, 'M', 'Q', 'T', 'T',
                //   protocol version
                5,
                //   connect flags
                (byte) 0b1110_1110,
                //   keep alive
                0, 10,
                //   properties
                88,
                //     session expiry interval
                0x11, 0, 0, 0, 10,
                //     auth method
                0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5',
                //     auth data
                0x16, 0, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                //     receive maximum
                0x21, 0, 5,
                //     maximum packet size
                0x27, 0, 0, 0, 100,
                //     topic alias maximum
                0x22, 0, 10,
                //     request response information
                0x19, 1,
                //     request problem information
                0x17, 0,
                //     user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2', //
                0x26, 0, 5, 't', 'e', 's', 't', '2', 0, 5, 'v', 'a', 'l', 'u', 'e',
                // payload
                //   client identifier
                0, 4, 't', 'e', 's', 't',
                //   will properties
                82,
                //     message expiry interval
                0x02, 0, 0, 0, 10,
                //     payload format indicator
                0x01, 1,
                //     content type
                0x03, 0, 4, 't', 'e', 'x', 't',
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     correlation data
                0x09, 0, 5, 5, 4, 3, 2, 1,
                //     user property
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2', //
                0x26, 0, 5, 't', 'e', 's', 't', '2', 0, 5, 'v', 'a', 'l', 'u', 'e',
                //     will delay interval
                24, 0, 0, 0, 5,
                //   will topic
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   will payload
                0, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                //   username
                0, 8, 'u', 's', 'e', 'r', 'n', 'a', 'm', 'e',
                //   password
                0, 4, 1, 5, 6, 3
        };

        final MqttClientIdentifierImpl clientIdentifier = MqttClientIdentifierImpl.of("test");

        final MqttClientIdentifierImpl username = MqttClientIdentifierImpl.of("username");
        final ByteBuffer password = ByteBuffer.wrap(new byte[]{1, 5, 6, 3});
        final MqttSimpleAuth simpleAuth = new MqttSimpleAuth(username, password);

        final MqttUtf8StringImpl authMethod = MqttUtf8StringImpl.of("GS2-KRB5");
        final ByteBuffer authData = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        final MqttEnhancedAuth enhancedAuth = new MqttEnhancedAuth(authMethod, authData);
        final Mqtt5EnhancedAuthMechanism enhancedAuthProvider = new TestEnhancedAuthMechanism(authMethod);

        final MqttUtf8StringImpl test = MqttUtf8StringImpl.of("test");
        final MqttUtf8StringImpl test2 = MqttUtf8StringImpl.of("test2");
        final MqttUtf8StringImpl value = MqttUtf8StringImpl.of("value");
        final MqttUtf8StringImpl value2 = MqttUtf8StringImpl.of("value2");
        final MqttUserPropertyImpl userProperty1 = new MqttUserPropertyImpl(test, value);
        final MqttUserPropertyImpl userProperty2 = new MqttUserPropertyImpl(test, value2);
        final MqttUserPropertyImpl userProperty3 = new MqttUserPropertyImpl(test2, value);
        final MqttUserPropertiesImpl userProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(userProperty1, userProperty2, userProperty3));

        final MqttTopicImpl willTopic = MqttTopicImpl.of("topic");
        final ByteBuffer willPayload = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        final MqttQos willQos = MqttQos.AT_LEAST_ONCE;
        final MqttUtf8StringImpl willContentType = MqttUtf8StringImpl.of("text");
        final MqttTopicImpl willResponseTopic = MqttTopicImpl.of("response");
        final ByteBuffer willCorrelationData = ByteBuffer.wrap(new byte[]{5, 4, 3, 2, 1});
        final MqttWillPublish willPublish =
                new MqttWillPublish(willTopic, willPayload, willQos, true, 10, Mqtt5PayloadFormatIndicator.UTF_8,
                        willContentType, willResponseTopic, willCorrelationData, userProperties, 5);

        final MqttConnectRestrictions restrictions = new MqttConnectRestrictionsBuilder.Default().receiveMaximum(5)
                .maximumPacketSize(100)
                .topicAliasMaximum(10)
                .requestProblemInformation(false)
                .requestResponseInformation(true)
                .build();

        final MqttConnect connect =
                new MqttConnect(10, true, 10, restrictions, simpleAuth, enhancedAuthProvider, willPublish,
                        userProperties);
        final MqttStatefulConnect connectWrapper = connect.createStateful(clientIdentifier, enhancedAuth);

        encode(expected, connectWrapper);
    }

    @Test
    void encode_simple() {
        final byte[] expected = {
                // fixed header
                //   type, reserved
                0b0001_0000,
                // remaining length
                17,
                // variable header
                //   protocol name
                0, 4, 'M', 'Q', 'T', 'T',
                //   protocol version
                5,
                //   connect flags
                (byte) 0b0000_0000,
                //   keep alive
                0, 0,
                //   properties
                0,
                // payload
                //   client identifier
                0, 4, 't', 'e', 's', 't'
        };

        final MqttClientIdentifierImpl clientIdentifier = MqttClientIdentifierImpl.of("test");
        final MqttConnect connect =
                new MqttConnect(0, false, 0, DEFAULT, null, null, null, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        final MqttStatefulConnect connectWrapper = connect.createStateful(clientIdentifier, null);

        encode(expected, connectWrapper);
    }

    @Test
    void encode_username() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0001_0000,
                // remaining length
                27,
                // variable header
                //   protocol name
                0, 4, 'M', 'Q', 'T', 'T',
                //   protocol version
                5,
                //   connect flags
                (byte) 0b1000_0000,
                //   keep alive
                0, 0,
                //   properties
                0,
                // payload
                //   client identifier
                0, 4, 't', 'e', 's', 't',
                //   username
                0, 8, 'u', 's', 'e', 'r', 'n', 'a', 'm', 'e'
        };

        final MqttClientIdentifierImpl clientIdentifier = MqttClientIdentifierImpl.of("test");
        final MqttUtf8StringImpl username = MqttUtf8StringImpl.of("username");
        final MqttSimpleAuth simpleAuth = new MqttSimpleAuth(username, null);

        final MqttConnect connect = new MqttConnect(0, false, 0, DEFAULT, simpleAuth, null, null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        final MqttStatefulConnect connectWrapper = connect.createStateful(clientIdentifier, null);

        encode(expected, connectWrapper);
    }

    @Test
    void encode_password() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0001_0000,
                // remaining length
                23,
                // variable header
                //   protocol name
                0, 4, 'M', 'Q', 'T', 'T',
                //   protocol version
                5,
                //   connect flags
                (byte) 0b0100_0000,
                //   keep alive
                0, 0,
                //   properties
                0,
                // payload
                //   client identifier
                0, 4, 't', 'e', 's', 't',
                //   username
                //   password
                0, 4, 1, 5, 6, 3
        };

        final MqttClientIdentifierImpl clientIdentifier = MqttClientIdentifierImpl.of("test");
        final ByteBuffer password = ByteBuffer.wrap(new byte[]{1, 5, 6, 3});
        final MqttSimpleAuth simpleAuth = new MqttSimpleAuth(null, password);

        final MqttConnect connect = new MqttConnect(0, false, 0, DEFAULT, simpleAuth, null, null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        final MqttStatefulConnect connectWrapper = connect.createStateful(clientIdentifier, null);

        encode(expected, connectWrapper);
    }

    @Test
    @Disabled("password will be validated in the builder, remove this test")
    void encode_passwordTooLong() {
        final MqttClientIdentifierImpl clientIdentifier = MqttClientIdentifierImpl.of("test");
        final ByteBuffer password = ByteBuffer.wrap(new byte[65536]);
        final MqttSimpleAuth simpleAuth = new MqttSimpleAuth(null, password);

        final MqttConnect connect = new MqttConnect(0, false, 0, DEFAULT, simpleAuth, null, null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        final MqttStatefulConnect connectWrapper = connect.createStateful(clientIdentifier, null);

        encodeNok(connectWrapper, EncoderException.class, "binary data size exceeded for password");
    }

    @Test
    void encode_zeroLengthClientIdentifier() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0001_0000,
                // remaining length
                13,
                // variable header
                //   protocol name
                0, 4, 'M', 'Q', 'T', 'T',
                //   protocol version
                5,
                //   connect flags
                (byte) 0b0000_0000,
                //   keep alive
                0, 0,
                //   properties
                0,
                // payload
                //   client identifier
                0, 0
        };

        final MqttClientIdentifierImpl clientIdentifier = MqttClientIdentifierImpl.of("");

        final MqttConnect connect =
                new MqttConnect(0, false, 0, DEFAULT, null, null, null, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        final MqttStatefulConnect connectWrapper = connect.createStateful(clientIdentifier, null);

        encode(expected, connectWrapper);
    }

    @Test
    void encode_will() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0001_0000,
                // remaining length
                37,
                // variable header
                //   protocol name
                0, 4, 'M', 'Q', 'T', 'T',
                //   protocol version
                5,
                //   connect flags
                (byte) 0b0000_0100,
                //   keep alive
                0, 0,
                //   properties
                0,
                // payload
                //   client identifier
                0, 4, 't', 'e', 's', 't',
                //   will properties
                0,
                //   will topic
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   will payload
                0, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };

        final MqttTopicImpl willTopic = MqttTopicImpl.of("topic");
        final ByteBuffer willPayload = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        final MqttClientIdentifierImpl clientIdentifier = MqttClientIdentifierImpl.of("test");
        final MqttWillPublish willPublish = new MqttWillPublish(willTopic, willPayload, MqttQos.AT_MOST_ONCE, false,
                MqttWillPublish.NO_MESSAGE_EXPIRY, null, null, null, null, MqttUserPropertiesImpl.NO_USER_PROPERTIES,
                0);
        final MqttConnect connect = new MqttConnect(0, false, 0, DEFAULT, null, null, willPublish,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        final MqttStatefulConnect connectWrapper = connect.createStateful(clientIdentifier, null);

        encode(expected, connectWrapper);
    }

    @Test
    @Disabled("will payload will be validated in the builder, remove this test")
    void encode_willPayloadTooLong() {
        final MqttTopicImpl willTopic = MqttTopicImpl.of("topic");
        final ByteBuffer willPayload = ByteBuffer.wrap(new byte[65536]);
        final MqttClientIdentifierImpl clientIdentifier = MqttClientIdentifierImpl.of("test");
        final MqttWillPublish willPublish = new MqttWillPublish(willTopic, willPayload, MqttQos.AT_MOST_ONCE, false,
                MqttWillPublish.NO_MESSAGE_EXPIRY, null, null, null, null, MqttUserPropertiesImpl.NO_USER_PROPERTIES,
                0);
        final MqttConnect connect = new MqttConnect(0, false, 0, DEFAULT, null, null, willPublish,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        final MqttStatefulConnect connectWrapper = connect.createStateful(clientIdentifier, null);

        encodeNok(connectWrapper, EncoderException.class, "binary data size exceeded for will payload");
    }

    @Test
    @Disabled("will correlation data will be validated in the builder, remove this test")
    void encode_willCorrelationDataTooLong() {
        final MqttTopicImpl willTopic = MqttTopicImpl.of("topic");
        final ByteBuffer correlationData = ByteBuffer.wrap(new byte[65536]);
        final MqttClientIdentifierImpl clientIdentifier = MqttClientIdentifierImpl.of("test");
        final MqttWillPublish willPublish =
                new MqttWillPublish(willTopic, null, MqttQos.AT_MOST_ONCE, false, MqttWillPublish.NO_MESSAGE_EXPIRY,
                        null, null, null, correlationData, MqttUserPropertiesImpl.NO_USER_PROPERTIES, 0);
        final MqttConnect connect = new MqttConnect(0, false, 0, DEFAULT, null, null, willPublish,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        final MqttStatefulConnect connectWrapper = connect.createStateful(clientIdentifier, null);

        encodeNok(connectWrapper, EncoderException.class, "binary data size exceeded for will correlation data");
    }

    @Test
    void encode_willPropertyLengthExceedsMax_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder();
        final MqttUserPropertiesImpl tooManyUserProperties = maxPacket.getUserProperties(
                (VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE / maxPacket.userPropertyBytes) + 1);

        final MqttTopicImpl willTopic = MqttTopicImpl.of("topic");
        final MqttClientIdentifierImpl clientIdentifier = MqttClientIdentifierImpl.of("test");
        final MqttWillPublish willPublish =
                new MqttWillPublish(willTopic, null, MqttQos.AT_MOST_ONCE, false, MqttWillPublish.NO_MESSAGE_EXPIRY,
                        null, null, null, null, tooManyUserProperties, 0);
        final MqttConnect connect = new MqttConnect(0, false, 0, DEFAULT, null, null, willPublish,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        final MqttStatefulConnect connectWrapper = connect.createStateful(clientIdentifier, null);

        encodeNok(connectWrapper, MqttEncodeException.class, "Will properties exceeded maximum length.");
    }

    @Test
    void encode_authentication() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0001_0000,
                // remaining length
                41,
                // variable header
                //   protocol name
                0, 4, 'M', 'Q', 'T', 'T',
                //   protocol version
                5,
                //   connect flags
                (byte) 0b0000_0000,
                //   keep alive
                0, 0,
                //   properties
                24,
                //     auth method
                0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5',
                //     auth data
                0x16, 0, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                // payload
                //   client identifier
                0, 4, 't', 'e', 's', 't'
        };

        final MqttClientIdentifierImpl clientIdentifier = MqttClientIdentifierImpl.of("test");
        final MqttUtf8StringImpl authMethod = MqttUtf8StringImpl.of("GS2-KRB5");
        final ByteBuffer authData = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        final MqttEnhancedAuth enhancedAuth = new MqttEnhancedAuth(authMethod, authData);
        final Mqtt5EnhancedAuthMechanism enhancedAuthProvider = new TestEnhancedAuthMechanism(authMethod);

        final MqttConnect connect = new MqttConnect(0, false, 0, DEFAULT, null, enhancedAuthProvider, null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        final MqttStatefulConnect connectWrapper = connect.createStateful(clientIdentifier, enhancedAuth);

        encode(expected, connectWrapper);
    }

    @Test
    @Disabled("authentication data will be validated in the builder, remove this test")
    void encode_authenticationDataTooLong_throwsException() {
        final MqttClientIdentifierImpl clientIdentifier = MqttClientIdentifierImpl.of("test");
        final MqttUtf8StringImpl authMethod = MqttUtf8StringImpl.of("GS2-KRB5");
        final ByteBuffer authData = ByteBuffer.wrap(new byte[65536]);
        final MqttEnhancedAuth enhancedAuth = new MqttEnhancedAuth(authMethod, authData);
        final Mqtt5EnhancedAuthMechanism enhancedAuthProvider = new TestEnhancedAuthMechanism(authMethod);

        final MqttConnect connect = new MqttConnect(0, false, 0, DEFAULT, null, enhancedAuthProvider, null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        final MqttStatefulConnect connectWrapper = connect.createStateful(clientIdentifier, enhancedAuth);

        encodeNok(connectWrapper, EncoderException.class, "binary data size exceeded for authentication data");
    }

    @Test
    @Disabled("transform to encode_maximumPacketSizeExceeded_omitUserPropertiesAndReasonString")
    void encode_maximumPacketSizeExceeded_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final MqttClientIdentifierImpl clientIdentifier = MqttClientIdentifierImpl.of(maxPacket.getClientId("a"));
        final MqttConnect connect =
                new MqttConnect(0, false, 0, DEFAULT, null, null, null, maxPacket.getMaxPossibleUserProperties());
        final MqttStatefulConnect connectWrapper = connect.createStateful(clientIdentifier, null);

        encodeNok(connectWrapper, EncoderException.class, "variable byte integer size exceeded for remaining length");
    }

    @Test
    @Disabled("transform to encode_propertyLengthExceeded_omitUserPropertiesAndReasonString")
    void encode_propertyLengthExceedsMax_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final MqttClientIdentifierImpl clientIdentifier = MqttClientIdentifierImpl.of(maxPacket.getClientId());
        final MqttConnect connect =
                new MqttConnect(0, false, 0, DEFAULT, null, null, null, maxPacket.getMaxPossibleUserProperties(2));
        final MqttStatefulConnect connectWrapper = connect.createStateful(clientIdentifier, null);

        encodeNok(connectWrapper, EncoderException.class, "variable byte integer size exceeded for property length");
    }

    private void encode(final @NotNull byte[] expected, final @NotNull MqttStatefulConnect connectWrapper) {
        encode(connectWrapper, expected);
    }

    private void encodeNok(
            final @NotNull MqttStatefulConnect connectWrapper,
            final @NotNull Class<? extends Exception> expectedException,
            final @NotNull String reason) {

        final Throwable exception = assertThrows(expectedException, () -> channel.writeOutbound(connectWrapper));
        assertTrue(exception.getMessage().contains(reason), () -> "found: " + exception.getMessage());
    }

    @SuppressWarnings("NullabilityAnnotations")
    private class MaximumPacketBuilder {

        private ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder;
        final @NotNull MqttUserPropertyImpl userProperty =
                new MqttUserPropertyImpl(MqttUtf8StringImpl.of("user"), MqttUtf8StringImpl.of("property"));
        char[] clientIdBytes;
        final int maxPropertyLength = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT - 1  // type, reserved
                - 4  // remaining length
                - 7  // protocol name + version
                - 1  // connect flags
                - 2  // keep alive
                - 4  // properties length
                - 2; // client id

        final int userPropertyBytes = 1 // identifier
                + 2  // key length
                + 4  // bytes to encode "user"
                + 2  // value length
                + 8; // bytes to encode "property"

        @NotNull MaximumPacketBuilder build() {
            final int ClientIdLength = maxPropertyLength % userPropertyBytes;

            clientIdBytes = new char[ClientIdLength];
            Arrays.fill(clientIdBytes, 'c');

            final int numberOfUserProperties = maxPropertyLength / userPropertyBytes;
            userPropertiesBuilder = ImmutableList.builder();
            for (int i = 0; i < numberOfUserProperties; i++) {
                userPropertiesBuilder.add(userProperty);
            }
            return this;
        }

        @NotNull String getClientId() {
            return getClientId("");
        }

        @NotNull String getClientId(final @NotNull String extraChars) {
            return new String(clientIdBytes) + extraChars;
        }

        @NotNull MqttUserPropertiesImpl getMaxPossibleUserProperties() {
            //return ImmutableList.of();
            return getMaxPossibleUserProperties(0);
        }

        @NotNull MqttUserPropertiesImpl getMaxPossibleUserProperties(final int withExtraUserProperties) {
            for (int i = 0; i < withExtraUserProperties; i++) {
                userPropertiesBuilder.add(userProperty);
            }
            return MqttUserPropertiesImpl.of(userPropertiesBuilder.build());
        }

        @NotNull MqttUserPropertiesImpl getUserProperties(final int totalCount) {
            final ImmutableList.Builder<MqttUserPropertyImpl> builder = ImmutableList.builder();
            for (int i = 0; i < totalCount; i++) {
                builder.add(userProperty);
            }
            return MqttUserPropertiesImpl.of(builder.build());
        }
    }

    private static class TestEnhancedAuthMechanism implements Mqtt5EnhancedAuthMechanism {

        private final @NotNull MqttUtf8String method;

        TestEnhancedAuthMechanism(@NotNull final MqttUtf8String method) {
            this.method = method;
        }

        @NotNull
        @Override
        public MqttUtf8String getMethod() {
            return method;
        }

        @Override
        public int getTimeout() {
            return 60;
        }

        @NotNull
        @Override
        public CompletableFuture<Void> onAuth(
                @NotNull final Mqtt5ClientConfig clientConfig,
                @NotNull final Mqtt5Connect connect,
                @NotNull final Mqtt5EnhancedAuthBuilder authBuilder) {
            throw new UnsupportedOperationException();
        }

        @NotNull
        @Override
        public CompletableFuture<Void> onReAuth(
                @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5AuthBuilder authBuilder) {
            throw new UnsupportedOperationException();
        }

        @NotNull
        @Override
        public CompletableFuture<Boolean> onServerReAuth(
                @NotNull final Mqtt5ClientConfig clientConfig,
                @NotNull final Mqtt5Auth auth,
                @NotNull final Mqtt5AuthBuilder authBuilder) {
            throw new UnsupportedOperationException();
        }

        @NotNull
        @Override
        public CompletableFuture<Boolean> onContinue(
                @NotNull final Mqtt5ClientConfig clientConfig,
                @NotNull final Mqtt5Auth auth,
                @NotNull final Mqtt5AuthBuilder authBuilder) {
            throw new UnsupportedOperationException();
        }

        @NotNull
        @Override
        public CompletableFuture<Boolean> onAuthSuccess(
                @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5ConnAck connAck) {
            throw new UnsupportedOperationException();
        }

        @NotNull
        @Override
        public CompletableFuture<Boolean> onReAuthSuccess(
                @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5Auth auth) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onAuthRejected(@NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5ConnAck connAck) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onReAuthRejected(
                @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5Disconnect disconnect) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onAuthError(@NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Throwable cause) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onReAuthError(@NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Throwable cause) {
            throw new UnsupportedOperationException();
        }
    }
}