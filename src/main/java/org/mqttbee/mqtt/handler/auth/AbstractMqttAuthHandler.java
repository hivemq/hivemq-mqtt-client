/*
 * Copyright 2018 The MQTT Bee project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.mqttbee.mqtt.handler.auth;

import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ClientData;
import org.mqttbee.api.mqtt.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5Auth;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5AuthBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.mqtt.handler.util.MqttTimeoutInboundHandler;
import org.mqttbee.mqtt.message.auth.MqttAuth;
import org.mqttbee.mqtt.message.auth.MqttAuthBuilder;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.util.Checks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION;

/**
 * Base for enhanced auth handling according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
abstract class AbstractMqttAuthHandler extends MqttTimeoutInboundHandler implements MqttAuthHandler {

    static final @NotNull Logger LOGGER = LoggerFactory.getLogger(AbstractMqttAuthHandler.class);

    enum MqttAuthState {
        NONE,
        WAIT_FOR_SERVER,
        IN_PROGRESS_INIT,
        IN_PROGRESS_RESPONSE,
        IN_PROGRESS_DONE
    }

    static boolean enhancedAuthProviderAccepted(final @Nullable Throwable throwable) {
        if (throwable != null) {
            LOGGER.error("auth cancelled because of an unexpected exception", throwable);
            return false;
        }
        return true;
    }

    static boolean enhancedAuthProviderAccepted(final @Nullable Boolean accepted, final @Nullable Throwable throwable) {
        if (throwable != null) {
            LOGGER.error("auth cancelled because of an unexpected exception", throwable);
            return false;
        } else if (accepted == null) {
            LOGGER.error("auth cancelled because of an unexpected null value");
            return false;
        }
        return accepted;
    }

    private static @NotNull MqttUTF8StringImpl getMethod(final @NotNull Mqtt5EnhancedAuthProvider authProvider) {
        return Checks.notImplemented(authProvider.getMethod(), MqttUTF8StringImpl.class, "Auth method");
    }

    final @NotNull MqttClientData clientData;
    final @NotNull Mqtt5EnhancedAuthProvider authProvider;
    final @NotNull MqttUTF8StringImpl authMethod;
    @NotNull MqttAuthState state = MqttAuthState.NONE;

    AbstractMqttAuthHandler(final @NotNull MqttClientData clientData, final @NotNull MqttConnect connect) {
        this.clientData = clientData;
        final Mqtt5EnhancedAuthProvider authProvider = connect.getRawEnhancedAuthProvider();
        assert authProvider != null;
        this.authProvider = authProvider;
        authMethod = getMethod(authProvider);
    }

    AbstractMqttAuthHandler(final @NotNull MqttConnectAuthHandler connectAuthHandler) {
        this.clientData = connectAuthHandler.clientData;
        this.authProvider = connectAuthHandler.authProvider;
        authMethod = getMethod(authProvider);
    }

    /**
     * Handles an incoming AUTH message. Sends a DISCONNECT message if the AUTH message is not valid.
     *
     * @param ctx  the channel handler context.
     * @param auth the incoming AUTH message.
     */
    final void readAuth(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttAuth auth) {
        cancelTimeout();

        if (validateAuth(ctx, auth)) {
            switch (auth.getReasonCode()) {
                case CONTINUE_AUTHENTICATION:
                    readAuthContinue(ctx, auth);
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

    /**
     * Validates an incoming AUTH message.
     * <p>
     * If validation fails, disconnection and closing of the channel is already handled.
     *
     * @param ctx  the channel handler context.
     * @param auth the incoming AUTH message.
     * @return true if the AUTH message is valid, otherwise false.
     */
    private boolean validateAuth(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttAuth auth) {
        if (!auth.getMethod().equals(authMethod)) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5MessageException(auth, "Auth method in AUTH must be the same as in the CONNECT"));
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
     * @param ctx  the channel handler context.
     * @param auth the received AUTH message.
     */
    private void readAuthContinue(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttAuth auth) {
        if (state != MqttAuthState.WAIT_FOR_SERVER) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "Must not receive an AUTH with Reason Code CONTINUE_AUTHENTICATION in no response to a client message");
        }

        final MqttAuthBuilder authBuilder = new MqttAuthBuilder(CONTINUE_AUTHENTICATION, authMethod);
        state = MqttAuthState.IN_PROGRESS_RESPONSE;
        authProvider.onContinue(clientData, auth, authBuilder).whenCompleteAsync((accepted, throwable) -> {
            if (enhancedAuthProviderAccepted(accepted, throwable)) {
                state = MqttAuthState.WAIT_FOR_SERVER;
                ctx.writeAndFlush(authBuilder.build()).addListener(this);
            } else {
                MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.NOT_AUTHORIZED,
                        new Mqtt5MessageException(auth, "Server auth not accepted"));
            }
        }, clientData.getEventLoop());
    }

    /**
     * Disconnects on an incoming AUTH message with the Reason Code SUCCESS.
     *
     * @param ctx  the channel handler context.
     * @param auth the incoming AUTH message.
     */
    abstract void readAuthSuccess(@NotNull ChannelHandlerContext ctx, @NotNull MqttAuth auth);

    /**
     * Disconnects on an incoming AUTH message with the Reason Code REAUTHENTICATE.
     *
     * @param ctx  the channel handler context.
     * @param auth the incoming AUTH message.
     */
    abstract void readReAuth(@NotNull ChannelHandlerContext ctx, @NotNull MqttAuth auth);

    @Override
    protected final long getTimeout() {
        return authProvider.getTimeout();
    }

    @Override
    protected final @NotNull Mqtt5DisconnectReasonCode getTimeoutReasonCode() {
        return Mqtt5DisconnectReasonCode.NOT_AUTHORIZED;
    }

}
