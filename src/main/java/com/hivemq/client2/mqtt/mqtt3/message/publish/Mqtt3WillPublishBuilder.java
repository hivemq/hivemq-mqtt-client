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

package com.hivemq.client2.mqtt.mqtt3.message.publish;

import com.hivemq.client2.annotations.CheckReturnValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for a {@link Mqtt3Publish} used as a Will Publish.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt3WillPublishBuilder extends Mqtt3PublishBuilderBase<Mqtt3WillPublishBuilder.Complete> {

    /**
     * {@link Mqtt3WillPublishBuilder} that is complete which means all mandatory fields are set.
     */
    @ApiStatus.NonExtendable
    interface Complete
            extends Mqtt3WillPublishBuilder, Mqtt3PublishBuilderBase.Complete<Mqtt3WillPublishBuilder.Complete> {

        /**
         * Builds the {@link Mqtt3Publish} as a Will Publish.
         *
         * @return the built {@link Mqtt3Publish}.
         */
        @CheckReturnValue
        @NotNull Mqtt3Publish build();
    }

    /**
     * Builder for a {@link Mqtt3Publish} that is applied to a parent as a Will Publish.
     *
     * @param <P> the type of the result when the built {@link Mqtt3Publish} is applied to the parent.
     */
    @ApiStatus.NonExtendable
    interface Nested<P> extends Mqtt3PublishBuilderBase<Nested.Complete<P>> {

        /**
         * {@link Nested} that is complete which means all mandatory fields are set.
         *
         * @param <P> the type of the result when the built {@link Mqtt3Publish} is applied to the parent.
         */
        @ApiStatus.NonExtendable
        interface Complete<P> extends Nested<P>, Mqtt3PublishBuilderBase.Complete<Nested.Complete<P>> {

            /**
             * Builds the {@link Mqtt3Publish} and applies it to the parent as a Will Publish.
             *
             * @return the result when the built {@link Mqtt3Publish} is applied to the parent.
             */
            @NotNull P applyWillPublish();
        }
    }
}
