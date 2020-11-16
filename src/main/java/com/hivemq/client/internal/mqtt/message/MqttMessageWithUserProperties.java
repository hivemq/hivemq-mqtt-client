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

package com.hivemq.client.internal.mqtt.message;

import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client.internal.util.StringUtil;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
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

    protected @NotNull String toAttributeString() {
        return userProperties.asList().isEmpty() ? "" : "userProperties=" + userProperties;
    }

    protected boolean partialEquals(final @NotNull MqttMessageWithUserProperties that) {
        return userProperties.equals(that.userProperties);
    }

    protected int partialHashCode() {
        return userProperties.hashCode();
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

        @Override
        protected @NotNull String toAttributeString() {
            return ((reasonString == null) ? super.toAttributeString() :
                    "reasonString=" + reasonString + StringUtil.prepend(", ", super.toAttributeString()));
        }

        protected boolean partialEquals(final @NotNull WithReason that) {
            return super.partialEquals(that) && Objects.equals(reasonString, that.reasonString);
        }

        protected int partialHashCode() {
            return 31 * super.partialHashCode() + Objects.hashCode(reasonString);
        }

        /**
         * Base class for MQTT messages with a Reason Code, an optional Reason String and optional User Properties.
         *
         * @param <R> the type of the Reason Code.
         */
        public abstract static class WithCode<R extends Mqtt5ReasonCode> extends WithReason {

            private final @NotNull R reasonCode;

            protected WithCode(
                    final @NotNull R reasonCode,
                    final @Nullable MqttUtf8StringImpl reasonString,
                    final @NotNull MqttUserPropertiesImpl userProperties) {

                super(reasonString, userProperties);
                this.reasonCode = reasonCode;
            }

            public @NotNull R getReasonCode() {
                return reasonCode;
            }

            protected boolean partialEquals(final @NotNull WithCode<R> that) {
                return super.partialEquals(that) && reasonCode.equals(that.reasonCode);
            }

            protected int partialHashCode() {
                return 31 * super.partialHashCode() + reasonCode.hashCode();
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
                        final int packetIdentifier,
                        final @NotNull R reasonCode,
                        final @Nullable MqttUtf8StringImpl reasonString,
                        final @NotNull MqttUserPropertiesImpl userProperties) {

                    super(reasonCode, reasonString, userProperties);
                    this.packetIdentifier = packetIdentifier;
                }

                @Override
                public int getPacketIdentifier() {
                    return packetIdentifier;
                }

                @Override
                protected @NotNull String toAttributeString() {
                    return "packetIdentifier=" + packetIdentifier + StringUtil.prepend(", ", super.toAttributeString());
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
            private final @NotNull ImmutableList<R> reasonCodes;

            protected WithCodesAndId(
                    final int packetIdentifier,
                    final @NotNull ImmutableList<R> reasonCodes,
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

            public @NotNull ImmutableList<R> getReasonCodes() {
                return reasonCodes;
            }

            @Override
            protected @NotNull String toAttributeString() {
                return "packetIdentifier=" + packetIdentifier + StringUtil.prepend(", ", super.toAttributeString());
            }

            protected boolean partialEquals(final @NotNull WithCodesAndId<R> that) {
                return super.partialEquals(that) && reasonCodes.equals(that.reasonCodes);
            }

            protected int partialHashCode() {
                return 31 * super.partialHashCode() + reasonCodes.hashCode();
            }
        }
    }
}
