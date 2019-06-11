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

package com.hivemq.client.mqtt.mqtt3.message.subscribe;

import com.hivemq.client.annotations.DoNotImplement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Builder base for a {@link Mqtt3Subscribe}.
 *
 * @param <C> the type of the complete builder.
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt3SubscribeBuilderBase<C extends Mqtt3SubscribeBuilderBase<C>> {

    /**
     * Adds a {@link Mqtt3Subscription} to the {@link Mqtt3Subscribe#getSubscriptions() list of subscriptions}. At least
     * one subscription is mandatory.
     *
     * @param subscription the subscription.
     * @return the builder that is now complete as at least one subscription is set.
     */
    @NotNull C addSubscription(@NotNull Mqtt3Subscription subscription);

    /**
     * Adds a list of {@link Mqtt3Subscription} to the {@link Mqtt3Subscribe#getSubscriptions() list of subscriptions}.
     * At least one subscription in the list is mandatory.
     *
     * @param subscriptions the subscriptions.
     * @return the builder that is now complete as at least one subscription is set.
     */
    @NotNull C addSubscriptions(@NotNull List<Mqtt3Subscription> subscriptions);

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
    @NotNull Mqtt3SubscriptionBuilder.Nested<? extends C> addSubscription();

    /**
     * {@link Mqtt3SubscribeBuilderBase} that provides additional methods for the first subscription.
     *
     * @param <C>  the type of the complete builder.
     * @param <SC> the type of the complete start builder.
     */
    // @formatter:off
    @DoNotImplement
    interface Start<
            C extends Mqtt3SubscribeBuilderBase<C>,
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
        @DoNotImplement
        interface Complete<
                C extends Mqtt3SubscribeBuilderBase<C>,
                SC extends Mqtt3SubscribeBuilderBase.Start.Complete<C, SC>>
                extends Mqtt3SubscribeBuilderBase.Start<C, SC>, Mqtt3SubscriptionBuilderBase.Complete<SC> {}
        // @formatter:on
    }
}
