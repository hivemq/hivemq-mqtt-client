/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.client.internal.mqtt.codec.decoder.mqtt3;

import com.google.common.primitives.Bytes;
import com.hivemq.client.internal.mqtt.codec.decoder.MqttMessageDecoders;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubAck;
import com.hivemq.client.mqtt.mqtt3.message.Mqtt3MessageType;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubAckReasonCode;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class Mqtt3SubAckDecoderTest extends AbstractMqtt3DecoderTest {

    private static final @NotNull byte[] WELLFORMED_SUBACK_BEGIN = {
            //   type, flags
            (byte) 0b1001_0000,
            //remaining length
            0b0000_0110
    };
    private static final @NotNull byte[] MALFORMED_SUBACK_BEGIN_WRONG_FLAGS = {
            //   type, flags
            (byte) 0b1001_0010,
            //remaining length
            0b0000_0010
    };
    private static final @NotNull byte[] MALFORMED_SUBACK_BEGIN_TOO_SHORT_LENGTH = {
            //   type, flags
            (byte) 0b1001_0010,
            //remaining length
            0b0000_0010
    };

    private static final @NotNull byte[] REASON_CODE_QOS_0 = {0x00};
    private static final @NotNull byte[] REASON_CODE_QOS_1 = {0x01};
    private static final @NotNull byte[] REASON_CODE_QOS_2 = {0x02};
    private static final @NotNull byte[] REASON_CODE_FAILURE = {(byte) 0x80};
    private static final @NotNull byte[] REASON_CODE_MALFORMED = {0x8, 0x2};

    private static final @NotNull byte[] MAX_PACKET_ID = {(byte) 0b1111_1111, (byte) 0b1111_1111};
    private static final @NotNull byte[] MIN_PACKET_ID = {0x00, 0x00};

    Mqtt3SubAckDecoderTest() {
        super(new MqttMessageDecoders() {{
            decoders[Mqtt3MessageType.SUBACK.getCode()] = new Mqtt3SubAckDecoder();
        }});
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_SUCESS(final boolean useMaxPacketId) {
        final byte[] encoded = Bytes.concat(WELLFORMED_SUBACK_BEGIN, (useMaxPacketId ? MAX_PACKET_ID : MIN_PACKET_ID),
                REASON_CODE_QOS_0, REASON_CODE_QOS_1, REASON_CODE_QOS_2, REASON_CODE_FAILURE);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final MqttSubAck subAckImpl = channel.readInbound();
        assertNotNull(subAckImpl);
        assertEquals(useMaxPacketId ? 65535 : 0, subAckImpl.getPacketIdentifier());
        assertEquals(Mqtt5SubAckReasonCode.GRANTED_QOS_0, subAckImpl.getReasonCodes().get(0));
        // Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_0
        assertEquals(Mqtt5SubAckReasonCode.GRANTED_QOS_1, subAckImpl.getReasonCodes().get(1));
        // Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_1
        assertEquals(Mqtt5SubAckReasonCode.GRANTED_QOS_2, subAckImpl.getReasonCodes().get(2));
        // Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_2
        assertEquals(Mqtt5SubAckReasonCode.UNSPECIFIED_ERROR, subAckImpl.getReasonCodes().get(3));
        // Mqtt3SubAckReturnCode.FAILURE
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "2", "3"})
    void decode_ERROR_CASES(final int errorcase) throws Exception {
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
        final MqttSubAck subAckImpl = channel.readInbound();
        assertFalse(channel.isOpen());
        assertNull(subAckImpl);
    }
}