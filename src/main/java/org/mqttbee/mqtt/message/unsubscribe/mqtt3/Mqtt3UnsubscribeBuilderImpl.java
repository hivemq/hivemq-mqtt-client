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
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3UnsubscribeBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3UnsubscribeBuilderBase;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.MustNotBeImplementedUtil;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
// @formatter:off
public abstract class Mqtt3UnsubscribeBuilderImpl<
            B extends Mqtt3UnsubscribeBuilderBase<B, C>,
            C extends B>
        implements Mqtt3UnsubscribeBuilderBase<B, C>,
                   Mqtt3UnsubscribeBuilderBase.Complete<B, C>,
                   Mqtt3UnsubscribeBuilderBase.Start<B, C> {
// @formatter:on

    private final @NotNull ImmutableList.Builder<MqttTopicFilterImpl> topicFiltersBuilder;

    Mqtt3UnsubscribeBuilderImpl() {
        topicFiltersBuilder = ImmutableList.builder();
    }

    Mqtt3UnsubscribeBuilderImpl(final @NotNull Mqtt3Unsubscribe unsubscribe) {
        final Mqtt3UnsubscribeView unsubscribeView =
                MustNotBeImplementedUtil.checkNotImplemented(unsubscribe, Mqtt3UnsubscribeView.class);
        final ImmutableList<MqttTopicFilterImpl> topicFilters = unsubscribeView.getDelegate().getTopicFilters();
        topicFiltersBuilder = ImmutableList.builderWithExpectedSize(topicFilters.size() + 1);
        topicFiltersBuilder.addAll(topicFilters);
    }

    abstract @NotNull C self();

    @Override
    public @NotNull C addTopicFilter(final @NotNull String topicFilter) {
        topicFiltersBuilder.add(MqttBuilderUtil.topicFilter(topicFilter));
        return self();
    }

    @Override
    public @NotNull C addTopicFilter(final @NotNull MqttTopicFilter topicFilter) {
        topicFiltersBuilder.add(MqttBuilderUtil.topicFilter(topicFilter));
        return self();
    }

    @Override
    public @NotNull C reverse(final @NotNull Mqtt3Subscribe subscribe) {
        final ImmutableList<? extends Mqtt3Subscription> subscriptions = subscribe.getSubscriptions();
        for (final Mqtt3Subscription subscription : subscriptions) {
            addTopicFilter(subscription.getTopicFilter());
        }
        return self();
    }

    @Override
    public @NotNull C topicFilter(final @NotNull String topicFilter) {
        return addTopicFilter(topicFilter);
    }

    @Override
    public @NotNull C topicFilter(final @NotNull MqttTopicFilter topicFilter) {
        return addTopicFilter(topicFilter);
    }

    public @NotNull Mqtt3Unsubscribe build() {
        final ImmutableList<MqttTopicFilterImpl> topicFilters = topicFiltersBuilder.build();
        if (topicFilters.isEmpty()) {
            throw new IllegalStateException("At least one topic filter must be added.");
        }
        return Mqtt3UnsubscribeView.of(topicFilters);
    }

    // @formatter:off
    public static class Impl
            extends Mqtt3UnsubscribeBuilderImpl<
                        Mqtt3UnsubscribeBuilder,
                        Mqtt3UnsubscribeBuilder.Complete>
            implements Mqtt3UnsubscribeBuilder,
                       Mqtt3UnsubscribeBuilder.Complete,
                       Mqtt3UnsubscribeBuilder.Start {
    // @formatter:on

        public Impl() {}

        public Impl(final @NotNull Mqtt3Unsubscribe unsubscribe) {
            super(unsubscribe);
        }

        @Override
        @NotNull Impl self() {
            return this;
        }
    }

    // @formatter:off
    public static class NestedImpl<P>
            extends Mqtt3UnsubscribeBuilderImpl<
                        Mqtt3UnsubscribeBuilder.Nested<P>,
                        Mqtt3UnsubscribeBuilder.Nested.Complete<P>>
            implements Mqtt3UnsubscribeBuilder.Nested<P>,
                       Mqtt3UnsubscribeBuilder.Nested.Complete<P>,
                       Mqtt3UnsubscribeBuilder.Nested.Start<P> {
    // @formatter:on

        private final @NotNull Function<? super Mqtt3Unsubscribe, P> parentConsumer;

        public NestedImpl(final @NotNull Function<? super Mqtt3Unsubscribe, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Mqtt3UnsubscribeBuilder.Nested.Complete<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyUnsubscribe() {
            return parentConsumer.apply(build());
        }
    }

    // @formatter:off
    public static class SendImpl<P>
            extends Mqtt3UnsubscribeBuilderImpl<
                        Mqtt3UnsubscribeBuilder.Send<P>,
                        Mqtt3UnsubscribeBuilder.Send.Complete<P>>
            implements Mqtt3UnsubscribeBuilder.Send<P>,
                       Mqtt3UnsubscribeBuilder.Send.Complete<P>,
                       Mqtt3UnsubscribeBuilder.Send.Start<P> {
    // @formatter:on

        private final @NotNull Function<? super Mqtt3Unsubscribe, P> parentConsumer;

        public SendImpl(final @NotNull Function<? super Mqtt3Unsubscribe, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Mqtt3UnsubscribeBuilder.Send.Complete<P> self() {
            return this;
        }

        @Override
        public @NotNull P send() {
            return parentConsumer.apply(build());
        }
    }
}
