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

package com.hivemq.client.mqtt.mqtt3.message.unsubscribe;

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilterBuilder;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
    @NotNull C addTopicFilter(@NotNull String topicFilter);

    /**
     * Adds a {@link MqttTopicFilter Topic Filter} to the {@link Mqtt3Unsubscribe#getTopicFilters() list of Topic
     * Filters}. At least one Topic Filter is mandatory.
     *
     * @param topicFilter the Topic Filter.
     * @return the builder that is now complete as at least one Topic Filter is set.
     */
    @NotNull C addTopicFilter(@NotNull MqttTopicFilter topicFilter);

    /**
     * Adds a list of {@link MqttTopicFilter Topic Filter} to the {@link Mqtt3Unsubscribe#getTopicFilters() list of
     * Topic Filters}. At least one Topic Filter is mandatory.
     *
     * @param topicFilters the strings representing of the Topic Filter's.
     * @return the builder that is now complete as at least one Topic Filter is set.
     */
    @NotNull C addTopicFilters(@NotNull List<String> topicFilters);

    /**
     * Adds a {@link MqttTopicFilter Topic Filter} to the {@link Mqtt3Unsubscribe#getTopicFilters() list of Topic
     * Filters}. At least one Topic Filter is mandatory.
     *
     * @param topicFilters the Topic Filter's.
     * @return the builder that is now complete as at least one Topic Filter is set.
     */
    @NotNull C addMqttTopicFilters(@NotNull List<MqttTopicFilter> topicFilters);

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
    @NotNull MqttTopicFilterBuilder.Nested<? extends C> addTopicFilter();

    /**
     * Reverses the subscriptions of a Subscribe message by adding their Topic Filters.
     *
     * @param subscribe the Subscribe message.
     * @return the builder that is now complete as at least one Topic Filter is set.
     */
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
        @NotNull C topicFilter(@NotNull String topicFilter);

        /**
         * Sets the mandatory {@link Mqtt3Unsubscribe#getTopicFilters() first Topic Filter}.
         *
         * @param topicFilter the Topic Filter.
         * @return the builder that is now complete as the mandatory Topic Filter is set.
         */
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
        @NotNull MqttTopicFilterBuilder.Nested<? extends C> topicFilter();
    }
}
