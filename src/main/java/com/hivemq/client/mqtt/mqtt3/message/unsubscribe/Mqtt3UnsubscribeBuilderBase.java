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
import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilterBuilder;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Builder base for a {@link Mqtt3Unsubscribe}.
 *
 * @param <C> the type of the complete builder.
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt3UnsubscribeBuilderBase<C extends Mqtt3UnsubscribeBuilderBase<C>> {

    /**
     * Adds a {@link MqttTopicFilter Topic Filter} to the {@link Mqtt3Unsubscribe#getTopicFilters() list of Topic
     * Filters}. At least one Topic Filter is mandatory.
     *
     * @param topicFilter the string representation of the Topic Filter.
     * @return the builder that is now complete as at least one Topic Filter is set.
     */
    @CheckReturnValue
    @NotNull C addTopicFilter(@NotNull String topicFilter);

    /**
     * Adds a {@link MqttTopicFilter Topic Filter} to the {@link Mqtt3Unsubscribe#getTopicFilters() list of Topic
     * Filters}. At least one Topic Filter is mandatory.
     *
     * @param topicFilter the Topic Filter.
     * @return the builder that is now complete as at least one Topic Filter is set.
     */
    @CheckReturnValue
    @NotNull C addTopicFilter(@NotNull MqttTopicFilter topicFilter);

    /**
     * Fluent counterpart of {@link #addTopicFilter(MqttTopicFilter)}.
     * <p>
     * Calling {@link MqttTopicFilterBuilder.Nested.Complete#applyTopicFilter()} on the returned builder has the same
     * effect as calling {@link #addTopicFilter(MqttTopicFilter)} with the result of {@link
     * MqttTopicFilterBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Topic Filter.
     * @see #addTopicFilter(MqttTopicFilter)
     */
    @CheckReturnValue
    @NotNull MqttTopicFilterBuilder.Nested<? extends C> addTopicFilter();

    /**
     * Adds {@link MqttTopicFilter Topic Filters} to the {@link Mqtt3Unsubscribe#getTopicFilters() list of Topic
     * Filters}. At least one Topic Filter is mandatory.
     *
     * @param topicFilters the Topic Filters.
     * @return the builder that is now complete as at least one Topic Filter is set.
     * @since 1.2
     */
    @CheckReturnValue
    @NotNull C addTopicFilters(@NotNull MqttTopicFilter @NotNull ... topicFilters);

    /**
     * Adds a collection of {@link MqttTopicFilter Topic Filters} to the {@link Mqtt3Unsubscribe#getTopicFilters() list
     * of Topic Filters}. At least one Topic Filter is mandatory.
     *
     * @param topicFilters the collection of Topic Filters.
     * @return the builder that is now complete as at least one Topic Filter is set.
     * @since 1.2
     */
    @CheckReturnValue
    @NotNull C addTopicFilters(@NotNull Collection<@NotNull ? extends MqttTopicFilter> topicFilters);

    /**
     * Adds a stream of {@link MqttTopicFilter Topic Filters} to the {@link Mqtt3Unsubscribe#getTopicFilters() list of
     * Topic Filters}. At least one Topic Filter is mandatory.
     *
     * @param topicFilters the stream of Topic Filters.
     * @return the builder that is now complete as at least one Topic Filter is set.
     * @since 1.2
     */
    @CheckReturnValue
    @NotNull C addTopicFilters(@NotNull Stream<@NotNull ? extends MqttTopicFilter> topicFilters);

    /**
     * Reverses the subscriptions of a Subscribe message by adding their Topic Filters.
     *
     * @param subscribe the Subscribe message.
     * @return the builder that is now complete as at least one Topic Filter is set.
     */
    @CheckReturnValue
    @NotNull C reverse(@NotNull Mqtt3Subscribe subscribe);

    /**
     * {@link Mqtt3UnsubscribeBuilderBase} that provides additional methods for the first Topic Filter.
     *
     * @param <C> the type of the complete builder.
     */
    @DoNotImplement
    interface Start<C extends Mqtt3UnsubscribeBuilderBase<C>> extends Mqtt3UnsubscribeBuilderBase<C> {

        /**
         * Sets the mandatory {@link Mqtt3Unsubscribe#getTopicFilters() first Topic Filter}.
         *
         * @param topicFilter the string representation of the Topic Filter.
         * @return the builder that is now complete as the mandatory Topic Filter is set.
         */
        @CheckReturnValue
        @NotNull C topicFilter(@NotNull String topicFilter);

        /**
         * Sets the mandatory {@link Mqtt3Unsubscribe#getTopicFilters() first Topic Filter}.
         *
         * @param topicFilter the Topic Filter.
         * @return the builder that is now complete as the mandatory Topic Filter is set.
         */
        @CheckReturnValue
        @NotNull C topicFilter(@NotNull MqttTopicFilter topicFilter);

        /**
         * Fluent counterpart of {@link #topicFilter(MqttTopicFilter)}.
         * <p>
         * Calling {@link MqttTopicFilterBuilder.Nested.Complete#applyTopicFilter()} on the returned builder has the
         * same effect as calling {@link #topicFilter(MqttTopicFilter)} with the result of {@link
         * MqttTopicFilterBuilder.Complete#build()}.
         *
         * @return the fluent builder for the Topic Filter.
         * @see #addTopicFilter(MqttTopicFilter)
         */
        @CheckReturnValue
        @NotNull MqttTopicFilterBuilder.Nested<? extends C> topicFilter();
    }
}
