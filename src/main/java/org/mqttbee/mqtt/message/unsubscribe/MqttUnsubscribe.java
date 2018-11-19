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

package org.mqttbee.mqtt.message.unsubscribe;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.MqttMessageWithUserProperties;

import javax.annotation.concurrent.Immutable;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttUnsubscribe extends MqttMessageWithUserProperties implements Mqtt5Unsubscribe {

    private final @NotNull ImmutableList<@NotNull MqttTopicFilterImpl> topicFilters;

    public MqttUnsubscribe(
            final @NotNull ImmutableList<@NotNull MqttTopicFilterImpl> topicFilters,
            final @NotNull MqttUserPropertiesImpl userProperties) {

        super(userProperties);
        this.topicFilters = topicFilters;
    }

    @Override
    public @NotNull ImmutableList<@NotNull MqttTopicFilterImpl> getTopicFilters() {
        return topicFilters;
    }

    public @NotNull MqttStatefulUnsubscribe createStateful(final int packetIdentifier) {
        return new MqttStatefulUnsubscribe(this, packetIdentifier);
    }
}
