package org.mqttbee.mqtt3.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt3.message.Mqtt3PingResp;
import org.mqttbee.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt3.message.pingresp.Mqtt3PingRespImpl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Mqtt3PingRespDecoderTest {

    private static final byte[] WELLFORMED_PINGRESP = {
            //   type, flags
            (byte) 0b1101_0000,
            //remaining length
            0b0000_0000
    };
    private static final byte[] MALFORMED_PINGRESP_WRONG_FLAGS = {
            //   type, flags
            (byte) 0b1101_0100,
            //remaining length
            0b0000_0000
    };
    private static final byte[] MALFORMED_PINGRESP_WRONG_REMAINING_LENGTH = {
            //   type, flags
            (byte) 0b1101_0100,
            //remaining length
            0b0000_0001
    };

    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        channel = new EmbeddedChannel(new Mqtt3Decoder(new Mqtt3PingRespTestMessageDecoders()));
    }

    @AfterEach
    void tearDown() throws Exception {
        channel.close();
    }

    @Test
    void decode_SUCCESS() throws Exception {
        // Given a valid PingResp message written to the channel
        final byte[] encoded = WELLFORMED_PINGRESP;

        ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);

        // When we decode the message by reading from the channel
        final Mqtt3PingRespImpl result = channel.readInbound();

        // Then we have a valid, non-null result
        assertNotNull(result);
    }

    @Test
    void decode_MALFORMED_FLAGS() throws Exception {
        // Given a PingResp with malformed flags written to the channel
        final byte[] encoded = MALFORMED_PINGRESP_WRONG_FLAGS;

        runMalformedTest(encoded);
    }

    @Test
    void decode_INVALID_REMAINING_LENGTH() throws Exception {
        // Given a PingResp with a greater-than-zero remaining length written to the channel
        final byte[] encoded = MALFORMED_PINGRESP_WRONG_REMAINING_LENGTH;

        runMalformedTest(encoded);
    }

    private void runMalformedTest(final byte[] encoded) {
        ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);

        // When we decode the message by reading from the channel
        final Mqtt3PingRespImpl result = channel.readInbound();

        // Then we have a null result and the channel has been closed
        assertNull(result);
        assertFalse(channel.isOpen());
    }

    private static class Mqtt3PingRespTestMessageDecoders implements Mqtt3MessageDecoders {
        @Nullable
        @Override
        public Mqtt3MessageDecoder get(final int code) {
            if (code == Mqtt3MessageType.PINGRESP.getCode()) {
                return new Mqtt3PingRespDecoder();
            }
            return null;
        }
    }

}
