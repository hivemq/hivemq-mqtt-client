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
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.MqttWrappedMessage;

import javax.annotation.concurrent.Immutable;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttUnsubscribe extends MqttWrappedMessage implements Mqtt5Unsubscribe {

    private final ImmutableList<MqttTopicFilterImpl> topicFilters;

    public MqttUnsubscribe(
            @NotNull final ImmutableList<MqttTopicFilterImpl> topicFilters,
            @NotNull final MqttUserPropertiesImpl userProperties) {

        super(userProperties);
        this.topicFilters = topicFilters;
    }

    @NotNull
    @Override
    public ImmutableList<MqttTopicFilterImpl> getTopicFilters() {
        return topicFilters;
    }

    public MqttUnsubscribeWrapper wrap(final int packetIdentifier) {
        return new MqttUnsubscribeWrapper(this, packetIdentifier);
    }

}
