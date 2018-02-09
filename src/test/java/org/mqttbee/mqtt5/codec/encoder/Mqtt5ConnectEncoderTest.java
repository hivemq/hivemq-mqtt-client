package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mqttbee.api.mqtt5.message.Mqtt5QoS;
import org.mqttbee.api.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.*;
import org.mqttbee.mqtt5.message.auth.Mqtt5ExtendedAuthImpl;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishImpl;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl.RestrictionsImpl.DEFAULT;

/**
 * @author Silvio Giebl
 * @author David Katz
 */
class Mqtt5ConnectEncoderTest extends AbstractMqtt5EncoderTest {

    private static final int VARIABLE_BYTE_INTEGER_VALUE_BITS = 7;
    private static final int VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE =
            (1 << (VARIABLE_BYTE_INTEGER_VALUE_BITS * 4)) - 1;

    Mqtt5ConnectEncoderTest() {
        super(false);
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
                //     request response information
                0x19, 1,
                //     request problem information
                0x17, 0,
                //     auth method
                0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5',
                //     auth data
                0x16, 0, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                //     receive maximum
                0x21, 0, 5,
                //     topic alias maximum
                0x22, 0, 10,
                //     maximum packet size
                0x27, 0, 0, 0, 100,
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

        final Mqtt5ClientIdentifierImpl clientIdentifier = requireNonNull(Mqtt5ClientIdentifierImpl.from("test"));

        final Mqtt5ClientIdentifierImpl username = requireNonNull(Mqtt5ClientIdentifierImpl.from("username"));
        final byte[] password = {1, 5, 6, 3};
        final Mqtt5ConnectImpl.SimpleAuthImpl simpleAuth = new Mqtt5ConnectImpl.SimpleAuthImpl(username, password);

        final Mqtt5UTF8StringImpl authMethod = requireNonNull(Mqtt5UTF8StringImpl.from("GS2-KRB5"));
        final byte[] authData = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        final Mqtt5ExtendedAuthImpl extendedAuth = new Mqtt5ExtendedAuthImpl(authMethod, authData);

        final Mqtt5UTF8StringImpl test = requireNonNull(Mqtt5UTF8StringImpl.from("test"));
        final Mqtt5UTF8StringImpl test2 = requireNonNull(Mqtt5UTF8StringImpl.from("test2"));
        final Mqtt5UTF8StringImpl value = requireNonNull(Mqtt5UTF8StringImpl.from("value"));
        final Mqtt5UTF8StringImpl value2 = requireNonNull(Mqtt5UTF8StringImpl.from("value2"));
        final Mqtt5UserPropertyImpl userProperty1 = new Mqtt5UserPropertyImpl(test, value);
        final Mqtt5UserPropertyImpl userProperty2 = new Mqtt5UserPropertyImpl(test, value2);
        final Mqtt5UserPropertyImpl userProperty3 = new Mqtt5UserPropertyImpl(test2, value);
        final Mqtt5UserPropertiesImpl userProperties =
                Mqtt5UserPropertiesImpl.of(ImmutableList.of(userProperty1, userProperty2, userProperty3));

        final Mqtt5TopicImpl willTopic = requireNonNull(Mqtt5TopicImpl.from("topic"));
        final byte[] willPayload = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        final Mqtt5QoS willQoS = Mqtt5QoS.AT_LEAST_ONCE;
        final Mqtt5UTF8StringImpl willContentType = requireNonNull(Mqtt5UTF8StringImpl.from("text"));
        final Mqtt5TopicImpl willResponseTopic = requireNonNull(Mqtt5TopicImpl.from("response"));
        final byte[] willCorrelationData = {5, 4, 3, 2, 1};
        final Mqtt5WillPublishImpl willPublish =
                new Mqtt5WillPublishImpl(willTopic, willPayload, willQoS, true, 10, Mqtt5PayloadFormatIndicator.UTF_8,
                        willContentType, willResponseTopic, willCorrelationData, userProperties, 5,
                        Mqtt5PublishEncoder.PROVIDER);

        final Mqtt5ConnectImpl.RestrictionsImpl restrictions = new Mqtt5ConnectImpl.RestrictionsImpl(5, 10, 100);

        final Mqtt5ConnectImpl connect =
                new Mqtt5ConnectImpl(clientIdentifier, 10, true, 10, true, false, restrictions, simpleAuth,
                        extendedAuth, willPublish, userProperties, Mqtt5ConnectEncoder.PROVIDER);

        encode(expected, connect);
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

        final Mqtt5ClientIdentifierImpl clientIdentifier = requireNonNull(Mqtt5ClientIdentifierImpl.from("test"));
        final Mqtt5ConnectImpl connect =
                new Mqtt5ConnectImpl(clientIdentifier, 0, false, 0, false, true, DEFAULT, null, null, null,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5ConnectEncoder.PROVIDER);

        encode(expected, connect);
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

        final Mqtt5ClientIdentifierImpl clientIdentifier = requireNonNull(Mqtt5ClientIdentifierImpl.from("test"));
        final Mqtt5UTF8StringImpl username = requireNonNull(Mqtt5UTF8StringImpl.from("username"));
        final Mqtt5ConnectImpl.SimpleAuthImpl simpleAuth = new Mqtt5ConnectImpl.SimpleAuthImpl(username, null);

        final Mqtt5ConnectImpl connect =
                new Mqtt5ConnectImpl(clientIdentifier, 0, false, 0, false, true, DEFAULT, simpleAuth, null, null,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5ConnectEncoder.PROVIDER);

        encode(expected, connect);
    }


    @Test
    void encode_usernameTooLong() {
        final Mqtt5ClientIdentifierImpl clientIdentifier = requireNonNull(Mqtt5ClientIdentifierImpl.from("test"));
        final char[] chars = new char[65536];
        Arrays.fill(chars, 'a');
        final Mqtt5UTF8StringImpl username = Mqtt5UTF8StringImpl.from(new String(chars));
        final Mqtt5ConnectImpl.SimpleAuthImpl simpleAuth = new Mqtt5ConnectImpl.SimpleAuthImpl(username, null);

        final Mqtt5ConnectImpl connect =
                new Mqtt5ConnectImpl(clientIdentifier, 0, false, 0, false, true, DEFAULT, simpleAuth, null, null,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5ConnectEncoder.PROVIDER);

        encodeNok(connect, EncoderException.class, "binary data size exceeded for UTF-8 encoded String");
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

        final Mqtt5ClientIdentifierImpl clientIdentifier = requireNonNull(Mqtt5ClientIdentifierImpl.from("test"));
        final byte[] password = {1, 5, 6, 3};
        final Mqtt5ConnectImpl.SimpleAuthImpl simpleAuth = new Mqtt5ConnectImpl.SimpleAuthImpl(null, password);

        final Mqtt5ConnectImpl connect =
                new Mqtt5ConnectImpl(clientIdentifier, 0, false, 0, false, true, DEFAULT, simpleAuth, null, null,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5ConnectEncoder.PROVIDER);

        encode(expected, connect);
    }

    @Test
    @Disabled("password will be validated in the builder, remove this test")
    void encode_passwordTooLong() {
        final Mqtt5ClientIdentifierImpl clientIdentifier = requireNonNull(Mqtt5ClientIdentifierImpl.from("test"));
        final byte[] password = new byte[65536];
        final Mqtt5ConnectImpl.SimpleAuthImpl simpleAuth = new Mqtt5ConnectImpl.SimpleAuthImpl(null, password);

        final Mqtt5ConnectImpl connect =
                new Mqtt5ConnectImpl(clientIdentifier, 0, false, 0, false, true, DEFAULT, simpleAuth, null, null,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5ConnectEncoder.PROVIDER);

        encodeNok(connect, EncoderException.class, "binary data size exceeded for password");
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

        final Mqtt5ClientIdentifierImpl clientIdentifier = requireNonNull(Mqtt5ClientIdentifierImpl.from(""));

        final Mqtt5ConnectImpl connect =
                new Mqtt5ConnectImpl(clientIdentifier, 0, false, 0, false, true, DEFAULT, null, null, null,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5ConnectEncoder.PROVIDER);

        encode(expected, connect);
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

        final Mqtt5TopicImpl willTopic = requireNonNull(Mqtt5TopicImpl.from("topic"));
        final byte[] willPayload = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        final Mqtt5ClientIdentifierImpl clientIdentifier = requireNonNull(Mqtt5ClientIdentifierImpl.from("test"));
        final Mqtt5WillPublishImpl willPublish =
                new Mqtt5WillPublishImpl(willTopic, willPayload, Mqtt5QoS.AT_MOST_ONCE, false,
                        Mqtt5WillPublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, null, null, null, null,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES, 0, Mqtt5PublishEncoder.PROVIDER);
        final Mqtt5ConnectImpl connect =
                new Mqtt5ConnectImpl(clientIdentifier, 0, false, 0, false, true, DEFAULT, null, null, willPublish,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5ConnectEncoder.PROVIDER);

        encode(expected, connect);
    }

    @Test
    @Disabled("will payload will be validated in the builder, remove this test")
    void encode_willPayloadTooLong() {
        final Mqtt5TopicImpl willTopic = requireNonNull(Mqtt5TopicImpl.from("topic"));
        final byte[] willPayload = new byte[65536];
        final Mqtt5ClientIdentifierImpl clientIdentifier = requireNonNull(Mqtt5ClientIdentifierImpl.from("test"));
        final Mqtt5WillPublishImpl willPublish =
                new Mqtt5WillPublishImpl(willTopic, willPayload, Mqtt5QoS.AT_MOST_ONCE, false,
                        Mqtt5WillPublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, null, null, null, null,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES, 0, Mqtt5PublishEncoder.PROVIDER);
        final Mqtt5ConnectImpl connect =
                new Mqtt5ConnectImpl(clientIdentifier, 0, false, 0, false, true, DEFAULT, null, null, willPublish,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5ConnectEncoder.PROVIDER);

        encodeNok(connect, EncoderException.class, "binary data size exceeded for will payload");
    }

    @Test
    @Disabled("will correlation data will be validated in the builder, remove this test")
    void encode_willCorrelationDataTooLong() {
        final Mqtt5TopicImpl willTopic = requireNonNull(Mqtt5TopicImpl.from("topic"));
        final byte[] correlationData = new byte[65536];
        final Mqtt5ClientIdentifierImpl clientIdentifier = requireNonNull(Mqtt5ClientIdentifierImpl.from("test"));
        final Mqtt5WillPublishImpl willPublish = new Mqtt5WillPublishImpl(willTopic, null, Mqtt5QoS.AT_MOST_ONCE, false,
                Mqtt5WillPublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, null, null, null, correlationData,
                Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES, 0, Mqtt5PublishEncoder.PROVIDER);
        final Mqtt5ConnectImpl connect =
                new Mqtt5ConnectImpl(clientIdentifier, 0, false, 0, false, true, DEFAULT, null, null, willPublish,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5ConnectEncoder.PROVIDER);

        encodeNok(connect, EncoderException.class, "binary data size exceeded for will correlation data");
    }

    @Test
    void encode_willPropertyLengthExceedsMax_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder();
        final Mqtt5UserPropertiesImpl tooManyUserProperties = maxPacket
                .getUserProperties((VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE / maxPacket.userPropertyBytes) + 1);

        final Mqtt5TopicImpl willTopic = requireNonNull(Mqtt5TopicImpl.from("topic"));
        final Mqtt5ClientIdentifierImpl clientIdentifier = requireNonNull(Mqtt5ClientIdentifierImpl.from("test"));
        final Mqtt5WillPublishImpl willPublish = new Mqtt5WillPublishImpl(willTopic, null, Mqtt5QoS.AT_MOST_ONCE, false,
                Mqtt5WillPublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, null, null, null, null, tooManyUserProperties, 0,
                Mqtt5PublishEncoder.PROVIDER);
        final Mqtt5ConnectImpl connect =
                new Mqtt5ConnectImpl(clientIdentifier, 0, false, 0, false, true, DEFAULT, null, null, willPublish,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5ConnectEncoder.PROVIDER);

        encodeNok(connect, EncoderException.class, "variable byte integer size exceeded for will properties length");
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

        final Mqtt5ClientIdentifierImpl clientIdentifier = requireNonNull(Mqtt5ClientIdentifierImpl.from("test"));
        final Mqtt5UTF8StringImpl authMethod = requireNonNull(Mqtt5UTF8StringImpl.from("GS2-KRB5"));
        final byte[] authData = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        final Mqtt5ExtendedAuthImpl extendedAuth = new Mqtt5ExtendedAuthImpl(authMethod, authData);

        final Mqtt5ConnectImpl connect =
                new Mqtt5ConnectImpl(clientIdentifier, 0, false, 0, false, true, DEFAULT, null, extendedAuth, null,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5ConnectEncoder.PROVIDER);

        encode(expected, connect);
    }

    @Test
    @Disabled("authentication data will be validated in the builder, remove this test")
    void encode_authenticationDataTooLong_throwsException() {
        final Mqtt5ClientIdentifierImpl clientIdentifier = requireNonNull(Mqtt5ClientIdentifierImpl.from("test"));
        final Mqtt5UTF8StringImpl authMethod = requireNonNull(Mqtt5UTF8StringImpl.from("GS2-KRB5"));
        final byte[] authData = new byte[65536];
        final Mqtt5ExtendedAuthImpl extendedAuth = new Mqtt5ExtendedAuthImpl(authMethod, authData);

        final Mqtt5ConnectImpl connect =
                new Mqtt5ConnectImpl(clientIdentifier, 0, false, 0, false, true, DEFAULT, null, extendedAuth, null,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5ConnectEncoder.PROVIDER);

        encodeNok(connect, EncoderException.class, "binary data size exceeded for authentication data");
    }

    @Test
    @Disabled("transform to encode_maximumPacketSizeExceeded_omitUserPropertiesAndReasonString")
    void encode_maximumPacketSizeExceeded_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final Mqtt5ClientIdentifierImpl clientIdentifier =
                requireNonNull(Mqtt5ClientIdentifierImpl.from(maxPacket.getClientId("a")));
        final Mqtt5ConnectImpl connect =
                new Mqtt5ConnectImpl(clientIdentifier, 0, false, 0, false, true, DEFAULT, null, null, null,
                        maxPacket.getMaxPossibleUserProperties(), Mqtt5ConnectEncoder.PROVIDER);

        encodeNok(connect, EncoderException.class, "variable byte integer size exceeded for remaining length");

    }

    @Test
    @Disabled("transform to encode_propertyLengthExceeded_omitUserPropertiesAndReasonString")
    void encode_propertyLengthExceedsMax_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final Mqtt5ClientIdentifierImpl clientIdentifier =
                requireNonNull(Mqtt5ClientIdentifierImpl.from(maxPacket.getClientId()));
        final Mqtt5ConnectImpl connect =
                new Mqtt5ConnectImpl(clientIdentifier, 0, false, 0, false, true, DEFAULT, null, null, null,
                        maxPacket.getMaxPossibleUserProperties(2), Mqtt5ConnectEncoder.PROVIDER);

        encodeNok(connect, EncoderException.class, "variable byte integer size exceeded for property length");
    }

    private void encode(final byte[] expected, final Mqtt5ConnectImpl connect) {
        channel.writeOutbound(connect);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();

        assertArrayEquals(expected, actual);
    }

    private void encodeNok(
            final Mqtt5ConnectImpl connect, final Class<? extends Exception> expectedException, final String reason) {
        final Throwable exception = assertThrows(expectedException, () -> channel.writeOutbound(connect));
        assertTrue(exception.getMessage().contains(reason), () -> "found: " + exception.getMessage());
    }

    private class MaximumPacketBuilder {

        private ImmutableList.Builder<Mqtt5UserPropertyImpl> userPropertiesBuilder;
        final Mqtt5UserPropertyImpl userProperty =
                new Mqtt5UserPropertyImpl(requireNonNull(Mqtt5UTF8StringImpl.from("user")),
                        requireNonNull(Mqtt5UTF8StringImpl.from("property")));
        char[] clientIdBytes;
        final int maxPropertyLength = Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT - 1  // type, reserved
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

        MaximumPacketBuilder build() {
            final int ClientIdLength = maxPropertyLength % userPropertyBytes;

            clientIdBytes = new char[ClientIdLength];
            Arrays.fill(clientIdBytes, 'c');

            final int numberOfUserProperties = maxPropertyLength / userPropertyBytes;
            userPropertiesBuilder = new ImmutableList.Builder<>();
            for (int i = 0; i < numberOfUserProperties; i++) {
                userPropertiesBuilder.add(userProperty);
            }
            return this;
        }

        String getClientId() {
            return getClientId("");
        }

        String getClientId(final String extraChars) {
            return new String(clientIdBytes) + extraChars;
        }

        Mqtt5UserPropertiesImpl getMaxPossibleUserProperties() {
            //return ImmutableList.of();
            return getMaxPossibleUserProperties(0);
        }

        Mqtt5UserPropertiesImpl getMaxPossibleUserProperties(final int withExtraUserProperties) {
            for (int i = 0; i < withExtraUserProperties; i++) {
                userPropertiesBuilder.add(userProperty);
            }
            return Mqtt5UserPropertiesImpl.of(userPropertiesBuilder.build());
        }

        Mqtt5UserPropertiesImpl getUserProperties(final int totalCount) {
            final ImmutableList.Builder<Mqtt5UserPropertyImpl> builder = new ImmutableList.Builder<>();
            for (int i = 0; i < totalCount; i++) {
                builder.add(userProperty);
            }
            return Mqtt5UserPropertiesImpl.of(builder.build());
        }
    }
}