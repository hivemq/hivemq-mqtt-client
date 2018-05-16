/*
 * Copyright 2018 The MQTT Bee project
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

package org.mqttbee.mqtt.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.MqttMessage;

/**
 * Encoder for a MQTT message.
 *
 * @author Silvio Giebl
 */
public abstract class MqttMessageEncoder<M extends MqttMessage> {

    /**
     * Encodes the given MQTT message.
     *
     * @param allocator         the allocator for allocating the byte buffer to encode to.
     * @param maximumPacketSize the maximum packet size for the MQTT message.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public ByteBuf castAndEncode(
            @NotNull final MqttMessage message, @NotNull final ByteBufAllocator allocator,
            final int maximumPacketSize) {

        return encode((M) message, allocator, maximumPacketSize);
    }

    @NotNull
    protected abstract ByteBuf encode(
            @NotNull final M message, @NotNull final ByteBufAllocator allocator, final int maximumPacketSize);

}
