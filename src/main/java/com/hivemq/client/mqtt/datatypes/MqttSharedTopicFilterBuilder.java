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
import com.hivemq.client.annotations.DoNotImplement;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for a {@link MqttSharedTopicFilter}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
// @formatter:off
@DoNotImplement
public interface MqttSharedTopicFilterBuilder extends
        MqttTopicFilterBuilderBase.SharedBase<
                MqttSharedTopicFilterBuilder, MqttSharedTopicFilterBuilder.Complete, MqttSharedTopicFilterBuilder.End> {
// @formatter:on

    /**
     * {@link MqttSharedTopicFilterBuilder} that is complete which means all mandatory fields are set.
     */
    // @formatter:off
    @DoNotImplement
    interface Complete extends MqttSharedTopicFilterBuilder, MqttSharedTopicFilterBuilder.End,
            MqttTopicFilterBuilderBase.SharedBase.Complete<
                    MqttSharedTopicFilterBuilder, MqttSharedTopicFilterBuilder.Complete,
                    MqttSharedTopicFilterBuilder.End> {}
    // @formatter:on

    /**
     * End of a {@link MqttSharedTopicFilterBuilder} that does not allow to add any more levels or wildcards.
     */
    @DoNotImplement
    interface End extends MqttTopicFilterBuilderBase.End {

        /**
         * Builds the {@link MqttSharedTopicFilter}.
         *
         * @return the built {@link MqttSharedTopicFilter}.
         */
        @CheckReturnValue
        @NotNull MqttSharedTopicFilter build();
    }

    /**
     * Builder for a {@link MqttSharedTopicFilter} that is applied to a parent.
     *
     * @param <P> the type of the result when the built {@link MqttSharedTopicFilter} is applied to the parent.
     */
    @DoNotImplement
    interface Nested<P> extends MqttTopicFilterBuilderBase.SharedBase<Nested<P>, Nested.Complete<P>, Nested.End<P>> {

        /**
         * {@link Nested} that is complete which means all mandatory fields are set.
         *
         * @param <P> the type of the result when the built {@link MqttTopicFilter} is applied to the parent.
         */
        @DoNotImplement
        interface Complete<P> extends Nested<P>, Nested.End<P>,
                MqttTopicFilterBuilderBase.SharedBase.Complete<Nested<P>, Nested.Complete<P>, Nested.End<P>> {}

        /**
         * End of a {@link Nested} that does not allow to add any more levels or wildcards.
         */
        @DoNotImplement
        interface End<P> extends MqttTopicFilterBuilderBase.End {

            /**
             * Builds the {@link MqttSharedTopicFilter} and applies it to the parent.
             *
             * @return the result when the built {@link MqttSharedTopicFilter} is applied to the parent.
             */
            @NotNull P applyTopicFilter();
        }
    }
}
