package org.mqttbee.mqtt.codec.encoder.mqtt3;

import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;
import org.mqttbee.mqtt.codec.encoder.AbstractMqtt5EncoderTest;
import org.mqttbee.mqtt.message.ping.MqttPingReqImpl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class Mqtt3PingReqEncoderTest extends AbstractMqtt5EncoderTest {

    Mqtt3PingReqEncoderTest() {
        super(true);
    }

    @Test
    void encode() {
        // Given
        final MqttPingReqImpl pingRequest = MqttPingReqImpl.INSTANCE;
        final byte[] expected = {(byte) 0b11000000, 0b0};

        // When
        channel.writeOutbound(pingRequest);

        // Then
        ByteBuf byteBuf = null;
        try {
            byteBuf = channel.readOutbound();
            final byte[] actual = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(actual);
            assertArrayEquals(expected, actual);
        } finally {
            if (byteBuf != null) {
                byteBuf.release();
            }
        }
    }

}
