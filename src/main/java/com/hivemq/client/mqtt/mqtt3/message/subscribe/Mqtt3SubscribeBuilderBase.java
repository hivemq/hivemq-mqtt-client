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

package com.hivemq.client.mqtt.mqtt3.message.subscribe;

import com.hivemq.client.annotations.CheckReturnValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Builder base for a {@link Mqtt3Subscribe}.
 *
 * @param <C> the type of the complete builder.
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt3SubscribeBuilderBase<C extends Mqtt3SubscribeBuilderBase.Complete<C>> {

    /**
     * Adds a {@link Mqtt3Subscription} to the {@link Mqtt3Subscribe#getSubscriptions() list of subscriptions}. At least
     * one subscription is mandatory.
     *
     * @param subscription the subscription.
     * @return the builder that is now complete as at least one subscription is set.
     */
    @CheckReturnValue
    @NotNull C addSubscription(@NotNull Mqtt3Subscription subscription);

    /**
     * Fluent counterpart of {@link #addSubscription(Mqtt3Subscription)}.
     * <p>
     * Calling {@link Mqtt3SubscriptionBuilder.Nested.Complete#applySubscription()} on the returned builder has the same
     * effect as calling {@link #addSubscription(Mqtt3Subscription)} with the result of {@link
     * Mqtt3SubscriptionBuilder.Complete#build()}.
     *
     * @return the fluent builder for the subscription.
     * @see #addSubscription(Mqtt3Subscription)
     */
    @CheckReturnValue
    Mqtt3SubscriptionBuilder.@NotNull Nested<? extends C> addSubscription();

    /**
     * Adds {@link Mqtt3Subscription}s to the {@link Mqtt3Subscribe#getSubscriptions() list of subscriptions}. At least
     * one subscription is mandatory.
     *
     * @param subscriptions the subscriptions.
     * @return the builder that is now complete as at least one subscription is set.
     * @since 1.2
     */
    @CheckReturnValue
    @NotNull C addSubscriptions(@NotNull Mqtt3Subscription @NotNull ... subscriptions);

    /**
     * Adds a collection of {@link Mqtt3Subscription}s to the {@link Mqtt3Subscribe#getSubscriptions() list of
     * subscriptions}. At least one subscription is mandatory.
     *
     * @param subscriptions the collection of subscriptions.
     * @return the builder that is now complete as at least one subscription is set.
     * @since 1.2
     */
    @CheckReturnValue
    @NotNull C addSubscriptions(@NotNull Collection<@NotNull ? extends Mqtt3Subscription> subscriptions);

    /**
     * Adds a stream of {@link Mqtt3Subscription}s to the {@link Mqtt3Subscribe#getSubscriptions() list of
     * subscriptions}. At least one subscription is mandatory.
     *
     * @param subscriptions the stream of subscriptions.
     * @return the builder that is now complete as at least one subscription is set.
     * @since 1.2
     */
    @CheckReturnValue
    @NotNull C addSubscriptions(@NotNull Stream<@NotNull ? extends Mqtt3Subscription> subscriptions);

    /**
     * {@link Mqtt3SubscribeBuilderBase} that is complete which means all mandatory fields are set.
     *
     * @param <C> the type of the complete builder.
     */
    @ApiStatus.NonExtendable
    interface Complete<C extends Mqtt3SubscribeBuilderBase.Complete<C>> extends Mqtt3SubscribeBuilderBase<C> {}

    /**
     * {@link Mqtt3SubscribeBuilderBase} that provides additional methods for the first subscription.
     *
     * @param <C>  the type of the complete builder.
     * @param <SC> the type of the complete start builder.
     */
    // @formatter:off
    @ApiStatus.NonExtendable
    interface Start<
            C extends Mqtt3SubscribeBuilderBase.Complete<C>,
            SC extends Mqtt3SubscribeBuilderBase.Start.Complete<C, SC>>
            extends Mqtt3SubscribeBuilderBase<C>, Mqtt3SubscriptionBuilderBase<SC> {
    // @formatter:on

        /**
         * {@link Start} that is complete which means all mandatory fields are set.
         *
         * @param <C>  the type of the complete builder.
         * @param <SC> the type of the complete start builder.
         */
        // @formatter:off
        @ApiStatus.NonExtendable
        interface Complete<
                C extends Mqtt3SubscribeBuilderBase.Complete<C>,
                SC extends Mqtt3SubscribeBuilderBase.Start.Complete<C, SC>>
                extends Mqtt3SubscribeBuilderBase.Start<C, SC>, Mqtt3SubscribeBuilderBase.Complete<C>,
                        Mqtt3SubscriptionBuilderBase.Complete<SC> {}
        // @formatter:on
    }
}
