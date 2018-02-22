package org.mqttbee.mqtt.codec.decoder.mqtt3;

import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoders;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAckImpl;

import static org.junit.jupiter.api.Assertions.*;

class Mqtt3PubAckDecoderTest extends AbstractMqtt3DecoderTest {

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

    Mqtt3PubAckDecoderTest() {
        super(new Mqtt3PubAckTestMessageDecoders());
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_SUCESS(final boolean useMaxPacketId) {

        final byte[] encoded = Bytes.concat(WELLFORMED_PUBACK_BEGIN, useMaxPacketId ? MAX_PACKET_ID : MIN_PACKET_ID);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final MqttPubAckImpl pubAck = channel.readInbound();
        assertNotNull(pubAck);
        assertEquals(useMaxPacketId ? 65535 : 0, pubAck.getPacketIdentifier());
    }

    @Test
    void decode_ERROR_MAlFORMED_FLAGS() {
        final byte[] encoded = Bytes.concat(MALFORMED_PUBACK_BEGIN_WRONG_FLAGS, MAX_PACKET_ID);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final MqttPubAckImpl pubAck = channel.readInbound();
        assertFalse(channel.isOpen());
        assertNull(pubAck);
    }


    @Test
    void decode_ERROR_TOO_LONG() {
        //final byte[] encoded = Bytes.concat(MALFORMED_PUBACK_BEGIN_WRONG_FLAGS, MAX_PACKET_ID);
        final byte[] encoded =
                Bytes.concat(MALFORMED_PUBACK_BEGIN_TOO_LONG_LENGTH, MAX_PACKET_ID, ENDING_TOO_LONG_MALFORMED);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final MqttPubAckImpl pubAck = channel.readInbound();
        assertFalse(channel.isOpen());
        assertNull(pubAck);
    }

    private static class Mqtt3PubAckTestMessageDecoders implements MqttMessageDecoders {
        @Nullable
        @Override
        public MqttMessageDecoder get(final int code) {
            if (code == Mqtt3MessageType.PUBACK.getCode()) {
                return new Mqtt3PubAckDecoder();
            }
            return null;
        }
    }

}