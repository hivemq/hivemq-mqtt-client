/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.message.unsubscribe;

import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImplBuilder;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImplBuilder;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscription;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubscribeBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Silvio Giebl
 */
public abstract class MqttUnsubscribeBuilder<B extends MqttUnsubscribeBuilder<B>> {

    private final @NotNull ImmutableList.Builder<MqttTopicFilterImpl> topicFiltersBuilder;
    private @NotNull MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    MqttUnsubscribeBuilder() {
        topicFiltersBuilder = ImmutableList.builder();
    }

    MqttUnsubscribeBuilder(final @NotNull MqttUnsubscribe unsubscribe) {
        final ImmutableList<MqttTopicFilterImpl> topicFilters = unsubscribe.getTopicFilters();
        topicFiltersBuilder = ImmutableList.builder(topicFilters.size() + 1);
        topicFiltersBuilder.addAll(topicFilters);
    }

    abstract @NotNull B self();

    public @NotNull B addTopicFilter(final @Nullable String topicFilter) {
        topicFiltersBuilder.add(MqttTopicFilterImpl.of(topicFilter));
        return self();
    }

    public @NotNull B addTopicFilter(final @Nullable MqttTopicFilter topicFilter) {
        topicFiltersBuilder.add(MqttChecks.topicFilter(topicFilter));
        return self();
    }

    public @NotNull B addTopicFilters(final @Nullable Collection<MqttTopicFilter> topicFilters) {
        Checks.notNull(topicFilters, "Topic Filters");
        Checks.atLeastOneElement(topicFilters, "Topic Filters");
        Checks.elementsNotNull(topicFilters, "Topic Filters");

        topicFilters.forEach(topicFilter -> topicFiltersBuilder.add(MqttChecks.topicFilter(topicFilter)));
        return self();
    }

    public @NotNull B addTopicFilters(final @Nullable MqttTopicFilter... topicFilters) {
        Checks.notNull(topicFilters, "Topic Filters");
        Checks.atLeastOneElement(topicFilters, "Topic Filters");
        Checks.elementsNotNull(topicFilters, "Topic Filters");

        for (final MqttTopicFilter topicFilter : topicFilters) {
            topicFiltersBuilder.add(MqttChecks.topicFilter(topicFilter));
        }

        return self();
    }

    public @NotNull B addTopicFilters(final @Nullable Stream<MqttTopicFilter> topicFilters) {
        Checks.notNull(topicFilters, "Topic Filters");

        final List<MqttTopicFilter> topicFilterList = topicFilters.collect(Collectors.toList());

        Checks.atLeastOneElement(topicFilterList, "Topic Filters");
        Checks.elementsNotNull(topicFilterList, "Topic Filters");

        topicFilterList.forEach(topicFilter -> topicFiltersBuilder.add(MqttChecks.topicFilter(topicFilter)));
        return self();
    }

    public @NotNull MqttTopicFilterImplBuilder.Nested<B> addTopicFilter() {
        return new MqttTopicFilterImplBuilder.Nested<>(this::addTopicFilter);
    }

    public @NotNull B reverse(final @Nullable Mqtt5Subscribe subscribe) {
        final ImmutableList<MqttSubscription> subscriptions = MqttChecks.subscribe(subscribe).getSubscriptions();
        for (final MqttSubscription subscription : subscriptions) {
            topicFiltersBuilder.add(subscription.getTopicFilter());
        }
        return self();
    }

    public @NotNull B userProperties(final @Nullable Mqtt5UserProperties userProperties) {
        this.userProperties = MqttChecks.userProperties(userProperties);
        return self();
    }

    public @NotNull MqttUserPropertiesImplBuilder.Nested<B> userProperties() {
        return new MqttUserPropertiesImplBuilder.Nested<>(userProperties, this::userProperties);
    }

    public @NotNull B topicFilter(final @Nullable String topicFilter) {
        return addTopicFilter(topicFilter);
    }

    public @NotNull B topicFilter(final @Nullable MqttTopicFilter topicFilter) {
        return addTopicFilter(topicFilter);
    }

    public @NotNull MqttTopicFilterImplBuilder.Nested<B> topicFilter() {
        return new MqttTopicFilterImplBuilder.Nested<>(this::topicFilter);
    }

    public @NotNull MqttUnsubscribe build() {
        final ImmutableList<MqttTopicFilterImpl> topicFilters = topicFiltersBuilder.build();
        if (topicFilters.isEmpty()) {
            throw new IllegalStateException("At least one topic filter must be added.");
        }
        return new MqttUnsubscribe(topicFilters, userProperties);
    }

    public static class Default extends MqttUnsubscribeBuilder<Default>
            implements Mqtt5UnsubscribeBuilder.Complete, Mqtt5UnsubscribeBuilder.Start {

        public Default() {}

        Default(final @NotNull MqttUnsubscribe unsubscribe) {
            super(unsubscribe);
        }

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends MqttUnsubscribeBuilder<Nested<P>>
            implements Mqtt5UnsubscribeBuilder.Nested.Complete<P>, Mqtt5UnsubscribeBuilder.Nested.Start<P> {

        private final @NotNull Function<? super MqttUnsubscribe, P> parentConsumer;

        public Nested(final @NotNull Function<? super MqttUnsubscribe, P> parentConsumer) {
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

    public static class Send<P> extends MqttUnsubscribeBuilder<Send<P>>
            implements Mqtt5UnsubscribeBuilder.Send.Complete<P>, Mqtt5UnsubscribeBuilder.Send.Start<P> {

        private final @NotNull Function<? super MqttUnsubscribe, P> parentConsumer;

        public Send(final @NotNull Function<? super MqttUnsubscribe, P> parentConsumer) {
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
