package org.mqttbee.mqtt5.handler.auth;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.api.mqtt5.message.auth.Mqtt5AuthReasonCode;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.Mqtt5ClientDataImpl;
import org.mqttbee.mqtt5.Mqtt5Util;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthBuilderImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5EnhancedAuthBuilderImpl;

import static org.mqttbee.api.mqtt5.message.auth.Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION;

/**
 * @author Silvio Giebl
 */
class Mqtt5AuthHandlerUtil {

    private Mqtt5AuthHandlerUtil() {
    }

    @NotNull
    static Mqtt5EnhancedAuthProvider getEnhancedAuthProvider(@NotNull final Mqtt5ClientDataImpl clientData) {
        final Mqtt5EnhancedAuthProvider enhancedAuthProvider =
                clientData.getRawClientConnectionData().getEnhancedAuthProvider();
        assert enhancedAuthProvider != null;
        return enhancedAuthProvider;
    }

    static Mqtt5AuthBuilderImpl getAuthBuilder(
            @NotNull final Mqtt5AuthReasonCode reasonCode,
            @NotNull final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {

        return new Mqtt5AuthBuilderImpl(reasonCode, (Mqtt5UTF8StringImpl) enhancedAuthProvider.getMethod());
    }

    static Mqtt5EnhancedAuthBuilderImpl getEnhancedAuthBuilder(
            @NotNull final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {

        return new Mqtt5EnhancedAuthBuilderImpl((Mqtt5UTF8StringImpl) enhancedAuthProvider.getMethod());
    }

    static void writeDisconnect(@NotNull final Channel channel) {
        Mqtt5Util.disconnect(Mqtt5DisconnectReasonCode.NOT_AUTHORIZED, channel); // TODO notify API
    }

    static void readAuthContinue(
            @NotNull final ChannelHandlerContext ctx, @NotNull final Mqtt5AuthImpl auth,
            @NotNull final Mqtt5ClientDataImpl clientData,
            @NotNull final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {

        final Mqtt5AuthBuilderImpl authBuilder = getAuthBuilder(CONTINUE_AUTHENTICATION, enhancedAuthProvider);

        enhancedAuthProvider.onContinue(clientData, auth, authBuilder).thenAcceptAsync(accepted -> {
            if (accepted) {
                ctx.writeAndFlush(authBuilder.build());
            } else {
                writeDisconnect(ctx.channel());
            }
        }, ctx.executor());
    }

}
