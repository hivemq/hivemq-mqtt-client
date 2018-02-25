package org.mqttbee.mqtt.codec.encoder.mqtt3;

import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;
import org.mqttbee.mqtt.codec.encoder.AbstractMqtt5EncoderTest;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubComp;
import org.mqttbee.mqtt.message.publish.pubcomp.mqtt3.Mqtt3PubCompView;

import static org.junit.Assert.assertArrayEquals;

class Mqtt3PubCompEncoderTest extends AbstractMqtt5EncoderTest {

    Mqtt3PubCompEncoderTest() {
        super(true);
    }

    @Test
    void encode() {
        final int id = 1;
        final byte msb = (byte) (id >>> 8);
        final byte lsb = (byte) id;
        final byte[] expected = {0x70, 0x02, msb, lsb};
        final MqttPubComp pubComp = Mqtt3PubCompView.wrapped(id);
        encode(expected, pubComp);
    }

    @Test
    void encodedRemainingLength() {
    }

    private void encode(final byte[] expected, final MqttPubComp pubComp) {
        channel.writeOutbound(pubComp);
        final ByteBuf byteBuf = channel.readOutbound();
        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();
        assertArrayEquals(expected, actual);
    }

}