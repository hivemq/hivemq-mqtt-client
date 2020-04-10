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

import javax.net.ssl.*;
import java.net.InetSocketAddress;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Christoph Schäbel
 * @author Silvio Giebl
 */
public final class MqttSslInitializer {

    private static final @NotNull String SSL_HANDLER_NAME = "ssl";

    public static void initChannel(
            final @NotNull Channel channel, final @NotNull MqttClientSslConfigImpl sslConfig,
            final @NotNull InetSocketAddress address, final @NotNull Consumer<Channel> onSuccess,
            final @NotNull BiConsumer<Channel, Throwable> onError) {

        final SslHandler sslHandler;
        try {
            final SslContext sslContext = createSslContext(sslConfig);
            sslHandler = sslContext.newHandler(channel.alloc(), address.getHostString(), address.getPort());
        } catch (final SSLException e) {
            onError.accept(channel, e);
            return;
        }

        channel.pipeline().addLast(SSL_HANDLER_NAME, sslHandler);

        final HostnameVerifier hostnameVerifier = sslConfig.getRawHostnameVerifier();
        sslHandler.handshakeFuture().addListener(future -> {
            if (future.isSuccess()) {
                if ((hostnameVerifier != null) &&
                        !hostnameVerifier.verify(address.getHostString(), sslHandler.engine().getSession())) {
                    onError.accept(channel, new SSLHandshakeException("Hostname verification failed"));
                } else {
                    onSuccess.accept(channel);
                }
            } else {
                onError.accept(channel, future.cause());
            }
        });
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
            protected void initEngine(final @NotNull SSLEngine engine) {}

            @Override
            protected void initHandler(final @NotNull SslHandler handler) {
                handler.setHandshakeTimeoutMillis(sslConfig.getHandshakeTimeoutMs());
                if (sslConfig.getRawHostnameVerifier() == null) {
                    final SSLParameters sslParameters = handler.engine().getSSLParameters();
                    sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
                    handler.engine().setSSLParameters(sslParameters);
                }
            }
        };
    }

    private MqttSslInitializer() {}
}
