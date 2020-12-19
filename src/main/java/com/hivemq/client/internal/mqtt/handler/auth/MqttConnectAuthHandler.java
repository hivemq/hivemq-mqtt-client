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

package com.hivemq.client.internal.mqtt.handler.auth;

import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectEvent;
import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectUtil;
import com.hivemq.client.internal.mqtt.ioc.ConnectionScope;
import com.hivemq.client.internal.mqtt.message.auth.MqttAuth;
import com.hivemq.client.internal.mqtt.message.auth.MqttEnhancedAuthBuilder;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnAck;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.internal.mqtt.message.connect.MqttStatefulConnect;
import com.hivemq.client.internal.netty.DefaultChannelOutboundHandler;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConfig;
import com.hivemq.client.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5AuthException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuth;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuthBuilder;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Enhanced auth handling during connection according to the MQTT 5 specification.
 * <p>
 * After successful connection the handler replaces itself with a {@link MqttReAuthHandler}.
 *
 * @author Silvio Giebl
 */
@ConnectionScope
public class MqttConnectAuthHandler extends AbstractMqttAuthHandler implements DefaultChannelOutboundHandler {

    @Inject
    MqttConnectAuthHandler(final @NotNull MqttClientConfig clientConfig, final @NotNull MqttConnect connect) {
        super(clientConfig, Checks.stateNotNull(connect.getRawEnhancedAuthMechanism(), "Auth mechanism"));
    }

    @Override
    public void write(
            final @NotNull ChannelHandlerContext ctx,
            final @NotNull Object msg,
            final @NotNull ChannelPromise promise) {

        if (msg instanceof MqttConnect) {
            writeConnect((MqttConnect) msg, promise);
        } else {
            ctx.write(msg, promise);
        }
    }

    /**
     * Handles the outgoing CONNECT message.
     * <ul>
     *   <li>Calls {@link Mqtt5EnhancedAuthMechanism#onAuth(Mqtt5ClientConfig, Mqtt5Connect, Mqtt5EnhancedAuthBuilder)}
     *     which can add enhanced auth data to the outgoing CONNECT message, then
     *   <li>Sends the CONNECT message with the enhanced auth data, or
     *   <li>Closes the connection with a {@link ConnectionFailedException} if the enhanced auth mechanism rejected the
     *     CONNECT message, which leads to {@link #onDisconnectEvent} being called.
     * </ul>
     *
     * @param connect the CONNECT message.
     * @param promise the write promise of the CONNECT message.
     */
    private void writeConnect(final @NotNull MqttConnect connect, final @NotNull ChannelPromise promise) {
        final MqttEnhancedAuthBuilder enhancedAuthBuilder = new MqttEnhancedAuthBuilder(getMethod());
        state = MqttAuthState.IN_PROGRESS_INIT;
        callMechanismFuture(() -> authMechanism.onAuth(clientConfig, connect, enhancedAuthBuilder), ctx -> {
            state = MqttAuthState.WAIT_FOR_SERVER;
            final MqttStatefulConnect statefulConnect =
                    connect.createStateful(clientConfig.getRawClientIdentifier(), enhancedAuthBuilder.build());
            ctx.writeAndFlush(statefulConnect, promise).addListener(this);

        }, (ctx, throwable) -> MqttDisconnectUtil.close(ctx.channel(), new ConnectionFailedException(throwable)));
    }

    @Override
    public void channelRead(final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg) {
        if (msg instanceof MqttConnAck) {
            readConnAck(ctx, (MqttConnAck) msg);
        } else if (msg instanceof MqttAuth) {
            readAuth(ctx, (MqttAuth) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    /**
     * Handles the incoming CONNACK message.
     * <p>
     * Sends a DISCONNECT message if the CONNACK message has a successful reason code but is not valid.
     *
     * @param ctx     the channel handler context.
     * @param connAck the received CONNACK message.
     */
    private void readConnAck(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttConnAck connAck) {
        cancelTimeout();

        if (connAck.getReasonCode().isError()) {
            readConnAckError(ctx, connAck);
        } else if (validateConnAck(ctx, connAck)) {
            readConnAckSuccess(ctx, connAck);
        }
    }

    /**
     * Handles the incoming CONNACK message with an error reason code.
     * <ul>
     *   <li>Calls {@link Mqtt5EnhancedAuthMechanism#onAuthRejected(Mqtt5ClientConfig, Mqtt5ConnAck)} and
     *   <li>Fires a disconnect event with a {@link Mqtt5ConnAckException} which leads to closing the connection.
     * </ul>
     *
     * @param ctx     the channel handler context.
     * @param connAck the received CONNACK message.
     */
    private void readConnAckError(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttConnAck connAck) {
        callMechanism(() -> authMechanism.onAuthRejected(clientConfig, connAck));
        state = MqttAuthState.NONE;

        MqttDisconnectUtil.fireDisconnectEvent(ctx.channel(), new Mqtt5ConnAckException(connAck,
                        "CONNECT failed as CONNACK contained an Error Code: " + connAck.getReasonCode() + "."),
                MqttDisconnectSource.SERVER);
    }

    /**
     * Handles the incoming CONNACK message with a successful reason code.
     * <ul>
     *   <li>Sends a DISCONNECT message if client side authentication is pending, or
     *   <li>Calls {@link Mqtt5EnhancedAuthMechanism#onAuthSuccess(Mqtt5ClientConfig, Mqtt5ConnAck)}, then
     *   <li>Fires the CONNACK message to the next handler, or
     *   <li>Sends a DISCONNECT message if the enhanced auth mechanism rejected the CONNACK message.
     * </ul>
     *
     * @param ctx     the channel handler context.
     * @param connAck the received CONNACK message.
     */
    private void readConnAckSuccess(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttConnAck connAck) {
        if (state != MqttAuthState.WAIT_FOR_SERVER) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5ConnAckException(connAck,
                            "Must not receive CONNACK with reason code SUCCESS if client side AUTH is pending."));
            return;
        }

        state = MqttAuthState.IN_PROGRESS_DONE;
        callMechanismFutureResult(() -> authMechanism.onAuthSuccess(clientConfig, connAck), ctx2 -> {
            state = MqttAuthState.NONE;
            ctx2.pipeline().replace(this, NAME, new MqttReAuthHandler(this));
            ctx2.fireChannelRead(connAck);

        }, (ctx2, throwable) -> MqttDisconnectUtil.disconnect(ctx2.channel(), Mqtt5DisconnectReasonCode.NOT_AUTHORIZED,
                new Mqtt5ConnAckException(connAck, "Server CONNACK with reason code SUCCESS not accepted.")));
    }

    /**
     * Validates the enhanced auth data of the incoming CONNACK message.
     * <p>
     * If validation fails, disconnection and closing of the channel is already handled.
     *
     * @param ctx     the channel handler context.
     * @param connAck the incoming CONNACK message.
     * @return true if the enhanced auth data of the CONNACK message is valid, otherwise false.
     */
    private boolean validateConnAck(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttConnAck connAck) {
        final Mqtt5EnhancedAuth enhancedAuth = connAck.getRawEnhancedAuth();
        if (enhancedAuth == null) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5ConnAckException(connAck, "Auth method in CONNACK must be present."));
            return false;
        }
        if (!enhancedAuth.getMethod().equals(getMethod())) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5ConnAckException(connAck, "Auth method in CONNACK must be the same as in the CONNECT."));
            return false;
        }
        return true;
    }

    /**
     * Disconnects on an incoming AUTH message with the reason code SUCCESS.
     *
     * @param ctx  the channel handler context.
     * @param auth the incoming AUTH message.
     */
    @Override
    void readAuthSuccess(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttAuth auth) {
        MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                new Mqtt5AuthException(auth, "Must not receive AUTH with reason code SUCCESS during connect auth."));
    }

    /**
     * Disconnects on an incoming AUTH message with the reason code REAUTHENTICATE.
     *
     * @param ctx  the channel handler context.
     * @param auth the incoming AUTH message.
     */
    @Override
    void readReAuth(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttAuth auth) {
        MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                new Mqtt5AuthException(auth,
                        "Must not receive AUTH with reason code REAUTHENTICATE during connect auth."));
    }

    /**
     * Calls {@link Mqtt5EnhancedAuthMechanism#onAuthError(Mqtt5ClientConfig, Throwable)} with the cause why the
     * connection was disconnected if auth is still in progress.
     *
     * @param disconnectEvent the disconnect event.
     */
    @Override
    protected void onDisconnectEvent(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttDisconnectEvent disconnectEvent) {

        super.onDisconnectEvent(ctx, disconnectEvent);

        if (state != MqttAuthState.NONE) {
            callMechanism(() -> authMechanism.onAuthError(clientConfig, disconnectEvent.getCause()));
            state = MqttAuthState.NONE;
        }
    }

    @Override
    protected @NotNull String getTimeoutReasonString() {
        return "Timeout while waiting for AUTH or CONNACK.";
    }
}
