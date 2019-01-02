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

package org.mqttbee.internal.mqtt.message;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUtf8String;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.mqttbee.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.internal.mqtt.datatypes.MqttUtf8StringImpl;
import org.mqttbee.util.collections.ImmutableList;

import java.util.Optional;

/**
 * Base class for MQTT messages with optional User Properties.
 */
public abstract class MqttMessageWithUserProperties implements MqttMessage.WithUserProperties {

    private final @NotNull MqttUserPropertiesImpl userProperties;

    protected MqttMessageWithUserProperties(final @NotNull MqttUserPropertiesImpl userProperties) {
        this.userProperties = userProperties;
    }

    @Override
    public @NotNull MqttUserPropertiesImpl getUserProperties() {
        return userProperties;
    }

    /**
     * Base class for MQTT messages with an optional Reason String and optional User Properties.
     */
    public abstract static class WithReason extends MqttMessageWithUserProperties {

        private final @Nullable MqttUtf8StringImpl reasonString;

        WithReason(
                final @Nullable MqttUtf8StringImpl reasonString, final @NotNull MqttUserPropertiesImpl userProperties) {

            super(userProperties);
            this.reasonString = reasonString;
        }

        public @NotNull Optional<MqttUtf8String> getReasonString() {
            return Optional.ofNullable(reasonString);
        }

        public @Nullable MqttUtf8StringImpl getRawReasonString() {
            return reasonString;
        }

        /**
         * Base class for MQTT messages with a Reason Code, an optional Reason String and optional User Properties.
         *
         * @param <R> the type of the Reason Code.
         */
        public abstract static class WithCode<R extends Mqtt5ReasonCode> extends WithReason {

            private final @NotNull R reasonCode;

            protected WithCode(
                    final @NotNull R reasonCode, final @Nullable MqttUtf8StringImpl reasonString,
                    final @NotNull MqttUserPropertiesImpl userProperties) {

                super(reasonString, userProperties);
                this.reasonCode = reasonCode;
            }

            public @NotNull R getReasonCode() {
                return reasonCode;
            }

            /**
             * Base class for MQTT messages with a Packet Identifier, a Reason Code, an optional Reason String and
             * optional User Properties.
             *
             * @param <R> the type of the Reason Code.
             */
            public abstract static class WithId<R extends Mqtt5ReasonCode> extends WithCode<R>
                    implements MqttMessage.WithId {

                private final int packetIdentifier;

                protected WithId(
                        final int packetIdentifier, final @NotNull R reasonCode,
                        final @Nullable MqttUtf8StringImpl reasonString,
                        final @NotNull MqttUserPropertiesImpl userProperties) {

                    super(reasonCode, reasonString, userProperties);
                    this.packetIdentifier = packetIdentifier;
                }

                @Override
                public int getPacketIdentifier() {
                    return packetIdentifier;
                }
            }
        }

        /**
         * Base class for MQTT messages with a Packet Identifier, Reason Codes, an optional Reason String and optional
         * User Properties.
         *
         * @param <R> the type of the Reason Codes.
         */
        public abstract static class WithCodesAndId<R extends Mqtt5ReasonCode> extends WithReason
                implements MqttMessage.WithId {

            private final int packetIdentifier;
            private final @NotNull ImmutableList<@NotNull R> reasonCodes;

            protected WithCodesAndId(
                    final int packetIdentifier, final @NotNull ImmutableList<@NotNull R> reasonCodes,
                    final @Nullable MqttUtf8StringImpl reasonString,
                    final @NotNull MqttUserPropertiesImpl userProperties) {

                super(reasonString, userProperties);
                this.packetIdentifier = packetIdentifier;
                this.reasonCodes = reasonCodes;
            }

            @Override
            public int getPacketIdentifier() {
                return packetIdentifier;
            }

            public @NotNull ImmutableList<@NotNull R> getReasonCodes() {
                return reasonCodes;
            }
        }
    }
}
