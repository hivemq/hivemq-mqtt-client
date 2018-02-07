package org.mqttbee.mqtt5.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.ScheduledFuture;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt5.message.ping.Mqtt5PingReq;
import org.mqttbee.api.mqtt5.message.ping.Mqtt5PingResp;
import org.mqttbee.mqtt5.message.connack.Mqtt5ConnAckImpl;
import org.mqttbee.mqtt5.message.ping.Mqtt5PingReqImpl;

import java.util.concurrent.TimeUnit;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PingHandler extends ChannelDuplexHandler {

    public static final String NAME = "PING";
    private static final String IDLE_STATE_HANDLER_NAME = "PING_IDLE";
    private static final int PING_RESP_DELAY = 60;

    private int keepAlive;
    private ScheduledFuture<?> disconnectNoPingRespFuture;

    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise)
            throws Exception {
        if (msg instanceof Mqtt5Connect) {
            keepAlive = ((Mqtt5Connect) msg).getKeepAlive();
        }
        if (msg instanceof Mqtt5PingReq) {
            if (disconnectNoPingRespFuture == null) {
                disconnectNoPingRespFuture = ctx.executor()
                        .schedule(new DisconnectNoPingRespRunnable(ctx.channel()), PING_RESP_DELAY, TimeUnit.SECONDS);
            }
        }
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof Mqtt5ConnAckImpl) {
            final int serverKeepAlive = ((Mqtt5ConnAckImpl) msg).getRawServerKeepAlive();
            if (serverKeepAlive != Mqtt5ConnAckImpl.KEEP_ALIVE_FROM_CONNECT) {
                keepAlive = serverKeepAlive;
            }
            if (keepAlive == 0) {
                ctx.pipeline().remove(this);
            } else {
                ctx.pipeline().addAfter(NAME, IDLE_STATE_HANDLER_NAME, new IdleStateHandler(0, keepAlive, 0));
            }
        } else if (msg instanceof Mqtt5PingResp) {
            if (disconnectNoPingRespFuture != null) {
                disconnectNoPingRespFuture.cancel(true);
                disconnectNoPingRespFuture = null;
            }
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.WRITER_IDLE) {
                ctx.channel().writeAndFlush(Mqtt5PingReqImpl.INSTANCE);
                return;
            }
        }
        super.userEventTriggered(ctx, evt);
    }


    private static class DisconnectNoPingRespRunnable implements Runnable {

        private final Channel channel;

        DisconnectNoPingRespRunnable(@NotNull final Channel channel) {
            this.channel = channel;
        }

        @Override
        public void run() {
            if (!Thread.currentThread().isInterrupted()) {
                channel.close();
            }
        }

    }

}
