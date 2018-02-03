package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5Disconnect;
import org.mqttbee.api.mqtt5.message.Mqtt5PubRel;
import org.mqttbee.mqtt5.ChannelAttributes;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5TopicImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertyImpl;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecInternal;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelImpl;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelInternal;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelReasonCode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.MALFORMED_PACKET;
import static org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelReasonCode.SUCCESS;

/**
 * @author Silvio Giebl
 * @author David Katz
 */
class Mqtt5PubRelDecoderTest {

    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        createChannel();
    }

    @AfterEach
    void tearDown() {
        channel.close();
    }

    @Test
    void decode_allParameters() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0110_0010,
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
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2',
        };

        final Mqtt5PubRelImpl pubRel = decode(encoded);

        assertEquals(SUCCESS, pubRel.getReasonCode());
        assertTrue(pubRel.getReasonString().isPresent());
        assertEquals("success", pubRel.getReasonString().get().toString());

        final ImmutableList<Mqtt5UserPropertyImpl> userProperties = pubRel.getUserProperties().asList();
        assertEquals(2, userProperties.size());
        assertEquals("test", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());
        assertEquals("test", userProperties.get(1).getName().toString());
        assertEquals("value2", userProperties.get(1).getValue().toString());
    }

    @Test
    void decode_reasonCodeAndPropertiesOmittedOnSuccess() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                2,
                // variable header
                //   packet identifier
                0, 5
        };

        final Mqtt5PubRel pubRel = decode(encoded);
        assertEquals(SUCCESS, pubRel.getReasonCode());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5PubRelReasonCode.class, mode = EnumSource.Mode.EXCLUDE, names = {"SUCCESS"})
    void decode_allReasonCodes(final Mqtt5PubRelReasonCode reasonCode) {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                4,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code placeholder
                0x00,
                //   properties length
                0

        };
        encoded[4] = (byte) reasonCode.getCode();
        final Mqtt5PubRel pubRel = decode(encoded);
        assertEquals(reasonCode, pubRel.getReasonCode());
    }

    @Test
    void decode_multipleUserProperties() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                33,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                0x00,
                //   properties
                29,
                // user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2'
        };

        final Mqtt5PubRelImpl pubRel = decode(encoded);
        final ImmutableList<Mqtt5UserPropertyImpl> userProperties = pubRel.getUserProperties().asList();
        assertEquals(2, userProperties.size());
        assertEquals("test", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());
        assertEquals("test", userProperties.get(1).getName().toString());
        assertEquals("value2", userProperties.get(1).getValue().toString());
    }

    @Test
    void decode_invalidFlags_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0110_1010,
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
                0b0110_0010,
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
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e'
        };
        channel.attr(ChannelAttributes.INCOMING_MAXIMUM_PACKET_SIZE).set(encoded.length - 1);
        decodeNok(encoded, Mqtt5DisconnectReasonCode.PACKET_TOO_LARGE);
    }

    @Test
    void decode_packetTooSmall_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0110_0010,
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
                0b0110_0010,
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
                0b0110_0010,
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
                0b0110_0010,
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
                0b0110_0010,
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
    void decode_invalidReasonCode_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0110_0010,
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
                0b0110_0010,
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

    @Test
    void decode_nullReasonString_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0110_0010,
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

    @NotNull
    private Mqtt5PubRelInternal decodeInternal(final byte[] encode) {
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encode);
        channel.writeInbound(byteBuf);
        final Mqtt5PubRelInternal pubRelInternal = channel.readInbound();
        assertNotNull(pubRelInternal);
        return pubRelInternal;
    }

    @NotNull
    private Mqtt5PubRelImpl decode(final byte[] encode) {
        final Mqtt5PubRelInternal pubRelInternal = decodeInternal(encode);
        assertNotNull(pubRelInternal);
        return pubRelInternal.getPubRel();
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

    private void createChannel() {
        channel = new EmbeddedChannel(new Mqtt5Decoder(new Mqtt5PubRelTestMessageDecoders()));
        channel.attr(ChannelAttributes.INCOMING_TOPIC_ALIAS_MAPPING).set(new Mqtt5TopicImpl[3]);
    }

    private static class Mqtt5PubRelTestMessageDecoders implements Mqtt5MessageDecoders {
        @Nullable
        @Override
        public Mqtt5MessageDecoder get(final int code) {
            if (code == Mqtt5MessageType.PUBREL.getCode()) {
                return new Mqtt5PubRelDecoder();
            }
            return null;
        }
    }

}