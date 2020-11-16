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

package com.hivemq.client.internal.mqtt.message.unsubscribe;

import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.message.MqttMessageWithUserProperties;
import com.hivemq.client.internal.util.StringUtil;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
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
    public MqttUnsubscribeBuilder.@NotNull Default extend() {
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

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttUnsubscribe)) {
            return false;
        }
        final MqttUnsubscribe that = (MqttUnsubscribe) o;

        return partialEquals(that) && topicFilters.equals(that.topicFilters);
    }

    @Override
    public int hashCode() {
        return 31 * partialHashCode() + topicFilters.hashCode();
    }
}
