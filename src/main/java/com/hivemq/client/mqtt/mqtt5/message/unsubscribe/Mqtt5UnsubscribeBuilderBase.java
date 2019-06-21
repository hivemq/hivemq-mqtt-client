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

package com.hivemq.client.mqtt.mqtt5.message.unsubscribe;

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilterBuilder;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Builder base for a {@link Mqtt5Unsubscribe}.
 *
 * @param <C> the type of the complete builder.
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt5UnsubscribeBuilderBase<C extends Mqtt5UnsubscribeBuilderBase.Complete<C>> {

    /**
     * Adds a {@link MqttTopicFilter Topic Filter} to the {@link Mqtt5Unsubscribe#getTopicFilters() list of Topic
     * Filters}. At least one Topic Filter is mandatory.
     *
     * @param topicFilter the string representation of the Topic Filter.
     * @return the builder that is now complete as at least one Topic Filter is set.
     */
    @NotNull C addTopicFilter(@NotNull String topicFilter);

    /**
     * Adds a {@link MqttTopicFilter Topic Filter} to the {@link Mqtt5Unsubscribe#getTopicFilters() list of Topic
     * Filters}. At least one Topic Filter is mandatory.
     *
     * @param topicFilter the Topic Filter.
     * @return the builder that is now complete as at least one Topic Filter is set.
     */
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
    @NotNull MqttTopicFilterBuilder.Nested<? extends C> addTopicFilter();

    /**
     * Adds {@link MqttTopicFilter Topic Filters} to the {@link Mqtt5Unsubscribe#getTopicFilters() list of Topic
     * Filters}. At least one Topic Filter is mandatory.
     *
     * @param topicFilters the Topic Filters.
     * @return the builder that is now complete as at least one Topic Filter is set.
     * @since 1.2
     */
    @NotNull C addTopicFilters(@NotNull MqttTopicFilter @NotNull ... topicFilters);

    /**
     * Adds a collection of {@link MqttTopicFilter Topic Filters} to the {@link Mqtt5Unsubscribe#getTopicFilters() list
     * of Topic Filters}. At least one Topic Filter is mandatory.
     *
     * @param topicFilters the collection of Topic Filters.
     * @return the builder that is now complete as at least one Topic Filter is set.
     * @since 1.2
     */
    @NotNull C addTopicFilters(@NotNull Collection<@NotNull MqttTopicFilter> topicFilters);

    /**
     * Adds a stream of {@link MqttTopicFilter Topic Filters} to the {@link Mqtt5Unsubscribe#getTopicFilters() list of
     * Topic Filters}. At least one Topic Filter is mandatory.
     *
     * @param topicFilters the stream of Topic Filters.
     * @return the builder that is now complete as at least one Topic Filter is set.
     * @since 1.2
     */
    @NotNull C addTopicFilters(@NotNull Stream<@NotNull MqttTopicFilter> topicFilters);

    /**
     * Reverses the subscriptions of a Subscribe message by adding their Topic Filters.
     *
     * @param subscribe the Subscribe message.
     * @return the builder that is now complete as at least one Topic Filter is set.
     */
    @NotNull C reverse(@NotNull Mqtt5Subscribe subscribe);

    /**
     * {@link Mqtt5UnsubscribeBuilderBase} that is complete which means all mandatory fields are set.
     *
     * @param <C> the type of the complete builder.
     */
    @DoNotImplement
    interface Complete<C extends Mqtt5UnsubscribeBuilderBase.Complete<C>> extends Mqtt5UnsubscribeBuilderBase<C> {

        /**
         * Sets the {@link Mqtt5Unsubscribe#getUserProperties() User Properties}.
         *
         * @param userProperties the User Properties.
         * @return the builder.
         */
        @NotNull C userProperties(@NotNull Mqtt5UserProperties userProperties);

        /**
         * Fluent counterpart of {@link #userProperties(Mqtt5UserProperties)}.
         * <p>
         * Calling {@link Mqtt5UserPropertiesBuilder.Nested#applyUserProperties()} on the returned builder has the
         * effect of {@link Mqtt5UserProperties#extend() extending} the current User Properties.
         *
         * @return the fluent builder for the User Properties.
         * @see #userProperties(Mqtt5UserProperties)
         */
        @NotNull Mqtt5UserPropertiesBuilder.Nested<? extends C> userProperties();
    }

    /**
     * {@link Mqtt5UnsubscribeBuilderBase} that provides additional methods for the first Topic Filter.
     *
     * @param <C> the type of the complete builder.
     */
    @DoNotImplement
    interface Start<C extends Mqtt5UnsubscribeBuilderBase.Complete<C>> extends Mqtt5UnsubscribeBuilderBase<C> {

        /**
         * Sets the mandatory {@link Mqtt5Unsubscribe#getTopicFilters() first Topic Filter}.
         *
         * @param topicFilter the string representation of the Topic Filter.
         * @return the builder that is now complete as the mandatory Topic Filter is set.
         */
        @NotNull C topicFilter(@NotNull String topicFilter);

        /**
         * Sets the mandatory {@link Mqtt5Unsubscribe#getTopicFilters() first Topic Filter}.
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
