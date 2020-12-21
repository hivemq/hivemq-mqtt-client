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

package com.hivemq.client2.internal.mqtt.message.unsubscribe.mqtt3;

import com.hivemq.client2.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client2.internal.mqtt.message.unsubscribe.MqttUnsubscribe;
import com.hivemq.client2.internal.util.collections.ImmutableList;
import com.hivemq.client2.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
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
    public Mqtt3UnsubscribeViewBuilder.@NotNull Default extend() {
        return new Mqtt3UnsubscribeViewBuilder.Default(this);
    }

    private @NotNull String toAttributeString() {
        return "topicFilters=" + getTopicFilters();
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
        if (!(o instanceof Mqtt3UnsubscribeView)) {
            return false;
        }
        final Mqtt3UnsubscribeView that = (Mqtt3UnsubscribeView) o;

        return delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
