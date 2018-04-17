/*
 *
 * *
 *  * Copyright 2018 The MQTT Bee project.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.mqttbee.mqtt.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;

/**
 * Encoder for a MQTT message.
 *
 * @author Silvio Giebl
 */
public interface MqttMessageEncoder {

    /**
     * Encodes the given MQTT message.
     *
     * @param out     the byte buffer to encode to.
     * @param channel the channel where the given byte buffer will be written to.
     */
    void encode(@NotNull ByteBuf out, @NotNull Channel channel);

    /**
     * Allocates a byte buffer with the correct size for the given MQTT message.
     *
     * @param channel the channel where the allocated byte buffer will be written to.
     * @return the allocated byte buffer.
     */
    @NotNull
    ByteBuf allocateBuffer(@NotNull final Channel channel);

    /**
     * Returns the byte count of the given MQTT message respecting the given maximum packet size. Calculation
     * is only performed if necessary.
     *
     * @param maxPacketSize the maximum packet size.
     * @return the encoded length of the MQTT message respecting the maximum packet size or -1 if the minimal encoded
     * length of the MQTT message is bigger than the maximum packet size.
     */
    int encodedLength(final int maxPacketSize);

}
