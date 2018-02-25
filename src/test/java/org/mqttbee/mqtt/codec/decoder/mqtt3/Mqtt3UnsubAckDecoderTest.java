package org.mqttbee.mqtt.codec.decoder.mqtt3;

import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoders;
import org.mqttbee.mqtt.message.unsubscribe.unsuback.MqttUnsubAck;

import static org.junit.jupiter.api.Assertions.*;

class Mqtt3UnsubAckDecoderTest extends AbstractMqtt3DecoderTest {

    private static final byte[] WELLFORMED_UNSUBACK_BEGIN = {
            //   type, flags
            (byte) 0b1011_0000,
            //remaining length
            0b0000_0010
    };
    private static final byte[] MALFORMED_UNSUBACK_BEGIN_WRONG_FLAGS = {
            //   type, flags
            (byte) 0b1011_0100,
            //remaining length
            0b0000_0010
    };
    private static final byte[] MALFORMED_UNSUBACK_BEGIN_TOO_LONG_LENGTH = {
            //   type, flags
            (byte) 0b1011_0000,
            //remaining length
            0b0000_0011
    };

    private static final byte[] ENDING_TOO_LONG_MALFORMED = {0x01};

    private static final byte[] MAX_PACKET_ID = {(byte) 0b1111_1111, (byte) 0b1111_1111};
    private static final byte[] MIN_PACKET_ID = {0x00, 0x00};

    Mqtt3UnsubAckDecoderTest() {
        super(new Mqtt3UnsubAckTestMessageDecoders());
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_SUCESS(final boolean useMaxPacketId) {
        final byte[] encoded = Bytes.concat(WELLFORMED_UNSUBACK_BEGIN, useMaxPacketId ? MAX_PACKET_ID : MIN_PACKET_ID);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final MqttUnsubAck unsubAck = channel.readInbound();
        assertNotNull(unsubAck);
        assertEquals(useMaxPacketId ? 65535 : 0, unsubAck.getPacketIdentifier());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "2"})
    void decode_ERROR_CASES(final int errorcase) throws Exception {
        final byte[] encoded;
        switch (errorcase) {
            case 1:
                encoded = Bytes.concat(MALFORMED_UNSUBACK_BEGIN_WRONG_FLAGS, MAX_PACKET_ID);
                break;
            case 2:
                encoded = Bytes.concat(MALFORMED_UNSUBACK_BEGIN_TOO_LONG_LENGTH, MIN_PACKET_ID,
                        ENDING_TOO_LONG_MALFORMED);
                break;
            default:
                throw new Exception();
        }

        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final MqttUnsubAck unsubAck = channel.readInbound();
        assertFalse(channel.isOpen());
        assertNull(unsubAck);
    }

    private static class Mqtt3UnsubAckTestMessageDecoders implements MqttMessageDecoders {
        @Nullable
        @Override
        public MqttMessageDecoder get(final int code) {
            if (code == Mqtt3MessageType.UNSUBACK.getCode()) {
                return new Mqtt3UnsubAckDecoder();
            }
            return null;
        }
    }

}