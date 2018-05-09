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
import io.netty.handler.ssl.*;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttClientSslData;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

/**
 * @author Christoph Schäbel
 */
public class SslUtil {

    public static SSLEngine createSslEngine(final Channel channel, final MqttClientSslData sslData)
            throws SSLException {

        Preconditions.checkNotNull(channel, "channel must not be null");
        Preconditions.checkNotNull(sslData, "SslData must not be null");

        //creates new SSLEngine from SslContext with the ByteBufAllocator from the channel
        final SSLEngine sslEngine = createSslContext(sslData).newEngine(channel.alloc());

        //sets client mode
        sslEngine.setUseClientMode(true);

        return sslEngine;
    }

    @NotNull
    private static SslContext createSslContext(final MqttClientSslData sslData) throws SSLException {

        final SslContextBuilder sslContextBuilder = SslContextBuilder.forClient()
                .sslProvider(SslProvider.JDK)
                .trustManager(sslData.trustManagerFactory())
                .keyManager(sslData.keyManagerFactory());

        //set chosen protocols if available, use defaults otherwise
        if (sslData.protocols() != null && sslData.protocols().size() > 0) {
            sslContextBuilder.protocols(sslData.protocols().toArray(new String[0]));
        }

        //set chosen cipher suites if available, use defaults otherwise
        if (sslData.cipherSuites() != null && sslData.cipherSuites().size() > 0) {
            sslContextBuilder.ciphers(sslData.cipherSuites(), SupportedCipherSuiteFilter.INSTANCE);
        }

        return sslContextBuilder.build();
    }
}
