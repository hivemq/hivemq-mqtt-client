/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.client2.internal.mqtt.handler.auth;

import com.hivemq.client2.internal.logging.InternalLogger;
import com.hivemq.client2.internal.logging.InternalLoggerFactory;
import com.hivemq.client2.internal.mqtt.exceptions.MqttClientStateExceptions;
import com.hivemq.client2.internal.mqtt.handler.disconnect.MqttDisconnectEvent;
import com.hivemq.client2.internal.mqtt.handler.disconnect.MqttDisconnectUtil;
import com.hivemq.client2.internal.mqtt.ioc.ConnectionScope;
import com.hivemq.client2.internal.mqtt.message.auth.MqttAuth;
import com.hivemq.client2.internal.mqtt.message.auth.MqttAuthBuilder;
import com.hivemq.client2.internal.mqtt.message.disconnect.MqttDisconnect;
import com.hivemq.client2.internal.rx.CompletableFlow;
import com.hivemq.client2.mqtt.mqtt5.Mqtt5ClientConfig;
import com.hivemq.client2.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism;
import com.hivemq.client2.mqtt.mqtt5.exceptions.Mqtt5AuthException;
import com.hivemq.client2.mqtt.mqtt5.message.auth.Mqtt5Auth;
import com.hivemq.client2.mqtt.mqtt5.message.auth.Mqtt5AuthBuilder;
import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.hivemq.client2.mqtt.mqtt5.message.auth.Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION;
import static com.hivemq.client2.mqtt.mqtt5.message.auth.Mqtt5AuthReasonCode.REAUTHENTICATE;

/**
 * Enhanced reauth handling according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
@ConnectionScope
public class MqttReAuthHandler extends AbstractMqttAuthHandler {

    private static final @NotNull InternalLogger LOGGER = InternalLoggerFactory.getLogger(MqttReAuthHandler.class);

    private @Nullable CompletableFlow flow;

    MqttReAuthHandler(final @NotNull MqttConnectAuthHandler connectAuthHandler) {
        super(connectAuthHandler.clientConfig, connectAuthHandler.authMechanism);
    }

    void reauth(final @NotNull CompletableFlow flow) {
        if (!clientConfig.executeInEventLoop(() -> writeReAuth(flow))) {
            flow.onError(MqttClientStateExceptions.notConnected());
        }
    }

    /**
     * <ul>
     *   <li>Errors the flow if the client is not connected, or
     *   <li>Errors the flow if reauthentication is pending, or
     *   <li>Calls {@link Mqtt5EnhancedAuthMechanism#onReAuth(Mqtt5ClientConfig, Mqtt5AuthBuilder)} which can add
     *     enhanced auth data to the outgoing AUTH message, then
     *   <li>Sends the AUTH message with the reason code REAUTHENTICATE, or
     *   <li>Calls {@link Mqtt5EnhancedAuthMechanism#onReAuthError(Mqtt5ClientConfig, Throwable)} if the enhanced auth
     *     mechanism rejected the reauthentication (it does not disconnect).
     * </ul>
     *
     * @param flow the flow for the reauth result.
     */
    private void writeReAuth(final @NotNull CompletableFlow flow) {
        if (ctx == null) {
            flow.onError(MqttClientStateExceptions.notConnected());
            return;
        }
        if (state != MqttAuthState.NONE) {
            flow.onError(new UnsupportedOperationException("Reauth is still pending."));
            return;
        }

        this.flow = flow;

        final MqttAuthBuilder authBuilder = new MqttAuthBuilder(REAUTHENTICATE, getMethod());
        state = MqttAuthState.IN_PROGRESS_INIT;
        callMechanismFuture(() -> authMechanism.onReAuth(clientConfig, authBuilder), ctx -> {
            state = MqttAuthState.WAIT_FOR_SERVER;
            MqttAuth auth = authBuilder.build();
            LOGGER.debug("Write re-AUTH {}", auth);
            ctx.writeAndFlush(auth).addListener(this);
        }, (ctx, throwable) -> {
            callMechanism(() -> authMechanism.onReAuthError(clientConfig, throwable));
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
     * Handles an incoming AUTH message with the reason code SUCCESS.
     * <ul>
     *   <li>Sends a DISCONNECT message if client side authentication is pending, or
     *   <li>Calls {@link Mqtt5EnhancedAuthMechanism#onReAuthSuccess(Mqtt5ClientConfig, Mqtt5Auth)}, then
     *   <li>Completes the flow, or
     *   <li>Sends a DISCONNECT message if the enhanced auth mechanism rejected the incoming AUTH message, which leads
     *     to {@link #onDisconnectEvent} being called.
     * </ul>
     *
     * @param ctx  the channel handler context.
     * @param auth the incoming AUTH message.
     */
    @Override
    void readAuthSuccess(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttAuth auth) {
        LOGGER.debug("Read AUTH success {}", auth);
        if (state != MqttAuthState.WAIT_FOR_SERVER) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5AuthException(auth,
                            "Must not receive AUTH with reason code SUCCESS if client side AUTH is pending."));
            return;
        }

        state = MqttAuthState.IN_PROGRESS_DONE;
        callMechanismFutureResult(() -> authMechanism.onReAuthSuccess(clientConfig, auth), ctx2 -> {
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
                new Mqtt5AuthException(auth, "Server AUTH with reason code SUCCESS not accepted.")));
    }

    /**
     * Handles an incoming AUTH message with the Reason Code REAUTHENTICATE.
     * <ul>
     *   <li>Sends a DISCONNECT message if reauthentication is already pending, or
     *   <li>Calls {@link Mqtt5EnhancedAuthMechanism#onServerReAuth(Mqtt5ClientConfig, Mqtt5Auth, Mqtt5AuthBuilder)}
     *     which can add enhanced auth data to the outgoing AUTH message, then
     *   <li>Sends the AUTH message with the reason code CONTINUE AUTHENTICATION, or
     *   <li>Sends a DISCONNECT message if the enhanced auth mechanism rejected the incoming AUTH message, which leads
     *     to {@link #onDisconnectEvent} being called.
     * </ul>
     *
     * @param ctx  the channel handler context.
     * @param auth the incoming AUTH message.
     */
    @Override
    void readReAuth(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttAuth auth) {
        LOGGER.debug("Read re-AUTH {}", auth);
        if (state != MqttAuthState.NONE) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5AuthException(auth,
                            "Must not receive AUTH with reason code REAUTHENTICATE if reauth is still pending."));
            return;
        }

        final MqttAuthBuilder authBuilder = new MqttAuthBuilder(CONTINUE_AUTHENTICATION, getMethod());
        state = MqttAuthState.IN_PROGRESS_INIT;
        callMechanismFutureResult(() -> authMechanism.onServerReAuth(clientConfig, auth, authBuilder), ctx2 -> {
            state = MqttAuthState.WAIT_FOR_SERVER;
            MqttAuth auth2 = authBuilder.build();
            LOGGER.debug("Write AUTH {}", auth2);
            ctx2.writeAndFlush(auth2).addListener(this);

        }, (ctx2, throwable) -> MqttDisconnectUtil.disconnect(ctx2.channel(), Mqtt5DisconnectReasonCode.NOT_AUTHORIZED,
                new Mqtt5AuthException(auth, "Server AUTH with reason code REAUTHENTICATE not accepted.")));
    }

    /**
     * Handles an incoming DISCONNECT message.
     * <p>
     * Calls {@link Mqtt5EnhancedAuthMechanism#onReAuthRejected(Mqtt5ClientConfig, Mqtt5Disconnect)}.
     *
     * @param ctx        the channel handler context.
     * @param disconnect the incoming DISCONNECT message.
     */
    private void readDisconnect(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttDisconnect disconnect) {
        LOGGER.trace("Read DISCONNECT {}", disconnect);
        cancelTimeout();

        if (state != MqttAuthState.NONE) {
            callMechanism(() -> authMechanism.onReAuthRejected(clientConfig, disconnect));
            state = MqttAuthState.NONE;
        }

        ctx.fireChannelRead(disconnect);
    }

    /**
     * Calls {@link Mqtt5EnhancedAuthMechanism#onReAuthError(Mqtt5ClientConfig, Throwable)} with the cause why the
     * connection was disconnected if reauth is still in progress.
     *
     * @param disconnectEvent the channel close event.
     */
    @Override
    protected void onDisconnectEvent(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttDisconnectEvent disconnectEvent) {

        super.onDisconnectEvent(ctx, disconnectEvent);

        if (state != MqttAuthState.NONE) {
            callMechanism(() -> authMechanism.onReAuthError(clientConfig, disconnectEvent.getCause()));
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
