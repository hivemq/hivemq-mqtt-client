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

/**
 * @author Christoph Schäbel
 * @author Silvio Giebl
 */
public final class SslUtil {

    private static final @NotNull String SSL_HANDLER_NAME = "ssl";

    public static void initChannel(final @NotNull Channel channel, final @NotNull MqttClientSslConfigImpl sslConfig)
            throws SSLException {

        channel.pipeline().addFirst(SSL_HANDLER_NAME, createSslHandler(channel, sslConfig));
    }

    private static @NotNull SslHandler createSslHandler(
            final @NotNull Channel channel, final @NotNull MqttClientSslConfigImpl sslConfig) throws SSLException {

        final SSLEngine sslEngine = createSslEngine(channel, sslConfig);
        final SslHandler sslHandler = new SslHandler(sslEngine);

        sslHandler.setHandshakeTimeoutMillis(sslConfig.getHandshakeTimeoutMs());
        return sslHandler;
    }

    static @NotNull SSLEngine createSslEngine(
            final @NotNull Channel channel, final @NotNull MqttClientSslConfigImpl sslConfig) throws SSLException {

        final SSLEngine sslEngine = createSslContext(sslConfig).newEngine(channel.alloc());

        sslEngine.setUseClientMode(true);

        return sslEngine;
    }

    private static @NotNull SslContext createSslContext(final @NotNull MqttClientSslConfigImpl sslConfig)
            throws SSLException {

        final SslContextBuilder sslContextBuilder = SslContextBuilder.forClient()
                .sslProvider(SslProvider.JDK)
                .trustManager(sslConfig.getRawTrustManagerFactory())
                .keyManager(sslConfig.getRawKeyManagerFactory());

        final ImmutableList<String> protocols = sslConfig.getRawProtocols();
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        final String[] protocolArray = (protocols == null) ? null : protocols.toArray(new String[protocols.size()]);
        sslContextBuilder.protocols(protocolArray);

        sslContextBuilder.ciphers(sslConfig.getRawCipherSuites(), SupportedCipherSuiteFilter.INSTANCE);

        return sslContextBuilder.build();
    }

    private SslUtil() {}
}
