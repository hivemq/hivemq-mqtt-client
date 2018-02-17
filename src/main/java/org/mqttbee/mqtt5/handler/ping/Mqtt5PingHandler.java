package org.mqttbee.mqtt5.handler.ping;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.handler.util.ChannelInboundHandlerWithTimeout;
import org.mqttbee.mqtt5.message.ping.Mqtt5PingReqImpl;
import org.mqttbee.mqtt5.message.ping.Mqtt5PingRespImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PingHandler extends ChannelInboundHandlerWithTimeout {

    public static final String NAME = "ping";
    private static final String IDLE_STATE_HANDLER_NAME = "ping.idle";
    private static final int PING_RESP_TIMEOUT = 60; // TODO configurable

    private final int keepAlive;

    public Mqtt5PingHandler(final int keepAlive) {
        this.keepAlive = keepAlive;
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) {
        super.handlerAdded(ctx);
        ctx.pipeline().addAfter(NAME, IDLE_STATE_HANDLER_NAME, new IdleStateHandler(0, keepAlive, 0));
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof Mqtt5PingRespImpl) {
            cancelTimeout();
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if ((evt instanceof IdleStateEvent) && ((IdleStateEvent) evt).state() == IdleState.WRITER_IDLE) {
            ctx.writeAndFlush(Mqtt5PingReqImpl.INSTANCE).addListener(this);
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    @Override
    protected long getTimeout(@NotNull final ChannelHandlerContext ctx) {
        return PING_RESP_TIMEOUT;
    }

    @NotNull
    @Override
    protected Mqtt5DisconnectReasonCode getTimeoutReasonCode() {
        return Mqtt5DisconnectReasonCode.KEEP_ALIVE_TIMEOUT;
    }

    @NotNull
    @Override
    protected String getTimeoutReasonString() {
        return "Timeout while waiting for PINGRESP";
    }

}
