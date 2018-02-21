package org.mqttbee.mqtt3.codec.decoder;

import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAckReturnCode;
import org.mqttbee.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt3.message.suback.Mqtt3SubAckImpl;

import static org.junit.jupiter.api.Assertions.*;

class Mqtt3SubAckDecoderTest {


    private static final byte[] WELLFORMED_SUBACK_BEGIN = {
            //   type, flags
            (byte) 0b1001_0000,
            //remaining length
            0b0000_0110
    };
    private static final byte[] MALFORMED_SUBACK_BEGIN_WRONG_FLAGS = {
            //   type, flags
            (byte) 0b1001_0010,
            //remaining length
            0b0000_0010
    };
    private static final byte[] MALFORMED_SUBACK_BEGIN_TOO_SHORT_LENGTH = {
            //   type, flags
            (byte) 0b1001_0010,
            //remaining length
            0b0000_0010
    };

    private static final byte[] ENDING_TOO_LONG_MALFORMED = {0x01};
    private static final byte[] REASON_CODE_QOS_0 = {0x00};
    private static final byte[] REASON_CODE_QOS_1 = {0x01};
    private static final byte[] REASON_CODE_QOS_2 = {0x02};
    private static final byte[] REASON_CODE_FAILURE = {(byte) 0x80};
    private static final byte[] REASON_CODE_MALFORMED = {0x8, 0x2};

    private static final byte[] MAX_PACKET_ID = {(byte) 0b1111_1111, (byte) 0b1111_1111};
    private static final byte[] MIN_PACKET_ID = {0x00, 0x00};
    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        channel = new EmbeddedChannel(new Mqtt3Decoder(new Mqtt3SubAckTestMessageDecoders()));
    }

    @AfterEach
    void tearDown() throws Exception {
        channel.close();
    }


    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_SUCESS(boolean useMaxPacketId) {
        final byte[] encoded = Bytes.concat(WELLFORMED_SUBACK_BEGIN, (useMaxPacketId ? MAX_PACKET_ID : MIN_PACKET_ID),
                REASON_CODE_QOS_0, REASON_CODE_QOS_1, REASON_CODE_QOS_2, REASON_CODE_FAILURE);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt3SubAckImpl subAckImpl = channel.readInbound();
        assertNotNull(subAckImpl);
        assertEquals(useMaxPacketId ? 65535 : 0, subAckImpl.getPacketId());
        assertEquals(Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_0, subAckImpl.getReasonCodes().get(0));
        assertEquals(Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_1, subAckImpl.getReasonCodes().get(1));
        assertEquals(Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_2, subAckImpl.getReasonCodes().get(2));
        assertEquals(Mqtt3SubAckReturnCode.FAILURE, subAckImpl.getReasonCodes().get(3));
    }


    @ParameterizedTest
    @ValueSource(strings = {"1", "2", "3"})
    void decode_ERROR_CASES(int errorcase) throws Exception {
        final byte[] encoded;
        switch (errorcase) {
            case 1:
                //wrong flags
                encoded = Bytes.concat(MALFORMED_SUBACK_BEGIN_WRONG_FLAGS, MAX_PACKET_ID, REASON_CODE_QOS_0,
                        REASON_CODE_QOS_1, REASON_CODE_QOS_2, REASON_CODE_FAILURE);
                break;
            case 2:
                // only 2 remaining length
                encoded = Bytes.concat(MALFORMED_SUBACK_BEGIN_TOO_SHORT_LENGTH, MIN_PACKET_ID);
                break;
            case 3:
                // malformed reason code
                encoded = Bytes.concat(WELLFORMED_SUBACK_BEGIN, MIN_PACKET_ID, REASON_CODE_MALFORMED, REASON_CODE_QOS_1,
                        REASON_CODE_QOS_2, REASON_CODE_FAILURE);
                break;
            default:
                throw new Exception();
        }

        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt3SubAckImpl subAckImpl = channel.readInbound();
        assertFalse(channel.isOpen());
        assertNull(subAckImpl);
    }

    private static class Mqtt3SubAckTestMessageDecoders implements Mqtt3MessageDecoders {
        @Nullable
        @Override
        public Mqtt3MessageDecoder get(final int code) {
            if (code == Mqtt3MessageType.SUBACK.getCode()) {
                return new Mqtt3SubAckDecoder();
            }
            return null;
        }
    }

}