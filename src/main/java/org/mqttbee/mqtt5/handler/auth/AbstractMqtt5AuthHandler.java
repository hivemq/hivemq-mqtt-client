package org.mqttbee.mqtt5.handler.auth;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.api.mqtt5.exception.Mqtt5MessageException;
import org.mqttbee.api.mqtt5.message.auth.Mqtt5AuthReasonCode;
import org.mqttbee.api.mqtt5.message.auth.Mqtt5EnhancedAuth;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.Mqtt5ClientDataImpl;
import org.mqttbee.mqtt5.handler.disconnect.Mqtt5DisconnectUtil;
import org.mqttbee.mqtt5.handler.util.ChannelInboundHandlerWithTimeout;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthBuilderImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5EnhancedAuthBuilderImpl;
import org.mqttbee.mqtt5.message.connect.connack.Mqtt5ConnAckImpl;

import static org.mqttbee.api.mqtt5.message.auth.Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION;

/**
 * @author Silvio Giebl
 */
abstract class AbstractMqtt5AuthHandler extends ChannelInboundHandlerWithTimeout {

    @NotNull
    static Mqtt5EnhancedAuthProvider getEnhancedAuthProvider(@NotNull final Mqtt5ClientDataImpl clientData) {
        final Mqtt5EnhancedAuthProvider enhancedAuthProvider =
                clientData.getRawClientConnectionData().getEnhancedAuthProvider();
        assert enhancedAuthProvider != null;
        return enhancedAuthProvider;
    }

    @NotNull
    static Mqtt5AuthBuilderImpl getAuthBuilder(
            @NotNull final Mqtt5AuthReasonCode reasonCode, @NotNull final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {

        return new Mqtt5AuthBuilderImpl(reasonCode, (Mqtt5UTF8StringImpl) enhancedAuthProvider.getMethod());
    }

    @NotNull
    static Mqtt5EnhancedAuthBuilderImpl getEnhancedAuthBuilder(
            @NotNull final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {

        return new Mqtt5EnhancedAuthBuilderImpl((Mqtt5UTF8StringImpl) enhancedAuthProvider.getMethod());
    }

    static boolean validateAuth(
            @NotNull final Channel channel, @NotNull final Mqtt5AuthImpl auth,
            @NotNull final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {

        if (!auth.getMethod().equals(enhancedAuthProvider.getMethod())) {
            Mqtt5DisconnectUtil.disconnect(channel, Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5MessageException(auth, "Auth method must be the same as in the CONNECT message"));
            return false;
        }
        return true;
    }

    static boolean validateEnhancedAuth(
            @NotNull final Channel channel, @NotNull final Mqtt5ConnAckImpl connAck,
            @NotNull final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {

        final Mqtt5EnhancedAuth enhancedAuth = connAck.getRawEnhancedAuth();
        if (enhancedAuth == null) {
            Mqtt5DisconnectUtil.disconnect(channel, Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5MessageException(connAck, "Auth method must be present"));
            return false;
        }
        if (!enhancedAuth.getMethod().equals(enhancedAuthProvider.getMethod())) {
            Mqtt5DisconnectUtil.disconnect(channel, Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5MessageException(connAck, "Auth method must be the same as in the CONNECT message"));
            return false;
        }
        return true;
    }

    void readAuthContinue(
            @NotNull final ChannelHandlerContext ctx, @NotNull final Mqtt5AuthImpl auth,
            @NotNull final Mqtt5ClientDataImpl clientData,
            @NotNull final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {

        final Mqtt5AuthBuilderImpl authBuilder = getAuthBuilder(CONTINUE_AUTHENTICATION, enhancedAuthProvider);

        enhancedAuthProvider.onContinue(clientData, auth, authBuilder).thenAcceptAsync(accepted -> {
            if (accepted) {
                ctx.writeAndFlush(authBuilder.build()).addListener(this);
            } else {
                Mqtt5DisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.NOT_AUTHORIZED,
                        new Mqtt5MessageException(auth, "Server auth not accepted"));
            }
        }, ctx.executor());
    }

    @Override
    protected final long getTimeout(@NotNull final ChannelHandlerContext ctx) {
        return getEnhancedAuthProvider(Mqtt5ClientDataImpl.from(ctx.channel())).getTimeout();
    }

    @NotNull
    @Override
    protected final Mqtt5DisconnectReasonCode getTimeoutReasonCode() {
        return Mqtt5DisconnectReasonCode.NOT_AUTHORIZED;
    }

}
