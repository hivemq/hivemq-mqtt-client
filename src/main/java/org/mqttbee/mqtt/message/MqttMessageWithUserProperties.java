/*
 *
 * *
 *  * Copyright 2018 The MQTT Bee project.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.mqttbee.mqtt.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;

import java.util.Optional;

/**
 * Base class for MQTT messages with optional User Properties.
 *
 * @param <M> the type of the MQTT message.
 * @param <P> the type of the encoder provider for the MQTT message.
 */
public abstract class MqttMessageWithUserProperties< //
        M extends MqttMessageWithUserProperties<M, P>, //
        P extends MqttMessageEncoderProvider<M>> //
        extends MqttMessageWithEncoder<M, P> {

    private final MqttUserPropertiesImpl userProperties;

    MqttMessageWithUserProperties(
            @NotNull final MqttUserPropertiesImpl userProperties, @NotNull final P encoderProvider) {

        super(encoderProvider);
        this.userProperties = userProperties;
    }

    @NotNull
    public MqttUserPropertiesImpl getUserProperties() {
        return userProperties;
    }


    /**
     * Base class for MQTT messages with an optional Reason String and optional User Properties.
     *
     * @param <M> the type of the MQTT message.
     * @param <P> the type of the encoder provider for the MQTT message.
     */
    public abstract static class MqttMessageWithReasonString< //
            M extends MqttMessageWithReasonString<M, P>, //
            P extends MqttMessageEncoderProvider<M>> //
            extends MqttMessageWithUserProperties<M, P> {

        private final MqttUTF8StringImpl reasonString;

        MqttMessageWithReasonString(
                @Nullable final MqttUTF8StringImpl reasonString, @NotNull final MqttUserPropertiesImpl userProperties,
                @NotNull final P encoderProvider) {

            super(userProperties, encoderProvider);
            this.reasonString = reasonString;
        }

        @NotNull
        public Optional<MqttUTF8String> getReasonString() {
            return Optional.ofNullable(reasonString);
        }

        @Nullable
        public MqttUTF8StringImpl getRawReasonString() {
            return reasonString;
        }

    }


    /**
     * Base class for MQTT messages with a Reason Code, an optional Reason String and optional User Properties.
     *
     * @param <M> the type of the MQTT message.
     * @param <R> the type of the Reason Code.
     * @param <P> the type of the encoder provider for the MQTT message.
     */
    public abstract static class MqttMessageWithReasonCode< //
            M extends MqttMessageWithReasonCode<M, R, P>, //
            R extends Mqtt5ReasonCode, //
            P extends MqttMessageEncoderProvider<M>> //
            extends MqttMessageWithReasonString<M, P> {

        private final R reasonCode;

        protected MqttMessageWithReasonCode(
                @NotNull final R reasonCode, @Nullable final MqttUTF8StringImpl reasonString,
                @NotNull final MqttUserPropertiesImpl userProperties, @NotNull final P encoderProvider) {

            super(reasonString, userProperties, encoderProvider);
            this.reasonCode = reasonCode;
        }

        @NotNull
        public R getReasonCode() {
            return reasonCode;
        }

    }


    /**
     * Base class for MQTT messages with a Packet Identifier, a Reason Code, an optional Reason String and optional User
     * Properties.
     *
     * @param <M> the type of the MQTT message.
     * @param <R> the type of the Reason Code.
     * @param <P> the type of the encoder provider for the MQTT message.
     */
    public abstract static class MqttMessageWithIdAndReasonCode< //
            M extends MqttMessageWithIdAndReasonCode<M, R, P>, //
            R extends Mqtt5ReasonCode, //
            P extends MqttMessageEncoderProvider<M>> //
            extends MqttMessageWithReasonCode<M, R, P> {

        private final int packetIdentifier;

        protected MqttMessageWithIdAndReasonCode(
                final int packetIdentifier, @NotNull final R reasonCode,
                @Nullable final MqttUTF8StringImpl reasonString, @NotNull final MqttUserPropertiesImpl userProperties,
                @NotNull final P encoderProvider) {

            super(reasonCode, reasonString, userProperties, encoderProvider);
            this.packetIdentifier = packetIdentifier;
        }

        public int getPacketIdentifier() {
            return packetIdentifier;
        }

    }


    /**
     * Base class for MQTT messages with a Packet Identifier, Reason Codes, an optional Reason String and optional User
     * Properties.
     *
     * @param <M> the type of the MQTT message.
     * @param <R> the type of the Reason Codes.
     * @param <P> the type of the encoder provider for the MQTT message.
     */
    public abstract static class MqttMessageWithIdAndReasonCodes< //
            M extends MqttMessageWithIdAndReasonCodes<M, R, P>, //
            R extends Mqtt5ReasonCode, //
            P extends MqttMessageEncoderProvider<M>> //
            extends MqttMessageWithReasonString<M, P> {

        private final int packetIdentifier;
        private final ImmutableList<R> reasonCodes;

        protected MqttMessageWithIdAndReasonCodes(
                final int packetIdentifier, @NotNull final ImmutableList<R> reasonCodes,
                @Nullable final MqttUTF8StringImpl reasonString, @NotNull final MqttUserPropertiesImpl userProperties,
                @NotNull final P encoderProvider) {

            super(reasonString, userProperties, encoderProvider);
            this.packetIdentifier = packetIdentifier;
            this.reasonCodes = reasonCodes;
        }

        public int getPacketIdentifier() {
            return packetIdentifier;
        }

        @NotNull
        public ImmutableList<R> getReasonCodes() {
            return reasonCodes;
        }

    }

}
