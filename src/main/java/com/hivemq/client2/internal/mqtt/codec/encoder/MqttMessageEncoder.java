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

package com.hivemq.client2.internal.mqtt.codec.encoder;

import com.hivemq.client2.internal.mqtt.message.MqttMessage;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * Encoder for a MQTT message type.
 *
 * @param <M> the type of the codable MQTT message.
 * @author Silvio Giebl
 */
public abstract class MqttMessageEncoder<M extends MqttMessage> {

    /**
     * Casts and encodes the given MQTT message.
     *
     * @param message the MQTT message to encode.
     * @param context the encoder context.
     * @return the byte buffer the MQTT message is encoded to.
     */
    @SuppressWarnings("unchecked")
    @NotNull ByteBuf castAndEncode(final @NotNull MqttMessage message, final @NotNull MqttEncoderContext context) {
        return encode((M) message, context);
    }

    /**
     * Encodes the given MQTT message.
     *
     * @param message the MQTT message to encode.
     * @param context the encoder context.
     * @return the byte buffer the MQTT message is encoded to.
     */
    protected abstract @NotNull ByteBuf encode(final @NotNull M message, final @NotNull MqttEncoderContext context);
}
