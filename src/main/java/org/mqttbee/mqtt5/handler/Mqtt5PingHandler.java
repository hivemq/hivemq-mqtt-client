package org.mqttbee.mqtt5.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.ScheduledFuture;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.Mqtt5Util;
import org.mqttbee.mqtt5.message.ping.Mqtt5PingReqImpl;
import org.mqttbee.mqtt5.message.ping.Mqtt5PingRespImpl;

import java.util.concurrent.TimeUnit;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PingHandler extends ChannelInboundHandlerAdapter implements Runnable {

    public static final String NAME = "ping";
    private static final String IDLE_STATE_HANDLER_NAME = "ping.idle";
    private static final int PING_RESP_TIMEOUT = 60; // TODO configurable

    private final int keepAlive;
    private Channel channel;
    private ScheduledFuture<?> disconnectNoPingRespFuture;

    Mqtt5PingHandler(final int keepAlive) {
        this.keepAlive = keepAlive;
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) {
        channel = ctx.channel();
        ctx.pipeline().addAfter(NAME, IDLE_STATE_HANDLER_NAME, new IdleStateHandler(0, keepAlive, 0));
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof Mqtt5PingRespImpl) {
            if (disconnectNoPingRespFuture != null) {
                disconnectNoPingRespFuture.cancel(true);
                disconnectNoPingRespFuture = null;
            }
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush(Mqtt5PingReqImpl.INSTANCE);
                disconnectNoPingRespFuture = ctx.executor().schedule(this, PING_RESP_TIMEOUT, TimeUnit.SECONDS);
                return;
            }
        }
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void run() {
        if (!Thread.currentThread().isInterrupted()) {
            if (channel.isActive()) {
                Mqtt5Util.disconnect(Mqtt5DisconnectReasonCode.KEEP_ALIVE_TIMEOUT, channel);
            } else {
                channel.close();
            }
        }
    }

}
