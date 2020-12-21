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

package com.hivemq.client2.internal.mqtt.message;

import com.hivemq.client2.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5MessageType;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for MQTT messages with state-specific data.
 *
 * @param <M> the type of the stateless MQTT message.
 * @author Silvio Giebl
 */
public abstract class MqttStatefulMessage<M extends MqttMessageWithUserProperties>
        implements MqttMessage.WithUserProperties {

    private final @NotNull M statelessMessage;

    protected MqttStatefulMessage(final @NotNull M statelessMessage) {
        this.statelessMessage = statelessMessage;
    }

    @Override
    public @NotNull Mqtt5MessageType getType() {
        return statelessMessage.getType();
    }

    @Override
    public @NotNull MqttUserPropertiesImpl getUserProperties() {
        return statelessMessage.getUserProperties();
    }

    /**
     * @return the stateless MQTT message.
     */
    public @NotNull M stateless() {
        return statelessMessage;
    }

    protected @NotNull String toAttributeString() {
        return "stateless=" + statelessMessage;
    }

    /**
     * Base class for MQTT messages with a packet identifier and other state-specific data.
     *
     * @param <M> the type of the stateless MQTT message.
     * @author Silvio Giebl
     */
    public abstract static class WithId<M extends MqttMessageWithUserProperties> extends MqttStatefulMessage<M>
            implements MqttMessage.WithId {

        private final int packetIdentifier;

        protected WithId(final @NotNull M statelessMessage, final int packetIdentifier) {
            super(statelessMessage);
            this.packetIdentifier = packetIdentifier;
        }

        @Override
        public int getPacketIdentifier() {
            return packetIdentifier;
        }

        @Override
        protected @NotNull String toAttributeString() {
            return super.toAttributeString() + ", packetIdentifier=" + packetIdentifier;
        }
    }
}
