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

package com.hivemq.mqtt.client2.internal.handler;

import com.hivemq.mqtt.client2.exceptions.ConnectionFailedException;
import com.hivemq.mqtt.client2.internal.MqttClientConfig;
import com.hivemq.mqtt.client2.internal.MqttProxyConfigImpl;
import com.hivemq.mqtt.client2.internal.MqttTlsConfigImpl;
import com.hivemq.mqtt.client2.internal.MqttWebSocketConfigImpl;
import com.hivemq.mqtt.client2.internal.codec.encoder.MqttEncoder;
import com.hivemq.mqtt.client2.internal.handler.auth.MqttAuthHandler;
import com.hivemq.mqtt.client2.internal.handler.connect.MqttConnAckFlow;
import com.hivemq.mqtt.client2.internal.handler.connect.MqttConnAckSingle;
import com.hivemq.mqtt.client2.internal.handler.connect.MqttConnectHandler;
import com.hivemq.mqtt.client2.internal.handler.disconnect.MqttDisconnectHandler;
import com.hivemq.mqtt.client2.internal.handler.proxy.MqttProxyInitializer;
import com.hivemq.mqtt.client2.internal.handler.tls.MqttTlsInitializer;
import com.hivemq.mqtt.client2.internal.handler.websocket.MqttWebSocketInitializer;
import com.hivemq.mqtt.client2.internal.message.connect.MqttConnect;
import com.hivemq.mqtt.client2.lifecycle.MqttDisconnectSource;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import org.jetbrains.annotations.NotNull;

/**
 * Initializes:
 * <ul>
 *   <li>the proxy handlers (optional)
 *   <li>the TLS handlers (optional)
 *   <li>the WebSocket handlers (optional)
 *   <li>the basic MQTT handlers: Encoder, AuthHandler, ConnectHandler, DisconnectHandler
 * </ul>
 *
 * @author Silvio Giebl
 * @author David Katz
 */
public class MqttChannelInitializer extends ChannelInboundHandlerAdapter {

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull MqttConnect connect;
    private final @NotNull MqttConnAckFlow connAckFlow;

    public MqttChannelInitializer(
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttConnect connect,
            final @NotNull MqttConnAckFlow connAckFlow) {
        this.clientConfig = clientConfig;
        this.connect = connect;
        this.connAckFlow = connAckFlow;
    }

    @Override
    public void handlerAdded(final @NotNull ChannelHandlerContext ctx) {
        ctx.pipeline().remove(this);

        ((SocketChannel) ctx.channel()).config()
                // close not on write error (concurrent write while remote closes the connection), only on read
                // this ensures that always all bytes are read, e.g. of the DISCONNECT sent before the close
                .setAutoClose(false)
                .setKeepAlive(true)
                .setTcpNoDelay(true)
                .setConnectTimeoutMillis(clientConfig.getCurrentTransportConfig().getSocketConnectTimeoutMs());

        initProxy(ctx.channel());
    }

    private void initProxy(final @NotNull Channel channel) {
        final MqttProxyConfigImpl proxyConfig = clientConfig.getCurrentTransportConfig().getRawProxyConfig();
        if (proxyConfig == null) {
            initTls(channel);
        } else {
            MqttProxyInitializer.initChannel(channel, clientConfig, proxyConfig, this::initTls, this::onError);
        }
    }

    private void initTls(final @NotNull Channel channel) {
        final MqttTlsConfigImpl tlsConfig = clientConfig.getCurrentTransportConfig().getRawTlsConfig();
        if (tlsConfig == null) {
            initWebsocket(channel);
        } else {
            MqttTlsInitializer.initChannel(channel, clientConfig, tlsConfig, this::initWebsocket, this::onError);
        }
    }

    private void initWebsocket(final @NotNull Channel channel) {
        final MqttWebSocketConfigImpl webSocketConfig =
                clientConfig.getCurrentTransportConfig().getRawWebSocketConfig();
        if (webSocketConfig == null) {
            initMqtt(channel);
        } else {
            MqttWebSocketInitializer.initChannel(channel, clientConfig, webSocketConfig, this::initMqtt, this::onError);
        }
    }

    private void initMqtt(final @NotNull Channel channel) {
        channel.pipeline()
                .addLast(MqttEncoder.NAME, MqttEncoder.create(clientConfig.getMqttVersion()))
                .addLast(
                        MqttAuthHandler.NAME,
                        MqttAuthHandler.create(clientConfig, connect.getRawEnhancedAuthMechanism()))
                .addLast(MqttConnectHandler.NAME, new MqttConnectHandler(connect, connAckFlow, clientConfig))
                .addLast(MqttDisconnectHandler.NAME, new MqttDisconnectHandler(clientConfig));
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
