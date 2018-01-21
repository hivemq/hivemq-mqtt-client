package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5Disconnect;
import org.mqttbee.api.mqtt5.message.Mqtt5PubRec;
import org.mqttbee.mqtt5.ChannelAttributes;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5Topic;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecInternal;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecReasonCode;

import static org.junit.Assert.*;
import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.MALFORMED_PACKET;
import static org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecReasonCode.SUCCESS;

/**
 * @author Silvio Giebl
 * @author David Katz
 */
class Mqtt5PubRecDecoderTest {

    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        channel = new EmbeddedChannel(new Mqtt5Decoder(new Mqtt5PubRecTestMessageDecoders()));
    }

    @AfterEach
    void tearDown() {
        channel.close();
    }

    @Test
    void decode_allProperties() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0101_0000,
                //   remaining length
                43,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code (success)
                0x00,
                //   properties
                39,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's',
                //     user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e',
                0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2',
        };

        final Mqtt5PubRec pubRec = decode(encoded);

        assertEquals(SUCCESS, pubRec.getReasonCode());
        assertTrue(pubRec.getReasonString().isPresent());
        assertEquals("success", pubRec.getReasonString().get().toString());

        final ImmutableList<Mqtt5UserProperty> userProperties = pubRec.getUserProperties();
        assertEquals(2, userProperties.size());
        final Mqtt5UTF8String test = Mqtt5UTF8String.from("test");
        final Mqtt5UTF8String value = Mqtt5UTF8String.from("value");
        final Mqtt5UTF8String value2 = Mqtt5UTF8String.from("value2");
        assertNotNull(test);
        assertNotNull(value);
        assertNotNull(value2);
        assertTrue(userProperties.contains(new Mqtt5UserProperty(test, value)));
        assertTrue(userProperties.contains(new Mqtt5UserProperty(test, value2)));
    }

    @Test
    void decode_simple() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0101_0000,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code (success)
                0x00
        };

        final Mqtt5PubRec pubRec = decode(encoded);
        assertEquals(SUCCESS, pubRec.getReasonCode());
    }

    @Test
    void decode_userProperty() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0101_0000,
                //   remaining length
                18,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                0x00,
                //   properties
                14,
                // user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e',
        };

        final Mqtt5PubRec pubRec = decode(encoded);
        assertEquals(SUCCESS, pubRec.getReasonCode());
    }

    @Test
    void decode_multipleUserProperties() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0101_0000,
                //   remaining length
                32,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                0x00,
                //   properties
                28,
                // user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e',
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e'
        };

        final Mqtt5PubRec pubRec = decode(encoded);
        assertEquals(SUCCESS, pubRec.getReasonCode());
    }

    @ParameterizedTest
    @ValueSource(ints = {0x00, 0x10, 0x80, 0x83, 0x87, 0x90, 0x91, 0x97, 0x99})
    void decode_allReasonCodes(final int reasonCode) {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0101_0000,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                0x00
        };

        encoded[4] = (byte) reasonCode;
        final Mqtt5PubRec pubRec = decode(encoded);
        assertEquals(Mqtt5PubRecReasonCode.fromCode(reasonCode), pubRec.getReasonCode());
    }

    @Test
    void decode_noReasonCodeDefaultsToSuccess() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0101_0000,
                //   remaining length
                2,
                // variable header
                //   packet identifier
                0, 5
        };

        final Mqtt5PubRec pubRec = decode(encoded);
        assertEquals(SUCCESS, pubRec.getReasonCode());
    }

    @Test
    void decode_invalidFlags_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0101_1010,
                //   remaining length
                2,
                // variable header
                //   packet identifier
                0, 5
        };

        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_packetLengthLargerThanMaxPacketSize_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0101_0000,
                //   remaining length
                32,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                0x00,
                //   properties
                28,
                // user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e',
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e'
        };
        channel.attr(ChannelAttributes.INCOMING_MAXIMUM_PACKET_SIZE).set((long) (encoded.length - 1));
        decodeNok(encoded, Mqtt5DisconnectReasonCode.PACKET_TOO_LARGE);
    }

    @Test
    void decode_packetTooSmall_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0101_0000,
                //   remaining length
                0
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_propertyLengthLessThanZero_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0101_0000,
                //   remaining length
                4,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                0x00,
                //   properties
                -1
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_propertyLengthTooLarge_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0101_0000,
                //   remaining length
                4,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                0x00,
                //   properties
                10
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_negativePropertyIdentifier_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0101_0000,
                //   remaining length
                11,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                0x00,
                //   properties
                7,
                // negative property id
                (byte) -3, 0, 4, 't', 'e', 's', 't'
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_invalidPropertyIdentifier_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0101_0000,
                //   remaining length
                11,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                0x00,
                //   properties
                7,
                // invalid property id
                (byte) 0x03, 0, 4, 't', 'e', 's', 't'
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_reasonString() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0101_0000,
                //   remaining length
                14,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code (success)
                0x00,
                //   properties
                10,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's'
        };

        final Mqtt5PubRec pubRec = decode(encoded);
        assertTrue(pubRec.getReasonString().isPresent());
        assertEquals("success", pubRec.getReasonString().get().toString());
    }

    @Test
    void decode_nullReasonString_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0101_0000,
                //   remaining length
                8,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code (success)
                0x00,
                //   properties
                4,
                //     reason string
                0x1F, 0, 1, '\u0000'
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_invalidReasonCode_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0101_0000,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code (success)
                (byte) 0xFF
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void decode_nullUserProperty_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0101_0000,
                //   remaining length
                11,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                0x00,
                //   properties
                7,
                // user properties
                0x26, 0, 1, '\u0000', 0, 1, 'x'
        };
        decodeNok(encoded, Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @NotNull
    private Mqtt5PubRec decode(final byte[] encoded) {
        final Mqtt5PubRecInternal pubRecInternal = decodeInternal(encoded);
        assertNotNull(pubRecInternal);
        return pubRecInternal.getPubRec();
    }

    private void decodeNok(final byte[] encoded, final Mqtt5DisconnectReasonCode reasonCode) {
        // try to decode the encoded byte array.
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt5PubRecInternal pubRecInternal = channel.readInbound();

        // Should not work! Mqtt5PubRecInternal returned should be null
        assertNull(pubRecInternal);

        // check that the reason code is correct and reset channel
        final Mqtt5Disconnect disconnect = channel.readOutbound();
        assertNotNull(disconnect);
        assertEquals(reasonCode, disconnect.getReasonCode());
        createChannel();
    }

    private Mqtt5PubRecInternal decodeInternal(final byte[] encoded) {
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        return channel.readInbound();
    }

    private static class Mqtt5PubRecTestMessageDecoders implements Mqtt5MessageDecoders {
        @Nullable
        @Override
        public Mqtt5MessageDecoder get(final int code) {
            if (code == Mqtt5MessageType.PUBREC.getCode()) {
                return new Mqtt5PubRecDecoder();
            }
            return null;
        }
    }

    private void createChannel() {
        channel = new EmbeddedChannel(new Mqtt5Decoder(new Mqtt5PubRecTestMessageDecoders()));
        channel.attr(ChannelAttributes.INCOMING_TOPIC_ALIAS_MAPPING).set(new Mqtt5Topic[3]);
    }

}