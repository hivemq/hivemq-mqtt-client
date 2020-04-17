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

package com.hivemq.client.internal.mqtt.handler.proxy;

import com.hivemq.client.internal.mqtt.MqttProxyConfigImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
class MqttProxyAdapterHandler extends ChannelOutboundHandlerAdapter {

    public static final @NotNull String NAME = "proxy.adapter";
    private static final @NotNull String PROXY_HANDLER_NAME = "proxy";

    private final @NotNull MqttProxyConfigImpl proxyConfig;
    private final @NotNull InetSocketAddress serverAddress;
    private final @NotNull Consumer<Channel> onSuccess;
    private final @NotNull BiConsumer<Channel, Throwable> onError;

    public MqttProxyAdapterHandler(
            final @NotNull MqttProxyConfigImpl proxyConfig,
            final @NotNull InetSocketAddress serverAddress,
            final @NotNull Consumer<Channel> onSuccess,
            final @NotNull BiConsumer<Channel, Throwable> onError) {

        this.proxyConfig = proxyConfig;
        this.serverAddress = serverAddress;
        this.onSuccess = onSuccess;
        this.onError = onError;
    }

    @Override
    public void connect(
            final @NotNull ChannelHandlerContext ctx,
            final @NotNull SocketAddress remoteAddress,
            final @Nullable SocketAddress localAddress,
            final @NotNull ChannelPromise promise) {

        final Channel channel = ctx.channel();
        final String username = proxyConfig.getRawUsername();
        final String password = proxyConfig.getRawPassword();
        final Consumer<Channel> onSuccess = this.onSuccess;
        final BiConsumer<Channel, Throwable> onError = this.onError;

        final ProxyHandler proxyHandler;
        switch (proxyConfig.getProtocol()) {
            case SOCKS_4:
                proxyHandler = new Socks4ProxyHandler(remoteAddress, username);
                break;
            case SOCKS_5:
                proxyHandler = new Socks5ProxyHandler(remoteAddress, username, password);
                break;
            case HTTP:
                if ((username == null) && (password == null)) {
                    proxyHandler = new HttpProxyHandler(remoteAddress);
                } else {
                    proxyHandler = new HttpProxyHandler(remoteAddress, (username == null) ? "" : username,
                            (password == null) ? "" : password);
                }
                break;
            default:
                onError.accept(
                        channel, new IllegalStateException("Unknown proxy protocol " + proxyConfig.getProtocol()));
                return;
        }

        proxyHandler.setConnectTimeoutMillis(proxyConfig.getHandshakeTimeoutMs());

        proxyHandler.connectFuture().addListener(future -> {
            channel.pipeline().remove(PROXY_HANDLER_NAME);
            if (future.isSuccess()) {
                onSuccess.accept(channel);
            } else {
                onError.accept(channel, future.cause());
            }
        });

        channel.pipeline().addFirst(PROXY_HANDLER_NAME, proxyHandler).remove(this);

        ctx.connect(serverAddress, localAddress, promise);
    }

    @Override
    public boolean isSharable() {
        return false;
    }
}
