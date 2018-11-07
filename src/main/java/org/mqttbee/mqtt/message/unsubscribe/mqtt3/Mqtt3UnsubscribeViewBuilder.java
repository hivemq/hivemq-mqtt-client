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

package org.mqttbee.mqtt.message.unsubscribe.mqtt3;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilterBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3UnsubscribeBuilder;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.MustNotBeImplementedUtil;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class Mqtt3UnsubscribeViewBuilder<B extends Mqtt3UnsubscribeViewBuilder<B>> {

    private final @NotNull ImmutableList.Builder<MqttTopicFilterImpl> topicFiltersBuilder;

    Mqtt3UnsubscribeViewBuilder() {
        topicFiltersBuilder = ImmutableList.builder();
    }

    Mqtt3UnsubscribeViewBuilder(final @NotNull Mqtt3Unsubscribe unsubscribe) {
        final Mqtt3UnsubscribeView unsubscribeView =
                MustNotBeImplementedUtil.checkNotImplemented(unsubscribe, Mqtt3UnsubscribeView.class);
        final ImmutableList<MqttTopicFilterImpl> topicFilters = unsubscribeView.getDelegate().getTopicFilters();
        topicFiltersBuilder = ImmutableList.builderWithExpectedSize(topicFilters.size() + 1);
        topicFiltersBuilder.addAll(topicFilters);
    }

    abstract @NotNull B self();

    public @NotNull B addTopicFilter(final @NotNull String topicFilter) {
        topicFiltersBuilder.add(MqttBuilderUtil.topicFilter(topicFilter));
        return self();
    }

    public @NotNull B addTopicFilter(final @NotNull MqttTopicFilter topicFilter) {
        topicFiltersBuilder.add(MqttBuilderUtil.topicFilter(topicFilter));
        return self();
    }

    public @NotNull MqttTopicFilterBuilder<B> addTopicFilter() {
        return new MqttTopicFilterBuilder<>(this::addTopicFilter);
    }

    public @NotNull B reverse(final @NotNull Mqtt3Subscribe subscribe) {
        final ImmutableList<? extends Mqtt3Subscription> subscriptions = subscribe.getSubscriptions();
        for (final Mqtt3Subscription subscription : subscriptions) {
            addTopicFilter(subscription.getTopicFilter());
        }
        return self();
    }

    public @NotNull B topicFilter(final @NotNull String topicFilter) {
        return addTopicFilter(topicFilter);
    }

    public @NotNull B topicFilter(final @NotNull MqttTopicFilter topicFilter) {
        return addTopicFilter(topicFilter);
    }

    public @NotNull MqttTopicFilterBuilder<B> topicFilter() {
        return new MqttTopicFilterBuilder<>(this::topicFilter);
    }

    public @NotNull Mqtt3UnsubscribeView build() {
        final ImmutableList<MqttTopicFilterImpl> topicFilters = topicFiltersBuilder.build();
        if (topicFilters.isEmpty()) {
            throw new IllegalStateException("At least one topic filter must be added.");
        }
        return Mqtt3UnsubscribeView.of(topicFilters);
    }

    public static class Default extends Mqtt3UnsubscribeViewBuilder<Default>
            implements Mqtt3UnsubscribeBuilder.Complete, Mqtt3UnsubscribeBuilder.Start {

        public Default() {}

        public Default(final @NotNull Mqtt3Unsubscribe unsubscribe) {
            super(unsubscribe);
        }

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends Mqtt3UnsubscribeViewBuilder<Nested<P>>
            implements Mqtt3UnsubscribeBuilder.Nested.Complete<P>, Mqtt3UnsubscribeBuilder.Nested.Start<P> {

        private final @NotNull Function<? super Mqtt3UnsubscribeView, P> parentConsumer;

        public Nested(final @NotNull Function<? super Mqtt3UnsubscribeView, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyUnsubscribe() {
            return parentConsumer.apply(build());
        }
    }

    public static class Send<P> extends Mqtt3UnsubscribeViewBuilder<Send<P>>
            implements Mqtt3UnsubscribeBuilder.Send.Complete<P>, Mqtt3UnsubscribeBuilder.Send.Start<P> {

        private final @NotNull Function<? super Mqtt3UnsubscribeView, P> parentConsumer;

        public Send(final @NotNull Function<? super Mqtt3UnsubscribeView, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Send<P> self() {
            return this;
        }

        @Override
        public @NotNull P send() {
            return parentConsumer.apply(build());
        }
    }
}
