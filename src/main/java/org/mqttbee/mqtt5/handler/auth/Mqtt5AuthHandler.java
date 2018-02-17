package org.mqttbee.mqtt5.handler.auth;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.api.mqtt5.exception.Mqtt5MessageException;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.Mqtt5ClientDataImpl;
import org.mqttbee.mqtt5.Mqtt5Component;
import org.mqttbee.mqtt5.handler.disconnect.Mqtt5DisconnectUtil;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5EnhancedAuthBuilderImpl;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectWrapper;
import org.mqttbee.mqtt5.message.connect.connack.Mqtt5ConnAckImpl;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.mqttbee.mqtt5.handler.auth.Mqtt5AuthHandlerUtil.*;

/**
 * @author Silvio Giebl
 */
@ChannelHandler.Sharable
@Singleton
public class Mqtt5AuthHandler extends ChannelDuplexHandler {

    public static final String NAME = "auth";

    @Inject
    Mqtt5AuthHandler() {
    }

    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
        if (msg instanceof Mqtt5ConnectImpl) {
            writeConnect((Mqtt5ConnectImpl) msg, ctx, promise);
        } else {
            ctx.write(msg, promise);
        }
    }

    private void writeConnect(
            @NotNull final Mqtt5ConnectImpl connect, @NotNull final ChannelHandlerContext ctx,
            @NotNull final ChannelPromise promise) {

        final Mqtt5ClientDataImpl clientData = Mqtt5ClientDataImpl.from(ctx.channel());
        final Mqtt5EnhancedAuthProvider enhancedAuthProvider = getEnhancedAuthProvider(clientData);
        final Mqtt5EnhancedAuthBuilderImpl enhancedAuthBuilder = getEnhancedAuthBuilder(enhancedAuthProvider);

        enhancedAuthProvider.onAuth(clientData, connect, enhancedAuthBuilder).thenRunAsync(() -> {
            final Mqtt5ConnectWrapper connectWrapper =
                    connect.wrap(clientData.getRawClientIdentifier(), enhancedAuthBuilder.build());
            ctx.writeAndFlush(connectWrapper, promise);
        }, ctx.executor());
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof Mqtt5ConnAckImpl) {
            readConnAck((Mqtt5ConnAckImpl) msg, ctx);
        } else if (msg instanceof Mqtt5AuthImpl) {
            readAuth((Mqtt5AuthImpl) msg, ctx);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void readConnAck(@NotNull final Mqtt5ConnAckImpl connAck, @NotNull final ChannelHandlerContext ctx) {
        final Mqtt5ClientDataImpl clientData = Mqtt5ClientDataImpl.from(ctx.channel());
        final Mqtt5EnhancedAuthProvider enhancedAuthProvider = getEnhancedAuthProvider(clientData);

        if (connAck.getReasonCode().isError()) {
            enhancedAuthProvider.onAuthError(clientData, connAck);
            ctx.fireChannelRead(connAck);
        } else {
            if (validateEnhancedAuth(ctx.channel(), connAck, enhancedAuthProvider)) {
                enhancedAuthProvider.onAuthSuccess(clientData, connAck).thenAcceptAsync(accepted -> {
                    if (!accepted) {
                        Mqtt5DisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.NOT_AUTHORIZED,
                                new Mqtt5MessageException(connAck, "Server auth success not accepted"));
                    }
                }, ctx.executor());
                ctx.fireChannelRead(connAck);
                ctx.pipeline().replace(this, Mqtt5ReAuthHandler.NAME, Mqtt5Component.INSTANCE.reAuthHandler());
            }
        }
    }

    private void readAuth(@NotNull final Mqtt5AuthImpl auth, @NotNull final ChannelHandlerContext ctx) {
        final Mqtt5ClientDataImpl clientData = Mqtt5ClientDataImpl.from(ctx.channel());
        final Mqtt5EnhancedAuthProvider enhancedAuthProvider = getEnhancedAuthProvider(clientData);

        if (validateAuth(ctx.channel(), auth, enhancedAuthProvider)) {
            switch (auth.getReasonCode()) {
                case CONTINUE_AUTHENTICATION:
                    readAuthContinue(ctx, auth, clientData, enhancedAuthProvider);
                    break;
                case SUCCESS:
                    readAuthSuccess(ctx, auth);
                    break;
                case REAUTHENTICATE:
                    readReAuth(ctx, auth);
                    break;
            }
        }
    }

    private void readAuthSuccess(@NotNull final ChannelHandlerContext ctx, @NotNull final Mqtt5AuthImpl auth) {
        Mqtt5DisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                new Mqtt5MessageException(auth, "Server must not send an AUTH message with the Reason Code SUCCESS"));
    }

    private void readReAuth(@NotNull final ChannelHandlerContext ctx, @NotNull final Mqtt5AuthImpl auth) {
        Mqtt5DisconnectUtil.disconnect(
                ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, new Mqtt5MessageException(auth,
                        "Server must not send an AUTH message with the Reason Code REAUTHENTICATE"));
    }

}
