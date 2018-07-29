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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilterBuilder;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5Subscription;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribe;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.FluentBuilder;
import org.mqttbee.util.MustNotBeImplementedUtil;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UnsubscribeBuilder<P> extends FluentBuilder<Mqtt5Unsubscribe, P> {

    private final ImmutableList.Builder<MqttTopicFilterImpl> topicFiltersBuilder;
    private MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    public Mqtt5UnsubscribeBuilder(@Nullable final Function<? super Mqtt5Unsubscribe, P> parentConsumer) {
        super(parentConsumer);
        topicFiltersBuilder = ImmutableList.builder();
    }

    Mqtt5UnsubscribeBuilder(@NotNull final Mqtt5Unsubscribe unsubscribe) {
        super(null);
        final MqttUnsubscribe unsubscribeImpl =
                MustNotBeImplementedUtil.checkNotImplemented(unsubscribe, MqttUnsubscribe.class);
        final ImmutableList<MqttTopicFilterImpl> topicFilters = unsubscribeImpl.getTopicFilters();
        topicFiltersBuilder = ImmutableList.builderWithExpectedSize(topicFilters.size() + 1);
        topicFiltersBuilder.addAll(topicFilters);
    }

    @NotNull
    public Mqtt5UnsubscribeBuilder<P> addTopicFilter(@NotNull final String topicFilter) {
        topicFiltersBuilder.add(MqttBuilderUtil.topicFilter(topicFilter));
        return this;
    }

    @NotNull
    public Mqtt5UnsubscribeBuilder<P> addTopicFilter(@NotNull final MqttTopicFilter topicFilter) {
        topicFiltersBuilder.add(MqttBuilderUtil.topicFilter(topicFilter));
        return this;
    }

    @NotNull
    public MqttTopicFilterBuilder<? extends Mqtt5UnsubscribeBuilder<P>> addTopicFilter() {
        return new MqttTopicFilterBuilder<>("", this::addTopicFilter);
    }

    @NotNull
    public Mqtt5UnsubscribeBuilder<P> reverse(@NotNull final Mqtt5Subscribe subscribe) {
        final ImmutableList<? extends Mqtt5Subscription> subscriptions = subscribe.getSubscriptions();
        for (final Mqtt5Subscription subscription : subscriptions) {
            addTopicFilter(subscription.getTopicFilter());
        }
        return this;
    }

    @NotNull
    public Mqtt5UnsubscribeBuilder<P> userProperties(@NotNull final Mqtt5UserProperties userProperties) {
        this.userProperties = MqttBuilderUtil.userProperties(userProperties);
        return this;
    }

    @NotNull
    public Mqtt5UserPropertiesBuilder<? extends Mqtt5UnsubscribeBuilder<P>> userProperties() {
        return new Mqtt5UserPropertiesBuilder<>(this::userProperties);
    }

    @NotNull
    @Override
    public Mqtt5Unsubscribe build() {
        final ImmutableList<MqttTopicFilterImpl> topicFilters = topicFiltersBuilder.build();
        Preconditions.checkState(!topicFilters.isEmpty());
        return new MqttUnsubscribe(topicFilters, userProperties);
    }

}
