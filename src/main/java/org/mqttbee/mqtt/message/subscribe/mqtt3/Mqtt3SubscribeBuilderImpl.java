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
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.*;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
// @formatter:off
public abstract class Mqtt3SubscribeBuilderImpl<
            C extends Mqtt3SubscribeBuilderBase.Complete<C>,
            S extends Mqtt3SubscribeBuilderBase.Start<C, S, SC>,
            SC extends S>
        implements Mqtt3SubscribeBuilderBase<C>,
                   Mqtt3SubscribeBuilderBase.Complete<C>,
                   Mqtt3SubscribeBuilderBase.Start<C, S, SC>,
                   Mqtt3SubscribeBuilderBase.Start.Complete<C, S, SC> {
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

    protected abstract @NotNull SC self2();

    @Override
    public @NotNull C addSubscription(final @NotNull Mqtt3Subscription subscription) {
        buildFirstSubscription();
        subscriptionsBuilder.add(Mqtt3SubscriptionView.delegate(subscription));
        return self();
    }

    private @NotNull Mqtt3SubscriptionBuilder getFirstSubscriptionBuilder() {
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
    public @NotNull SC topicFilter(final @NotNull String topicFilter) {
        getFirstSubscriptionBuilder().topicFilter(topicFilter);
        return self2();
    }

    @Override
    public @NotNull SC topicFilter(final @NotNull MqttTopicFilter topicFilter) {
        getFirstSubscriptionBuilder().topicFilter(topicFilter);
        return self2();
    }

    @Override
    public @NotNull SC qos(final @NotNull MqttQos qos) {
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
                        Mqtt3SubscribeBuilder.Complete,
                        Mqtt3SubscribeBuilder.Start,
                        Mqtt3SubscribeBuilder.Start.Complete>
            implements Mqtt3SubscribeBuilder,
                       Mqtt3SubscribeBuilder.Complete,
                       Mqtt3SubscribeBuilder.Start,
                       Mqtt3SubscribeBuilder.Start.Complete {
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
        protected @NotNull Mqtt3SubscribeBuilder.Start.Complete self2() {
            return this;
        }
    }

    // @formatter:off
    public static class NestedImpl<P>
            extends Mqtt3SubscribeBuilderImpl<
                        Mqtt3SubscribeBuilder.Nested.Complete<P>,
                        Mqtt3SubscribeBuilder.Nested.Start<P>,
                        Mqtt3SubscribeBuilder.Nested.Start.Complete<P>>
            implements Mqtt3SubscribeBuilder.Nested<P>,
                       Mqtt3SubscribeBuilder.Nested.Complete<P>,
                       Mqtt3SubscribeBuilder.Nested.Start<P>,
                       Mqtt3SubscribeBuilder.Nested.Start.Complete<P> {
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
        protected @NotNull Mqtt3SubscribeBuilder.Nested.Start.Complete<P> self2() {
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
                        Mqtt3SubscribeBuilder.Send.Complete<P>,
                        Mqtt3SubscribeBuilder.Send.Start<P>,
                        Mqtt3SubscribeBuilder.Send.Start.Complete<P>>
            implements Mqtt3SubscribeBuilder.Send<P>,
                       Mqtt3SubscribeBuilder.Send.Complete<P>,
                       Mqtt3SubscribeBuilder.Send.Start<P>,
                       Mqtt3SubscribeBuilder.Send.Start.Complete<P> {
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
        protected @NotNull Mqtt3SubscribeBuilder.Send.Start.Complete<P> self2() {
            return this;
        }

        @Override
        public @NotNull P send() {
            return parentConsumer.apply(build());
        }
    }
}
