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

package org.mqttbee.api.mqtt.mqtt5.message.unsubscribe;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.DoNotImplement;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5Message;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;

import java.util.function.Function;

/**
 * MQTT 5 UNSUBSCRIBE packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5Unsubscribe extends Mqtt5Message {

    @NotNull
    static Mqtt5UnsubscribeBuilder<Void> builder() {
        return new Mqtt5UnsubscribeBuilder<>((Function<Mqtt5Unsubscribe, Void>) null);
    }

    @NotNull
    static Mqtt5UnsubscribeBuilder<Void> extend(@NotNull final Mqtt5Unsubscribe unsubscribe) {
        return new Mqtt5UnsubscribeBuilder<>(unsubscribe);
    }

    /**
     * @return the Topic Filters of this UNSUBSCRIBE packet. The list contains at least one Topic Filter.
     */
    @NotNull
    ImmutableList<? extends MqttTopicFilter> getTopicFilters();

    /**
     * @return the optional user properties of this UNSUBSCRIBE packet.
     */
    @NotNull
    Mqtt5UserProperties getUserProperties();

    @NotNull
    @Override
    default Mqtt5MessageType getType() {
        return Mqtt5MessageType.UNSUBSCRIBE;
    }

}
