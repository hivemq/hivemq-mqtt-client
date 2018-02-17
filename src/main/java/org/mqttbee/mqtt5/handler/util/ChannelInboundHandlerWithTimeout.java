package org.mqttbee.mqtt5.handler.util;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ScheduledFuture;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.handler.disconnect.Mqtt5DisconnectUtil;

import java.util.concurrent.TimeUnit;

/**
 * @author Silvio Giebl
 */
public abstract class ChannelInboundHandlerWithTimeout extends ChannelInboundHandlerAdapter
        implements Runnable, GenericFutureListener<Future<? super Void>> {

    private ChannelHandlerContext ctx;
    private ScheduledFuture<?> timeoutFuture;

    protected ChannelInboundHandlerWithTimeout() {
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void operationComplete(final Future future) {
        if (future.isSuccess()) {
            scheduleTimeout(ctx);
        } else {
            Mqtt5DisconnectUtil.close(ctx.channel(), future.cause());
        }
    }

    @Override
    public void run() {
        if (!Thread.currentThread().isInterrupted()) {
            if (ctx.channel().isActive()) {
                Mqtt5DisconnectUtil.disconnect(ctx.channel(), getTimeoutReasonCode(), getTimeoutReasonString());
            } else {
                Mqtt5DisconnectUtil.close(ctx.channel(), getTimeoutReasonString());
            }
        }
    }

    private void scheduleTimeout(@NotNull final ChannelHandlerContext ctx) {
        timeoutFuture = ctx.executor().schedule(this, getTimeout(ctx), TimeUnit.SECONDS);
    }

    protected void cancelTimeout() {
        if (timeoutFuture != null) {
            timeoutFuture.cancel(true);
            timeoutFuture = null;
        }
    }

    protected abstract long getTimeout(@NotNull ChannelHandlerContext ctx);

    @NotNull
    protected abstract Mqtt5DisconnectReasonCode getTimeoutReasonCode();

    @NotNull
    protected abstract String getTimeoutReasonString();

}
