package org.mqttbee.mqtt5.handler.util;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;

/**
 * @author Silvio Giebl
 */
public interface DefaultChannelOutboundHandler extends ChannelOutboundHandler {

    @Override
    default void bind(final ChannelHandlerContext ctx, final SocketAddress localAddress, final ChannelPromise promise) {
        ctx.bind(localAddress, promise);
    }

    @Override
    default void connect(
            final ChannelHandlerContext ctx, final SocketAddress remoteAddress, final SocketAddress localAddress,
            final ChannelPromise promise) {
        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Override
    default void disconnect(final ChannelHandlerContext ctx, final ChannelPromise promise) {
        ctx.disconnect(promise);
    }

    @Override
    default void close(final ChannelHandlerContext ctx, final ChannelPromise promise) {
        ctx.close(promise);
    }

    @Override
    default void deregister(final ChannelHandlerContext ctx, final ChannelPromise promise) {
        ctx.deregister(promise);
    }

    @Override
    default void read(final ChannelHandlerContext ctx) {
        ctx.read();
    }

    @Override
    default void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
        ctx.write(msg, promise);
    }

    @Override
    default void flush(final ChannelHandlerContext ctx) {
        ctx.flush();
    }

}
