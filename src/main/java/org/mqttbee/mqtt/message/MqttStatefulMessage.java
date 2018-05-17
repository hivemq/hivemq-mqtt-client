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

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;

import static org.mqttbee.mqtt.message.MqttMessageWithUserProperties.MqttMessageWithUserPropertiesImpl;

/**
 * Base class for MQTT messages with state-specific data.
 *
 * @param <M> the type of the stateless MQTT message.
 * @author Silvio Giebl
 */
public abstract class MqttStatefulMessage<M extends MqttMessageWithUserPropertiesImpl>
        implements MqttMessageWithUserProperties {

    private final M statelessMessage;

    protected MqttStatefulMessage(@NotNull final M statelessMessage) {
        this.statelessMessage = statelessMessage;
    }

    @NotNull
    @Override
    public Mqtt5MessageType getType() {
        return statelessMessage.getType();
    }

    @NotNull
    @Override
    public MqttUserPropertiesImpl getUserProperties() {
        return statelessMessage.getUserProperties();
    }

    /**
     * @return the stateless MQTT message.
     */
    @NotNull
    public M getStatelessMessage() {
        return statelessMessage;
    }


    /**
     * Base class for MQTT messages with a packet identifier and other state-specific data.
     *
     * @param <M> the type of the stateless MQTT message.
     * @author Silvio Giebl
     */
    public abstract static class MqttStatefulMessageWithId<M extends MqttMessageWithUserPropertiesImpl>
            extends MqttStatefulMessage<M> {

        private final int packetIdentifier;

        protected MqttStatefulMessageWithId(@NotNull final M wrapped, final int packetIdentifier) {
            super(wrapped);
            this.packetIdentifier = packetIdentifier;
        }

        public int getPacketIdentifier() {
            return packetIdentifier;
        }

    }

}
