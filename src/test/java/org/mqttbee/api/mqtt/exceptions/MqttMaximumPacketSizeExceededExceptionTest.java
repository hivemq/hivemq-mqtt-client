package org.mqttbee.api.mqtt.exceptions;

import org.junit.jupiter.api.Test;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5AuthReasonCode;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5AuthEncoder;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.auth.MqttAuth;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MqttMaximumPacketSizeExceededExceptionTest {

    @Test
    void fillInStackTrace() {
        final MqttMaximumPacketSizeExceededException exception = new MqttMaximumPacketSizeExceededException(
                new MqttAuth(
                        Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION,
                        requireNonNull(MqttUTF8StringImpl.from("test")), null, null,
                        MqttUserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5AuthEncoder.PROVIDER), 100);
        exception.fillInStackTrace();
        assertEquals(0, exception.getStackTrace().length);
    }
}