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

package com.hivemq.client.mqtt.mqtt5.message.subscribe;

import com.hivemq.client.annotations.CheckReturnValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for a {@link Mqtt5Subscription}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5SubscriptionBuilder extends Mqtt5SubscriptionBuilderBase<Mqtt5SubscriptionBuilder.Complete> {

    /**
     * {@link Mqtt5Subscription} that is complete which means all mandatory fields are set.
     */
    @ApiStatus.NonExtendable
    interface Complete
            extends Mqtt5SubscriptionBuilder, Mqtt5SubscriptionBuilderBase.Complete<Mqtt5SubscriptionBuilder.Complete> {

        /**
         * Builds the {@link Mqtt5Subscription}.
         *
         * @return the built {@link Mqtt5Subscription}.
         */
        @CheckReturnValue
        @NotNull Mqtt5Subscription build();
    }

    /**
     * Builder for a {@link Mqtt5Subscription} that is applied to a parent.
     *
     * @param <P> the type of the result when the built {@link Mqtt5Subscription} is applied to the parent.
     */
    @ApiStatus.NonExtendable
    interface Nested<P> extends Mqtt5SubscriptionBuilderBase<Nested.Complete<P>> {

        /**
         * {@link Nested} that is complete which means all mandatory fields are set.
         *
         * @param <P> the type of the result when the built {@link Mqtt5Subscription} is applied to the parent.
         */
        @ApiStatus.NonExtendable
        interface Complete<P> extends Nested<P>, Mqtt5SubscriptionBuilderBase.Complete<Nested.Complete<P>> {

            /**
             * Builds the {@link Mqtt5Subscription} and applies it to the parent.
             *
             * @return the result when the built {@link Mqtt5Subscription} is applied to the parent.
             */
            @NotNull P applySubscription();
        }
    }
}
