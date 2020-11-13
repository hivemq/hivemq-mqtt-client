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

/**
 * Builder for a {@link Mqtt3Subscription}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt3SubscriptionBuilder extends Mqtt3SubscriptionBuilderBase<Mqtt3SubscriptionBuilder.Complete> {

    /**
     * {@link Mqtt3SubscriptionBuilder} that is complete which means all mandatory fields are set.
     */
    @ApiStatus.NonExtendable
    interface Complete
            extends Mqtt3SubscriptionBuilder, Mqtt3SubscriptionBuilderBase.Complete<Mqtt3SubscriptionBuilder.Complete> {

        /**
         * Builds the {@link Mqtt3Subscription}.
         *
         * @return the built {@link Mqtt3Subscription}.
         */
        @CheckReturnValue
        @NotNull Mqtt3Subscription build();
    }

    /**
     * Builder for a {@link Mqtt3Subscription} that is applied to a parent.
     *
     * @param <P> the type of the result when the built {@link Mqtt3Subscription} is applied to the parent.
     */
    @ApiStatus.NonExtendable
    interface Nested<P> extends Mqtt3SubscriptionBuilderBase<Nested.Complete<P>> {

        /**
         * {@link Nested} that is complete which means all mandatory fields are set.
         *
         * @param <P> the type of the result when the built {@link Mqtt3Subscription} is applied to the parent.
         */
        @ApiStatus.NonExtendable
        interface Complete<P> extends Nested<P>, Mqtt3SubscriptionBuilderBase.Complete<Nested.Complete<P>> {

            /**
             * Builds the {@link Mqtt3Subscription} and applies it to the parent.
             *
             * @return the result when the built {@link Mqtt3Subscription} is applied to the parent.
             */
            @NotNull P applySubscription();
        }
    }
}
