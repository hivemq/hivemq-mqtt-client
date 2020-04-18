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

package com.hivemq.client.mqtt.mqtt5.message.subscribe;

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilterBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Builder base for a {@link Mqtt5Subscription}.
 *
 * @param <C> the type of the complete builder.
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt5SubscriptionBuilderBase<C extends Mqtt5SubscriptionBuilderBase.Complete<C>> {

    /**
     * Sets the mandatory {@link Mqtt5Subscription#getTopicFilter() Topic Filter}.
     *
     * @param topicFilter the string representation of the Topic Filter.
     * @return the builder that is now complete as the mandatory Topic Filter is set.
     */
    @CheckReturnValue
    @NotNull C topicFilter(@NotNull String topicFilter);

    /**
     * Sets the mandatory {@link Mqtt5Subscription#getTopicFilter() Topic Filter}.
     *
     * @param topicFilter the Topic Filter.
     * @return the builder that is now complete as the mandatory Topic Filter is set.
     */
    @CheckReturnValue
    @NotNull C topicFilter(@NotNull MqttTopicFilter topicFilter);

    /**
     * Fluent counterpart of {@link #topicFilter(MqttTopicFilter)}.
     * <p>
     * Calling {@link MqttTopicFilterBuilder.Nested.Complete#applyTopicFilter()} on the returned builder has the same
     * effect as calling {@link #topicFilter(MqttTopicFilter)} with the result of {@link
     * MqttTopicFilterBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Topic Filter.
     * @see #topicFilter(MqttTopicFilter)
     */
    @CheckReturnValue
    @NotNull MqttTopicFilterBuilder.Nested<? extends C> topicFilter();

    /**
     * {@link Mqtt5SubscriptionBuilderBase} that is complete which means all mandatory fields are set.
     *
     * @param <C> the type of the complete builder.
     */
    @DoNotImplement
    interface Complete<C extends Mqtt5SubscriptionBuilderBase.Complete<C>> extends Mqtt5SubscriptionBuilderBase<C> {

        /**
         * Sets the {@link Mqtt5Subscription#getQos() QoS}.
         *
         * @param qos the QoS.
         * @return the builder.
         */
        @CheckReturnValue
        @NotNull C qos(@NotNull MqttQos qos);

        /**
         * Sets whether the subscription is {@link Mqtt5Subscription#isNoLocal() not local}.
         *
         * @param noLocal whether the subscription is not local.
         * @return the builder.
         */
        @CheckReturnValue
        @NotNull C noLocal(boolean noLocal);

        /**
         * Sets the {@link Mqtt5Subscription#getRetainHandling() retain handling}.
         *
         * @param retainHandling the retain handling.
         * @return the builder.
         */
        @CheckReturnValue
        @NotNull C retainHandling(@NotNull Mqtt5RetainHandling retainHandling);

        /**
         * Sets whether the Publish messages for the subscription are {@link Mqtt5Subscription#isRetainAsPublished()
         * published with the retain flag}.
         *
         * @param retainAsPublished whether the Publish messages for the subscription are published with the retain
         *                          flag.
         * @return the builder.
         */
        @CheckReturnValue
        @NotNull C retainAsPublished(boolean retainAsPublished);
    }
}
