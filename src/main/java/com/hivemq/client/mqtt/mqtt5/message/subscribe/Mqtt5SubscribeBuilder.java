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
 * Builder for a {@link Mqtt5Subscribe}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5SubscribeBuilder extends Mqtt5SubscribeBuilderBase<Mqtt5SubscribeBuilder.Complete> {

    /**
     * {@link Mqtt5SubscribeBuilder} that is complete which means all mandatory fields are set.
     */
    @ApiStatus.NonExtendable
    interface Complete
            extends Mqtt5SubscribeBuilder, Mqtt5SubscribeBuilderBase.Complete<Mqtt5SubscribeBuilder.Complete> {

        /**
         * Builds the {@link Mqtt5Subscribe}.
         *
         * @return the built {@link Mqtt5Subscribe}.
         */
        @CheckReturnValue
        @NotNull Mqtt5Subscribe build();
    }

    /**
     * {@link Mqtt5SubscribeBuilder} that provides additional methods for the first subscription.
     */
    @ApiStatus.NonExtendable
    interface Start extends Mqtt5SubscribeBuilder,
            Mqtt5SubscribeBuilderBase.Start<Mqtt5SubscribeBuilder.Complete, Mqtt5SubscribeBuilder.Start.Complete> {

        /**
         * {@link Mqtt5SubscribeBuilder.Start} that is complete which means all mandatory fields are set.
         */
        // @formatter:off
        @ApiStatus.NonExtendable
        interface Complete extends Mqtt5SubscribeBuilder.Start, Mqtt5SubscribeBuilder.Complete,
                Mqtt5SubscribeBuilderBase.Start.Complete<
                        Mqtt5SubscribeBuilder.Complete, Mqtt5SubscribeBuilder.Start.Complete> {}
        // @formatter:on
    }

    /**
     * Builder for a {@link Mqtt5Subscribe} that is applied to a parent.
     *
     * @param <P> the type of the result when the built {@link Mqtt5Subscribe} is applied to the parent.
     */
    @ApiStatus.NonExtendable
    interface Nested<P> extends Mqtt5SubscribeBuilderBase<Nested.Complete<P>> {

        /**
         * {@link Nested} that is complete which means all mandatory fields are set.
         *
         * @param <P> the type of the result when the built {@link Mqtt5Subscribe} is applied to the parent.
         */
        @ApiStatus.NonExtendable
        interface Complete<P> extends Nested<P>, Mqtt5SubscribeBuilderBase.Complete<Nested.Complete<P>> {

            /**
             * Builds the {@link Mqtt5Subscribe} and applies it to the parent.
             *
             * @return the result when the built {@link Mqtt5Subscribe} is applied to the parent.
             */
            @NotNull P applySubscribe();
        }

        /**
         * {@link Nested} that provides additional methods for the first subscription.
         *
         * @param <P> the type of the result when the built {@link Mqtt5Subscribe} is applied to the parent.
         */
        @ApiStatus.NonExtendable
        interface Start<P>
                extends Nested<P>, Mqtt5SubscribeBuilderBase.Start<Nested.Complete<P>, Nested.Start.Complete<P>> {

            /**
             * {@link Nested.Start} that is complete which means all mandatory fields are set.
             *
             * @param <P> the type of the result when the built {@link Mqtt5Subscribe} is applied to the parent.
             */
            @ApiStatus.NonExtendable
            interface Complete<P> extends Nested.Start<P>, Nested.Complete<P>,
                    Mqtt5SubscribeBuilderBase.Start.Complete<Nested.Complete<P>, Nested.Start.Complete<P>> {}
        }
    }

    /**
     * Builder for a {@link Mqtt5Subscribe} that is applied to a parent {@link com.hivemq.client.mqtt.mqtt5.Mqtt5Client
     * Mqtt5Client} which then sends the Subscribe message.
     *
     * @param <P> the type of the result when the built {@link Mqtt5Subscribe} is sent by the parent.
     */
    @ApiStatus.NonExtendable
    interface Send<P> extends Mqtt5SubscribeBuilderBase<Send.Complete<P>> {

        /**
         * {@link Send} that is complete which means all mandatory fields are set.
         *
         * @param <P> the type of the result when the built {@link Mqtt5Subscribe} is sent by the parent.
         */
        @ApiStatus.NonExtendable
        interface Complete<P> extends Send<P>, Mqtt5SubscribeBuilderBase.Complete<Send.Complete<P>> {

            /**
             * Builds the {@link Mqtt5Subscribe} and applies it to the parent which then sends the Subscribe message.
             *
             * @return the result when the built {@link Mqtt5Subscribe} is sent by the parent.
             */
            @NotNull P send();
        }

        /**
         * {@link Send} that provides additional methods for the first subscription.
         *
         * @param <P> the type of the result when the built {@link Mqtt5Subscribe} is sent by the parent.
         */
        @ApiStatus.NonExtendable
        interface Start<P> extends Send<P>, Mqtt5SubscribeBuilderBase.Start<Send.Complete<P>, Send.Start.Complete<P>> {

            /**
             * {@link Send.Start} that is complete which means all mandatory fields are set.
             *
             * @param <P> the type of the result when the built {@link Mqtt5Subscribe} is sent by the parent.
             */
            @ApiStatus.NonExtendable
            interface Complete<P> extends Send.Start<P>, Send.Complete<P>,
                    Mqtt5SubscribeBuilderBase.Start.Complete<Send.Complete<P>, Send.Start.Complete<P>> {}
        }
    }

    /**
     * Builder for a {@link Mqtt5Subscribe} and additional arguments that are applied to a parent {@link
     * com.hivemq.client.mqtt.mqtt5.Mqtt5Client Mqtt5Client} <code>subscribePublishes</code> call.
     *
     * @param <P> the type of the result when the built {@link Mqtt5Subscribe} is applied to the parent.
     * @since 1.2
     */
    @ApiStatus.NonExtendable
    interface Publishes<P> extends Mqtt5SubscribeBuilderBase<Publishes.Complete<P>> {

        /**
         * {@link Publishes} that is complete which means all mandatory fields are set.
         *
         * @param <P> the type of the result when the built {@link Mqtt5Subscribe} is applied to the parent.
         */
        @ApiStatus.NonExtendable
        interface Complete<P>
                extends Publishes<P>, Publishes.Args<P>, Mqtt5SubscribeBuilderBase.Complete<Publishes.Complete<P>> {}

        /**
         * {@link Publishes} that provides additional methods for the first subscription.
         *
         * @param <P> the type of the result when the built {@link Mqtt5Subscribe} is applied to the parent.
         */
        @ApiStatus.NonExtendable
        interface Start<P> extends Publishes<P>,
                Mqtt5SubscribeBuilderBase.Start<Publishes.Complete<P>, Publishes.Start.Complete<P>> {

            /**
             * {@link Publishes.Start} that is complete which means all mandatory fields are set.
             *
             * @param <P> the type of the result when the built {@link Mqtt5Subscribe} is applied to the parent.
             */
            @ApiStatus.NonExtendable
            interface Complete<P> extends Publishes.Start<P>, Publishes.Complete<P>,
                    Mqtt5SubscribeBuilderBase.Start.Complete<Publishes.Complete<P>, Publishes.Start.Complete<P>> {}
        }

        /**
         * Builder for additional arguments alongside the {@link Mqtt5Subscribe} that are applied to a parent {@link
         * com.hivemq.client.mqtt.mqtt5.Mqtt5Client Mqtt5Client} <code>subscribePublishes</code> call.
         *
         * @param <P> the type of the result when the built {@link Mqtt5Subscribe} is applied to the parent.
         */
        @ApiStatus.NonExtendable
        interface Args<P> {

            /**
             * Sets whether the matching Publish messages consumed via the subscriptions are acknowledged manually.
             *
             * @param manualAcknowledgement whether the matching Publish messages are acknowledged manually.
             * @return the builder.
             */
            @CheckReturnValue
            @NotNull Args<P> manualAcknowledgement(boolean manualAcknowledgement);

            /**
             * Builds the {@link Mqtt5Subscribe} and applies it and additional arguments to the parent.
             *
             * @return the result when the built {@link Mqtt5Subscribe} is applied to the parent.
             */
            @NotNull P applySubscribe();
        }
    }
}
