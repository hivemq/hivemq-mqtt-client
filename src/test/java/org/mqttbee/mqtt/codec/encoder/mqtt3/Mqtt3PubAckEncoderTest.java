package org.mqttbee.mqtt.codec.encoder.mqtt3;

import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;
import org.mqttbee.mqtt.codec.encoder.AbstractMqtt5EncoderTest;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAckImpl;
import org.mqttbee.mqtt.message.publish.puback.mqtt3.Mqtt3PubAckView;

import static org.junit.Assert.assertArrayEquals;

class Mqtt3PubAckEncoderTest extends AbstractMqtt5EncoderTest {

    Mqtt3PubAckEncoderTest() {
        super(true);
    }

    @Test
    void encode() {
        final int id = 1;
        final byte msb = (byte) (id >>> 8);
        final byte lsb = (byte) id;
        final byte[] expected = {0x40, 0x02, msb, lsb};
        final MqttPubAckImpl pubAck = Mqtt3PubAckView.wrapped(id);
        encode(expected, pubAck);
    }

    @Test
    void encodedRemainingLength() {
    }

    private void encode(final byte[] expected, final MqttPubAckImpl pubAck) {
        channel.writeOutbound(pubAck);
        final ByteBuf byteBuf = channel.readOutbound();
        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();
        assertArrayEquals(expected, actual);
    }

}