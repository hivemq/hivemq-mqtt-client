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
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.mqttbee.mqtt.MqttServerConnectionData;
import org.mqttbee.mqtt.message.MqttMessage;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Main encoder for MQTT messages which delegates to the individual {@link MqttMessageEncoder}s.
 *
 * @author Silvio Giebl
 */
@ChannelHandler.Sharable
@Singleton
public class MqttEncoder extends ChannelOutboundHandlerAdapter {

    public static final String NAME = "encoder.mqtt5";

    @Inject
    MqttEncoder() {
    }

    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
        if (msg instanceof MqttMessage) {
            final MqttMessage message = (MqttMessage) msg;
            final ByteBuf out = message.getEncoder()
                    .encode(message, ctx.alloc(), MqttServerConnectionData.getMaximumPacketSize(ctx.channel()));
            ctx.write(out, promise);
        } else {
            ctx.write(msg, promise);
        }
    }

}
