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
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.exceptions.MqttMaximumPacketSizeExceededException;
import org.mqttbee.mqtt.MqttServerConnectionData;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderApplier;
import org.mqttbee.mqtt.message.MqttMessage;

/**
 * Encoder for a MQTT message with a {@link MqttMessage} that is applied for encoding.
 *
 * @param <M> the type of the MQTT message.
 * @author Silvio Giebl
 */
public abstract class MqttMessageEncoderWithMessage<M extends MqttMessage>
        implements MqttMessageEncoder, MqttMessageEncoderApplier<M> {

    protected M message;

    @NotNull
    @Override
    public MqttMessageEncoder apply(@NotNull final M message) {
        this.message = message;
        return this;
    }

    @Override
    public abstract void encode(@NotNull ByteBuf out, @NotNull Channel channel);

    @NotNull
    @Override
    public ByteBuf allocateBuffer(@NotNull final Channel channel) {
        final int maximumPacketSize = MqttServerConnectionData.getMaximumPacketSize(channel);
        final int encodedLength = encodedLength(maximumPacketSize);
        if (encodedLength < 0) {
            throw new MqttMaximumPacketSizeExceededException(message, maximumPacketSize);
        }
        return channel.alloc().ioBuffer(encodedLength, encodedLength);
    }

    @Override
    public abstract int encodedLength(final int maxPacketSize);

}
