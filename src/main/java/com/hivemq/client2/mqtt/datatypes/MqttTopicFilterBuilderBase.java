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

package com.hivemq.client2.mqtt.datatypes;

import com.hivemq.client2.annotations.CheckReturnValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Builder base for a {@link MqttTopicFilter}.
 *
 * @param <C>  the type of the complete builder.
 * @param <E>  the type of the end builder.
 * @param <S>  the type of the builder for a {@link MqttSharedTopicFilter}.
 * @param <SC> the type of the complete builder for a {@link MqttSharedTopicFilter}.
 * @param <SE> the type of the end builder for a {@link MqttSharedTopicFilter}.
 * @author Silvio Giebl
 * @since 1.0
 */
// @formatter:off
@ApiStatus.NonExtendable
public interface MqttTopicFilterBuilderBase<
        C extends MqttTopicFilterBuilderBase<C, E, S, SC, SE>,
        E extends MqttTopicFilterBuilderBase.End,
        S extends MqttTopicFilterBuilderBase.SharedBase<S, SC, SE>,
        SC extends S,
        SE extends MqttTopicFilterBuilderBase.End> {
// @formatter:on

    /**
     * Adds a {@link MqttTopicFilter#getLevels() Topic level}.
     *
     * @param topicLevel the level.
     * @return the builder that is now complete as at least one Topic level is set.
     */
    @CheckReturnValue
    @NotNull C addLevel(@NotNull String topicLevel);

    /**
     * Adds a {@link MqttTopicFilter#SINGLE_LEVEL_WILDCARD}.
     *
     * @return the builder that is now complete as at least one single-level wildcard is set.
     */
    @CheckReturnValue
    @NotNull C singleLevelWildcard();

    /**
     * Adds a {@link MqttTopicFilter#MULTI_LEVEL_WILDCARD}.
     *
     * @return the end builder.
     */
    @CheckReturnValue
    @NotNull E multiLevelWildcard();

    /**
     * Creates a builder for a {@link MqttSharedTopicFilter} that extends the current Topic Filter of this builder.
     *
     * @param shareName the Share Name.
     * @return the created builder for a Shared Topic Filter.
     */
    @CheckReturnValue
    @NotNull S share(@NotNull String shareName);

    /**
     * {@link MqttTopicFilterBuilderBase} that is complete which means all mandatory fields are set.
     *
     * @param <C>  the type of the complete builder.
     * @param <E>  the type of the end builder.
     * @param <S>  the type of the builder for a {@link MqttSharedTopicFilter}.
     * @param <SC> the type of the complete builder for a {@link MqttSharedTopicFilter}.
     * @param <SE> the type of the end builder for a {@link MqttSharedTopicFilter}.
     */
    // @formatter:off
    @ApiStatus.NonExtendable
    interface Complete<
            C extends MqttTopicFilterBuilderBase<C, E, S, SC, SE>,
            E extends MqttTopicFilterBuilderBase.End,
            S extends MqttTopicFilterBuilderBase.SharedBase<S, SC, SE>,
            SC extends S,
            SE extends MqttTopicFilterBuilderBase.End>
            extends MqttTopicFilterBuilderBase<C, E, S, SC, SE> {
    // @formatter:on

        /**
         * Creates a builder for a {@link MqttSharedTopicFilter} that extends the current Topic Filter of this builder.
         *
         * @param shareName the Share Name.
         * @return the created complete builder for a Shared Topic Filter.
         */
        @Override
        @CheckReturnValue
        @NotNull SC share(@NotNull String shareName);
    }

    /**
     * End of a {@link MqttTopicFilterBuilderBase} that does not allow to add any more levels or wildcards.
     */
    @ApiStatus.NonExtendable
    interface End {}

    /**
     * Builder base for a {@link MqttSharedTopicFilter}.
     *
     * @param <S>  the type of the builder.
     * @param <SC> the type of the complete builder.
     * @param <SE> the type of the end builder.
     */
    // @formatter:off
    @ApiStatus.NonExtendable
    interface SharedBase<
            S extends SharedBase<S, SC, SE>,
            SC extends S,
            SE extends MqttTopicFilterBuilderBase.End>
            extends MqttTopicFilterBuilderBase<SC, SE, S, SC, SE> {
    // @formatter:on

        /**
         * {@link SharedBase} that is complete which means all mandatory fields are set.
         *
         * @param <S>  the type of the builder.
         * @param <SC> the type of the complete builder.
         * @param <SE> the type of the end builder.
         */
        // @formatter:off
        @ApiStatus.NonExtendable
        interface Complete<
                S extends SharedBase<S, SC, SE>,
                SC extends S,
                SE extends MqttTopicFilterBuilderBase.End>
                extends MqttTopicFilterBuilderBase.Complete<SC, SE, S, SC, SE> {}
        // @formatter:on
    }
}
