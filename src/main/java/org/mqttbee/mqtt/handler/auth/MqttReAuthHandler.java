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
import org.mqttbee.api.mqtt.exceptions.NotConnectedException;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ClientData;
import org.mqttbee.api.mqtt.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5Auth;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5AuthBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectEvent;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.mqtt.ioc.ConnectionScope;
import org.mqttbee.mqtt.message.auth.MqttAuth;
import org.mqttbee.mqtt.message.auth.MqttAuthBuilder;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;
import org.mqttbee.rx.CompletableFlow;

import static org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION;
import static org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5AuthReasonCode.REAUTHENTICATE;

/**
 * Enhanced reauth handling according during connection according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
@ConnectionScope
public class MqttReAuthHandler extends AbstractMqttAuthHandler {

    private @Nullable CompletableFlow flow;

    MqttReAuthHandler(final @NotNull MqttConnectAuthHandler connectAuthHandler) {
        super(connectAuthHandler);
    }

    void reauth(final @NotNull CompletableFlow flow) {
        if (!clientData.executeInEventLoop(() -> writeReAuth(flow))) {
            flow.onError(new NotConnectedException());
        }
    }

    /**
     * Sends a AUTH message with the Reason Code REAUTHENTICATE.
     * <p>
     * Calls {@link Mqtt5EnhancedAuthProvider#onReAuth(Mqtt5ClientData, Mqtt5AuthBuilder)}.
     *
     * @param flow the flow for the reauth result.
     */
    private void writeReAuth(final @NotNull CompletableFlow flow) {
        if (ctx == null) {
            flow.onError(new NotConnectedException());
            return;
        }
        if (state != MqttAuthState.NONE) {
            flow.onError(new UnsupportedOperationException("Reauth is still pending."));
            return;
        }

        this.flow = flow;

        final MqttAuthBuilder authBuilder = new MqttAuthBuilder(REAUTHENTICATE, getMethod());
        state = MqttAuthState.IN_PROGRESS_INIT;
        callProviderFuture(() -> authProvider.onReAuth(clientData, authBuilder), ctx -> {
            state = MqttAuthState.WAIT_FOR_SERVER;
            ctx.writeAndFlush(authBuilder.build()).addListener(this);
        }, (ctx, throwable) -> {
            callProvider(() -> authProvider.onReAuthError(clientData, throwable));
            state = MqttAuthState.NONE;
            this.flow.onError(throwable);
            this.flow = null;
        });
    }

    @Override
    public void channelRead(final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg) {
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
     * @param ctx  the channel handler context.
     * @param auth the incoming AUTH message.
     */
    void readAuthSuccess(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttAuth auth) {
        if (state != MqttAuthState.WAIT_FOR_SERVER) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5MessageException(auth,
                            "Must not receive AUTH with reason code SUCCESS in no response to a client message."));
            return;
        }

        state = MqttAuthState.IN_PROGRESS_DONE;
        callProviderFutureResult(() -> authProvider.onReAuthSuccess(clientData, auth), ctx2 -> {
            state = MqttAuthState.NONE;
            if (flow != null) {
                if (!flow.isCancelled()) {
                    flow.onComplete();
                } else {
                    LOGGER.warn("Reauth was successful but the Completable has been cancelled.");
                }
                flow = null;
            }
        }, (ctx2, throwable) -> MqttDisconnectUtil.disconnect(ctx2.channel(), Mqtt5DisconnectReasonCode.NOT_AUTHORIZED,
                new Mqtt5MessageException(auth, "Server auth success not accepted.")));
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
     * @param ctx  the channel handler context.
     * @param auth the incoming AUTH message.
     */
    void readReAuth(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttAuth auth) {
        if (!clientData.allowsServerReAuth()) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5MessageException(auth, "Must not receive an AUTH with reason code REAUTHENTICATE."));
            return;
        }
        if (state != MqttAuthState.NONE) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5MessageException(auth,
                            "Must not receive AUTH with reason code REAUTHENTICATE if reauth is still pending."));
            return;
        }

        final MqttAuthBuilder authBuilder = new MqttAuthBuilder(CONTINUE_AUTHENTICATION, getMethod());
        state = MqttAuthState.IN_PROGRESS_INIT;
        callProviderFutureResult(() -> authProvider.onServerReAuth(clientData, auth, authBuilder), ctx2 -> {
            state = MqttAuthState.WAIT_FOR_SERVER;
            ctx2.writeAndFlush(authBuilder.build()).addListener(this);

        }, (ctx2, throwable) -> MqttDisconnectUtil.disconnect(ctx2.channel(), Mqtt5DisconnectReasonCode.NOT_AUTHORIZED,
                new Mqtt5MessageException(auth, "Server reauth not accepted.")));
    }

    /**
     * Handles an incoming DISCONNECT message.
     * <p>
     * Calls {@link Mqtt5EnhancedAuthProvider#onReAuthRejected(Mqtt5ClientData, Mqtt5Disconnect)}.
     *
     * @param ctx        the channel handler context.
     * @param disconnect the incoming DISCONNECT message.
     */
    private void readDisconnect(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttDisconnect disconnect) {
        cancelTimeout();

        if (state != MqttAuthState.NONE) {
            callProvider(() -> authProvider.onReAuthRejected(clientData, disconnect));
            state = MqttAuthState.NONE;
        }

        ctx.fireChannelRead(disconnect);
    }

    /**
     * Calls {@link Mqtt5EnhancedAuthProvider#onReAuthError(Mqtt5ClientData, Throwable)} with the cause why the channel
     * was closed if reauth is still in progress.
     *
     * @param disconnectEvent the channel close event.
     */
    protected void handleDisconnectEvent(final @NotNull MqttDisconnectEvent disconnectEvent) {
        super.handleDisconnectEvent(disconnectEvent);

        if (state != MqttAuthState.NONE) {
            callProvider(() -> authProvider.onReAuthError(clientData, disconnectEvent.getCause()));
            state = MqttAuthState.NONE;
        }
        if (flow != null) {
            flow.onError(disconnectEvent.getCause());
            flow = null;
        }
    }

    @Override
    protected @NotNull String getTimeoutReasonString() {
        return "Timeout while waiting for AUTH or DISCONNECT.";
    }
}
