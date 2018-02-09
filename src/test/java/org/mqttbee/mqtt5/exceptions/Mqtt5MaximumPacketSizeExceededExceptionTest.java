package org.mqttbee.mqtt5.exceptions;

import org.junit.jupiter.api.Test;
import org.mqttbee.api.mqtt5.message.auth.Mqtt5AuthReasonCode;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5AuthEncoder;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthImpl;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Mqtt5MaximumPacketSizeExceededExceptionTest {

    @Test
    void fillInStackTrace() {
        final Mqtt5MaximumPacketSizeExceededException exception = new Mqtt5MaximumPacketSizeExceededException(
                new Mqtt5AuthImpl(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION,
                        requireNonNull(Mqtt5UTF8StringImpl.from("test")), null, null,
                        Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5AuthEncoder.PROVIDER), 100);
        exception.fillInStackTrace();
        assertEquals(0, exception.getStackTrace().length);
    }
}