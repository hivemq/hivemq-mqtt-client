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

package com.hivemq.client.mqtt.datatypes;

import com.hivemq.client.annotations.CheckReturnValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for a {@link MqttTopic}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface MqttTopicBuilder extends MqttTopicBuilderBase<MqttTopicBuilder.Complete> {

    /**
     * Creates a builder for a {@link MqttTopicFilter} that extends the current Topic Name of this builder.
     *
     * @return the created builder for a Topic Filter.
     */
    @CheckReturnValue
    @NotNull MqttTopicFilterBuilder filter();

    /**
     * Creates a builder for a {@link MqttSharedTopicFilter} that extends the current Topic Name of this builder.
     *
     * @param shareName the Share Name.
     * @return the created builder for a Shared Topic Filter.
     */
    @CheckReturnValue
    @NotNull MqttSharedTopicFilterBuilder share(@NotNull String shareName);

    /**
     * {@link MqttTopicBuilder} that is complete which means all mandatory fields are set.
     */
    @ApiStatus.NonExtendable
    interface Complete extends MqttTopicBuilder, MqttTopicBuilderBase<MqttTopicBuilder.Complete> {

        /**
         * Builds the {@link MqttTopic}.
         *
         * @return the built {@link MqttTopic}.
         */
        @CheckReturnValue
        @NotNull MqttTopic build();
    }

    /**
     * Builder for a {@link MqttTopic} that is applied to a parent.
     *
     * @param <P> the type of the result when the built {@link MqttTopic} is applied to the parent.
     */
    @ApiStatus.NonExtendable
    interface Nested<P> extends MqttTopicBuilderBase<Nested.Complete<P>> {

        /**
         * {@link Nested} that is complete which means all mandatory fields are set.
         *
         * @param <P> the type of the result when the built {@link MqttTopic} is applied to the parent.
         */
        @ApiStatus.NonExtendable
        interface Complete<P> extends Nested<P>, MqttTopicBuilderBase<Nested.Complete<P>> {

            /**
             * Builds the {@link MqttTopic} and applies it to the parent.
             *
             * @return the result when the built {@link MqttTopic} is applied to the parent.
             */
            @NotNull P applyTopic();
        }
    }
}
