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

package com.hivemq.client2.mqtt.mqtt5.message.publish;

import com.hivemq.client2.internal.mqtt.message.publish.MqttPublishBuilder;
import com.hivemq.client2.internal.util.UnsignedDataTypes;
import com.hivemq.client2.mqtt.datatypes.MqttQos;
import com.hivemq.client2.mqtt.datatypes.MqttTopic;
import com.hivemq.client2.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client2.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5Message;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5MessageType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * MQTT 5 Publish message. This message is translated from and to an MQTT 5 PUBLISH packet.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5Publish extends Mqtt5Message {

    /**
     * Default {@link MqttQos QoS} level of a Publish message. It is chosen as {@link MqttQos#AT_MOST_ONCE} as the QoS
     * level should be explicitly specified if special delivery guarantees are needed.
     */
    @NotNull MqttQos DEFAULT_QOS = MqttQos.AT_MOST_ONCE;

    /**
     * Creates a builder for a Publish message.
     *
     * @return the created builder.
     */
    static @NotNull Mqtt5PublishBuilder builder() {
        return new MqttPublishBuilder.Default();
    }

    /**
     * @return the topic of this Publish message.
     */
    @NotNull MqttTopic getTopic();

    /**
     * @return the optional payload of this Publish message.
     */
    @NotNull Optional<ByteBuffer> getPayload();

    /**
     * @return the payload of this Publish message as a byte array. Empty byte array if the payload is not present.
     */
    byte @NotNull [] getPayloadAsBytes();

    /**
     * @return the QoS of this Publish message.
     */
    @NotNull MqttQos getQos();

    /**
     * @return whether this Publish message is a retained message.
     */
    boolean isRetain();

    /**
     * @return the optional message expiry interval in seconds of this Publish message.
     */
    @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE) @NotNull OptionalLong getMessageExpiryInterval();

    /**
     * @return the optional payload format indicator of this Publish message.
     */
    @NotNull Optional<Mqtt5PayloadFormatIndicator> getPayloadFormatIndicator();

    /**
     * @return the optional content type of this Publish message.
     */
    @NotNull Optional<MqttUtf8String> getContentType();

    /**
     * @return the optional response topic of this Publish message.
     */
    @NotNull Optional<MqttTopic> getResponseTopic();

    /**
     * @return the optional correlation data of this Publish message.
     */
    @NotNull Optional<ByteBuffer> getCorrelationData();

    /**
     * @return the optional user properties of this Publish message.
     */
    @NotNull Mqtt5UserProperties getUserProperties();

    /**
     * Acknowledges this Publish message.
     *
     * @throws UnsupportedOperationException if manual acknowledgement is not enabled.
     * @throws IllegalStateException         if the message is acknowledged more than once.
     * @since 1.2
     */
    void acknowledge();

    @Override
    default @NotNull Mqtt5MessageType getType() {
        return Mqtt5MessageType.PUBLISH;
    }

    /**
     * Transforms this Publish message into a Will Publish with the same properties.
     *
     * @return the Will Publish.
     */
    @NotNull Mqtt5WillPublish asWill();

    /**
     * Creates a builder for extending this Publish message.
     *
     * @return the created builder.
     */
    Mqtt5PublishBuilder.@NotNull Complete extend();
}
