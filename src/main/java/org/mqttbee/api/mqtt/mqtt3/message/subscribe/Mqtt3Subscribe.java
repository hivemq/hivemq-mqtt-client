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

package org.mqttbee.api.mqtt.mqtt3.message.subscribe;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3Message;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt.message.subscribe.mqtt3.Mqtt3SubscribeViewBuilder;

/**
 * MQTT 3 SUBSCRIBE packet.
 */
@DoNotImplement
public interface Mqtt3Subscribe extends Mqtt3Message {

    static @NotNull Mqtt3SubscribeBuilder.Start builder() {
        return new Mqtt3SubscribeViewBuilder.Default();
    }

    static @NotNull Mqtt3SubscribeBuilder.Complete extend(final @NotNull Mqtt3Subscribe subscribe) {
        return new Mqtt3SubscribeViewBuilder.Default(subscribe);
    }

    /**
     * @return the {@link Mqtt3Subscription}s of this SUBSCRIBE packet. The list contains at least one subscription.
     */
    @NotNull ImmutableList<? extends Mqtt3Subscription> getSubscriptions();

    @Override
    default @NotNull Mqtt3MessageType getType() {
        return Mqtt3MessageType.SUBSCRIBE;
    }

}
