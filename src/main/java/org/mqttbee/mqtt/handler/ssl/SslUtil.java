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
 */

package org.mqttbee.mqtt.handler.ssl;

import dagger.internal.Preconditions;
import io.netty.channel.Channel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttClientSslConfig;

/** @author Christoph Schäbel */
public class SslUtil {

    @NotNull
    static SSLEngine createSslEngine(
            @NotNull final Channel channel, @NotNull final MqttClientSslConfig sslConfig)
            throws SSLException {

        Preconditions.checkNotNull(channel, "channel must not be null");
        Preconditions.checkNotNull(sslConfig, "SslData must not be null");

        final SSLEngine sslEngine = createSslContext(sslConfig).newEngine(channel.alloc());

        sslEngine.setUseClientMode(true);

        return sslEngine;
    }

    @NotNull
    private static SslContext createSslContext(@NotNull final MqttClientSslConfig sslConfig)
            throws SSLException {
        final SslContextBuilder sslContextBuilder =
                SslContextBuilder.forClient()
                        .sslProvider(SslProvider.JDK)
                        .trustManager(sslConfig.getTrustManagerFactory())
                        .keyManager(sslConfig.getKeyManagerFactory());

        String[] protocols = null;
        if (sslConfig.getProtocols() != null) {
            protocols =
                    sslConfig.getProtocols().toArray(new String[sslConfig.getProtocols().size()]);
        }
        sslContextBuilder.protocols(protocols);

        sslContextBuilder.ciphers(sslConfig.getCipherSuites(), SupportedCipherSuiteFilter.INSTANCE);

        return sslContextBuilder.build();
    }

    @NotNull
    public static SslHandler createSslHandler(
            @NotNull final Channel channel, @NotNull final MqttClientSslConfig sslConfig)
            throws SSLException {

        final SSLEngine sslEngine = createSslEngine(channel, sslConfig);
        final SslHandler sslHandler = new SslHandler(sslEngine);

        sslHandler.setHandshakeTimeoutMillis(sslConfig.getHandshakeTimeoutMs());
        return sslHandler;
    }
}
