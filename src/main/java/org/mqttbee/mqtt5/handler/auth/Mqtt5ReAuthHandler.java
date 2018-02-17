package org.mqttbee.mqtt5.handler.auth;

import io.netty.channel.ChannelHandlerContext;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.api.mqtt5.exception.Mqtt5MessageException;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.Mqtt5ClientDataImpl;
import org.mqttbee.mqtt5.handler.disconnect.Mqtt5DisconnectUtil;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthBuilderImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthImpl;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl;

import static org.mqttbee.api.mqtt5.message.auth.Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION;
import static org.mqttbee.api.mqtt5.message.auth.Mqtt5AuthReasonCode.REAUTHENTICATE;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ReAuthHandler extends AbstractMqtt5AuthHandler {

    public static final String NAME = "reauth";

    Mqtt5ReAuthHandler() {
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (evt instanceof Mqtt5ReAuthEvent) {
            writeReAuth(ctx);
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    private void writeReAuth(@NotNull final ChannelHandlerContext ctx) {
        final Mqtt5ClientDataImpl clientData = Mqtt5ClientDataImpl.from(ctx.channel());
        final Mqtt5EnhancedAuthProvider enhancedAuthProvider = getEnhancedAuthProvider(clientData);
        final Mqtt5AuthBuilderImpl authBuilder = getAuthBuilder(REAUTHENTICATE, enhancedAuthProvider);

        enhancedAuthProvider.onReAuth(clientData, authBuilder)
                .thenRunAsync(() -> ctx.writeAndFlush(authBuilder.build()), ctx.executor());
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof Mqtt5AuthImpl) {
            readAuth((Mqtt5AuthImpl) msg, ctx);
        } else if (msg instanceof Mqtt5DisconnectImpl) {
            readDisconnect((Mqtt5DisconnectImpl) msg, ctx);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void readAuth(@NotNull final Mqtt5AuthImpl auth, @NotNull final ChannelHandlerContext ctx) {
        cancelTimeout();

        final Mqtt5ClientDataImpl clientData = Mqtt5ClientDataImpl.from(ctx.channel());
        final Mqtt5EnhancedAuthProvider enhancedAuthProvider = getEnhancedAuthProvider(clientData);

        if (validateAuth(ctx.channel(), auth, enhancedAuthProvider)) {
            switch (auth.getReasonCode()) {
                case CONTINUE_AUTHENTICATION:
                    readAuthContinue(ctx, auth, clientData, enhancedAuthProvider);
                    break;
                case SUCCESS:
                    readAuthSuccess(ctx, auth, clientData, enhancedAuthProvider);
                    break;
                case REAUTHENTICATE:
                    readReAuth(ctx, auth, clientData, enhancedAuthProvider);
                    break;
            }
        }
    }

    private void readAuthSuccess(
            @NotNull final ChannelHandlerContext ctx, @NotNull final Mqtt5AuthImpl auth,
            @NotNull final Mqtt5ClientDataImpl clientData,
            @NotNull final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {

        enhancedAuthProvider.onReAuthSuccess(clientData, auth).thenAcceptAsync(accepted -> {
            if (!accepted) {
                Mqtt5DisconnectUtil.disconnect(
                        ctx.channel(), Mqtt5DisconnectReasonCode.NOT_AUTHORIZED, "Server auth success not accepted");
            }
        }, ctx.executor());
    }

    private void readReAuth(
            @NotNull final ChannelHandlerContext ctx, @NotNull final Mqtt5AuthImpl auth,
            @NotNull final Mqtt5ClientDataImpl clientData,
            @NotNull final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {

        if (clientData.allowsServerReAuth()) {
            final Mqtt5AuthBuilderImpl authBuilder = getAuthBuilder(CONTINUE_AUTHENTICATION, enhancedAuthProvider);

            enhancedAuthProvider.onServerReAuth(clientData, auth, authBuilder).thenAcceptAsync(accepted -> {
                if (accepted) {
                    ctx.writeAndFlush(authBuilder.build());
                } else {
                    Mqtt5DisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.NOT_AUTHORIZED,
                            new Mqtt5MessageException(auth, "Server reauth not accepted"));
                }
            }, ctx.executor());
        } else {
            Mqtt5DisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5MessageException(auth, "Server must not send AUTH with the Reason Code REAUTHENTICATE"));
        }
    }

    private void readDisconnect(
            @NotNull final Mqtt5DisconnectImpl disconnect, @NotNull final ChannelHandlerContext ctx) {

        cancelTimeout();

        final Mqtt5ClientDataImpl clientData = Mqtt5ClientDataImpl.from(ctx.channel());
        final Mqtt5EnhancedAuthProvider enhancedAuthProvider = getEnhancedAuthProvider(clientData);

        enhancedAuthProvider.onReAuthError(clientData, disconnect);
    }

    @NotNull
    @Override
    protected String getTimeoutReasonString() {
        return "Timeout while waiting for AUTH or DISCONNECT";
    }

}
