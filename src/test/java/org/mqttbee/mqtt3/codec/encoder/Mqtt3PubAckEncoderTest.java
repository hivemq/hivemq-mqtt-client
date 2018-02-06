package org.mqttbee.mqtt3.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mqttbee.mqtt3.message.puback.Mqtt3PubAckImpl;
import org.mqttbee.mqtt3.message.puback.Mqtt3PubAckInternal;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5Encoder;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckImpl;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckInternal;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.*;

class Mqtt3PubAckEncoderTest {

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
    int id = 1;

        final byte msb = (byte)(id>>>8);
        final byte lsb = (byte)id;
        byte[] exspcected = {0x40, 0x02, msb, lsb};
        Mqtt3PubAckInternal internal = new Mqtt3PubAckInternal(new Mqtt3PubAckImpl(id), id);
        encode(exspcected, internal, id);
    }

    @Test
    void encodedRemainingLength() {


    }






    private void encode(final byte[] expected, final Mqtt3PubAckInternal internal, final int packetIdentifier) {


        channel.writeOutbound(internal);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);

        assertArrayEquals(expected, actual);
    }
}