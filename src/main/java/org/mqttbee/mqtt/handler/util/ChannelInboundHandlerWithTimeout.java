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

package org.mqttbee.mqtt.handler.util;

import io.netty.channel.*;
import io.netty.util.concurrent.ScheduledFuture;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.CallByThread;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectUtil;

import java.util.concurrent.TimeUnit;

/**
 * ChannelInboundHandler with timeout handling. Subclasses must not be {@link io.netty.channel.ChannelHandler.Sharable}.
 *
 * @author Silvio Giebl
 */
public abstract class ChannelInboundHandlerWithTimeout extends ChannelInboundHandlerAdapter
        implements Runnable, ChannelFutureListener {

    private ChannelHandlerContext ctx;
    private ScheduledFuture<?> timeoutFuture;

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Schedules a timeout if the given future succeeded. Otherwise the channel is closed.
     *
     * @param future the future of the operation that triggers a timeout.
     */
    @Override
    public void operationComplete(final ChannelFuture future) {
        if (future.isSuccess()) {
            scheduleTimeout(future.channel());
        } else {
            MqttDisconnectUtil.close(future.channel(), future.cause());
        }
    }

    /**
     * Invoked when a timeout happens. Sends a DISCONNECT message if the channel is still active and always closes the
     * channel.
     */
    @CallByThread("Netty EventLoop")
    @Override
    public void run() {
        if (!Thread.currentThread().isInterrupted()) {
            final Channel channel = ctx.channel();
            if (channel.isActive()) {
                MqttDisconnectUtil.disconnect(channel, getTimeoutReasonCode(), getTimeoutReasonString());
            } else {
                MqttDisconnectUtil.close(channel, getTimeoutReasonString());
            }
        }
    }

    /**
     * Schedules a timeout.
     */
    @CallByThread("Netty EventLoop")
    protected void scheduleTimeout(@NotNull final Channel channel) {
        timeoutFuture = channel.eventLoop().schedule(this, getTimeout(), TimeUnit.SECONDS);
    }

    /**
     * Cancels a scheduled timeout.
     */
    @CallByThread("Netty EventLoop")
    protected void cancelTimeout() {
        if (timeoutFuture != null) {
            timeoutFuture.cancel(true);
            timeoutFuture = null;
        }
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
    @NotNull
    protected abstract Mqtt5DisconnectReasonCode getTimeoutReasonCode();

    /**
     * @return the Reason String that will be used to notify the API and may also be sent with a DISCONNECT message.
     */
    @NotNull
    protected abstract String getTimeoutReasonString();

    @Override
    public final boolean isSharable() {
        return false;
    }
}
