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

package org.mqttbee.mqtt.codec.decoder;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;

/**
 * Exception when decoding an invalid MQTT message.
 *
 * @author Silvio Giebl
 */
public class MqttDecoderException extends Exception {

    private final Mqtt5DisconnectReasonCode reasonCode;
    private Mqtt5MessageType messageType;

    /**
     * Creates a new Decoder exception with the given Disconnect reason code and message.
     *
     * @param reasonCode the reason code of the decoder exception.
     * @param message    the description of the decoder exception.
     */
    public MqttDecoderException(@NotNull final Mqtt5DisconnectReasonCode reasonCode, @NotNull final String message) {
        super(message);
        this.reasonCode = reasonCode;
    }

    /**
     * Creates a new Decoder exception with the Disconnect reason code {@link Mqtt5DisconnectReasonCode#MALFORMED_PACKET}.
     *
     * @param message the description of the decoder exception.
     */
    public MqttDecoderException(@NotNull final String message) {
        this(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    /**
     * Adds the MQTT message type which caused the decoder exception.
     *
     * @param messageType the MQTT message type which caused the decoder exception.
     */
    void setMessageType(@Nullable final Mqtt5MessageType messageType) {
        this.messageType = messageType;
    }

    /**
     * @return the reason code of the decoder exception.
     */
    @NotNull
    public Mqtt5DisconnectReasonCode getReasonCode() {
        return reasonCode;
    }

    @Override
    public String getMessage() {
        return "Decoder exception for " + ((messageType == null) ? "UNKNOWN" : messageType) + ": " + super.getMessage();
    }

}
