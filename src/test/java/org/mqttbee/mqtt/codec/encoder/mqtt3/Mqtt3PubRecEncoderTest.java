package org.mqttbee.mqtt.codec.encoder.mqtt3;

import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;
import org.mqttbee.mqtt.codec.encoder.AbstractMqtt5EncoderTest;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRecImpl;
import org.mqttbee.mqtt.message.publish.pubrec.mqtt3.Mqtt3PubRecView;

import static org.junit.Assert.assertArrayEquals;

class Mqtt3PubRecEncoderTest extends AbstractMqtt5EncoderTest {

    Mqtt3PubRecEncoderTest() {
        super(true);
    }

    @Test
    void encode() {
        final int id = 1;
        final byte msb = (byte) (id >>> 8);
        final byte lsb = (byte) id;
        final byte[] expected = {0x50, 0x02, msb, lsb};
        final MqttPubRecImpl pubRec = Mqtt3PubRecView.wrapped(id);
        encode(expected, pubRec);
    }

    @Test
    void encodedRemainingLength() {
    }

    private void encode(final byte[] expected, final MqttPubRecImpl pubRec) {
        channel.writeOutbound(pubRec);
        final ByteBuf byteBuf = channel.readOutbound();
        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();
        assertArrayEquals(expected, actual);
    }

}