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

import com.hivemq.client2.internal.mqtt.message.publish.mqtt3.Mqtt3PublishViewBuilder;
import com.hivemq.client2.mqtt.datatypes.MqttQos;
import com.hivemq.client2.mqtt.datatypes.MqttTopic;
import com.hivemq.client2.mqtt.mqtt3.message.Mqtt3Message;
import com.hivemq.client2.mqtt.mqtt3.message.Mqtt3MessageType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * MQTT 3 Publish message. This message is translated from and to an MQTT 3 PUBLISH packet.
 *
 * @author Daniel Krüger
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt3Publish extends Mqtt3Message {

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
    static @NotNull Mqtt3PublishBuilder builder() {
        return new Mqtt3PublishViewBuilder.Default();
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
     * Acknowledges this Publish message.
     *
     * @throws UnsupportedOperationException if manual acknowledgement is not enabled.
     * @throws IllegalStateException         if the message is acknowledged more than once.
     * @since 1.2
     */
    void acknowledge();

    @Override
    default @NotNull Mqtt3MessageType getType() {
        return Mqtt3MessageType.PUBLISH;
    }

    /**
     * Creates a builder for extending this Publish message.
     *
     * @return the created builder.
     */
    Mqtt3PublishBuilder.@NotNull Complete extend();
}
