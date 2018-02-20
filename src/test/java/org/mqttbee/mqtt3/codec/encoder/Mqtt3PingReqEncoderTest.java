package org.mqttbee.mqtt3.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mqttbee.mqtt3.message.pingreq.Mqtt3PingReqImpl;

import static org.junit.Assert.assertArrayEquals;

class Mqtt3PingReqEncoderTest {

    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        channel = new EmbeddedChannel(new Mqtt3Encoder());
    }

    @AfterEach
    void tearDown() {
        channel.close();
    }

    @Test
    void encode() {
        // Given
        final Mqtt3PingReqImpl pingRequest = new Mqtt3PingReqImpl();
        final byte[] expected = {(byte) 0b11000000, 0b0};

        // When
        channel.writeOutbound(pingRequest);

        // Then
        final ByteBuf byteBuf = channel.readOutbound();
        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        assertArrayEquals(expected, actual);
    }
}
