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

package org.mqttbee.mqtt.mqtt5.message.subscribe;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.internal.mqtt.message.subscribe.MqttSubscribeBuilder;
import org.mqttbee.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.mqtt.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt.mqtt5.message.Mqtt5MessageType;

import java.util.List;

/**
 * MQTT 5 SUBSCRIBE packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5Subscribe extends Mqtt5Message {

    static @NotNull Mqtt5SubscribeBuilder.Start builder() {
        return new MqttSubscribeBuilder.Default();
    }

    /**
     * @return the {@link Mqtt5Subscription}s of this SUBSCRIBE packet. The list contains at least one subscription.
     */
    @Immutable @NotNull List<@NotNull ? extends Mqtt5Subscription> getSubscriptions();

    /**
     * @return the optional user properties of this SUBSCRIBE packet.
     */
    @NotNull Mqtt5UserProperties getUserProperties();

    @Override
    default @NotNull Mqtt5MessageType getType() {
        return Mqtt5MessageType.SUBSCRIBE;
    }

    @NotNull Mqtt5SubscribeBuilder.Complete extend();
}
