package org.mqttbee.mqtt5.handler.auth;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ClientData;
import org.mqttbee.api.mqtt.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5Auth;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5AuthBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5AuthReasonCode;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.message.auth.MqttAuthBuilderImpl;
import org.mqttbee.mqtt.message.auth.MqttAuthImpl;
import org.mqttbee.mqtt5.Mqtt5ClientDataImpl;
import org.mqttbee.mqtt5.handler.disconnect.Mqtt5DisconnectUtil;
import org.mqttbee.mqtt5.handler.util.ChannelInboundHandlerWithTimeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION;

/**
 * Base for enhanced auth handling according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
abstract class AbstractMqtt5AuthHandler extends ChannelInboundHandlerWithTimeout {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMqtt5AuthHandler.class);

    /**
     * Utility method to get the non null enhanced auth provider for the client data.
     *
     * @param clientData the data of the client.
     * @return the enhanced auth provider.
     */
    @NotNull
    static Mqtt5EnhancedAuthProvider getEnhancedAuthProvider(@NotNull final Mqtt5ClientDataImpl clientData) {
        final Mqtt5EnhancedAuthProvider enhancedAuthProvider =
                clientData.getRawClientConnectionData().getEnhancedAuthProvider();
        assert enhancedAuthProvider != null;
        return enhancedAuthProvider;
    }

    /**
     * Utility method to get a builder for a new AUTH message.
     *
     * @param reasonCode           the reason code for the AUTH message.
     * @param enhancedAuthProvider the enhanced auth provider for the new AUTH message.
     * @return a builder for a new AUTH message.
     */
    @NotNull
    static MqttAuthBuilderImpl getAuthBuilder(
            @NotNull final Mqtt5AuthReasonCode reasonCode,
            @NotNull final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {

        return new MqttAuthBuilderImpl(reasonCode, (MqttUTF8StringImpl) enhancedAuthProvider.getMethod());
    }

    static boolean enhancedAuthProviderAccepted(@Nullable final Throwable throwable) {
        if (throwable != null) {
            LOGGER.error("reauth cancelled because of an unexpected exception", throwable);
            return false;
        }
        return true;
    }

    static boolean enhancedAuthProviderAccepted(@Nullable final Boolean accepted, @Nullable final Throwable throwable) {
        if (throwable != null) {
            LOGGER.error("reauth cancelled because of an unexpected exception", throwable);
            return false;
        } else if (accepted == null) {
            LOGGER.error("reauth cancelled because of an unexpected null value");
            return false;
        }
        return accepted;
    }

    /**
     * Handles an incoming AUTH message. Sends a DISCONNECT message if the AUTH message is not valid.
     *
     * @param ctx  the channel handler context.
     * @param auth the incoming AUTH message.
     */
    final void readAuth(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttAuthImpl auth) {
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

    /**
     * Validates an incoming AUTH message.
     * <p>
     * If validation fails, disconnection and closing of the channel is already handled.
     *
     * @param channel              the channel.
     * @param auth                 the incoming AUTH message.
     * @param enhancedAuthProvider the enhanced auth provider.
     * @return true if the AUTH message is valid, otherwise false.
     */
    private boolean validateAuth(
            @NotNull final Channel channel, @NotNull final MqttAuthImpl auth,
            @NotNull final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {

        if (!auth.getMethod().equals(enhancedAuthProvider.getMethod())) {
            Mqtt5DisconnectUtil.disconnect(channel, Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5MessageException(auth, "Auth method must be the same as in the CONNECT message"));
            return false;
        }
        return true;
    }

    /**
     * Handles an incoming AUTH message with the Reason Code CONTINUE AUTHENTICATION.
     * <ul>
     * <li>Calls {@link Mqtt5EnhancedAuthProvider#onContinue(Mqtt5ClientData, Mqtt5Auth, Mqtt5AuthBuilder)}.</li>
     * <li>Sends a new AUTH message if the enhanced auth provider accepted the incoming AUTH message.</li>
     * <li>Otherwise sends a DISCONNECT message.</li>
     * </ul>
     *
     * @param ctx                  the channel handler context.
     * @param auth                 the received AUTH message.
     * @param clientData           the data of the client.
     * @param enhancedAuthProvider the enhanced auth provider.
     */
    private void readAuthContinue(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttAuthImpl auth,
            @NotNull final Mqtt5ClientDataImpl clientData,
            @NotNull final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {

        final MqttAuthBuilderImpl authBuilder = getAuthBuilder(CONTINUE_AUTHENTICATION, enhancedAuthProvider);

        enhancedAuthProvider.onContinue(clientData, auth, authBuilder).whenCompleteAsync((accepted, throwable) -> {
            if (enhancedAuthProviderAccepted(accepted, throwable)) {
                ctx.writeAndFlush(authBuilder.build()).addListener(this);
            } else {
                Mqtt5DisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.NOT_AUTHORIZED,
                        new Mqtt5MessageException(auth, "Server auth not accepted"));
            }
        }, ctx.executor());
    }

    /**
     * Disconnects on an incoming AUTH message with the Reason Code SUCCESS.
     *
     * @param ctx                  the channel handler context.
     * @param auth                 the incoming AUTH message.
     * @param clientData           the data of the client.
     * @param enhancedAuthProvider the enhanced auth provider.
     */
    abstract void readAuthSuccess(
            @NotNull ChannelHandlerContext ctx, @NotNull MqttAuthImpl auth, @NotNull Mqtt5ClientDataImpl clientData,
            @NotNull Mqtt5EnhancedAuthProvider enhancedAuthProvider);

    /**
     * Disconnects on an incoming AUTH message with the Reason Code REAUTHENTICATE.
     *
     * @param ctx                  the channel handler context.
     * @param auth                 the incoming AUTH message.
     * @param clientData           the data of the client.
     * @param enhancedAuthProvider the enhanced auth provider.
     */
    abstract void readReAuth(
            @NotNull ChannelHandlerContext ctx, @NotNull MqttAuthImpl auth, @NotNull Mqtt5ClientDataImpl clientData,
            @NotNull Mqtt5EnhancedAuthProvider enhancedAuthProvider);

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
