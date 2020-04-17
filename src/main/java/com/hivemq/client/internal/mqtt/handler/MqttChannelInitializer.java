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

package com.hivemq.client.internal.mqtt.handler;

import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.MqttClientSslConfigImpl;
import com.hivemq.client.internal.mqtt.MqttProxyConfigImpl;
import com.hivemq.client.internal.mqtt.MqttWebSocketConfigImpl;
import com.hivemq.client.internal.mqtt.codec.encoder.MqttEncoder;
import com.hivemq.client.internal.mqtt.handler.auth.MqttAuthHandler;
import com.hivemq.client.internal.mqtt.handler.connect.MqttConnAckFlow;
import com.hivemq.client.internal.mqtt.handler.connect.MqttConnAckSingle;
import com.hivemq.client.internal.mqtt.handler.connect.MqttConnectHandler;
import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectHandler;
import com.hivemq.client.internal.mqtt.handler.proxy.MqttProxyInitializer;
import com.hivemq.client.internal.mqtt.handler.ssl.MqttSslInitializer;
import com.hivemq.client.internal.mqtt.handler.websocket.MqttWebSocketInitializer;
import com.hivemq.client.internal.mqtt.ioc.ConnectionScope;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import dagger.Lazy;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Initializes:
 * <ul>
 *   <li>the proxy handlers (optional)
 *   <li>the SSL/TLS handlers (optional)
 *   <li>the WebSocket handlers (optional)
 *   <li>the basic MQTT handlers: Encoder, AuthHandler, ConnectHandler, DisconnectHandler
 * </ul>
 *
 * @author Silvio Giebl
 * @author David Katz
 */
@ConnectionScope
public class MqttChannelInitializer extends ChannelInboundHandlerAdapter {

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull MqttConnect connect;
    private final @NotNull MqttConnAckFlow connAckFlow;

    private final @NotNull MqttEncoder encoder;
    private final @NotNull MqttConnectHandler connectHandler;
    private final @NotNull MqttDisconnectHandler disconnectHandler;
    private final @NotNull MqttAuthHandler authHandler;

    private final @NotNull Lazy<MqttWebSocketInitializer> webSocketInitializer;

    @Inject
    MqttChannelInitializer(
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttConnect connect,
            final @NotNull MqttConnAckFlow connAckFlow,
            final @NotNull MqttEncoder encoder,
            final @NotNull MqttConnectHandler connectHandler,
            final @NotNull MqttDisconnectHandler disconnectHandler,
            final @NotNull MqttAuthHandler authHandler,
            final @NotNull Lazy<MqttWebSocketInitializer> webSocketInitializer) {

        this.clientConfig = clientConfig;
        this.connect = connect;
        this.connAckFlow = connAckFlow;
        this.encoder = encoder;
        this.connectHandler = connectHandler;
        this.disconnectHandler = disconnectHandler;
        this.authHandler = authHandler;
        this.webSocketInitializer = webSocketInitializer;
    }

    @Override
    public void handlerAdded(final @NotNull ChannelHandlerContext ctx) {
        ctx.pipeline().remove(this);

        ((SocketChannel) ctx.channel()).config()
                .setKeepAlive(true)
                .setTcpNoDelay(true)
                .setConnectTimeoutMillis(clientConfig.getCurrentTransportConfig().getSocketConnectTimeoutMs());

        initProxy(ctx.channel());
    }

    private void initProxy(final @NotNull Channel channel) {
        final MqttProxyConfigImpl proxyConfig = clientConfig.getCurrentTransportConfig().getRawProxyConfig();
        if (proxyConfig == null) {
            initSsl(channel);
        } else {
            MqttProxyInitializer.initChannel(channel, clientConfig, proxyConfig, this::initSsl, this::onError);
        }
    }

    private void initSsl(final @NotNull Channel channel) {
        final MqttClientSslConfigImpl sslConfig = clientConfig.getCurrentTransportConfig().getRawSslConfig();
        if (sslConfig == null) {
            initWebsocket(channel);
        } else {
            MqttSslInitializer.initChannel(channel, clientConfig, sslConfig, this::initWebsocket, this::onError);
        }
    }

    private void initWebsocket(final @NotNull Channel channel) {
        final MqttWebSocketConfigImpl webSocketConfig =
                clientConfig.getCurrentTransportConfig().getRawWebSocketConfig();
        if (webSocketConfig == null) {
            initMqtt(channel);
        } else {
            webSocketInitializer.get()
                    .initChannel(channel, clientConfig, webSocketConfig, this::initMqtt, this::onError);
        }
    }

    private void initMqtt(final @NotNull Channel channel) {
        channel.pipeline()
                .addLast(MqttEncoder.NAME, encoder)
                .addLast(MqttAuthHandler.NAME, authHandler)
                .addLast(MqttConnectHandler.NAME, connectHandler)
                .addLast(MqttDisconnectHandler.NAME, disconnectHandler);
    }

    private void onError(final @NotNull Channel channel, final @NotNull Throwable cause) {
        channel.close();
        MqttConnAckSingle.reconnect(clientConfig, MqttDisconnectSource.CLIENT, new ConnectionFailedException(cause),
                connect, connAckFlow, channel.eventLoop());
    }

    @Override
    public boolean isSharable() {
        return false;
    }
}
