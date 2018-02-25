package org.mqttbee.mqtt3.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt3.message.publish.Mqtt3PublishImpl;
import org.mqttbee.mqtt3.message.publish.Mqtt3PublishInternal;

import static org.junit.jupiter.api.Assertions.*;

class Mqtt3PublishDecoderTest {
    private static final Byte WELLFORMED_PUBLISH_BEGIN = 0b0011_0000;
    private static final Byte DUP_BIT = 0b0000_1000;
    private static final Byte RETAIN_BIT = 0b0000_001;
    private EmbeddedChannel channel;

    private ByteBuf createWellformedPublish(
            boolean dup, int qos, boolean retained, int packetId, byte[] topic, byte[] payload) throws Exception {

        final ByteBuf byteBuf = channel.alloc().buffer();


        int topicLength = topic.length;

        final int remainingLength;
        if (qos == 0) {
            remainingLength = 2 + topicLength + payload.length;
        } else {
            remainingLength = 2 + 2 + topicLength + payload.length;
        }

        if (topicLength > 1 << 7) {
            throw new Exception("Topic is too long");
        }


        if (remainingLength > 1024) {
            throw new Exception(); // too avoid numbers which must be represented with a variable byte integer. (Of course the limit could be much greater than 1024)
        }
        byte fixedHeaderFirstByte = WELLFORMED_PUBLISH_BEGIN;
        //set dup bit
        if (dup) {
            fixedHeaderFirstByte = (byte) (fixedHeaderFirstByte | DUP_BIT);
        }
        //set qos
        fixedHeaderFirstByte = (byte) (fixedHeaderFirstByte | (qos << 1));
        //set retained
        if (retained) {
            fixedHeaderFirstByte = (byte) (fixedHeaderFirstByte | RETAIN_BIT);
        }
        byteBuf.writeByte(fixedHeaderFirstByte);

        final Byte fixedHeaderSecondByte = (byte) remainingLength;
        byteBuf.writeByte(fixedHeaderSecondByte);
        byteBuf.writeShort(topicLength);
        byteBuf.writeBytes(topic);

        if (qos != 0) {
            byteBuf.writeShort(packetId);
        }

        byteBuf.writeBytes(payload);
        return byteBuf;
    }


    @BeforeEach
    void setUp() {
        channel = new EmbeddedChannel(new Mqtt3Decoder(new Mqtt3PublishTestMessageDecoders()));
    }

    @AfterEach
    void tearDown() throws Exception {
        channel.close();
    }

    @ParameterizedTest
    @CsvSource({
            "true, true, 0", "true, false , 0", "false, true , 0", "false, false , 0", //all qos=0 combination
            "true, true, 1", "true, false , 1", "false, true , 1", "false, false , 1", "true, true, 2",
            "true, false , 2", "false, true , 2", "false, false , 2"
    })
    void decode_SUCESS(boolean retained, boolean isDup, int qos) throws Exception {
        final String topic = "Hello/World/Topic";
        final String payload = "Hallo World!";
        final int packetId = 1;

        ByteBuf byteBuf = createWellformedPublish(isDup, qos, retained, 1, topic.getBytes(), payload.getBytes());
        channel.writeInbound(byteBuf);
        final Mqtt3PublishInternal publishInternal = channel.readInbound();
        assertNotNull(publishInternal);
        //TODO equal topics
        //assertEquals(topic, publishInternal.getPublish().getTopic().);
        assertArrayEquals(payload.getBytes(), publishInternal.getPublish().getPayload().get());
        assertEquals(isDup, publishInternal.getPublish().isDup());
        assertEquals(qos, publishInternal.getPublish().getQos().getCode());
        if (qos == 0) {
            assertEquals(Mqtt3PublishImpl.PACKET_ID_NOT_SET, publishInternal.getPacketId());
        } else {
            assertEquals(packetId, publishInternal.getPacketId());
        }

    }


    @ParameterizedTest
    @CsvSource({
            "true, true, 0", "true, false , 0", "false, true , 0", "false, false , 0", //all qos=0 combination
            "true, true, 1", "true, false , 1", "false, true , 1", "false, false , 1", "true, true, 2",
            "true, false , 2", "false, true , 2", "false, false , 2"
    })
    void decode_SUCESS_NO_PAYLOAD(boolean retained, boolean isDup, int qos) throws Exception {
        final String topic = "Hello/World/Topic";
        final String payload = "";
        final int packetId = 1;

        ByteBuf byteBuf = createWellformedPublish(isDup, qos, retained, 1, topic.getBytes(), payload.getBytes());
        channel.writeInbound(byteBuf);
        final Mqtt3PublishInternal publishInternal = channel.readInbound();
        assertNotNull(publishInternal);
        //TODO equal topics
        //assertEquals(topic, publishInternal.getPublish().getTopic().);
        assertFalse(publishInternal.getPublish().getPayload().isPresent());
        assertEquals(isDup, publishInternal.getPublish().isDup());
        assertEquals(qos, publishInternal.getPublish().getQos().getCode());
        if (qos == 0) {
            assertEquals(Mqtt3PublishImpl.PACKET_ID_NOT_SET, publishInternal.getPacketId());
        } else {
            assertEquals(packetId, publishInternal.getPacketId());
        }

    }

    @ParameterizedTest
    @CsvSource({
            "true, true, 0", "true, false , 0", "false, true , 0", "false, false , 0", //all qos=0 combination
            "true, true, 1", "true, false , 1", "false, true , 1", "false, false , 1", "true, true, 2",
            "true, false , 2", "false, true , 2", "false, false , 2"
    })
    void decode_SUCESS_TOO_MUCH_BYTES(boolean retained, boolean isDup, int qos) throws Exception {
        final String topic = "Hello/World/Topic";
        final String payload = "test";
        final int packetId = 1;

        ByteBuf byteBuf = createWellformedPublish(isDup, qos, retained, 1, topic.getBytes(), payload.getBytes());
        byteBuf.writeBytes("not readable".getBytes());
        channel.writeInbound(byteBuf);
        final Mqtt3PublishInternal publishInternal = channel.readInbound();
        assertNotNull(publishInternal);
        assertArrayEquals(payload.getBytes(), publishInternal.getPublish().getPayload().get());
        assertEquals(isDup, publishInternal.getPublish().isDup());
        assertEquals(qos, publishInternal.getPublish().getQos().getCode());
        if (qos == 0) {
            assertEquals(Mqtt3PublishImpl.PACKET_ID_NOT_SET, publishInternal.getPacketId());
        } else {
            assertEquals(packetId, publishInternal.getPacketId());
        }

    }

    @ParameterizedTest
    @ValueSource(ints = {0x2b, 0x23})
        //the wildcoards 0x2b: + and 0x21: # must not be in topic
    void decode_INVALID_TOPIC(int invalidLetter) throws Exception {
        final byte[] topic = "beispieltopic".getBytes();
        byte invalidByte = (byte) invalidLetter;
        topic[3] = (byte) invalidLetter;
        final String payload = "example";
        final int packetId = 1;
        ByteBuf byteBuf = createWellformedPublish(false, 1, false, 1, topic, payload.getBytes());
        channel.writeInbound(byteBuf);
        final Mqtt3PublishInternal publishInternal = channel.readInbound();
        assertNull(publishInternal);
        assertFalse(channel.isOpen());
    }


    @ParameterizedTest
    @CsvSource({
            "true, true", "true, false", "false, true", "false, false", //all qos=0 combination
            "true, true", "true, false", "false, true", "false, false", "true, true", "true, false", "false, true",
            "false, false"
    })
    void decode_INVALID_QOS(boolean retained, boolean isDup) throws Exception {
        final String topic = "Hello/World/Topic";
        final String payload = "Hallo World!";
        final int qos = 3;
        ByteBuf byteBuf = createWellformedPublish(isDup, qos, retained, 1, topic.getBytes(), payload.getBytes());
        channel.writeInbound(byteBuf);
        final Mqtt3PublishInternal publishInternal = channel.readInbound();
        assertNull(publishInternal);
        assertFalse(channel.isOpen());
    }


    private static class Mqtt3PublishTestMessageDecoders implements Mqtt3MessageDecoders {
        @Nullable
        @Override
        public Mqtt3MessageDecoder get(final int code) {
            if (code == Mqtt3MessageType.PUBLISH.getCode()) {
                return new Mqtt3PublishDecoder();
            }
            return null;
        }
    }

}