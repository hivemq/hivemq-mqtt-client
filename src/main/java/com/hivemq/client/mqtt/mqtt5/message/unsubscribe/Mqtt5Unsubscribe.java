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

package com.hivemq.client.mqtt.mqtt5.message.unsubscribe;

import com.hivemq.client.annotations.Immutable;
import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttUnsubscribeBuilder;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5Message;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5MessageType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * MQTT 5 Unsubscribe message. This message is translated from and to an MQTT 5 UNSUBSCRIBE packet.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5Unsubscribe extends Mqtt5Message {

    /**
     * Creates a builder for an Unsubscribe message.
     *
     * @return the created builder.
     */
    static Mqtt5UnsubscribeBuilder.@NotNull Start builder() {
        return new MqttUnsubscribeBuilder.Default();
    }

    /**
     * @return the Topic Filters of this Unsubscribe message. The list contains at least one Topic Filter.
     */
    @Immutable @NotNull List<@NotNull ? extends MqttTopicFilter> getTopicFilters();

    /**
     * @return the optional user properties of this Unsubscribe message.
     */
    @NotNull Mqtt5UserProperties getUserProperties();

    @Override
    default @NotNull Mqtt5MessageType getType() {
        return Mqtt5MessageType.UNSUBSCRIBE;
    }

    /**
     * Creates a builder for extending this Unsubscribe message.
     *
     * @return the created builder.
     */
    Mqtt5UnsubscribeBuilder.@NotNull Complete extend();
}
