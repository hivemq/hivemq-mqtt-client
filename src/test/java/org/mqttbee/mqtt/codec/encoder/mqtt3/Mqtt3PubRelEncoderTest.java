package org.mqttbee.mqtt.codec.encoder.mqtt3;

import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;
import org.mqttbee.mqtt.codec.encoder.AbstractMqtt5EncoderTest;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRelImpl;
import org.mqttbee.mqtt.message.publish.pubrel.mqtt3.Mqtt3PubRelView;

import static org.junit.Assert.assertArrayEquals;

class Mqtt3PubRelEncoderTest extends AbstractMqtt5EncoderTest {

    Mqtt3PubRelEncoderTest() {
        super(true);
    }

    @Test
    void encode() {
        final int id = 1;
        final byte msb = (byte) (id >>> 8);
        final byte lsb = (byte) id;
        final byte[] expected = {0x60, 0x02, msb, lsb};
        final MqttPubRelImpl pubRel = Mqtt3PubRelView.wrapped(id);
        encode(expected, pubRel);
    }

    @Test
    void encodedRemainingLength() {
    }

    private void encode(final byte[] expected, final MqttPubRelImpl pubRel) {
        channel.writeOutbound(pubRel);
        final ByteBuf byteBuf = channel.readOutbound();
        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();
        assertArrayEquals(expected, actual);
    }

}