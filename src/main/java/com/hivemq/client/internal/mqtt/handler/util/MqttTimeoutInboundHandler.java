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

package com.hivemq.client.internal.mqtt.handler.util;

import com.hivemq.client.internal.annotations.CallByThread;
import com.hivemq.client.internal.mqtt.handler.MqttConnectionAwareHandler;
import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectEvent;
import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectUtil;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.ScheduledFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * ChannelInboundHandler with timeout handling. Subclasses must not be {@link io.netty.channel.ChannelHandler.Sharable}.
 *
 * @author Silvio Giebl
 */
public abstract class MqttTimeoutInboundHandler extends MqttConnectionAwareHandler
        implements Runnable, ChannelFutureListener {

    private @Nullable ScheduledFuture<?> timeoutFuture;

    /**
     * Schedules a timeout if the given future succeeded. Otherwise the channel is closed.
     *
     * @param future the future of the operation that triggers a timeout.
     */
    @Override
    public void operationComplete(final @NotNull ChannelFuture future) throws Exception {
        if (ctx == null) {
            return;
        }
        final Throwable cause = future.cause();
        if (cause == null) {
            operationSuccessful(ctx);
        } else if (!(cause instanceof IOException)) {
            exceptionCaught(ctx, cause);
        }
    }

    protected void operationSuccessful(final @NotNull ChannelHandlerContext ctx) {
        scheduleTimeout(ctx.channel());
    }

    /**
     * Invoked when a timeout happens. Sends a DISCONNECT message if the channel is still active and always closes the
     * channel.
     */
    @CallByThread("Netty EventLoop")
    @Override
    public void run() {
        if (ctx == null) {
            return;
        }
        final Channel channel = ctx.channel();
        if (channel.isActive()) {
            MqttDisconnectUtil.disconnect(channel, getTimeoutReasonCode(), getTimeoutReasonString());
        } else {
            MqttDisconnectUtil.close(channel, getTimeoutReasonString());
        }
    }

    /**
     * Schedules a timeout.
     *
     * @param channel the channel to schedule the timeout for.
     */
    @CallByThread("Netty EventLoop")
    protected void scheduleTimeout(final @NotNull Channel channel) {
        if (timeoutFuture != null) {
            timeoutFuture.cancel(false);
            timeoutFuture = null;
            run();
        } else {
            timeoutFuture = channel.eventLoop().schedule(this, getTimeout(), TimeUnit.SECONDS);
        }
    }

    /**
     * Cancels a scheduled timeout.
     */
    @CallByThread("Netty EventLoop")
    protected void cancelTimeout() {
        if (timeoutFuture != null) {
            timeoutFuture.cancel(false);
            timeoutFuture = null;
        }
    }

    @Override
    protected void onDisconnectEvent(final @NotNull MqttDisconnectEvent disconnectEvent) {
        super.onDisconnectEvent(disconnectEvent);
        cancelTimeout();
    }

    /**
     * Returns the timeout interval in seconds.
     *
     * @return the timeout interval in seconds.
     */
    protected abstract long getTimeout();

    /**
     * @return the Reason Code that will be used in the DISCONNECT message if a timeout happens and the channel is still
     *         active.
     */
    protected abstract @NotNull Mqtt5DisconnectReasonCode getTimeoutReasonCode();

    /**
     * @return the Reason String that will be used to notify the API and may also be sent with a DISCONNECT message.
     */
    protected abstract @NotNull String getTimeoutReasonString();

    @Override
    public final boolean isSharable() {
        return false;
    }
}
