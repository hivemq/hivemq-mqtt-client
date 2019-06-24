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

package com.hivemq.client.internal.mqtt.message.unsubscribe.mqtt3;

import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImplBuilder;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscription;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.Mqtt3UnsubscribeBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Silvio Giebl
 */
public abstract class Mqtt3UnsubscribeViewBuilder<B extends Mqtt3UnsubscribeViewBuilder<B>> {

    private final @NotNull ImmutableList.Builder<MqttTopicFilterImpl> topicFiltersBuilder;

    Mqtt3UnsubscribeViewBuilder() {
        topicFiltersBuilder = ImmutableList.builder();
    }

    Mqtt3UnsubscribeViewBuilder(final @NotNull Mqtt3UnsubscribeView unsubscribe) {
        final ImmutableList<MqttTopicFilterImpl> topicFilters = unsubscribe.getDelegate().getTopicFilters();
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

    public @NotNull MqttTopicFilterImplBuilder.Nested<B> addTopicFilter() {
        return new MqttTopicFilterImplBuilder.Nested<>(this::addTopicFilter);
    }

    public @NotNull B addTopicFilters(final @Nullable MqttTopicFilter @Nullable ... topicFilters) {
        Checks.notNull(topicFilters, "Topic Filters");
        topicFiltersBuilder.ensureFree(topicFilters.length);
        for (final MqttTopicFilter topicFilter : topicFilters) {
            addTopicFilter(topicFilter);
        }
        ensureAtLeastOneSubscription();
        return self();
    }

    public @NotNull B addTopicFilters(final @Nullable Collection<@Nullable MqttTopicFilter> topicFilters) {
        Checks.notNull(topicFilters, "Topic Filters");
        topicFiltersBuilder.ensureFree(topicFilters.size());
        topicFilters.forEach(this::addTopicFilter);
        ensureAtLeastOneSubscription();
        return self();
    }

    public @NotNull B addTopicFilters(final @Nullable Stream<@Nullable MqttTopicFilter> topicFilters) {
        Checks.notNull(topicFilters, "Topic Filters");
        topicFilters.forEach(this::addTopicFilter);
        ensureAtLeastOneSubscription();
        return self();
    }

    public @NotNull B reverse(final @Nullable Mqtt3Subscribe subscribe) {
        final ImmutableList<MqttSubscription> subscriptions = MqttChecks.subscribe(subscribe).getSubscriptions();
        for (final MqttSubscription subscription : subscriptions) {
            topicFiltersBuilder.add(subscription.getTopicFilter());
        }
        return self();
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

    private void ensureAtLeastOneSubscription() {
        if (topicFiltersBuilder.getSize() == 0) {
            throw new IllegalStateException("At least one topic filter must be added.");
        }
    }

    public @NotNull Mqtt3UnsubscribeView build() {
        ensureAtLeastOneSubscription();
        return Mqtt3UnsubscribeView.of(topicFiltersBuilder.build());
    }

    public static class Default extends Mqtt3UnsubscribeViewBuilder<Default>
            implements Mqtt3UnsubscribeBuilder.Complete, Mqtt3UnsubscribeBuilder.Start {

        public Default() {}

        Default(final @NotNull Mqtt3UnsubscribeView unsubscribe) {
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

    public static class SendVoid extends Mqtt3UnsubscribeViewBuilder<SendVoid>
            implements Mqtt3UnsubscribeBuilder.SendVoid.Complete, Mqtt3UnsubscribeBuilder.SendVoid.Start {

        private final @NotNull Consumer<? super Mqtt3UnsubscribeView> parentConsumer;

        public SendVoid(final @NotNull Consumer<? super Mqtt3UnsubscribeView> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull SendVoid self() {
            return this;
        }

        @Override
        public void send() {
            parentConsumer.accept(build());
        }
    }
}
