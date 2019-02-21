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
import org.jetbrains.annotations.NotNull;

/**
 * Builder for a {@link Mqtt5Subscribe}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt5SubscribeBuilder extends Mqtt5SubscribeBuilderBase<Mqtt5SubscribeBuilder.Complete> {

    /**
     * {@link Mqtt5SubscribeBuilder} that is complete which means all mandatory fields are set.
     */
    @DoNotImplement
    interface Complete
            extends Mqtt5SubscribeBuilder, Mqtt5SubscribeBuilderBase.Complete<Mqtt5SubscribeBuilder.Complete> {

        /**
         * Builds the {@link Mqtt5Subscribe}.
         *
         * @return the built {@link Mqtt5Subscribe}.
         */
        @NotNull Mqtt5Subscribe build();
    }

    /**
     * {@link Mqtt5SubscribeBuilder} that provides additional methods for the first subscription.
     */
    // @formatter:off
    @DoNotImplement
    interface Start extends
            Mqtt5SubscribeBuilder,
            Mqtt5SubscribeBuilderBase.Start<Mqtt5SubscribeBuilder.Complete, Mqtt5SubscribeBuilder.Start.Complete> {
    // @formatter:on

        /**
         * {@link Mqtt5SubscribeBuilder.Start} that is complete which means all mandatory fields are set.
         */
        // @formatter:off
        @DoNotImplement
        interface Complete extends
                Mqtt5SubscribeBuilder.Start, Mqtt5SubscribeBuilder.Complete,
                Mqtt5SubscribeBuilderBase.Start.Complete<
                        Mqtt5SubscribeBuilder.Complete, Mqtt5SubscribeBuilder.Start.Complete> {}
        // @formatter:on
    }

    /**
     * Builder for a {@link Mqtt5Subscribe} that is applied to a parent.
     *
     * @param <P> the type of the result when the built {@link Mqtt5Subscribe} is applied to the parent.
     */
    @DoNotImplement
    interface Nested<P> extends Mqtt5SubscribeBuilderBase<Nested.Complete<P>> {

        /**
         * {@link Nested} that is complete which means all mandatory fields are set.
         *
         * @param <P> the type of the result when the built {@link Mqtt5Subscribe} is applied to the parent.
         */
        @DoNotImplement
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
        @DoNotImplement
        interface Start<P>
                extends Nested<P>, Mqtt5SubscribeBuilderBase.Start<Nested.Complete<P>, Nested.Start.Complete<P>> {

            /**
             * {@link Nested.Start} that is complete which means all mandatory fields are set.
             *
             * @param <P> the type of the result when the built {@link Mqtt5Subscribe} is applied to the parent.
             */
            // @formatter:off
            @DoNotImplement
            interface Complete<P> extends
                    Nested.Start<P>, Nested.Complete<P>,
                    Mqtt5SubscribeBuilderBase.Start.Complete<Nested.Complete<P>, Nested.Start.Complete<P>> {}
            // @formatter:on
        }
    }

    /**
     * Builder for a {@link Mqtt5Subscribe} that is applied to a parent {@link com.hivemq.client.mqtt.mqtt5.Mqtt5Client}
     * which then sends the Subscribe message.
     *
     * @param <P> the type of the result when the built {@link Mqtt5Subscribe} is sent by the parent.
     */
    @DoNotImplement
    interface Send<P> extends Mqtt5SubscribeBuilderBase<Send.Complete<P>> {

        /**
         * {@link Send} that is complete which means all mandatory fields are set.
         *
         * @param <P> the type of the result when the built {@link Mqtt5Subscribe} is sent by the parent.
         */
        @DoNotImplement
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
        @DoNotImplement
        interface Start<P> extends Send<P>, Mqtt5SubscribeBuilderBase.Start<Send.Complete<P>, Send.Start.Complete<P>> {

            /**
             * {@link Send.Start} that is complete which means all mandatory fields are set.
             *
             * @param <P> the type of the result when the built {@link Mqtt5Subscribe} is sent by the parent.
             */
            // @formatter:off
            @DoNotImplement
            interface Complete<P> extends
                    Send.Start<P>, Send.Complete<P>,
                    Mqtt5SubscribeBuilderBase.Start.Complete<Send.Complete<P>, Send.Start.Complete<P>> {}
            // @formatter:on
        }
    }
}
