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

package com.hivemq.client.mqtt.mqtt3.message.unsubscribe;

import com.hivemq.client.annotations.CheckReturnValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for a {@link Mqtt3Unsubscribe}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt3UnsubscribeBuilder extends Mqtt3UnsubscribeBuilderBase<Mqtt3UnsubscribeBuilder.Complete> {

    /**
     * {@link Mqtt3UnsubscribeBuilder} that is complete which means all mandatory fields are set.
     */
    @ApiStatus.NonExtendable
    interface Complete
            extends Mqtt3UnsubscribeBuilder, Mqtt3UnsubscribeBuilderBase.Complete<Mqtt3UnsubscribeBuilder.Complete> {

        /**
         * Builds the {@link Mqtt3Unsubscribe}.
         *
         * @return the built {@link Mqtt3Unsubscribe}.
         */
        @CheckReturnValue
        @NotNull Mqtt3Unsubscribe build();
    }

    /**
     * {@link Mqtt3UnsubscribeBuilder} that provides additional methods for the first Topic Filter.
     */
    @ApiStatus.NonExtendable
    interface Start
            extends Mqtt3UnsubscribeBuilder, Mqtt3UnsubscribeBuilderBase.Start<Mqtt3UnsubscribeBuilder.Complete> {}

    /**
     * Builder for a {@link Mqtt3Unsubscribe} that is applied to a parent.
     *
     * @param <P> the type of the result when the built {@link Mqtt3Unsubscribe} is applied to the parent.
     */
    @ApiStatus.NonExtendable
    interface Nested<P> extends Mqtt3UnsubscribeBuilderBase<Nested.Complete<P>> {

        /**
         * {@link Nested} that is complete which means all mandatory fields are set.
         *
         * @param <P> the type of the result when the built {@link Mqtt3Unsubscribe} is applied to the parent.
         */
        @ApiStatus.NonExtendable
        interface Complete<P> extends Nested<P>, Mqtt3UnsubscribeBuilderBase.Complete<Nested.Complete<P>> {

            /**
             * Builds the {@link Mqtt3Unsubscribe} and applies it to the parent.
             *
             * @return the result when the built {@link Mqtt3Unsubscribe} is applied to the parent.
             */
            @NotNull P applyUnsubscribe();
        }

        /**
         * {@link Nested} that provides additional methods for the first Topic Filter.
         *
         * @param <P> the type of the result when the built {@link Mqtt3Unsubscribe} is applied to the parent.
         */
        @ApiStatus.NonExtendable
        interface Start<P> extends Nested<P>, Mqtt3UnsubscribeBuilderBase.Start<Nested.Complete<P>> {}
    }

    /**
     * Builder for a {@link Mqtt3Unsubscribe} that is applied to a parent {@link com.hivemq.client.mqtt.mqtt3.Mqtt3Client}
     * which then sends the Unsubscribe message.
     *
     * @param <P> the type of the result when the built {@link Mqtt3Unsubscribe} is sent by the parent.
     */
    @ApiStatus.NonExtendable
    interface Send<P> extends Mqtt3UnsubscribeBuilderBase<Send.Complete<P>> {

        /**
         * {@link Send} that is complete which means all mandatory fields are set.
         *
         * @param <P> the type of the result when the built {@link Mqtt3Unsubscribe} is sent by the parent.
         */
        @ApiStatus.NonExtendable
        interface Complete<P> extends Send<P>, Mqtt3UnsubscribeBuilderBase.Complete<Send.Complete<P>> {

            /**
             * Builds the {@link Mqtt3Unsubscribe} and applies it to the parent which then sends the Unsubscribe
             * message.
             *
             * @return the result when the built {@link Mqtt3Unsubscribe} is sent by the parent.
             */
            @NotNull P send();
        }

        /**
         * {@link Send} that provides additional methods for the first Topic Filter.
         *
         * @param <P> the type of the result when the built {@link Mqtt3Unsubscribe} is sent by the parent.
         */
        @ApiStatus.NonExtendable
        interface Start<P> extends Send<P>, Mqtt3UnsubscribeBuilderBase.Start<Send.Complete<P>> {}
    }
}
