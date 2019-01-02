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

package org.mqttbee.internal.mqtt.message.unsubscribe.mqtt3;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.internal.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.internal.mqtt.message.unsubscribe.MqttUnsubscribe;
import org.mqttbee.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import org.mqttbee.util.collections.ImmutableList;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3UnsubscribeView implements Mqtt3Unsubscribe {

    private static @NotNull MqttUnsubscribe delegate(final @NotNull ImmutableList<MqttTopicFilterImpl> topicFilters) {
        return new MqttUnsubscribe(topicFilters, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

    static @NotNull Mqtt3UnsubscribeView of(final @NotNull ImmutableList<MqttTopicFilterImpl> topicFilters) {
        return new Mqtt3UnsubscribeView(delegate(topicFilters));
    }

    public static @NotNull Mqtt3UnsubscribeView of(final @NotNull MqttUnsubscribe delegate) {
        return new Mqtt3UnsubscribeView(delegate);
    }

    private final @NotNull MqttUnsubscribe delegate;

    private Mqtt3UnsubscribeView(final @NotNull MqttUnsubscribe delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull ImmutableList<MqttTopicFilterImpl> getTopicFilters() {
        return delegate.getTopicFilters();
    }

    public @NotNull MqttUnsubscribe getDelegate() {
        return delegate;
    }

    @Override
    public @NotNull Mqtt3UnsubscribeViewBuilder.Default extend() {
        return new Mqtt3UnsubscribeViewBuilder.Default(this);
    }
}
