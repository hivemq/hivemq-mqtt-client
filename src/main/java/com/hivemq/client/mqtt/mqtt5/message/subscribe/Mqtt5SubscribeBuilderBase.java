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

package com.hivemq.client.mqtt.mqtt5.message.subscribe;

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Builder base for a {@link Mqtt5Subscribe}.
 *
 * @param <C> the type of the complete builder.
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt5SubscribeBuilderBase<C extends Mqtt5SubscribeBuilderBase.Complete<C>> {

    /**
     * Adds a {@link Mqtt5Subscription} to the {@link Mqtt5Subscribe#getSubscriptions() list of subscriptions}. At least
     * one subscription is mandatory.
     *
     * @param subscription the subscription.
     * @return the builder that is now complete as at least one subscription is set.
     */
    @NotNull C addSubscription(@NotNull Mqtt5Subscription subscription);

    /**
     * Adds a list of {@link Mqtt5Subscription} to the {@link Mqtt5Subscribe#getSubscriptions() list of subscriptions}.
     * At least one subscription in the list is mandatory.
     *
     * @param subscriptions the subscriptions.
     * @return the builder that is now complete as at least one subscription is set.
     */
    @NotNull C addSubscriptions(@NotNull List<Mqtt5Subscription> subscriptions);

    /**
     * Fluent counterpart of {@link #addSubscription(Mqtt5Subscription)}.
     * <p>
     * Calling {@link Mqtt5SubscriptionBuilder.Nested.Complete#applySubscription()} on the returned builder has the same
     * effect as calling {@link #addSubscription(Mqtt5Subscription)} with the result of {@link
     * Mqtt5SubscriptionBuilder.Complete#build()}.
     *
     * @return the fluent builder for the subscription.
     * @see #addSubscription(Mqtt5Subscription)
     */
    @NotNull Mqtt5SubscriptionBuilder.Nested<? extends C> addSubscription();

    /**
     * {@link Mqtt5SubscribeBuilderBase} that is complete which means all mandatory fields are set.
     *
     * @param <C> the type of the complete builder.
     */
    @DoNotImplement
    interface Complete<C extends Mqtt5SubscribeBuilderBase.Complete<C>> extends Mqtt5SubscribeBuilderBase<C> {

        /**
         * Sets the {@link Mqtt5Subscribe#getUserProperties() User Properties}.
         *
         * @param userProperties the User Properties.
         * @return the builder.
         */
        @NotNull C userProperties(@NotNull Mqtt5UserProperties userProperties);

        /**
         * Fluent counterpart of {@link #userProperties(Mqtt5UserProperties)}.
         * <p>
         * Calling {@link Mqtt5UserPropertiesBuilder.Nested#applyUserProperties()} on the returned builder has the
         * effect of {@link Mqtt5UserProperties#extend() extending} the current User Properties.
         *
         * @return the fluent builder for the User Properties.
         * @see #userProperties(Mqtt5UserProperties)
         */
        @NotNull Mqtt5UserPropertiesBuilder.Nested<? extends C> userProperties();
    }

    /**
     * {@link Mqtt5SubscribeBuilderBase} that provides additional methods for the first subscription.
     *
     * @param <C>  the type of the complete builder.
     * @param <SC> the type of the complete start builder.
     */
    // @formatter:off
    @DoNotImplement
    interface Start<
            C extends Mqtt5SubscribeBuilderBase.Complete<C>,
            SC extends Mqtt5SubscribeBuilderBase.Start.Complete<C, SC>>
            extends Mqtt5SubscribeBuilderBase<C>, Mqtt5SubscriptionBuilderBase<SC> {
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
                C extends Mqtt5SubscribeBuilderBase.Complete<C>,
                SC extends Mqtt5SubscribeBuilderBase.Start.Complete<C, SC>>
                extends Mqtt5SubscribeBuilderBase.Start<C, SC>, Mqtt5SubscribeBuilderBase.Complete<C>,
                        Mqtt5SubscriptionBuilderBase.Complete<SC> {}
        // @formatter:on
    }
}
