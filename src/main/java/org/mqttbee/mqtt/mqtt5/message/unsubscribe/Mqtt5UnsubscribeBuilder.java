/*
 * Copyright 2018 The MQTT Bee project
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

package org.mqttbee.mqtt.mqtt5.message.unsubscribe;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;

/**
 * Builder for a {@link Mqtt5Unsubscribe}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt5UnsubscribeBuilder extends Mqtt5UnsubscribeBuilderBase<Mqtt5UnsubscribeBuilder.Complete> {

    /**
     * {@link Mqtt5UnsubscribeBuilder} that is complete which means all mandatory fields are set.
     */
    // @formatter:off
    @DoNotImplement
    interface Complete extends
            Mqtt5UnsubscribeBuilder,
            Mqtt5UnsubscribeBuilderBase.Complete<Mqtt5UnsubscribeBuilder.Complete> {
    // @formatter:on

        /**
         * Builds the {@link Mqtt5Unsubscribe}.
         *
         * @return the built {@link Mqtt5Unsubscribe}.
         */
        @NotNull Mqtt5Unsubscribe build();
    }

    /**
     * {@link Mqtt5UnsubscribeBuilder} that provides additional methods for the first Topic Filter.
     */
    // @formatter:off
    @DoNotImplement
    interface Start extends
            Mqtt5UnsubscribeBuilder,
            Mqtt5UnsubscribeBuilderBase.Start<Mqtt5UnsubscribeBuilder.Complete> {}
    // @formatter:on

    /**
     * Builder for a {@link Mqtt5Unsubscribe} that is applied to a parent.
     *
     * @param <P> the type of the result when the built {@link Mqtt5Unsubscribe} is applied to the parent.
     */
    @DoNotImplement
    interface Nested<P> extends Mqtt5UnsubscribeBuilderBase<Nested.Complete<P>> {

        /**
         * {@link Nested} that is complete which means all mandatory fields are set.
         *
         * @param <P> the type of the result when the built {@link Mqtt5Unsubscribe} is applied to the parent.
         */
        @DoNotImplement
        interface Complete<P> extends Nested<P>, Mqtt5UnsubscribeBuilderBase.Complete<Nested.Complete<P>> {

            /**
             * Builds the {@link Mqtt5Unsubscribe} and applies it to the parent.
             *
             * @return the result when the built {@link Mqtt5Unsubscribe} is applied to the parent.
             */
            @NotNull P applyUnsubscribe();
        }

        /**
         * {@link Nested} that provides additional methods for the first Topic Filter.
         *
         * @param <P> the type of the result when the built {@link Mqtt5Unsubscribe} is applied to the parent.
         */
        @DoNotImplement
        interface Start<P> extends Nested<P>, Mqtt5UnsubscribeBuilderBase.Start<Nested.Complete<P>> {}
    }

    /**
     * Builder for a {@link Mqtt5Unsubscribe} that is applied to a parent {@link org.mqttbee.mqtt.mqtt3.Mqtt3Client}
     * which then sends the Unsubscribe message.
     *
     * @param <P> the type of the result when the built {@link Mqtt5Unsubscribe} is sent by the parent.
     */
    @DoNotImplement
    interface Send<P> extends Mqtt5UnsubscribeBuilderBase<Send.Complete<P>> {

        /**
         * {@link Send} that is complete which means all mandatory fields are set.
         *
         * @param <P> the type of the result when the built {@link Mqtt5Unsubscribe} is sent by the parent.
         */
        @DoNotImplement
        interface Complete<P> extends Send<P>, Mqtt5UnsubscribeBuilderBase.Complete<Send.Complete<P>> {

            /**
             * Builds the {@link Mqtt5Unsubscribe} and applies it to the parent which then sends the Unsubscribe
             * message.
             *
             * @return the result when the built {@link Mqtt5Unsubscribe} is sent by the parent.
             */
            @NotNull P send();
        }

        /**
         * {@link Send} that provides additional methods for the first Topic Filter.
         *
         * @param <P> the type of the result when the built {@link Mqtt5Unsubscribe} is sent by the parent.
         */
        @DoNotImplement
        interface Start<P> extends Send<P>, Mqtt5UnsubscribeBuilderBase.Start<Send.Complete<P>> {}
    }
}
