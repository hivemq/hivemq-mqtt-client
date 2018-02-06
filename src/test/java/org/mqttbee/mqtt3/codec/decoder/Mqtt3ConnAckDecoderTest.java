package org.mqttbee.mqtt3.codec.decoder;

import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt3.message.Mqtt3ConnAck;
import org.mqttbee.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt3.message.connack.Mqtt3ConnAckReasonCode;
import org.mqttbee.mqtt3.message.puback.Mqtt3PubAckImpl;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Daniel Krüger
 */
class Mqtt3ConnAckDecoderTest {

    private static final byte[] WELLFORMED_CONNACK_BEGIN = {
            //   type, flags
            0b0010_0000,
            //remaining length
            0b0000_0010
    };
    private static final byte[] MALFORMED_CONNACK_BEGIN_WORNG_FLAGS = {
            //   type, flags
            0b0010_0100,
            //remaining length
            0b0000_0010
    };
    private static final byte[] MALFORMED_CONNACK_BEGIN_TOO_LONG_LENGTH = {
            //   type, flags
            0b0010_0100,
            //remaining length
            0b0000_0011
    };
    private static final byte[] ENDING_TOO_LONG_MALFORMED = {0x01};
    private static final byte[] SESSION_PRESENT_TRUE = {0b0000_0001};
    private static final byte[] SESSION_PRESENT_FALSE = {0b0000_0000};
    private static final byte[] REASON_CODE_SUCCESS = {0x00};
    private static final byte[] REASON_CODE_UNACCEPTED_PROTOCOL_VERSION = {0x01};
    private static final byte[] REASON_CODE_IDENTFIER_REJECTED = {0x02};
    private static final byte[] REASON_CODE_SERVER_UNAVAILABLE = {0x03};
    private static final byte[] REASON_CODE_SERVER_BAD_USERNAME_OR_PASSWORD = {0x04};
    private static final byte[] REASON_CODE_NOT_AUTHORIZIED = {0x05};
    private static final byte[] REASON_CODE_BAD = {0x13};
    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        channel = new EmbeddedChannel(new Mqtt3Decoder(new Mqtt3ConnAckDecoderTest.Mqtt3ConnAckTestMessageDecoders()));
    }

    @AfterEach
    void tearDown() throws Exception {
        channel.close();
    }


    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    public void decode_SUCCESS(boolean sessionPresent) throws Exception {
        final byte[] encoded =
                Bytes.concat(WELLFORMED_CONNACK_BEGIN, sessionPresent ? SESSION_PRESENT_TRUE : SESSION_PRESENT_FALSE,
                        REASON_CODE_SUCCESS);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt3ConnAck connAck = channel.readInbound();
        assertNotNull(connAck);
        assertEquals(Mqtt3ConnAckReasonCode.SUCCESS, connAck.getReasonCode());
        assertEquals(sessionPresent, connAck.isSessionPresent());
    }


    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    public void decode_WRONG_PROTOCOL(boolean sessionPresent) throws Exception {
        final byte[] encoded =
                Bytes.concat(WELLFORMED_CONNACK_BEGIN, sessionPresent ? SESSION_PRESENT_TRUE : SESSION_PRESENT_FALSE,
                        REASON_CODE_UNACCEPTED_PROTOCOL_VERSION);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt3ConnAck connAck = channel.readInbound();
        assertNotNull(connAck);
        assertEquals(Mqtt3ConnAckReasonCode.UNSUPPORTED_PROTOCOL_VERSION, connAck.getReasonCode());
        assertEquals(sessionPresent, connAck.isSessionPresent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    public void decode_SERVER_IDENTFIER_REJECTED(boolean sessionPresent) throws Exception {
        final byte[] encoded =
                Bytes.concat(WELLFORMED_CONNACK_BEGIN, sessionPresent ? SESSION_PRESENT_TRUE : SESSION_PRESENT_FALSE,
                        REASON_CODE_IDENTFIER_REJECTED);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt3ConnAck connAck = channel.readInbound();
        assertNotNull(connAck);
        assertEquals(Mqtt3ConnAckReasonCode.IDENTIFIER_REJECTED, connAck.getReasonCode());
        assertEquals(sessionPresent, connAck.isSessionPresent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    public void decode_SERVER_UNAVAILABLE(boolean sessionPresent) throws Exception {
        final byte[] encoded =
                Bytes.concat(WELLFORMED_CONNACK_BEGIN, sessionPresent ? SESSION_PRESENT_TRUE : SESSION_PRESENT_FALSE,
                        REASON_CODE_SERVER_UNAVAILABLE);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt3ConnAck connAck = channel.readInbound();
        assertNotNull(connAck);
        assertEquals(Mqtt3ConnAckReasonCode.SERVER_UNAVAILABLE, connAck.getReasonCode());
        assertEquals(sessionPresent, connAck.isSessionPresent());
    }


    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    public void decode_SERVER_BAD_USERNAME_OR_PASSWORD(boolean sessionPresent) throws Exception {
        final byte[] encoded =
                Bytes.concat(WELLFORMED_CONNACK_BEGIN, sessionPresent ? SESSION_PRESENT_TRUE : SESSION_PRESENT_FALSE,
                        REASON_CODE_SERVER_BAD_USERNAME_OR_PASSWORD);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt3ConnAck connAck = channel.readInbound();
        assertNotNull(connAck);
        assertEquals(Mqtt3ConnAckReasonCode.BAD_USERNAME_OR_PASSWORD, connAck.getReasonCode());
        assertEquals(sessionPresent, connAck.isSessionPresent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    public void decode_NOT_AUTHORIZED(boolean sessionPresent) throws Exception {
        final byte[] encoded =
                Bytes.concat(WELLFORMED_CONNACK_BEGIN, sessionPresent ? SESSION_PRESENT_TRUE : SESSION_PRESENT_FALSE,
                        REASON_CODE_NOT_AUTHORIZIED);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt3ConnAck connAck = channel.readInbound();
        assertNotNull(connAck);
        assertEquals(Mqtt3ConnAckReasonCode.NOT_AUTHORIZED, connAck.getReasonCode());
        assertEquals(sessionPresent, connAck.isSessionPresent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    public void decode_BAD_RETURNCODE(boolean sessionPresent) throws Exception {
        final byte[] encoded =
                Bytes.concat(WELLFORMED_CONNACK_BEGIN, sessionPresent ? SESSION_PRESENT_TRUE : SESSION_PRESENT_FALSE,
                        REASON_CODE_BAD);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt3ConnAck connAck = channel.readInbound();
        assertNull(connAck);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "2"})
    void decode_ERROR_CASES(int errorcase) throws Exception {
        final byte[] encoded;
        switch (errorcase) {
            case 1:
                encoded = Bytes.concat(MALFORMED_CONNACK_BEGIN_WORNG_FLAGS, SESSION_PRESENT_FALSE, REASON_CODE_BAD);
                break;
            case 2:
                encoded = Bytes.concat(MALFORMED_CONNACK_BEGIN_TOO_LONG_LENGTH, SESSION_PRESENT_FALSE, REASON_CODE_BAD, ENDING_TOO_LONG_MALFORMED);
                break;
            default:
                throw new Exception();
        }

        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt3PubAckImpl pubAck = channel.readInbound();
        assertFalse(channel.isOpen());
        assertNull(pubAck);
    }


    private static class Mqtt3ConnAckTestMessageDecoders implements Mqtt3MessageDecoders {
        @Nullable
        @Override
        public Mqtt3MessageDecoder get(final int code) {
            if (code == Mqtt3MessageType.CONNACK.getCode()) {
                return new Mqtt3ConnAckDecoder();
            }
            return null;
        }
    }
}