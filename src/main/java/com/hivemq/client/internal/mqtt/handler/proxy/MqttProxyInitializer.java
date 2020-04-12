/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.handler.proxy;

import com.hivemq.client.internal.mqtt.MqttProxyConfigImpl;
import io.netty.channel.Channel;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
public final class MqttProxyInitializer {

    private static final @NotNull String PROXY_HANDLER_NAME = "proxy";

    public static void initChannel(
            final @NotNull Channel channel, final @NotNull MqttProxyConfigImpl proxyConfig,
            final @NotNull Consumer<Channel> onSuccess, final @NotNull BiConsumer<Channel, Throwable> onError) {

        final InetSocketAddress address = proxyConfig.getProxyAddress();
        final String username = proxyConfig.getRawProxyUsername();
        final String password = proxyConfig.getRawProxyPassword();

        final ProxyHandler proxyHandler;
        switch (proxyConfig.getProxyProtocol()) {
            case SOCKS_4:
                proxyHandler = new Socks4ProxyHandler(address, username);
                break;
            case SOCKS_5:
                proxyHandler = new Socks5ProxyHandler(address, username, password);
                break;
            case HTTP:
                if ((username == null) && (password == null)) {
                    proxyHandler = new HttpProxyHandler(address);
                } else {
                    proxyHandler = new HttpProxyHandler(address, (username == null) ? "" : username,
                            (password == null) ? "" : password);
                }
                break;
            default:
                onError.accept(
                        channel, new IllegalStateException("Unknown proxy protocol " + proxyConfig.getProxyProtocol()));
                return;
        }
        proxyHandler.setConnectTimeoutMillis(proxyConfig.getHandshakeTimeoutMs());
        channel.pipeline().addLast(PROXY_HANDLER_NAME, proxyHandler);

        proxyHandler.connectFuture().addListener(future -> {
            if (future.isSuccess()) {
                onSuccess.accept(channel);
            } else {
                onError.accept(channel, future.cause());
            }
        });
    }

    private MqttProxyInitializer() {}
}
