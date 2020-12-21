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

package com.hivemq.client2.mqtt.mqtt5.message.subscribe;

import com.hivemq.client2.internal.mqtt.message.subscribe.MqttSubscribeBuilder;
import com.hivemq.client2.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5Message;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5MessageType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * MQTT 5 Subscribe message. This message is translated from and to an MQTT 5 SUBSCRIBE packet.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5Subscribe extends Mqtt5Message {

    /**
     * Creates a builder for a Subscribe message.
     *
     * @return the created builder.
     */
    static Mqtt5SubscribeBuilder.@NotNull Start builder() {
        return new MqttSubscribeBuilder.Default();
    }

    /**
     * @return the {@link Mqtt5Subscription Subscriptions} of this Subscribe message. The list contains at least one
     *         Subscription.
     */
    @Unmodifiable @NotNull List<@NotNull ? extends Mqtt5Subscription> getSubscriptions();

    /**
     * @return the optional user properties of this Subscribe message.
     */
    @NotNull Mqtt5UserProperties getUserProperties();

    @Override
    default @NotNull Mqtt5MessageType getType() {
        return Mqtt5MessageType.SUBSCRIBE;
    }

    /**
     * Creates a builder for extending this Subscribe message.
     *
     * @return the created builder.
     */
    Mqtt5SubscribeBuilder.@NotNull Complete extend();
}
