package org.mqttbee.mqtt5.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Before;
import org.junit.Test;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5Disconnect;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertyImpl;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.message.unsuback.Mqtt5UnsubAckImpl;
import org.mqttbee.mqtt5.message.unsuback.Mqtt5UnsubAckInternal;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mqttbee.mqtt5.message.unsuback.Mqtt5UnsubAckReasonCode.*;

/**
 * @author David Katz
 */
public class Mqtt5UnsubAckDecoderTest {

    private EmbeddedChannel channel;

    @Before
    public void setUp() {
        channel = new EmbeddedChannel(new Mqtt5Decoder(new Mqtt5UnsubAckTestMessageDecoders()));
    }

    @Test
    public void decode_simple() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                27,
                // variable header
                //   packet identifier MSB, LSB
                1, 2,
                //   properties length in bytes
                23,
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                //     user property
                0x26, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e',
                // payload reason code: success
                0x00
        };

        final Mqtt5UnsubAckInternal unsubackInternal = decodeMqtt5UnsubAck(encoded);
        //0x0102 = 258
        assertEquals(258, unsubackInternal.getPacketIdentifier());
        final Mqtt5UnsubAckImpl unsuback = unsubackInternal.getUnsubAck();

        assertNotNull(unsuback);

        final Optional<Mqtt5UTF8String> reasonString = unsuback.getReasonString();
        assertTrue(reasonString.isPresent());
        assertEquals("reason", reasonString.get().toString());

        final List<Mqtt5UserPropertyImpl> userProperties = unsuback.getUserProperties().asList();
        assertEquals(1, userProperties.size());
        assertEquals("name", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());

        assertEquals(1, unsuback.getReasonCodes().size());
        assertEquals(SUCCESS, unsuback.getReasonCodes().get(0));
    }

    @Test
    public void decode_reasonStringMissing() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                18,
                // variable header
                //   packet identifier MSB, LSB
                0xF, 0x5,
                //   properties length in bytes
                14,
                //     user property
                0x26, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e',
                // payload reason code: success
                0x00
        };

        final Mqtt5UnsubAckInternal unsubackInternal = decodeMqtt5UnsubAck(encoded);
        // 0x0F05 = 3845
        assertEquals(3845, unsubackInternal.getPacketIdentifier());
        final Mqtt5UnsubAckImpl unsuback = unsubackInternal.getUnsubAck();

        assertNotNull(unsuback);

        assertFalse(unsuback.getReasonString().isPresent());

        final List<Mqtt5UserPropertyImpl> userProperties = unsuback.getUserProperties().asList();
        assertEquals(1, userProperties.size());
        assertEquals("name", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());

        assertEquals(1, unsuback.getReasonCodes().size());
        assertEquals(SUCCESS, unsuback.getReasonCodes().get(0));
    }

    @Test
    public void decode_userPropertiesMultiple() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                55,
                // variable header
                //   packet identifier MSB, LSB
                0, 2,
                //   properties length in bytes
                51,
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                //     user properties
                0x26, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e',
                // payload reason code: success
                0x00
        };

        final Mqtt5UnsubAckInternal unsubackInternal = decodeMqtt5UnsubAck(encoded);
        assertEquals(2, unsubackInternal.getPacketIdentifier());
        final Mqtt5UnsubAckImpl unsuback = unsubackInternal.getUnsubAck();

        assertNotNull(unsuback);

        final Optional<Mqtt5UTF8String> reasonString = unsuback.getReasonString();
        assertTrue(reasonString.isPresent());
        assertEquals("reason", reasonString.get().toString());

        final List<Mqtt5UserPropertyImpl> userProperties = unsuback.getUserProperties().asList();
        assertEquals(3, userProperties.size());
        assertEquals("name", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());
        assertEquals("name", userProperties.get(1).getName().toString());
        assertEquals("value", userProperties.get(1).getValue().toString());
        assertEquals("name", userProperties.get(2).getName().toString());
        assertEquals("value", userProperties.get(2).getValue().toString());

        assertEquals(1, unsuback.getReasonCodes().size());
        assertEquals(SUCCESS, unsuback.getReasonCodes().get(0));
    }

    @Test
    public void decode_reasonCodesMultiple() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                33,
                // variable header
                //   packet identifier MSB, LSB
                8, 8,
                //   properties length in bytes
                23,
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                //     user property
                0x26, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e',
                // payload reason codes, one of each possible, in order of spec
                0x00, 0x11, (byte) 0x80, (byte) 0x83, (byte) 0x87, (byte) 0x8F, (byte) 0x91
        };

        final Mqtt5UnsubAckInternal unsubackInternal = decodeMqtt5UnsubAck(encoded);
        // 0x0808 = 2056
        assertEquals(2056, unsubackInternal.getPacketIdentifier());
        final Mqtt5UnsubAckImpl unsuback = unsubackInternal.getUnsubAck();

        assertNotNull(unsuback);

        final Optional<Mqtt5UTF8String> reasonString = unsuback.getReasonString();
        assertTrue(reasonString.isPresent());
        assertEquals("reason", reasonString.get().toString());

        final List<Mqtt5UserPropertyImpl> userProperties = unsuback.getUserProperties().asList();
        assertEquals(1, userProperties.size());
        assertEquals("name", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());

        assertEquals(7, unsuback.getReasonCodes().size());
        assertEquals(SUCCESS, unsuback.getReasonCodes().get(0));
        assertEquals(NO_SUBSCRIPTIONS_EXISTED, unsuback.getReasonCodes().get(1));
        assertEquals(UNSPECIFIED_ERROR, unsuback.getReasonCodes().get(2));
        assertEquals(IMPLEMENTATION_SPECIFIC_ERROR, unsuback.getReasonCodes().get(3));
        assertEquals(NOT_AUTHORIZED, unsuback.getReasonCodes().get(4));
        assertEquals(TOPIC_FILTER_INVALID, unsuback.getReasonCodes().get(5));
        assertEquals(PACKET_IDENTIFIER_IN_USE, unsuback.getReasonCodes().get(6));
    }

    @Test
    public void decode_reasonCodesInvalid_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                27,
                // variable header
                //   packet identifier MSB, LSB
                0, 1,
                //   properties length in bytes
                23,
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                //     user property
                0x26, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e',
                // payload reason code
                (byte) 0x00
        };

        assertNotNull(decodeMqtt5UnsubAck(encoded));
        assertEquals(0x00, encoded[28]);

        encoded[28] = (byte) 0xFF; // invalid reason code
        decodeMqtt5UnsubAckNok(encoded, Mqtt5DisconnectReasonCode.MALFORMED_PACKET);

        encoded[28] = 0x00; // set reason code ok again
        assertNotNull(decodeMqtt5UnsubAck(encoded));
    }

    @Test
    public void decode_propertiesLengthTooLong_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                27,
                // variable header
                //   packet identifier MSB, LSB
                0, 1,
                //   properties length in bytes
                23,
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                //     user property
                0x26, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e',
                // payload reason code: SUCCESS
                (byte) 0x00
        };

        assertNotNull(decodeMqtt5UnsubAck(encoded));
        assertEquals(23, encoded[4]);

        encoded[4] = 125; // property length
        decodeMqtt5UnsubAckNok(encoded, Mqtt5DisconnectReasonCode.MALFORMED_PACKET);

        encoded[4] = 23; // set length ok again
        assertNotNull(decodeMqtt5UnsubAck(encoded));
    }

    @Test
    public void decode_propertiesLengthTooShort_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                27,
                // variable header
                //   packet identifier MSB, LSB
                0, 1,
                //   properties length in bytes
                23,
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                //     user property
                0x26, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e',
                // payload reason code: SUCCESS
                (byte) 0x00
        };

        assertNotNull(decodeMqtt5UnsubAck(encoded));
        assertEquals(23, encoded[4]);

        encoded[4] = 10; // set length too short
        decodeMqtt5UnsubAckNok(encoded, Mqtt5DisconnectReasonCode.MALFORMED_PACKET);

        encoded[4] = 23; // set length ok again
        assertNotNull(decodeMqtt5UnsubAck(encoded));
    }

    @Test
    public void decode_reasonStringLengthTooLong_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type _ flags (reserved)
                (byte) 0b1011_0000,
                //   remaining length
                27,
                // variable header
                //   packet identifier MSB, LSB
                0, 1,
                //   properties length in bytes
                23,
                //     reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n',
                //     user property
                0x26, 0, 4, 'n', 'a', 'm', 'e', 0, 5, 'v', 'a', 'l', 'u', 'e',
                // payload reason code: SUCCESS
                (byte) 0x00
        };

        assertNotNull(decodeMqtt5UnsubAck(encoded));
        assertEquals(6, encoded[7]);

        encoded[7] = 7; // set length too long
        decodeMqtt5UnsubAckNok(encoded, Mqtt5DisconnectReasonCode.MALFORMED_PACKET);

        encoded[7] = 6; // set length ok again
        assertNotNull(decodeMqtt5UnsubAck(encoded));
    }

    @NotNull
    private Mqtt5UnsubAckInternal decodeMqtt5UnsubAck(final byte[] encoded) {
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);

        final Mqtt5UnsubAckInternal unsubackInternal = channel.readInbound();
        assertNotNull(unsubackInternal);

        return unsubackInternal;
    }

    private void decodeMqtt5UnsubAckNok(final byte[] encoded, final Mqtt5DisconnectReasonCode reasonCode) {
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);

        final Mqtt5UnsubAckInternal unsubackInternal = channel.readInbound();
        assertNull(unsubackInternal);

        final Mqtt5Disconnect disconnect = channel.readOutbound();
        assertNotNull(disconnect);
        assertEquals(reasonCode, disconnect.getReasonCode());

        channel = new EmbeddedChannel(new Mqtt5Decoder(new Mqtt5UnsubAckTestMessageDecoders()));
    }

    private static class Mqtt5UnsubAckTestMessageDecoders implements Mqtt5MessageDecoders {
        @Nullable
        @Override
        public Mqtt5MessageDecoder get(final int code) {
            if (Mqtt5MessageType.UNSUBACK.getCode() == code) {
                return new Mqtt5UnsubAckDecoder();
            }
            return null;
        }
    }
}
