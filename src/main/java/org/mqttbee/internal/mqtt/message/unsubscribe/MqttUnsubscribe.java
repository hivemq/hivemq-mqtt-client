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

package org.mqttbee.internal.mqtt.message.unsubscribe;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.internal.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.internal.mqtt.message.MqttMessageWithUserProperties;
import org.mqttbee.internal.util.StringUtil;
import org.mqttbee.internal.util.collections.ImmutableList;
import org.mqttbee.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttUnsubscribe extends MqttMessageWithUserProperties implements Mqtt5Unsubscribe {

    private final @NotNull ImmutableList<MqttTopicFilterImpl> topicFilters;

    public MqttUnsubscribe(
            final @NotNull ImmutableList<MqttTopicFilterImpl> topicFilters,
            final @NotNull MqttUserPropertiesImpl userProperties) {

        super(userProperties);
        this.topicFilters = topicFilters;
    }

    @Override
    public @NotNull ImmutableList<MqttTopicFilterImpl> getTopicFilters() {
        return topicFilters;
    }

    @Override
    public @NotNull MqttUnsubscribeBuilder.Default extend() {
        return new MqttUnsubscribeBuilder.Default(this);
    }

    public @NotNull MqttStatefulUnsubscribe createStateful(final int packetIdentifier) {
        return new MqttStatefulUnsubscribe(this, packetIdentifier);
    }

    @Override
    protected @NotNull String toAttributeString() {
        return "topicFilters=" + topicFilters + StringUtil.prepend(", ", super.toAttributeString());
    }

    @Override
    public @NotNull String toString() {
        return "MqttUnsubscribe{" + toAttributeString() + '}';
    }
}
