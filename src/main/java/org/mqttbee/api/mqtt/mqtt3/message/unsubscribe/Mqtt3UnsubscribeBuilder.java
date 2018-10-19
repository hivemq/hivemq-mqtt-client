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

package org.mqttbee.api.mqtt.mqtt3.message.unsubscribe;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilterBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.message.unsubscribe.mqtt3.Mqtt3UnsubscribeView;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.FluentBuilder;
import org.mqttbee.util.MustNotBeImplementedUtil;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt3UnsubscribeBuilder<P> extends FluentBuilder<Mqtt3Unsubscribe, P> {

    private final @NotNull ImmutableList.Builder<MqttTopicFilterImpl> topicFiltersBuilder;
    private @Nullable MqttTopicFilterImpl firstTopicFilter;

    public Mqtt3UnsubscribeBuilder(final @Nullable Function<? super Mqtt3Unsubscribe, P> parentConsumer) {
        super(parentConsumer);
        topicFiltersBuilder = ImmutableList.builder();
    }

    Mqtt3UnsubscribeBuilder(final @NotNull Mqtt3Unsubscribe unsubscribe) {
        super(null);
        final Mqtt3UnsubscribeView unsubscribeView =
                MustNotBeImplementedUtil.checkNotImplemented(unsubscribe, Mqtt3UnsubscribeView.class);
        final ImmutableList<MqttTopicFilterImpl> topicFilters = unsubscribeView.getDelegate().getTopicFilters();
        topicFiltersBuilder = ImmutableList.builderWithExpectedSize(topicFilters.size() + 1);
        topicFiltersBuilder.addAll(topicFilters);
    }

    public @NotNull Mqtt3UnsubscribeBuilder<P> topicFilter(final @NotNull String topicFilter) {
        firstTopicFilter = MqttBuilderUtil.topicFilter(topicFilter);
        return this;
    }

    public @NotNull Mqtt3UnsubscribeBuilder<P> topicFilter(final @NotNull MqttTopicFilter topicFilter) {
        firstTopicFilter = MqttBuilderUtil.topicFilter(topicFilter);
        return this;
    }

    public @NotNull MqttTopicFilterBuilder<? extends Mqtt3UnsubscribeBuilder<P>> topicFilter() {
        return new MqttTopicFilterBuilder<>("", this::topicFilter);
    }

    public @NotNull Mqtt3UnsubscribeBuilder<P> addTopicFilter(final @NotNull String topicFilter) {
        topicFiltersBuilder.add(MqttBuilderUtil.topicFilter(topicFilter));
        return this;
    }

    public @NotNull Mqtt3UnsubscribeBuilder<P> addTopicFilter(final @NotNull MqttTopicFilter topicFilter) {
        topicFiltersBuilder.add(MqttBuilderUtil.topicFilter(topicFilter));
        return this;
    }

    public @NotNull MqttTopicFilterBuilder<? extends Mqtt3UnsubscribeBuilder<P>> addTopicFilter() {
        return new MqttTopicFilterBuilder<>("", this::addTopicFilter);
    }

    public @NotNull Mqtt3UnsubscribeBuilder<P> reverse(final @NotNull Mqtt3Subscribe subscribe) {
        final ImmutableList<? extends Mqtt3Subscription> subscriptions = subscribe.getSubscriptions();
        for (final Mqtt3Subscription subscription : subscriptions) {
            addTopicFilter(subscription.getTopicFilter());
        }
        return this;
    }

    @Override
    public @NotNull Mqtt3Unsubscribe build() {
        if (firstTopicFilter != null) {
            addTopicFilter(firstTopicFilter); // TODO add as first subscription #192
        }
        final ImmutableList<MqttTopicFilterImpl> topicFilters = topicFiltersBuilder.build();
        Preconditions.checkState(!topicFilters.isEmpty());
        return Mqtt3UnsubscribeView.of(topicFilters);
    }

}
