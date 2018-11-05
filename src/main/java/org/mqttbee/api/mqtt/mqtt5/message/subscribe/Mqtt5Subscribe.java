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

package org.mqttbee.api.mqtt.mqtt5.message.subscribe;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5Message;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeBuilder;

/**
 * MQTT 5 SUBSCRIBE packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5Subscribe extends Mqtt5Message {

    static @NotNull Mqtt5SubscribeBuilder builder() {
        return new MqttSubscribeBuilder.Default();
    }

    static @NotNull Mqtt5SubscribeBuilder.Complete extend(final @NotNull Mqtt5Subscribe subscribe) {
        return new MqttSubscribeBuilder.Default(subscribe);
    }

    /**
     * @return the {@link Mqtt5Subscription}s of this SUBSCRIBE packet. The list contains at least one subscription.
     */
    @NotNull ImmutableList<? extends Mqtt5Subscription> getSubscriptions();

    /**
     * @return the optional user properties of this SUBSCRIBE packet.
     */
    @NotNull Mqtt5UserProperties getUserProperties();

    @Override
    default @NotNull Mqtt5MessageType getType() {
        return Mqtt5MessageType.SUBSCRIBE;
    }

}
