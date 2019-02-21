/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.handler.ping;

import com.hivemq.client.internal.mqtt.handler.MqttConnectionAwareHandler;
import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectEvent;
import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectUtil;
import com.hivemq.client.internal.mqtt.ioc.ConnectionScope;
import com.hivemq.client.internal.mqtt.message.ping.MqttPingReq;
import com.hivemq.client.internal.mqtt.message.ping.MqttPingResp;
import com.hivemq.client.internal.util.netty.DefaultChannelOutboundHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.ScheduledFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

/**
 * MQTT Keep Alive Handling.
 * <ul>
 * <li>Sends a PINGREQ message when no write has been performed for the Keep Alive interval.</li>
 * <li>Disconnects or closes the channel if the PINGRESP message is not received in the timeout.</li>
 * </ul>
 *
 * @author Silvio Giebl
 */
@ConnectionScope
public class MqttPingHandler extends MqttConnectionAwareHandler
        implements DefaultChannelOutboundHandler, Runnable, ChannelFutureListener {

    public static final @NotNull String NAME = "ping";
    private static final boolean PINGRESP_REQUIRED = false; // TODO configurable

    private final long keepAliveNanos;
    private long lastFlushTimeNanos;
    private boolean pingReqWritten;
    private boolean pingReqFlushed;
    private boolean messageRead;
    private @Nullable ScheduledFuture<?> timeoutFuture;

    public MqttPingHandler(final int keepAlive) {
        keepAliveNanos = TimeUnit.SECONDS.toNanos(keepAlive) - TimeUnit.MILLISECONDS.toNanos(100);
    }

    @Override
    public void handlerAdded(final @NotNull ChannelHandlerContext ctx) {
        super.handlerAdded(ctx);
        lastFlushTimeNanos = System.nanoTime();
        schedule(ctx, keepAliveNanos);
    }

    @Override
    public void flush(final @NotNull ChannelHandlerContext ctx) {
        lastFlushTimeNanos = System.nanoTime();
        ctx.flush();
    }

    @Override
    public void channelRead(final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg) {
        if (msg instanceof MqttPingResp) {
            messageRead = true;
        } else {
            messageRead = !PINGRESP_REQUIRED;
            ctx.fireChannelRead(msg);
        }
    }

    private void schedule(final @NotNull ChannelHandlerContext ctx, final long delayNanos) {
        timeoutFuture = ctx.executor().schedule(this, delayNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public void run() {
        if (ctx == null) {
            return;
        }
        if (pingReqWritten) {
            if (!pingReqFlushed) {
                MqttDisconnectUtil.close(ctx.channel(), "Timeout while writing PINGREQ");
                return;
            }
            if (!messageRead) {
                MqttDisconnectUtil.close(ctx.channel(), "Timeout while waiting for PINGRESP");
                return;
            }
        }
        pingReqFlushed = false;
        messageRead = false;
        final long nextDelayNanos = keepAliveNanos - (System.nanoTime() - lastFlushTimeNanos);
        if (nextDelayNanos > 1_000) {
            pingReqWritten = false;
            schedule(ctx, nextDelayNanos);
        } else {
            pingReqWritten = true;
            schedule(ctx, keepAliveNanos);
            ctx.writeAndFlush(MqttPingReq.INSTANCE).addListener(this);
        }
    }

    @Override
    public void operationComplete(final @NotNull ChannelFuture future) {
        if (future.isSuccess()) {
            pingReqFlushed = true;
        } else {
            MqttDisconnectUtil.close(future.channel(), future.cause());
        }
    }

    @Override
    protected void onDisconnectEvent(final @NotNull MqttDisconnectEvent disconnectEvent) {
        super.onDisconnectEvent(disconnectEvent);
        if (timeoutFuture != null) {
            timeoutFuture.cancel(false);
            timeoutFuture = null;
        }
    }
}
