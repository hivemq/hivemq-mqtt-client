package org.mqttbee.mqtt5.message;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckReasonCode;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/**
 * @author Christian Hoff
 */
class Mqtt5PubAckReasonCodeTest {

    @ParameterizedTest
    @MethodSource("pubAckReasonCodeProvider")
    void getCode(final Mqtt5PubAckReasonCode reasonCode, final int expectedValue) {
        assertEquals(expectedValue, reasonCode.getCode());
    }

    private static Stream<Arguments> pubAckReasonCodeProvider() {
        return Stream.of(
                Arguments.of(Mqtt5PubAckReasonCode.SUCCESS, 0x00),
                Arguments.of(Mqtt5PubAckReasonCode.NO_MATCHING_SUBSCRIBERS, 0x10),
                Arguments.of(Mqtt5PubAckReasonCode.UNSPECIFIED_ERROR, 0x80),
                Arguments.of(Mqtt5PubAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR, 0x83),
                Arguments.of(Mqtt5PubAckReasonCode.NOT_AUTHORIZED, 0x87),
                Arguments.of(Mqtt5PubAckReasonCode.TOPIC_NAME_INVALID, 0x90),
                Arguments.of(Mqtt5PubAckReasonCode.PACKET_IDENTIFIER_IN_USE, 0x91),
                Arguments.of(Mqtt5PubAckReasonCode.QUOTA_EXCEEDED, 0x97),
                Arguments.of(Mqtt5PubAckReasonCode.PAYLOAD_FORMAT_INVALID, 0x99));
    }
}