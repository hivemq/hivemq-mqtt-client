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

package com.hivemq.client2.internal.mqtt.codec.decoder;

import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.jetbrains.annotations.NotNull;

/**
 * Exception when decoding an invalid MQTT message.
 *
 * @author Silvio Giebl
 */
public class MqttDecoderException extends Exception {

    private final @NotNull Mqtt5DisconnectReasonCode reasonCode;

    /**
     * Creates a new Decoder exception with the given Disconnect reason code and message.
     *
     * @param reasonCode the reason code of the decoder exception.
     * @param message    the description of the decoder exception.
     */
    public MqttDecoderException(final @NotNull Mqtt5DisconnectReasonCode reasonCode, final @NotNull String message) {
        super(message, null, false, false);
        this.reasonCode = reasonCode;
    }

    /**
     * Creates a new Decoder exception with the Disconnect reason code {@link Mqtt5DisconnectReasonCode#MALFORMED_PACKET}.
     *
     * @param message the description of the decoder exception.
     */
    public MqttDecoderException(final @NotNull String message) {
        this(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, message);
    }

    /**
     * @return the reason code of the decoder exception.
     */
    public @NotNull Mqtt5DisconnectReasonCode getReasonCode() {
        return reasonCode;
    }
}
