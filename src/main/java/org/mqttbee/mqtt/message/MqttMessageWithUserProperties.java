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

package org.mqttbee.mqtt.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;

import java.util.Optional;

public interface MqttMessageWithUserProperties extends MqttMessage {

    @NotNull
    MqttUserPropertiesImpl getUserProperties();


    /**
     * Base class for MQTT messages with optional User Properties.
     */
    abstract class MqttMessageWithUserPropertiesImpl implements MqttMessageWithUserProperties {

        private final MqttUserPropertiesImpl userProperties;

        protected MqttMessageWithUserPropertiesImpl(@NotNull final MqttUserPropertiesImpl userProperties) {
            this.userProperties = userProperties;
        }

        @NotNull
        public MqttUserPropertiesImpl getUserProperties() {
            return userProperties;
        }

    }


    /**
     * Base class for MQTT messages with an optional Reason String and optional User Properties.
     */
    abstract class MqttMessageWithReasonString extends MqttMessageWithUserPropertiesImpl {

        private final MqttUTF8StringImpl reasonString;

        MqttMessageWithReasonString(
                @Nullable final MqttUTF8StringImpl reasonString, @NotNull final MqttUserPropertiesImpl userProperties) {

            super(userProperties);
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
     * @param <R> the type of the Reason Code.
     */
    abstract class MqttMessageWithReasonCode<R extends Mqtt5ReasonCode> extends MqttMessageWithReasonString {

        private final R reasonCode;

        protected MqttMessageWithReasonCode(
                @NotNull final R reasonCode, @Nullable final MqttUTF8StringImpl reasonString,
                @NotNull final MqttUserPropertiesImpl userProperties) {

            super(reasonString, userProperties);
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
     * @param <R> the type of the Reason Code.
     */
    abstract class MqttMessageWithIdAndReasonCode<R extends Mqtt5ReasonCode> extends MqttMessageWithReasonCode<R> {

        private final int packetIdentifier;

        protected MqttMessageWithIdAndReasonCode(
                final int packetIdentifier, @NotNull final R reasonCode,
                @Nullable final MqttUTF8StringImpl reasonString, @NotNull final MqttUserPropertiesImpl userProperties) {

            super(reasonCode, reasonString, userProperties);
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
     * @param <R> the type of the Reason Codes.
     */
    abstract class MqttMessageWithIdAndReasonCodes<R extends Mqtt5ReasonCode> extends MqttMessageWithReasonString {

        private final int packetIdentifier;
        private final ImmutableList<R> reasonCodes;

        protected MqttMessageWithIdAndReasonCodes(
                final int packetIdentifier, @NotNull final ImmutableList<R> reasonCodes,
                @Nullable final MqttUTF8StringImpl reasonString, @NotNull final MqttUserPropertiesImpl userProperties) {

            super(reasonString, userProperties);
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
