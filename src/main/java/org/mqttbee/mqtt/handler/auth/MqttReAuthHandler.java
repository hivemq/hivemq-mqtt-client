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
import io.reactivex.CompletableEmitter;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ClientData;
import org.mqttbee.api.mqtt.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5Auth;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5AuthBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.handler.disconnect.ChannelCloseEvent;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.mqtt.ioc.ChannelScope;
import org.mqttbee.mqtt.message.auth.MqttAuth;
import org.mqttbee.mqtt.message.auth.MqttAuthBuilder;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;

import javax.inject.Inject;

import static org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION;
import static org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5AuthReasonCode.REAUTHENTICATE;

/**
 * Enhanced reauth handling according during connection according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
@ChannelScope
public class MqttReAuthHandler extends AbstractMqttAuthHandler {

    public static final String NAME = "reauth";

    /**
     * Flag indicating whether enhanced reauth is done. It is true when
     * <ul>
     * <li>{@link Mqtt5EnhancedAuthProvider#onReAuthSuccess(Mqtt5ClientData, Mqtt5Auth)},</li>
     * <li>{@link Mqtt5EnhancedAuthProvider#onReAuthRejected(Mqtt5ClientData, Mqtt5Disconnect)} or</li>
     * <li>{@link Mqtt5EnhancedAuthProvider#onReAuthError(Mqtt5ClientData, Throwable)}</li>
     * </ul>
     * have been called.
     */
    private boolean done;
    private CompletableEmitter reAuthEmitter;

    @Inject
    MqttReAuthHandler() {
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (evt instanceof MqttReAuthEvent) {
            writeReAuth(ctx, (MqttReAuthEvent) evt);
        } else if (evt instanceof ChannelCloseEvent) {
            handleChannelCloseEvent(ctx, (ChannelCloseEvent) evt);
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    /**
     * Sends a AUTH message with the Reason Code REAUTHENTICATE. Calls
     * {@link Mqtt5EnhancedAuthProvider#onReAuth(Mqtt5ClientData, Mqtt5AuthBuilder)}.
     *
     * @param ctx         the channel handler context.
     * @param reAuthEvent the reauth event.
     */
    private void writeReAuth(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttReAuthEvent reAuthEvent) {
        reAuthEmitter = reAuthEvent.getReAuthEmitter();

        final MqttClientData clientData = MqttClientData.from(ctx.channel());
        final Mqtt5EnhancedAuthProvider enhancedAuthProvider = getEnhancedAuthProvider(clientData);
        final MqttAuthBuilder authBuilder = getAuthBuilder(REAUTHENTICATE, enhancedAuthProvider);

        enhancedAuthProvider.onReAuth(clientData, authBuilder).whenCompleteAsync((aVoid, throwable) -> {
            if (enhancedAuthProviderAccepted(throwable)) {
                done = false;
                ctx.writeAndFlush(authBuilder.build());
            } else {
                enhancedAuthProvider.onReAuthError(clientData, throwable);
                reAuthEmitter.onError(throwable);
                reAuthEmitter = null;
            }
        }, ctx.executor());
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof MqttAuth) {
            readAuth(ctx, (MqttAuth) msg);
        } else if (msg instanceof MqttDisconnect) {
            readDisconnect(ctx, (MqttDisconnect) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    /**
     * Handles an incoming AUTH message with the Reason Code SUCCESS.
     * <ul>
     * <li>Calls {@link Mqtt5EnhancedAuthProvider#onReAuthSuccess(Mqtt5ClientData, Mqtt5Auth)}.</li>
     * <li>Sends a DISCONNECT message if the enhanced auth provider did not accept the AUTH message.</li>
     * </ul>
     *
     * @param ctx                  the channel handler context.
     * @param auth                 the incoming AUTH message.
     * @param clientData           the data of the client.
     * @param enhancedAuthProvider the enhanced auth provider.
     */
    void readAuthSuccess(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttAuth auth,
            @NotNull final MqttClientData clientData,
            @NotNull final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {

        enhancedAuthProvider.onReAuthSuccess(clientData, auth).whenCompleteAsync((accepted, throwable) -> {
            if (enhancedAuthProviderAccepted(accepted, throwable)) {
                reAuthEmitter.onComplete();
                reAuthEmitter = null;
            } else {
                MqttDisconnectUtil.disconnect(
                        ctx.channel(), Mqtt5DisconnectReasonCode.NOT_AUTHORIZED, "Server auth success not accepted");
            }
        }, ctx.executor());
        done = true;
    }

    /**
     * Handles an incoming AUTH message with the Reason Code REAUTHENTICATE.
     * <ul>
     * <li>Sends a DISCONNECT message if server reauth is not allowed.</li>
     * <li>Otherwise calls
     * {@link Mqtt5EnhancedAuthProvider#onServerReAuth(Mqtt5ClientData, Mqtt5Auth, Mqtt5AuthBuilder)}.</li>
     * <li>Sends a new AUTH message if the enhanced auth provider accepted the incoming AUTH message.</li>
     * <li>Otherwise sends a DISCONNECT message.</li>
     * </ul>
     *
     * @param ctx                  the channel handler context.
     * @param auth                 the incoming AUTH message.
     * @param clientData           the data of the client.
     * @param enhancedAuthProvider the enhanced auth provider.
     */
    void readReAuth(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttAuth auth,
            @NotNull final MqttClientData clientData,
            @NotNull final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {

        if (clientData.allowsServerReAuth()) {
            final MqttAuthBuilder authBuilder = getAuthBuilder(CONTINUE_AUTHENTICATION, enhancedAuthProvider);

            enhancedAuthProvider.onServerReAuth(clientData, auth, authBuilder)
                    .whenCompleteAsync((accepted, throwable) -> {
                        if (enhancedAuthProviderAccepted(accepted, throwable)) {
                            ctx.writeAndFlush(authBuilder.build());
                        } else {
                            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.NOT_AUTHORIZED,
                                    new Mqtt5MessageException(auth, "Server reauth not accepted"));
                        }
                    }, ctx.executor());
            done = false;
        } else {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5MessageException(auth, "Server must not send AUTH with the Reason Code REAUTHENTICATE"));
        }
    }

    /**
     * Handles an incoming DISCONNECT message. Calls
     * {@link Mqtt5EnhancedAuthProvider#onReAuthRejected(Mqtt5ClientData, Mqtt5Disconnect)}.
     *
     * @param ctx        the channel handler context.
     * @param disconnect the incoming DISCONNECT message.
     */
    private void readDisconnect(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttDisconnect disconnect) {

        cancelTimeout();

        final MqttClientData clientData = MqttClientData.from(ctx.channel());
        final Mqtt5EnhancedAuthProvider enhancedAuthProvider = getEnhancedAuthProvider(clientData);

        enhancedAuthProvider.onReAuthRejected(clientData, disconnect);
        done = true;

        ctx.fireChannelRead(disconnect);
    }

    /**
     * Calls {@link Mqtt5EnhancedAuthProvider#onReAuthError(Mqtt5ClientData, Throwable)} with the cause why the channel
     * was closed if reauth is not {@link #done} yet.
     *
     * @param ctx               the channel handler context.
     * @param channelCloseEvent the channel close event.
     */
    private void handleChannelCloseEvent(
            @NotNull final ChannelHandlerContext ctx, @NotNull final ChannelCloseEvent channelCloseEvent) {

        cancelTimeout();

        if (!done) {
            final MqttClientData clientData = MqttClientData.from(ctx.channel());
            final Mqtt5EnhancedAuthProvider enhancedAuthProvider = getEnhancedAuthProvider(clientData);

            enhancedAuthProvider.onReAuthError(clientData, channelCloseEvent.getCause());
            done = true;
        }

        if (reAuthEmitter != null) {
            reAuthEmitter.onError(channelCloseEvent.getCause());
            reAuthEmitter = null;
        }
    }

    @NotNull
    @Override
    protected String getTimeoutReasonString() {
        return "Timeout while waiting for AUTH or DISCONNECT";
    }

}
