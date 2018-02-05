package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5Disconnect;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertyImpl;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.message.suback.Mqtt5SubAckImpl;
import org.mqttbee.mqtt5.message.suback.Mqtt5SubAckReasonCode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.MALFORMED_PACKET;

/**
 * @author Silvio Giebl
 * @author David Katz
 */
class Mqtt5SubAckDecoderTest extends AbstractMqtt5DecoderTest {

    Mqtt5SubAckDecoderTest() {
        super(code -> {
            if (code == Mqtt5MessageType.SUBACK.getCode()) {
                return new Mqtt5SubAckDecoder();
            }
            return null;
        });
    }

    @Test
    void encode_simple() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b1001_0000,
                //   remaining length
                28,
                // variable header
                //   packet identifier
                0, 3,
                //   properties
                24,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's',
                //     user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e',
                // payload
                0x00
        };

        final Mqtt5SubAckImpl subAck = decodeOk(encoded);

        assertEquals(3, subAck.getPacketIdentifier());
        assertTrue(subAck.getReasonString().isPresent());
        assertEquals("success", subAck.getReasonString().get().toString());

        final ImmutableList<Mqtt5UserPropertyImpl> userProperties = subAck.getUserProperties().asList();
        assertEquals(1, userProperties.size());
        assertEquals("test", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());

        final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes = subAck.getReasonCodes();
        assertEquals(1, reasonCodes.size());
        assertEquals(Mqtt5SubAckReasonCode.GRANTED_QOS_0, reasonCodes.get(0));
    }

    @Test
    void encode_multipleUserProperties() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b1001_0000,
                //   remaining length
                43,
                // variable header
                //   packet identifier
                0, 3,
                //   properties
                39,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's',
                //     user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2',
                // payload
                0x00
        };

        final Mqtt5SubAckImpl subAck = decodeOk(encoded);

        assertTrue(subAck.getReasonString().isPresent());
        assertEquals("success", subAck.getReasonString().get().toString());

        final ImmutableList<Mqtt5UserPropertyImpl> userProperties = subAck.getUserProperties().asList();
        assertEquals(2, userProperties.size());
        assertEquals("test", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());
        assertEquals("test", userProperties.get(1).getName().toString());
        assertEquals("value2", userProperties.get(1).getValue().toString());

        final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes = subAck.getReasonCodes();
        assertEquals(1, reasonCodes.size());
        assertEquals(Mqtt5SubAckReasonCode.GRANTED_QOS_0, reasonCodes.get(0));
    }

    @Test
    void encode_invalidUserProperty_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b1001_0000,
                //   remaining length
                15,
                // variable header
                //   packet identifier
                0, 3,
                //   properties
                11,
                //     user properties
                0x26, 0, 1, (byte) 'k', 0, 5, 'v', 'a', 'l', 'u', 'e',
                // payload
                0x00
        };
        decodeOk(encoded);
        encoded[8] = (byte) '\uFFFF';
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void encode_reasonString() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b1001_0000,
                //   remaining length
                14,
                // variable header
                //   packet identifier
                0, 3,
                //   properties
                10,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's',
                // payload
                0x00
        };

        final Mqtt5SubAckImpl subAck = decodeOk(encoded);
        assertTrue(subAck.getReasonString().isPresent());
        assertEquals("success", subAck.getReasonString().get().toString());
    }

    @Test
    void encode_invalidReasonString_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b1001_0000,
                //   remaining length
                7,
                // variable header
                //   packet identifier
                0, 3,
                //   properties
                3,
                //     reason string
                0x1F, 0, 1, (byte) '\uFFFF',
                // payload
                0x00
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @ParameterizedTest
    @EnumSource(Mqtt5SubAckReasonCode.class)
    void encode_eachReasonCode(final Mqtt5SubAckReasonCode reasonCode) {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b1001_0000,
                //   remaining length
                4,
                // variable header
                //   packet identifier
                0, 3,
                //   properties
                0,
                // payload
                0x00
        };

        encoded[5] = (byte) reasonCode.getCode();
        final Mqtt5SubAckImpl subAck = decodeOk(encoded);
        final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes = subAck.getReasonCodes();
        assertEquals(1, reasonCodes.size());
        assertEquals(reasonCode, reasonCodes.get(0));
    }

    @Test
    void encode_multipleReasonCodes() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b1001_0000,
                //   remaining length
                15,
                // variable header
                //   packet identifier
                0, 3,
                //   properties
                0,
                // payload
                0x00, 0x02, 0x01, (byte) 0x80, (byte) 0x83, (byte) 0x87, (byte) 0x8F, (byte) 0x91, (byte) 0x97,
                (byte) 0x9E, (byte) 0xA1, (byte) 0xA2
        };

        final Mqtt5SubAckImpl subAck = decodeOk(encoded);
        final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes = subAck.getReasonCodes();
        assertEquals(12, reasonCodes.size());
        assertEquals(Mqtt5SubAckReasonCode.GRANTED_QOS_0, reasonCodes.get(0));
        assertEquals(Mqtt5SubAckReasonCode.GRANTED_QOS_2, reasonCodes.get(1));
        assertEquals(Mqtt5SubAckReasonCode.GRANTED_QOS_1, reasonCodes.get(2));
        assertEquals(Mqtt5SubAckReasonCode.UNSPECIFIED_ERROR, reasonCodes.get(3));
        assertEquals(Mqtt5SubAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR, reasonCodes.get(4));
        assertEquals(Mqtt5SubAckReasonCode.NOT_AUTHORIZED, reasonCodes.get(5));
        assertEquals(Mqtt5SubAckReasonCode.TOPIC_FILTER_INVALID, reasonCodes.get(6));
        assertEquals(Mqtt5SubAckReasonCode.PACKET_IDENTIFIER_IN_USE, reasonCodes.get(7));
        assertEquals(Mqtt5SubAckReasonCode.QUOTA_EXCEEDED, reasonCodes.get(8));
        assertEquals(Mqtt5SubAckReasonCode.SHARED_SUBSCRIPTION_NOT_SUPPORTED, reasonCodes.get(9));
        assertEquals(Mqtt5SubAckReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED, reasonCodes.get(10));
        assertEquals(Mqtt5SubAckReasonCode.WILDCARD_SUBSCRIPTION_NOT_SUPPORTED, reasonCodes.get(11));
    }

    @Test
    void encode_noReasonCode_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b1001_0000,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                0, 3,
                //   properties
                0
        };
        decodeNok(encoded, Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void encode_invalidReasonCode_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b1001_0000,
                //   remaining length
                4,
                // variable header
                //   packet identifier
                0, 3,
                //   properties
                0,
                // payload
                0x00
        };

        final Mqtt5SubAckImpl subAck = decodeOk(encoded);
        final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes = subAck.getReasonCodes();
        assertEquals(1, reasonCodes.size());
        assertEquals(Mqtt5SubAckReasonCode.GRANTED_QOS_0, reasonCodes.get(0));

        encoded[5] = (byte) 0xA5; // invalid reason code
        decodeNok(encoded, MALFORMED_PACKET);

        encoded[5] = (byte) 0x00; // ok reason code
        decodeOk(encoded);
    }

    @Test
    void encode_invalidFlags_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b1001_1000,
                //   remaining length
                4,
                // variable header
                //   packet identifier
                0, 3,
                //   properties
                0,
                // payload
                0x00
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void encode_invalidRemainingLength_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b1001_0000,
                //   remaining length
                2,
                // variable header
                //   packet identifier
                0, 3
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void encode_propertyLengthLessThanZero_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b1001_0000,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                0, 3,
                //   property length
                -1
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void encode_invalidPropertyType_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b1001_0000,
                //   remaining length
                14,
                // variable header
                //   packet identifier
                0, 3,
                //   properties
                10,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's',
                // payload
                0x00
        };

        decodeOk(encoded);

        encoded[5] = 0x01; // invalid property type for suback
        decodeNok(encoded, MALFORMED_PACKET);
    }


    @Test
    void encode_propertyLengthLongerThanEncoded_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b1001_0000,
                //   remaining length
                14,
                // variable header
                //   packet identifier
                0, 3,
                //   properties
                10,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's',
                // payload
                0x00
        };

        decodeOk(encoded);

        encoded[4] = (byte) (encoded[4] + 2); // make property length longer than readable bytes
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void encode_propertyLengthTooShort_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b1001_0000,
                //   remaining length
                14,
                // variable header
                //   packet identifier
                0, 3,
                //   properties
                10,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's',
                // payload
                0x00
        };

        decodeOk(encoded);

        encoded[4] = (byte) (encoded[4] - 1); // make property length shorter
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    void encode_propertyIdentifierLessThanZero_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b1001_0000,
                //   remaining length
                14,
                // variable header
                //   packet identifier
                0, 3,
                //   properties
                10,
                //     reason string
                (byte) 0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's',
                // payload
                0x00
        };

        decodeOk(encoded);

        encoded[5] = (byte) 0xFF; // invalid property type for suback
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @NotNull
    private Mqtt5SubAckImpl decodeOk(final byte[] encoded) {
        final Mqtt5SubAckImpl subAck = decode(encoded);
        assertNotNull(subAck);
        return subAck;
    }

    private void decodeNok(final byte[] encoded, final Mqtt5DisconnectReasonCode reasonCode) {
        final Mqtt5SubAckImpl subAck = decode(encoded);
        assertNull(subAck);

        final Mqtt5Disconnect disconnect = channel.readOutbound();
        assertNotNull(disconnect);
        assertEquals(reasonCode, disconnect.getReasonCode());

        createChannel();
    }

    @Nullable
    private Mqtt5SubAckImpl decode(final byte[] encoded) {
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);

        return channel.readInbound();
    }

}