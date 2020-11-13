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
 * Builder for a {@link Mqtt3Subscribe}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt3SubscribeBuilder extends Mqtt3SubscribeBuilderBase<Mqtt3SubscribeBuilder.Complete> {

    /**
     * {@link Mqtt3SubscribeBuilder} that is complete which means all mandatory fields are set.
     */
    @ApiStatus.NonExtendable
    interface Complete extends Mqtt3SubscribeBuilder, Mqtt3SubscribeBuilderBase<Mqtt3SubscribeBuilder.Complete> {

        /**
         * Builds the {@link Mqtt3Subscribe}.
         *
         * @return the built {@link Mqtt3Subscribe}.
         */
        @CheckReturnValue
        @NotNull Mqtt3Subscribe build();
    }

    /**
     * {@link Mqtt3SubscribeBuilder} that provides additional methods for the first subscription.
     */
    @ApiStatus.NonExtendable
    interface Start extends Mqtt3SubscribeBuilder,
            Mqtt3SubscribeBuilderBase.Start<Mqtt3SubscribeBuilder.Complete, Mqtt3SubscribeBuilder.Start.Complete> {

        /**
         * {@link Mqtt3SubscribeBuilder.Start} that is complete which means all mandatory fields are set.
         */
        // @formatter:off
        @ApiStatus.NonExtendable
        interface Complete extends Mqtt3SubscribeBuilder.Start, Mqtt3SubscribeBuilder.Complete,
                Mqtt3SubscribeBuilderBase.Start.Complete<
                        Mqtt3SubscribeBuilder.Complete, Mqtt3SubscribeBuilder.Start.Complete> {}
        // @formatter:on
    }

    /**
     * Builder for a {@link Mqtt3Subscribe} that is applied to a parent.
     *
     * @param <P> the type of the result when the built {@link Mqtt3Subscribe} is applied to the parent.
     */
    @ApiStatus.NonExtendable
    interface Nested<P> extends Mqtt3SubscribeBuilderBase<Nested.Complete<P>> {

        /**
         * {@link Nested} that is complete which means all mandatory fields are set.
         *
         * @param <P> the type of the result when the built {@link Mqtt3Subscribe} is applied to the parent.
         */
        @ApiStatus.NonExtendable
        interface Complete<P> extends Nested<P>, Mqtt3SubscribeBuilderBase<Nested.Complete<P>> {

            /**
             * Builds the {@link Mqtt3Subscribe} and applies it to the parent.
             *
             * @return the result when the built {@link Mqtt3Subscribe} is applied to the parent.
             */
            @NotNull P applySubscribe();
        }

        /**
         * {@link Nested} that provides additional methods for the first subscription.
         *
         * @param <P> the type of the result when the built {@link Mqtt3Subscribe} is applied to the parent.
         */
        @ApiStatus.NonExtendable
        interface Start<P>
                extends Nested<P>, Mqtt3SubscribeBuilderBase.Start<Nested.Complete<P>, Nested.Start.Complete<P>> {

            /**
             * {@link Nested.Start} that is complete which means all mandatory fields are set.
             *
             * @param <P> the type of the result when the built {@link Mqtt3Subscribe} is applied to the parent.
             */
            @ApiStatus.NonExtendable
            interface Complete<P> extends Nested.Start<P>, Nested.Complete<P>,
                    Mqtt3SubscribeBuilderBase.Start.Complete<Nested.Complete<P>, Nested.Start.Complete<P>> {}
        }
    }

    /**
     * Builder for a {@link Mqtt3Subscribe} that is applied to a parent {@link com.hivemq.client.mqtt.mqtt3.Mqtt3Client
     * Mqtt3Client} which then sends the Subscribe message.
     *
     * @param <P> the type of the result when the built {@link Mqtt3Subscribe} is sent by the parent.
     */
    @ApiStatus.NonExtendable
    interface Send<P> extends Mqtt3SubscribeBuilderBase<Send.Complete<P>> {

        /**
         * {@link Send} that is complete which means all mandatory fields are set.
         *
         * @param <P> the type of the result when the built {@link Mqtt3Subscribe} is sent by the parent.
         */
        @ApiStatus.NonExtendable
        interface Complete<P> extends Send<P>, Mqtt3SubscribeBuilderBase<Send.Complete<P>> {

            /**
             * Builds the {@link Mqtt3Subscribe} and applies it to the parent which then sends the Subscribe message.
             *
             * @return the result when the built {@link Mqtt3Subscribe} is sent by the parent.
             */
            @NotNull P send();
        }

        /**
         * {@link Send} that provides additional methods for the first subscription.
         *
         * @param <P> the type of the result when the built {@link Mqtt3Subscribe} is sent by the parent.
         */
        @ApiStatus.NonExtendable
        interface Start<P> extends Send<P>, Mqtt3SubscribeBuilderBase.Start<Send.Complete<P>, Send.Start.Complete<P>> {

            /**
             * {@link Send.Start} that is complete which means all mandatory fields are set.
             *
             * @param <P> the type of the result when the built {@link Mqtt3Subscribe} is sent by the parent.
             */
            @ApiStatus.NonExtendable
            interface Complete<P> extends Send.Start<P>, Send.Complete<P>,
                    Mqtt3SubscribeBuilderBase.Start.Complete<Send.Complete<P>, Send.Start.Complete<P>> {}
        }
    }

    /**
     * Builder for a {@link Mqtt3Subscribe} and additional arguments that are applied to a parent {@link
     * com.hivemq.client.mqtt.mqtt3.Mqtt3Client Mqtt3Client} <code>subscribePublishes</code> call.
     *
     * @param <P> the type of the result when the built {@link Mqtt3Subscribe} is applied to the parent.
     * @since 1.2
     */
    @ApiStatus.NonExtendable
    interface Publishes<P> extends Mqtt3SubscribeBuilderBase<Publishes.Complete<P>> {

        /**
         * {@link Publishes} that is complete which means all mandatory fields are set.
         *
         * @param <P> the type of the result when the built {@link Mqtt3Subscribe} is applied to the parent.
         */
        @ApiStatus.NonExtendable
        interface Complete<P>
                extends Publishes<P>, Publishes.Args<P>, Mqtt3SubscribeBuilderBase<Publishes.Complete<P>> {}

        /**
         * {@link Publishes} that provides additional methods for the first subscription.
         *
         * @param <P> the type of the result when the built {@link Mqtt3Subscribe} is applied to the parent.
         */
        @ApiStatus.NonExtendable
        interface Start<P> extends Publishes<P>,
                Mqtt3SubscribeBuilderBase.Start<Publishes.Complete<P>, Publishes.Start.Complete<P>> {

            /**
             * {@link Publishes.Start} that is complete which means all mandatory fields are set.
             *
             * @param <P> the type of the result when the built {@link Mqtt3Subscribe} is applied to the parent.
             */
            @ApiStatus.NonExtendable
            interface Complete<P> extends Publishes.Start<P>, Publishes.Complete<P>,
                    Mqtt3SubscribeBuilderBase.Start.Complete<Publishes.Complete<P>, Publishes.Start.Complete<P>> {}
        }

        /**
         * Builder for additional arguments alongside the {@link Mqtt3Subscribe} that are applied to a parent {@link
         * com.hivemq.client.mqtt.mqtt3.Mqtt3Client Mqtt3Client} <code>subscribePublishes</code> call.
         *
         * @param <P> the type of the result when the built {@link Mqtt3Subscribe} is applied to the parent.
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
             * Builds the {@link Mqtt3Subscribe} and applies it and additional arguments to the parent.
             *
             * @return the result when the built {@link Mqtt3Subscribe} is applied to the parent.
             */
            @NotNull P applySubscribe();
        }
    }
}
