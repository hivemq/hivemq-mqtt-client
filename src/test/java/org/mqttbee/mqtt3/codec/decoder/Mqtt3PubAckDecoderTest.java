package org.mqttbee.mqtt3.codec.decoder;

import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt3.message.puback.Mqtt3PubAckImpl;

import static org.junit.jupiter.api.Assertions.*;

class Mqtt3PubAckDecoderTest {


    private static final byte[] WELLFORMED_PUBACK_BEGIN = {
            //   type, flags
            0b0100_0000,
            //remaining length
            0b0000_0010
    };
    private static final byte[] MALFORMED_PUBACK_BEGIN_WRONG_FLAGS = {
            //   type, flags
            0b0100_0100,
            //remaining length
            0b0000_0010
    };
    private static final byte[] MALFORMED_PUBACK_BEGIN_TOO_LONG_LENGTH = {
            //   type, flags
            0b0100_0000,
            //remaining length
            0b0000_0011
    };
    private static final byte[] ENDING_TOO_LONG_MALFORMED = {0x01};
    private static final byte[] MAX_PACKET_ID = {(byte) 0b1111_1111, (byte) 0b1111_1111};
    private static final byte[] MIN_PACKET_ID = {0x00, 0x00};
    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        channel = new EmbeddedChannel(new Mqtt3Decoder(new Mqtt3PubAckTestMessageDecoders()));
    }

    @AfterEach
    void tearDown() throws Exception {
        channel.close();
    }


    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_SUCESS(boolean useMaxPacketId) {

        final byte[] encoded = Bytes.concat(WELLFORMED_PUBACK_BEGIN, useMaxPacketId ? MAX_PACKET_ID : MIN_PACKET_ID);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt3PubAckImpl pubAck = channel.readInbound();
        assertNotNull(pubAck);
        assertEquals(useMaxPacketId ? 65535 : 0, pubAck.getPacketId());
    }

    @Test
    void decode_ERROR_MAlFORMED_FLAGS() throws Exception {
        final byte[] encoded = Bytes.concat(MALFORMED_PUBACK_BEGIN_WRONG_FLAGS, MAX_PACKET_ID);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt3PubAckImpl pubAck = channel.readInbound();
        assertFalse(channel.isOpen());
        assertNull(pubAck);
    }


    @Test
    void decode_ERROR_TOO_LONG() throws Exception {
        //final byte[] encoded = Bytes.concat(MALFORMED_PUBACK_BEGIN_WRONG_FLAGS, MAX_PACKET_ID);
        final byte[] encoded = Bytes.concat(MALFORMED_PUBACK_BEGIN_TOO_LONG_LENGTH, MAX_PACKET_ID, ENDING_TOO_LONG_MALFORMED);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt3PubAckImpl pubAck = channel.readInbound();
        assertFalse(channel.isOpen());
        assertNull(pubAck);
    }




    private static class Mqtt3PubAckTestMessageDecoders implements Mqtt3MessageDecoders {
        @Nullable
        @Override
        public Mqtt3MessageDecoder get(final int code) {
            if (code == Mqtt3MessageType.PUBACK.getCode()) {
                return new Mqtt3PubAckDecoder();
            }
            return null;
        }
    }

}