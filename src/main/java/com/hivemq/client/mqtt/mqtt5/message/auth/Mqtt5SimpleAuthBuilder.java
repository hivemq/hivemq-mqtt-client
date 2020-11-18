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

package com.hivemq.client.mqtt.mqtt5.message.auth;

import com.hivemq.client.annotations.CheckReturnValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for a {@link Mqtt5SimpleAuth}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5SimpleAuthBuilder extends Mqtt5SimpleAuthBuilderBase<Mqtt5SimpleAuthBuilder.Complete> {

    /**
     * {@link Mqtt5SimpleAuthBuilder} that is complete which means all mandatory fields are set.
     */
    @ApiStatus.NonExtendable
    interface Complete
            extends Mqtt5SimpleAuthBuilder, Mqtt5SimpleAuthBuilderBase.Complete<Mqtt5SimpleAuthBuilder.Complete> {

        /**
         * Builds the {@link Mqtt5SimpleAuth}.
         *
         * @return the built {@link Mqtt5SimpleAuth}.
         */
        @CheckReturnValue
        @NotNull Mqtt5SimpleAuth build();
    }

    /**
     * Builder for a {@link Mqtt5SimpleAuth} that is applied to a parent.
     *
     * @param <P> the type of the result when the built {@link Mqtt5SimpleAuth} is applied to the parent.
     */
    @ApiStatus.NonExtendable
    interface Nested<P> extends Mqtt5SimpleAuthBuilderBase<Nested.Complete<P>> {

        /**
         * {@link Nested} that is complete which means all mandatory fields are set.
         *
         * @param <P> the type of the result when the built {@link Mqtt5SimpleAuth} is applied to the parent.
         */
        @ApiStatus.NonExtendable
        interface Complete<P> extends Nested<P>, Mqtt5SimpleAuthBuilderBase.Complete<Nested.Complete<P>> {

            /**
             * Builds the {@link Mqtt5SimpleAuth} and applies it to the parent.
             *
             * @return the result when the built {@link Mqtt5SimpleAuth} is applied to the parent.
             */
            @NotNull P applySimpleAuth();
        }
    }
}
