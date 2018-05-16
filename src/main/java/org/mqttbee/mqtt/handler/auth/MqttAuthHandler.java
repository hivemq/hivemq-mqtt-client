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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ClientData;
import org.mqttbee.api.mqtt.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuth;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuthBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.handler.disconnect.ChannelCloseEvent;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.mqtt.handler.util.DefaultChannelOutboundHandler;
import org.mqttbee.mqtt.ioc.ChannelComponent;
import org.mqttbee.mqtt.ioc.ChannelScope;
import org.mqttbee.mqtt.message.auth.MqttAuth;
import org.mqttbee.mqtt.message.auth.MqttEnhancedAuthBuilder;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.mqtt.message.connect.MqttConnectWrapper;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAck;

import javax.inject.Inject;

/**
 * Enhanced auth handling according during connection according to the MQTT 5 specification.
 * <p>
 * After successful connection the handler replaces itself with a {@link MqttReAuthHandler}.
 *
 * @author Silvio Giebl
 */
@ChannelScope
public class MqttAuthHandler extends AbstractMqttAuthHandler implements DefaultChannelOutboundHandler {

    public static final String NAME = "auth";

    /**
     * Flag indicating whether enhanced auth is done. It is true when
     * <ul>
     * <li>{@link Mqtt5EnhancedAuthProvider#onAuthSuccess(Mqtt5ClientData, Mqtt5ConnAck)},</li>
     * <li>{@link Mqtt5EnhancedAuthProvider#onAuthRejected(Mqtt5ClientData, Mqtt5ConnAck)} or</li>
     * <li>{@link Mqtt5EnhancedAuthProvider#onAuthError(Mqtt5ClientData, Throwable)}</li>
     * </ul>
     * have been called.
     */
    private boolean done;

    @Inject
    MqttAuthHandler() {
    }

    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
        if (msg instanceof MqttConnect) {
            writeConnect(ctx, (MqttConnect) msg, promise);
        } else {
            ctx.write(msg, promise);
        }
    }

    /**
     * Handles the outgoing CONNECT message.
     * <ul>
     * <li>Calls {@link Mqtt5EnhancedAuthProvider#onAuth(Mqtt5ClientData, Mqtt5Connect, Mqtt5EnhancedAuthBuilder)} which
     * adds enhanced auth data to the CONNECT message.</li>
     * <li>Sends the CONNECT message with the enhanced auth data.</li>
     * </ul>
     *
     * @param ctx     the channel handler context.
     * @param connect the CONNECT message.
     * @param promise the write promise of the CONNECT message.
     */
    private void writeConnect(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttConnect connect,
            @NotNull final ChannelPromise promise) {

        final MqttClientData clientData = MqttClientData.from(ctx.channel());
        final Mqtt5EnhancedAuthProvider enhancedAuthProvider = getEnhancedAuthProvider(clientData);
        final MqttEnhancedAuthBuilder enhancedAuthBuilder =
                new MqttEnhancedAuthBuilder((MqttUTF8StringImpl) enhancedAuthProvider.getMethod());

        enhancedAuthProvider.onAuth(clientData, connect, enhancedAuthBuilder).whenCompleteAsync((aVoid, throwable) -> {
            if (enhancedAuthProviderAccepted(throwable)) {
                final MqttConnectWrapper connectWrapper =
                        connect.wrap(clientData.getRawClientIdentifier(), enhancedAuthBuilder.build());
                ctx.writeAndFlush(connectWrapper, promise).addListener(this);
            } else {
                MqttDisconnectUtil.close(ctx.channel(), throwable);
            }
        }, ctx.executor());
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
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
    private void readConnAck(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttConnAck connAck) {
        cancelTimeout();

        final MqttClientData clientData = MqttClientData.from(ctx.channel());
        final Mqtt5EnhancedAuthProvider enhancedAuthProvider = getEnhancedAuthProvider(clientData);

        if (connAck.getReasonCode().isError()) {
            enhancedAuthProvider.onAuthRejected(clientData, connAck);
            done = true;

            MqttDisconnectUtil.close(
                    ctx.channel(),
                    new Mqtt5MessageException(connAck, "Connection failed with CONNACK with Error Code"));

        } else if (validateConnAck(ctx.channel(), connAck, enhancedAuthProvider)) {
            enhancedAuthProvider.onAuthSuccess(clientData, connAck).whenCompleteAsync((accepted, throwable) -> {
                if (!enhancedAuthProviderAccepted(accepted, throwable)) {
                    MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.NOT_AUTHORIZED,
                            new Mqtt5MessageException(connAck, "Server auth success not accepted"));
                }
            }, ctx.executor());
            done = true;

            ctx.fireChannelRead(connAck);
            ctx.pipeline().replace(this, MqttReAuthHandler.NAME, ChannelComponent.get(ctx.channel()).reAuthHandler());
        }
    }

    /**
     * Validates the enhanced auth data of an incoming CONNACK message.
     * <p>
     * If validation fails, disconnection and closing of the channel is already handled.
     *
     * @param channel              the channel.
     * @param connAck              the incoming CONNACK message.
     * @param enhancedAuthProvider the enhanced auth provider.
     * @return true if the enhanced auth data of the CONNACK message is valid, otherwise false.
     */
    private boolean validateConnAck(
            @NotNull final Channel channel, @NotNull final MqttConnAck connAck,
            @NotNull final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {

        final Mqtt5EnhancedAuth enhancedAuth = connAck.getRawEnhancedAuth();
        if (enhancedAuth == null) {
            MqttDisconnectUtil.disconnect(channel, Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5MessageException(connAck, "Auth method must be present"));
            return false;
        }
        if (!enhancedAuth.getMethod().equals(enhancedAuthProvider.getMethod())) {
            MqttDisconnectUtil.disconnect(channel, Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5MessageException(connAck, "Auth method must be the same as in the CONNECT message"));
            return false;
        }
        return true;
    }

    /**
     * Disconnects on an incoming AUTH message with the Reason Code SUCCESS.
     *
     * @param ctx                  the channel handler context.
     * @param auth                 the incoming AUTH message.
     * @param clientData           the data of the client.
     * @param enhancedAuthProvider the enhanced auth provider.
     */
    @Override
    void readAuthSuccess(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttAuth auth,
            @NotNull final MqttClientData clientData, @NotNull final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {

        MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                new Mqtt5MessageException(auth, "Server must not send an AUTH message with the Reason Code SUCCESS"));
    }

    /**
     * Disconnects on an incoming AUTH message with the Reason Code REAUTHENTICATE.
     *
     * @param ctx                  the channel handler context.
     * @param auth                 the incoming AUTH message.
     * @param clientData           the data of the client.
     * @param enhancedAuthProvider the enhanced auth provider.
     */
    @Override
    void readReAuth(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttAuth auth,
            @NotNull final MqttClientData clientData, @NotNull final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {

        MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                new Mqtt5MessageException(
                        auth,
                        "Server must not send an AUTH message with the Reason Code REAUTHENTICATE"));
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (evt instanceof ChannelCloseEvent) {
            handleChannelCloseEvent(ctx, (ChannelCloseEvent) evt);
        }
        ctx.fireUserEventTriggered(evt);
    }

    /**
     * Calls {@link Mqtt5EnhancedAuthProvider#onAuthError(Mqtt5ClientData, Throwable)} with the cause why the channel
     * was closed if auth is not {@link #done} yet.
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

            enhancedAuthProvider.onAuthError(clientData, channelCloseEvent.getCause());
            done = true;
        }
    }

    @NotNull
    @Override
    protected String getTimeoutReasonString() {
        return "Timeout while waiting for AUTH or CONNACK";
    }

}
