package org.mqttbee.mqtt.codec.decoder.mqtt3;

import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoders;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRecImpl;

import static org.junit.jupiter.api.Assertions.*;

class Mqtt3PubRecDecoderTest extends AbstractMqtt3DecoderTest {

    private static final byte[] WELLFORMED_PUBREC_BEGIN = {
            //   type, flags
            0b0101_0000,
            //remaining length
            0b0000_0010
    };
    private static final byte[] MALFORMED_PUBREC_BEGIN_WRONG_FLAGS = {
            //   type, flags
            0b0101_0100,
            //remaining length
            0b0000_0010
    };

    private static final byte[] MALFORMED_PUBREC_BEGIN_TOO_LONG_LENGTH = {
            //   type, flags
            0b0101_0100,
            //remaining length
            0b0000_0001
    };
    private static final byte[] ENDING_TOO_LONG_MALFORMED = {0x01};
    private static final byte[] MAX_PACKET_ID = {(byte) 0b1111_1111, (byte) 0b1111_1111};
    private static final byte[] MIN_PACKET_ID = {0x00, 0x00};

    Mqtt3PubRecDecoderTest() {
        super(new Mqtt3PubRecTestMessageDecoders());
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_SUCESS(final boolean useMaxPacketId) {
        final byte[] encoded = Bytes.concat(WELLFORMED_PUBREC_BEGIN, useMaxPacketId ? MAX_PACKET_ID : MIN_PACKET_ID);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final MqttPubRecImpl pubRec = channel.readInbound();
        assertNotNull(pubRec);
        assertEquals(useMaxPacketId ? 65535 : 0, pubRec.getPacketIdentifier());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "2"})
    void decode_ERROR_CASES(final int errorcase) throws Exception {
        final byte[] encoded;
        switch (errorcase) {
            case 1:
                encoded = Bytes.concat(MALFORMED_PUBREC_BEGIN_WRONG_FLAGS, MAX_PACKET_ID);
                break;
            case 2:
                encoded =
                        Bytes.concat(MALFORMED_PUBREC_BEGIN_TOO_LONG_LENGTH, MIN_PACKET_ID, ENDING_TOO_LONG_MALFORMED);
                break;
            default:
                throw new Exception();
        }

        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final MqttPubRecImpl pubRec = channel.readInbound();
        assertFalse(channel.isOpen());
        assertNull(pubRec);
    }

    private static class Mqtt3PubRecTestMessageDecoders implements MqttMessageDecoders {
        @Nullable
        @Override
        public MqttMessageDecoder get(final int code) {
            if (code == Mqtt3MessageType.PUBREC.getCode()) {
                return new Mqtt3PubRecDecoder();
            }
            return null;
        }
    }

}