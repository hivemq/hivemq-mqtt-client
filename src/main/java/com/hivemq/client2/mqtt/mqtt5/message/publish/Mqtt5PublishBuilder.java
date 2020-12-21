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

package com.hivemq.client2.mqtt.mqtt5.message.publish;

import com.hivemq.client2.annotations.CheckReturnValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for a {@link Mqtt5Publish}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5PublishBuilder extends Mqtt5PublishBuilderBase<Mqtt5PublishBuilder.Complete> {

    /**
     * Creates a builder for a {@link Mqtt5WillPublish} that extends the current Publish message of this builder.
     *
     * @return the created builder for a Will Publish.
     */
    @CheckReturnValue
    @NotNull Mqtt5WillPublishBuilder asWill();

    /**
     * {@link Mqtt5PublishBuilder} that is complete which means all mandatory fields are set.
     */
    @ApiStatus.NonExtendable
    interface Complete extends Mqtt5PublishBuilder, Mqtt5PublishBuilderBase.Complete<Mqtt5PublishBuilder.Complete> {

        /**
         * Creates a builder for a {@link Mqtt5WillPublish} that extends the current Publish message of this builder.
         *
         * @return the created complete builder for a Will Publish.
         */
        @Override
        @CheckReturnValue
        Mqtt5WillPublishBuilder.@NotNull Complete asWill();

        /**
         * Builds the {@link Mqtt5Publish}.
         *
         * @return the built {@link Mqtt5Publish}.
         */
        @CheckReturnValue
        @NotNull Mqtt5Publish build();
    }

    /**
     * Builder for a {@link Mqtt5Publish} that is applied to a parent {@link com.hivemq.client2.mqtt.mqtt5.Mqtt5Client}
     * which then sends the Publish message.
     *
     * @param <P> the type of the result when the built {@link Mqtt5Publish} is sent by the parent.
     */
    @ApiStatus.NonExtendable
    interface Nested<P> extends Mqtt5PublishBuilderBase<Nested.Complete<P>> {

        /**
         * {@link Nested} that is complete which means all mandatory fields are set.
         *
         * @param <P> the type of the result when the built {@link Mqtt5Publish} is applied to the parent.
         */
        @ApiStatus.NonExtendable
        interface Complete<P> extends Nested<P>, Mqtt5PublishBuilderBase.Complete<Nested.Complete<P>> {

            /**
             * Builds the {@link Mqtt5Publish} and applies it to the parent.
             *
             * @return the result when the built {@link Mqtt5Publish} is applied to the parent.
             */
            @NotNull P applyPublish();
        }
    }

    /**
     * Builder for a {@link Mqtt5Publish} that is applied to a parent {@link com.hivemq.client2.mqtt.mqtt5.Mqtt5Client}
     * which then sends the Publish message without returning a result.
     */
    @ApiStatus.NonExtendable
    interface Send<P> extends Mqtt5PublishBuilderBase<Send.Complete<P>> {

        /**
         * {@link Send} that is complete which means all mandatory fields are set.
         *
         * @param <P> the type of the result when the built {@link Mqtt5Publish} is sent by the parent.
         */
        @ApiStatus.NonExtendable
        interface Complete<P> extends Send<P>, Mqtt5PublishBuilderBase.Complete<Send.Complete<P>> {

            /**
             * Builds the {@link Mqtt5Publish} and applies it to the parent which then sends the Publish message.
             *
             * @return the result when the built {@link Mqtt5Publish} is sent by the parent.
             */
            @NotNull P send();
        }
    }
}
