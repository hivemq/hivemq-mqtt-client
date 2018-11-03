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

package org.mqttbee.mqtt.message.subscribe.mqtt3;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3SubscribeBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3SubscribeBuilderBase;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
// @formatter:off
public abstract class Mqtt3SubscribeBuilderImpl<
            B extends Mqtt3SubscribeBuilderBase<B, C>,
            C extends B,
            F extends Mqtt3SubscribeBuilderBase.First<F, FC>,
            FC extends F>
        implements Mqtt3SubscribeBuilderBase<B, C>,
                   Mqtt3SubscribeBuilderBase.Complete<B, C>,
                   Mqtt3SubscribeBuilderBase.First<F, FC>,
                   Mqtt3SubscribeBuilderBase.First.Complete<B, C, F, FC>,
                   Mqtt3SubscribeBuilderBase.Start<B, C, F, FC> {
// @formatter:on

    private final @NotNull ImmutableList.Builder<MqttSubscription> subscriptionsBuilder;
    private @Nullable Mqtt3SubscriptionBuilderImpl.Impl firstSubscriptionBuilder;

    protected Mqtt3SubscribeBuilderImpl() {
        subscriptionsBuilder = ImmutableList.builder();
    }

    Mqtt3SubscribeBuilderImpl(final @NotNull Mqtt3Subscribe subscribe) {
        final MqttSubscribe mqttSubscribe = Mqtt3SubscribeView.delegate(subscribe);
        final ImmutableList<MqttSubscription> subscriptions = mqttSubscribe.getSubscriptions();
        subscriptionsBuilder = ImmutableList.builderWithExpectedSize(subscriptions.size() + 1);
        subscriptionsBuilder.addAll(subscriptions);
    }

    protected abstract @NotNull C self();

    protected abstract @NotNull FC self2();

    @Override
    public @NotNull C addSubscription(final @NotNull Mqtt3Subscription subscription) {
        buildFirstSubscription();
        subscriptionsBuilder.add(Mqtt3SubscriptionView.delegate(subscription));
        return self();
    }

    private @NotNull Mqtt3SubscriptionBuilderImpl.Impl getFirstSubscriptionBuilder() {
        if (firstSubscriptionBuilder == null) {
            firstSubscriptionBuilder = new Mqtt3SubscriptionBuilderImpl.Impl();
        }
        return firstSubscriptionBuilder;
    }

    private void buildFirstSubscription() {
        if (firstSubscriptionBuilder != null) {
            subscriptionsBuilder.add(Mqtt3SubscriptionView.delegate(firstSubscriptionBuilder.build()));
            firstSubscriptionBuilder = null;
        }
    }

    @Override
    public @NotNull FC topicFilter(final @NotNull String topicFilter) {
        getFirstSubscriptionBuilder().topicFilter(topicFilter);
        return self2();
    }

    @Override
    public @NotNull FC topicFilter(final @NotNull MqttTopicFilter topicFilter) {
        getFirstSubscriptionBuilder().topicFilter(topicFilter);
        return self2();
    }

    @Override
    public @NotNull FC qos(final @NotNull MqttQos qos) {
        getFirstSubscriptionBuilder().qos(qos);
        return self2();
    }

    public @NotNull Mqtt3Subscribe build() {
        buildFirstSubscription();
        final ImmutableList<MqttSubscription> subscriptions = subscriptionsBuilder.build();
        if (subscriptions.isEmpty()) {
            throw new IllegalStateException("At least one subscription must be added.");
        }
        return Mqtt3SubscribeView.of(subscriptions);
    }

    // @formatter:off
    public static class Impl
            extends Mqtt3SubscribeBuilderImpl<
                        Mqtt3SubscribeBuilder,
                        Mqtt3SubscribeBuilder.Complete,
                        Mqtt3SubscribeBuilder.First,
                        Mqtt3SubscribeBuilder.First.Complete>
            implements Mqtt3SubscribeBuilder,
                       Mqtt3SubscribeBuilder.Complete,
                       Mqtt3SubscribeBuilder.First,
                       Mqtt3SubscribeBuilder.First.Complete,
                       Mqtt3SubscribeBuilder.Start {
    // @formatter:on

        public Impl() {}

        public Impl(final @NotNull Mqtt3Subscribe subscribe) {
            super(subscribe);
        }

        @Override
        protected @NotNull Mqtt3SubscribeBuilder.Complete self() {
            return this;
        }

        @Override
        protected @NotNull Mqtt3SubscribeBuilder.First.Complete self2() {
            return this;
        }
    }

    // @formatter:off
    public static class NestedImpl<P>
            extends Mqtt3SubscribeBuilderImpl<
                        Mqtt3SubscribeBuilder.Nested<P>,
                        Mqtt3SubscribeBuilder.Nested.Complete<P>,
                        Mqtt3SubscribeBuilder.Nested.First<P>,
                        Mqtt3SubscribeBuilder.Nested.First.Complete<P>>
            implements Mqtt3SubscribeBuilder.Nested<P>,
                       Mqtt3SubscribeBuilder.Nested.Complete<P>,
                       Mqtt3SubscribeBuilder.Nested.First<P>,
                       Mqtt3SubscribeBuilder.Nested.First.Complete<P>,
                       Mqtt3SubscribeBuilder.Nested.Start<P> {
    // @formatter:on

        private final @NotNull Function<? super Mqtt3Subscribe, P> parentConsumer;

        public NestedImpl(final @NotNull Function<? super Mqtt3Subscribe, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        protected @NotNull Mqtt3SubscribeBuilder.Nested.Complete<P> self() {
            return this;
        }

        @Override
        protected @NotNull Mqtt3SubscribeBuilder.Nested.First.Complete<P> self2() {
            return this;
        }

        @Override
        public @NotNull P applySubscribe() {
            return parentConsumer.apply(build());
        }
    }

    // @formatter:off
    public static class SendImpl<P>
            extends Mqtt3SubscribeBuilderImpl<
                        Mqtt3SubscribeBuilder.Send<P>,
                        Mqtt3SubscribeBuilder.Send.Complete<P>,
                        Mqtt3SubscribeBuilder.Send.First<P>,
                        Mqtt3SubscribeBuilder.Send.First.Complete<P>>
            implements Mqtt3SubscribeBuilder.Send<P>,
                       Mqtt3SubscribeBuilder.Send.Complete<P>,
                       Mqtt3SubscribeBuilder.Send.First<P>,
                       Mqtt3SubscribeBuilder.Send.First.Complete<P>,
                       Mqtt3SubscribeBuilder.Send.Start<P> {
    // @formatter:on

        private final @NotNull Function<? super Mqtt3Subscribe, P> parentConsumer;

        public SendImpl(final @NotNull Function<? super Mqtt3Subscribe, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        protected @NotNull Mqtt3SubscribeBuilder.Send.Complete<P> self() {
            return this;
        }

        @Override
        protected @NotNull Mqtt3SubscribeBuilder.Send.First.Complete<P> self2() {
            return this;
        }

        @Override
        public @NotNull P send() {
            return parentConsumer.apply(build());
        }
    }
}
