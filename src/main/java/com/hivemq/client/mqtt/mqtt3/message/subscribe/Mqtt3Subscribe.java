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

package com.hivemq.client.mqtt.mqtt3.message.subscribe;

import com.hivemq.client.internal.mqtt.message.subscribe.mqtt3.Mqtt3SubscribeViewBuilder;
import com.hivemq.client.mqtt.mqtt3.message.Mqtt3Message;
import com.hivemq.client.mqtt.mqtt3.message.Mqtt3MessageType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * MQTT 3 Subscribe message. This message is translated from and to an MQTT 3 SUBSCRIBE packet.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt3Subscribe extends Mqtt3Message {

    /**
     * Creates a builder for a Subscribe message.
     *
     * @return the created builder.
     */
    static Mqtt3SubscribeBuilder.@NotNull Start builder() {
        return new Mqtt3SubscribeViewBuilder.Default();
    }

    /**
     * @return the {@link Mqtt3Subscription Subscriptions} of this Subscribe message. The list contains at least one
     *         Subscription.
     */
    @Unmodifiable @NotNull List<@NotNull ? extends Mqtt3Subscription> getSubscriptions();

    @Override
    default @NotNull Mqtt3MessageType getType() {
        return Mqtt3MessageType.SUBSCRIBE;
    }

    /**
     * Creates a builder for extending this Subscribe message.
     *
     * @return the created builder.
     */
    Mqtt3SubscribeBuilder.@NotNull Complete extend();
}
