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

package com.hivemq.client.mqtt.mqtt5.message.publish;

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.internal.util.UnsignedDataTypes;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.datatypes.MqttTopicBuilder;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.nio.ByteBuffer;

/**
 * Builder base for a {@link Mqtt5Publish}.
 *
 * @param <C> the type of the complete builder.
 * @author Bilvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5PublishBuilderBase<C extends Mqtt5PublishBuilderBase.Complete<C>> {

    /**
     * Sets the mandatory {@link Mqtt5Publish#getTopic() Topic}.
     *
     * @param topic the string representation of the Topic.
     * @return the builder that is now complete as the mandatory Topic is set.
     */
    @CheckReturnValue
    @NotNull C topic(@NotNull String topic);

    /**
     * Sets the mandatory {@link Mqtt5Publish#getTopic() Topic}.
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
    MqttTopicBuilder.@NotNull Nested<? extends C> topic();

    /**
     * {@link Mqtt5PublishBuilderBase} that is complete which means all mandatory fields are set.
     *
     * @param <C> the type of the complete builder.
     */
    @ApiStatus.NonExtendable
    interface Complete<C extends Mqtt5PublishBuilderBase.Complete<C>> extends Mqtt5PublishBuilderBase<C> {

        /**
         * Sets the optional {@link Mqtt5Publish#getPayload() payload}.
         *
         * @param payload the payload as byte array or <code>null</code> to remove any previously set payload.
         * @return the builder.
         */
        @CheckReturnValue
        @NotNull C payload(byte @Nullable [] payload);

        /**
         * Sets the optional {@link Mqtt5Publish#getPayload() payload}.
         *
         * @param payload the payload as {@link ByteBuffer} or <code>null</code> to remove any previously set payload.
         * @return the builder.
         */
        @CheckReturnValue
        @NotNull C payload(@Nullable ByteBuffer payload);

        /**
         * Sets the {@link Mqtt5Publish#getQos() QoS}.
         *
         * @param qos the QoS.
         * @return the builder.
         */
        @CheckReturnValue
        @NotNull C qos(@NotNull MqttQos qos);

        /**
         * Sets whether the Publish message should be {@link Mqtt5Publish#isRetain() retained}.
         *
         * @param retain whether the Publish message should be retained.
         * @return the builder.
         */
        @CheckReturnValue
        @NotNull C retain(boolean retain);

        /**
         * Sets the {@link Mqtt5Publish#getMessageExpiryInterval() message expiry interval} in seconds.
         * <p>
         * The value must be in the range of an unsigned int: [0, 4_294_967_295].
         *
         * @param messageExpiryInterval the message expiry interval in seconds.
         * @return the builder.
         */
        @CheckReturnValue
        @NotNull C messageExpiryInterval(
                @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE) long messageExpiryInterval);

        /**
         * Disables the {@link Mqtt5Publish#getMessageExpiryInterval() message expiry}.
         *
         * @return the builder.
         */
        @CheckReturnValue
        @NotNull C noMessageExpiry();

        /**
         * Sets the optional {@link Mqtt5Publish#getPayloadFormatIndicator() payload format indicator}.
         *
         * @param payloadFormatIndicator the payload format indicator or <code>null</code> to remove any previously set
         *                               payload format indicator.
         * @return the builder.
         */
        @CheckReturnValue
        @NotNull C payloadFormatIndicator(@Nullable Mqtt5PayloadFormatIndicator payloadFormatIndicator);

        /**
         * Sets the optional {@link Mqtt5Publish#getContentType() content type}.
         *
         * @param contentType the content type or <code>null</code> to remove any previously set content type.
         * @return the builder.
         */
        @CheckReturnValue
        @NotNull C contentType(@Nullable String contentType);

        /**
         * Sets the optional {@link Mqtt5Publish#getContentType() content type}.
         *
         * @param contentType the content type or <code>null</code> to remove any previously set content type.
         * @return the builder.
         */
        @CheckReturnValue
        @NotNull C contentType(@Nullable MqttUtf8String contentType);

        /**
         * Sets the optional {@link Mqtt5Publish#getResponseTopic() response topic}.
         *
         * @param responseTopic the response topic or <code>null</code> to remove any previously set response topic.
         * @return the builder.
         */
        @CheckReturnValue
        @NotNull C responseTopic(@Nullable String responseTopic);

        /**
         * Sets the optional {@link Mqtt5Publish#getResponseTopic() response topic}.
         *
         * @param responseTopic the response topic or <code>null</code> to remove any previously set response topic.
         * @return the builder.
         */
        @CheckReturnValue
        @NotNull C responseTopic(@Nullable MqttTopic responseTopic);

        /**
         * Fluent counterpart of {@link #responseTopic(MqttTopic)}.
         * <p>
         * Calling {@link MqttTopicBuilder.Nested.Complete#applyTopic()} on the returned builder has the same effect as
         * calling {@link #responseTopic(MqttTopic)} with the result of {@link MqttTopicBuilder.Complete#build()}.
         *
         * @return the fluent builder for the response topic.
         * @see #responseTopic(MqttTopic)
         */
        @CheckReturnValue
        MqttTopicBuilder.@NotNull Nested<? extends C> responseTopic();

        /**
         * Sets the optional {@link Mqtt5Publish#getCorrelationData() correlation data}.
         *
         * @param correlationData the correlation data as byte array or <code>null</code> to remove any previously set
         *                        correlation data.
         * @return the builder.
         */
        @CheckReturnValue
        @NotNull C correlationData(byte @Nullable [] correlationData);

        /**
         * Sets the optional {@link Mqtt5Publish#getCorrelationData() correlation data}.
         *
         * @param correlationData the correlation data as {@link ByteBuffer} or <code>null</code> to remove any
         *                        previously set correlation data.
         * @return the builder.
         */
        @CheckReturnValue
        @NotNull C correlationData(@Nullable ByteBuffer correlationData);

        /**
         * Sets the {@link Mqtt5Publish#getUserProperties() User Properties}.
         *
         * @param userProperties the User Properties.
         * @return the builder.
         */
        @CheckReturnValue
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
        @CheckReturnValue
        Mqtt5UserPropertiesBuilder.@NotNull Nested<? extends C> userProperties();
    }

    /**
     * Builder base for a {@link Mqtt5WillPublish}.
     *
     * @param <C> the type of the complete builder.
     */
    @ApiStatus.NonExtendable
    interface WillBase<C extends WillBase.Complete<C>> extends Mqtt5PublishBuilderBase<C> {

        /**
         * {@link WillBase} that is complete which means all mandatory fields are set.
         *
         * @param <C> the type of the complete builder.
         */
        @ApiStatus.NonExtendable
        interface Complete<C extends WillBase.Complete<C>> extends Mqtt5PublishBuilderBase.Complete<C>, WillBase<C> {

            /**
             * Sets the {@link Mqtt5WillPublish#getDelayInterval() delay interval} in seconds.
             * <p>
             * The value must be in the range of an unsigned int: [0, 4_294_967_295].
             *
             * @param delayInterval the delay interval in seconds.
             * @return the builder.
             */
            @CheckReturnValue
            @NotNull C delayInterval(
                    @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE) long delayInterval);
        }
    }
}
