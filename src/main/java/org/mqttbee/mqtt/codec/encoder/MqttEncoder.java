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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.MqttServerConnectionData;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.ioc.ConnectionScope;
import org.mqttbee.mqtt.message.MqttMessage;

import javax.inject.Inject;

/**
 * Main encoder for MQTT messages which delegates to the individual {@link MqttMessageEncoder}s.
 *
 * @author Silvio Giebl
 */
@ConnectionScope
public class MqttEncoder extends ChannelOutboundHandlerAdapter {

    public static final @NotNull String NAME = "encoder";

    private final @NotNull MqttClientData clientData;
    private final @NotNull MqttMessageEncoders encoders;

    private int maximumPacketSize;

    @Inject
    MqttEncoder(final @NotNull MqttClientData clientData, final @NotNull MqttMessageEncoders encoders) {
        this.clientData = clientData;
        this.encoders = encoders;
    }

    @Override
    public void write(
            final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg,
            final @NotNull ChannelPromise promise) {

        if (msg instanceof MqttMessage) {
            final MqttMessage message = (MqttMessage) msg;
            final MqttMessageEncoder messageEncoder = encoders.get(message.getType().getCode());
            if (messageEncoder == null) {
                throw new UnsupportedOperationException();
            }
            final ByteBuf out = messageEncoder.castAndEncode(message, ctx.alloc(), getMaximumPacketSize());
            ctx.write(out, promise);
        } else {
            ctx.write(msg, promise);
        }
    }

    private int getMaximumPacketSize() {
        final int maximumPacketSize = this.maximumPacketSize;
        if (maximumPacketSize != 0) {
            return maximumPacketSize;
        }
        final MqttServerConnectionData serverConnectionData = clientData.getRawServerConnectionData();
        if (serverConnectionData == null) {
            return MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT;
        }
        return this.maximumPacketSize = serverConnectionData.getMaximumPacketSize();
    }
}
