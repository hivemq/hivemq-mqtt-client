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
import io.netty.channel.ChannelPromise;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ClientData;
import org.mqttbee.api.mqtt.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuth;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuthBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectEvent;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.mqtt.handler.util.DefaultChannelOutboundHandler;
import org.mqttbee.mqtt.ioc.ConnectionScope;
import org.mqttbee.mqtt.message.auth.MqttAuth;
import org.mqttbee.mqtt.message.auth.MqttEnhancedAuthBuilder;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.mqtt.message.connect.MqttStatefulConnect;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAck;

import javax.inject.Inject;

/**
 * Enhanced auth handling according during connection according to the MQTT 5 specification.
 * <p>
 * After successful connection the handler replaces itself with a {@link MqttReAuthHandler}.
 *
 * @author Silvio Giebl
 */
@ConnectionScope
public class MqttConnectAuthHandler extends AbstractMqttAuthHandler implements DefaultChannelOutboundHandler {

    @Inject
    MqttConnectAuthHandler(final @NotNull MqttClientData clientData, final @NotNull MqttConnect connect) {
        super(clientData, connect);
    }

    @Override
    public void write(
            final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg,
            final @NotNull ChannelPromise promise) {

        if (msg instanceof MqttConnect) {
            writeConnect(ctx, (MqttConnect) msg, promise);
        } else {
            ctx.write(msg, promise);
        }
    }

    /**
     * Handles the outgoing CONNECT message.
     * <ul>
     * <li>Calls {@link Mqtt5EnhancedAuthProvider#onAuth(Mqtt5ClientData, Mqtt5Connect, Mqtt5EnhancedAuthBuilder)}
     * which adds enhanced auth data to the CONNECT message.</li>
     * <li>Sends the CONNECT message with the enhanced auth data.</li>
     * </ul>
     *
     * @param ctx     the channel handler context.
     * @param connect the CONNECT message.
     * @param promise the write promise of the CONNECT message.
     */
    private void writeConnect(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttConnect connect,
            final @NotNull ChannelPromise promise) {

        final MqttEnhancedAuthBuilder enhancedAuthBuilder = new MqttEnhancedAuthBuilder(authMethod);
        state = MqttAuthState.IN_PROGRESS_INIT;
        authProvider.onAuth(clientData, connect, enhancedAuthBuilder).whenCompleteAsync((aVoid, throwable) -> {
            if (enhancedAuthProviderAccepted(throwable)) {
                state = MqttAuthState.WAIT_FOR_SERVER;
                final MqttStatefulConnect statefulConnect =
                        connect.createStateful(clientData.getRawClientIdentifier(), enhancedAuthBuilder.build());
                ctx.writeAndFlush(statefulConnect, promise).addListener(this);
            } else {
                MqttDisconnectUtil.close(ctx.channel(), throwable);
            }
        }, clientData.getEventLoop());
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
     * <ul>
     * <li>Calls {@link Mqtt5EnhancedAuthProvider#onAuthRejected(Mqtt5ClientData, Mqtt5ConnAck)} and closes the channel
     * if the CONNACK message contains an Error Code.</li>
     * <li>Sends a DISCONNECT message if the enhanced auth data of the CONNACK message is not valid.</li>
     * <li>Otherwise calls {@link Mqtt5EnhancedAuthProvider#onAuthSuccess(Mqtt5ClientData, Mqtt5ConnAck)}.</li>
     * <li>Sends a DISCONNECT message if the enhanced auth provider did not accept the enhanced auth data.</li>
     * </ul>
     *
     * @param ctx     the channel handler context.
     * @param connAck the received CONNACK message.
     */
    private void readConnAck(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttConnAck connAck) {
        if (state != MqttAuthState.WAIT_FOR_SERVER) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "Must not receive a CONNACK in no response to a client message");
        }

        cancelTimeout();

        if (connAck.getReasonCode().isError()) {
            authProvider.onAuthRejected(clientData, connAck);
            state = MqttAuthState.NONE;

            MqttDisconnectUtil.close(ctx.channel(),
                    new Mqtt5MessageException(connAck, "Connection failed with CONNACK with Error Code"));

        } else if (validateConnAck(ctx, connAck)) {
            state = MqttAuthState.IN_PROGRESS_DONE;
            authProvider.onAuthSuccess(clientData, connAck).whenCompleteAsync((accepted, throwable) -> {
                if (enhancedAuthProviderAccepted(accepted, throwable)) {
                    state = MqttAuthState.NONE;
                } else {
                    MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.NOT_AUTHORIZED,
                            new Mqtt5MessageException(connAck, "Server auth success not accepted"));
                }
            }, clientData.getEventLoop());

            ctx.fireChannelRead(connAck);
            ctx.pipeline().replace(this, NAME, new MqttReAuthHandler(this));
        }
    }

    /**
     * Validates the enhanced auth data of an incoming CONNACK message.
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
                    new Mqtt5MessageException(connAck, "Auth method in CONNACK must be present"));
            return false;
        }
        if (!enhancedAuth.getMethod().equals(authMethod)) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5MessageException(connAck, "Auth method in CONNACK must be the same as in the CONNECT"));
            return false;
        }
        return true;
    }

    /**
     * Disconnects on an incoming AUTH message with the Reason Code SUCCESS.
     *
     * @param ctx  the channel handler context.
     * @param auth the incoming AUTH message.
     */
    @Override
    void readAuthSuccess(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttAuth auth) {
        MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                new Mqtt5MessageException(auth,
                        "Must not receive an AUTH with Reason Code SUCCESS during connect auth"));
    }

    /**
     * Disconnects on an incoming AUTH message with the Reason Code REAUTHENTICATE.
     *
     * @param ctx  the channel handler context.
     * @param auth the incoming AUTH message.
     */
    @Override
    void readReAuth(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttAuth auth) {
        MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                new Mqtt5MessageException(auth,
                        "Must not receive an AUTH with Reason Code REAUTHENTICATE during connect auth"));
    }

    /**
     * Calls {@link Mqtt5EnhancedAuthProvider#onAuthError(Mqtt5ClientData, Throwable)} with the cause why the channel
     * was closed if auth is still in progress.
     *
     * @param disconnectEvent the channel close event.
     */
    protected void handleDisconnectEvent(final @NotNull MqttDisconnectEvent disconnectEvent) {
        super.handleDisconnectEvent(disconnectEvent);

        if (state != MqttAuthState.NONE) {
            authProvider.onAuthError(clientData, disconnectEvent.getCause());
            state = MqttAuthState.NONE;
        }
    }

    @Override
    protected @NotNull String getTimeoutReasonString() {
        return "Timeout while waiting for AUTH or CONNACK";
    }

}
