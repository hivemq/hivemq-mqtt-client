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

package com.hivemq.client.mqtt.mqtt3.message.publish;

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.datatypes.MqttTopicBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

/**
 * Builder base for a {@link Mqtt3Publish}.
 *
 * @param <C> the type of the complete builder.
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt3PublishBuilderBase<C extends Mqtt3PublishBuilderBase.Complete<C>> {

    /**
     * Sets the mandatory {@link Mqtt3Publish#getTopic() Topic}.
     *
     * @param topic the string representation of the Topic.
     * @return the builder that is now complete as the mandatory Topic is set.
     */
    @CheckReturnValue
    @NotNull C topic(@NotNull String topic);

    /**
     * Sets the mandatory {@link Mqtt3Publish#getTopic() Topic}.
     *
     * @param topic the Topic.
     * @return the builder that is now complete as the mandatory Topic is set.
     */
    @CheckReturnValue
    @NotNull C topic(@NotNull MqttTopic topic);

    /**
     * Fluent counterpart of {@link #topic(MqttTopic)}.
     * <p>
     * Calling {@link MqttTopicBuilder.Nested.Complete#applyTopic()} on the returned builder has the same effect as
     * calling {@link #topic(MqttTopic)} with the result of {@link MqttTopicBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Topic.
     * @see #topic(MqttTopic)
     */
    @CheckReturnValue
    MqttTopicBuilder.@NotNull Nested<? extends C> topicWith();

    /**
     * {@link Mqtt3PublishBuilderBase} that is complete which means all mandatory fields are set.
     *
     * @param <C> the type of the complete builder.
     */
    @ApiStatus.NonExtendable
    interface Complete<C extends Mqtt3PublishBuilderBase.Complete<C>> extends Mqtt3PublishBuilderBase<C> {

        /**
         * Sets the optional {@link Mqtt3Publish#getPayload() payload}.
         *
         * @param payload the payload as byte array or <code>null</code> to remove any previously set payload.
         * @return the builder.
         */
        @CheckReturnValue
        @NotNull C payload(byte @Nullable [] payload);

        /**
         * Sets the optional {@link Mqtt3Publish#getPayload() payload}.
         *
         * @param payload the payload as {@link ByteBuffer} or <code>null</code> to remove any previously set payload.
         * @return the builder.
         */
        @CheckReturnValue
        @NotNull C payload(@Nullable ByteBuffer payload);

        /**
         * Sets the {@link Mqtt3Publish#getQos() QoS}.
         *
         * @param qos the QoS.
         * @return the builder.
         */
        @CheckReturnValue
        @NotNull C qos(@NotNull MqttQos qos);

        /**
         * Sets whether the Publish message should be {@link Mqtt3Publish#isRetain() retained}.
         *
         * @param retain whether the Publish message should be retained.
         * @return the builder.
         */
        @CheckReturnValue
        @NotNull C retain(boolean retain);
    }
}
