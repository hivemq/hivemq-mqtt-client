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

package org.mqttbee.internal.mqtt.message.connect;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.internal.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictionsBuilder;
import org.mqttbee.util.Checks;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttConnectRestrictionsBuilder<B extends MqttConnectRestrictionsBuilder<B>> {

    private int receiveMaximum = MqttConnectRestrictions.DEFAULT_RECEIVE_MAXIMUM;
    private int maximumPacketSize = MqttConnectRestrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT;
    private int topicAliasMaximum = MqttConnectRestrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM;

    abstract @NotNull B self();

    public @NotNull B receiveMaximum(final int receiveMaximum) {
        this.receiveMaximum = Checks.unsignedShort(receiveMaximum, "Receive maximum");
        return self();
    }

    public @NotNull B maximumPacketSize(final int maximumPacketSize) {
        if ((maximumPacketSize <= 0) || (maximumPacketSize > MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT)) {
            throw new IllegalArgumentException("Maximum packet size must not exceed the value range of ]0, " +
                    MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT + "], but was " + maximumPacketSize + ".");
        }
        this.maximumPacketSize = maximumPacketSize;
        return self();
    }

    public @NotNull B topicAliasMaximum(final int topicAliasMaximum) {
        this.topicAliasMaximum = Checks.unsignedShort(topicAliasMaximum, "Topic alias maximum");
        return self();
    }

    public @NotNull MqttConnectRestrictions build() {
        return new MqttConnectRestrictions(receiveMaximum, maximumPacketSize, topicAliasMaximum);
    }

    public static class Default extends MqttConnectRestrictionsBuilder<Default>
            implements Mqtt5ConnectRestrictionsBuilder {

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends MqttConnectRestrictionsBuilder<Nested<P>>
            implements Mqtt5ConnectRestrictionsBuilder.Nested<P> {

        public Nested(final @NotNull Function<? super MqttConnectRestrictions, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        private final @NotNull Function<? super MqttConnectRestrictions, P> parentConsumer;

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyRestrictions() {
            return parentConsumer.apply(build());
        }
    }
}
