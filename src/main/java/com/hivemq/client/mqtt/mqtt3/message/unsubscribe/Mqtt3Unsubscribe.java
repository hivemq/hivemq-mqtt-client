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

package com.hivemq.client.mqtt.mqtt3.message.unsubscribe;

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.annotations.Immutable;
import com.hivemq.client.internal.mqtt.message.unsubscribe.mqtt3.Mqtt3UnsubscribeViewBuilder;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt3.message.Mqtt3Message;
import com.hivemq.client.mqtt.mqtt3.message.Mqtt3MessageType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * MQTT 3 Unsubscribe message. This message is translated from and to an MQTT 3 UNSUBSCRIBE packet.
 *
 * @author Daniel Krüger
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt3Unsubscribe extends Mqtt3Message {

    /**
     * Creates a builder for an Unsubscribe message.
     *
     * @return the created builder.
     */
    static @NotNull Mqtt3UnsubscribeBuilder.Start builder() {
        return new Mqtt3UnsubscribeViewBuilder.Default();
    }

    /**
     * @return the Topic Filters of this Unsubscribe message. The list contains at least one Topic Filter.
     */
    @Immutable @NotNull List<@NotNull ? extends MqttTopicFilter> getTopicFilters();

    @Override
    default @NotNull Mqtt3MessageType getType() {
        return Mqtt3MessageType.UNSUBSCRIBE;
    }

    /**
     * Creates a builder for extending this Unsubscribe message.
     *
     * @return the created builder.
     */
    @NotNull Mqtt3UnsubscribeBuilder.Complete extend();
}
