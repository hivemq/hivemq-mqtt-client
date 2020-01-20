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
 */

package com.hivemq.client.internal.mqtt.handler.ssl;

import com.hivemq.client.internal.mqtt.MqttClientSslConfigImpl;
import com.hivemq.client.internal.util.collections.ImmutableList;
import io.netty.channel.Channel;
import io.netty.handler.ssl.*;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;

/**
 * @author Christoph Schäbel
 * @author Silvio Giebl
 */
public final class SslInitializer {

    private static final @NotNull String SSL_HANDLER_NAME = "ssl";

    public static void initChannel(
            final @NotNull Channel channel, final @NotNull MqttClientSslConfigImpl sslConfig,
            final @NotNull InetSocketAddress address) throws SSLException {

        channel.pipeline().addLast(SSL_HANDLER_NAME, createSslHandler(channel, sslConfig, address));
    }

    private static @NotNull SslHandler createSslHandler(
            final @NotNull Channel channel, final @NotNull MqttClientSslConfigImpl sslConfig,
            final @NotNull InetSocketAddress address) throws SSLException {

        return createSslContext(sslConfig).newHandler(channel.alloc(), address.getHostString(), address.getPort());
    }

    static @NotNull SslContext createSslContext(final @NotNull MqttClientSslConfigImpl sslConfig) throws SSLException {

        final ImmutableList<String> protocols = sslConfig.getRawProtocols();

        final SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(sslConfig.getRawTrustManagerFactory())
                .keyManager(sslConfig.getRawKeyManagerFactory())
                .protocols((protocols == null) ? null : protocols.toArray(new String[0]))
                .ciphers(sslConfig.getRawCipherSuites(), SupportedCipherSuiteFilter.INSTANCE)
                .build();

        return new DelegatingSslContext(sslContext) {
            @Override
            protected void initEngine(@NotNull final SSLEngine engine) {
            }

            @Override
            protected void initHandler(@NotNull final SslHandler handler) {
                handler.setHandshakeTimeoutMillis(sslConfig.getHandshakeTimeoutMs());
            }
        };
    }

    private SslInitializer() {}
}
