/*
 * Copyright 2018 The MQTT Bee project
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
 *
 */

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
                        MqttUserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5AuthEncoder.PROVIDER), 200, 100);
        exception.fillInStackTrace();
        assertEquals(0, exception.getStackTrace().length);
    }
}