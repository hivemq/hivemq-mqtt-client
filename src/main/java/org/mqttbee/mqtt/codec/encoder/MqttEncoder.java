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
import org.mqttbee.mqtt.MqttServerConnectionData;
import org.mqttbee.mqtt.ioc.ChannelScope;
import org.mqttbee.mqtt.message.MqttMessage;

import javax.inject.Inject;

/**
 * Main encoder for MQTT messages which delegates to the individual {@link MqttMessageEncoder}s.
 *
 * @author Silvio Giebl
 */
@ChannelScope
public class MqttEncoder extends ChannelOutboundHandlerAdapter {

    public static final String NAME = "encoder";

    private final MqttMessageEncoders encoders;

    @Inject
    MqttEncoder(final MqttMessageEncoders encoders) {
        this.encoders = encoders;
    }

    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
        if (msg instanceof MqttMessage) {
            final MqttMessage message = (MqttMessage) msg;
            final MqttMessageEncoder messageEncoder = encoders.get(message.getType().getCode());
            if (messageEncoder == null) {
                throw new UnsupportedOperationException();
            }
            final ByteBuf out = messageEncoder.castAndEncode(message, ctx.alloc(),
                    MqttServerConnectionData.getMaximumPacketSize(ctx.channel()));
            ctx.write(out, promise);
        } else {
            ctx.write(msg, promise);
        }
    }

}
