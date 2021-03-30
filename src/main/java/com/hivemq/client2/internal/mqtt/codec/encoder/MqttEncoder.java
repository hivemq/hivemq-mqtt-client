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

import com.hivemq.client2.internal.logging.InternalLogger;
import com.hivemq.client2.internal.logging.InternalLoggerFactory;
import com.hivemq.client2.internal.mqtt.MqttClientConnectionConfig;
import com.hivemq.client2.internal.mqtt.ioc.ConnectionScope;
import com.hivemq.client2.internal.mqtt.message.MqttMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Main encoder for MQTT messages which delegates to the individual {@link MqttMessageEncoder}s.
 *
 * @author Silvio Giebl
 */
@ConnectionScope
public class MqttEncoder extends ChannelDuplexHandler {

    public static final @NotNull String NAME = "encoder";
    private static final @NotNull InternalLogger LOGGER = InternalLoggerFactory.getLogger(MqttEncoder.class);

    private final @NotNull MqttMessageEncoders encoders;
    private final @NotNull MqttEncoderContext context;

    private boolean inRead = false;
    private boolean pendingFlush = false;

    @Inject
    MqttEncoder(final @NotNull MqttMessageEncoders encoders) {
        this.encoders = encoders;
        context = new MqttEncoderContext(ByteBufAllocator.DEFAULT);
    }

    public void onConnected(final @NotNull MqttClientConnectionConfig connectionConfig) {
        context.setMaximumPacketSize(connectionConfig.getSendMaximumPacketSize());
    }

    @Override
    public void write(
            final @NotNull ChannelHandlerContext ctx,
            final @NotNull Object msg,
            final @NotNull ChannelPromise promise) {

        if (msg instanceof MqttMessage) {
            final MqttMessage message = (MqttMessage) msg;
            final MqttMessageEncoder<?> messageEncoder = encoders.get(message.getType().getCode());
            if (messageEncoder == null) {
                throw new UnsupportedOperationException();
            }
            final ByteBuf out = messageEncoder.castAndEncode(message, context);
            LOGGER.trace("Write MqttMessage {} to {}", message, ctx.channel().remoteAddress());
            ctx.write(out, promise);
        } else {
            LOGGER.trace("Write message {} to {}", msg, ctx.channel().remoteAddress());
            ctx.write(msg, promise);
        }
    }

    @Override
    public void flush(final @NotNull ChannelHandlerContext ctx) {
        if (inRead) {
            pendingFlush = true;
        } else {
            LOGGER.trace("flush");
            ctx.flush();
        }
    }

    @Override
    public void channelRead(final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg) {
        LOGGER.trace("channelRead: {} from {}", msg, ctx.channel().remoteAddress());
        inRead = true;
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(final @NotNull ChannelHandlerContext ctx) {
        LOGGER.trace("channelRead complete");
        ctx.fireChannelReadComplete();
        inRead = false;
        if (pendingFlush) {
            pendingFlush = false;
            LOGGER.trace("flush pendingFlush");
            ctx.flush();
        }
    }

    @Override
    public boolean isSharable() {
        return false;
    }
}
