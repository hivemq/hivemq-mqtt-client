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

package com.hivemq.client.internal.mqtt.handler.tls;

import com.hivemq.client.internal.mqtt.MqttClientTlsConfigImpl;
import com.hivemq.client.internal.mqtt.MqttClientTlsConfigImplBuilder;
import com.hivemq.client.internal.util.collections.ImmutableList;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Christoph Schäbel
 */
class MqttTlsInitializerTest {

    @SuppressWarnings("NullabilityAnnotations")
    private EmbeddedChannel embeddedChannel;

    @BeforeEach
    public void before() {
        embeddedChannel = new EmbeddedChannel();
    }

    @Test
    public void test_createSslEngine_null_values() throws Exception {

        final TrustManagerFactory tmf = null;

        final SSLEngine sslEngine = createSslEngine(embeddedChannel,
                new MqttClientTlsConfigImplBuilder.Default().trustManagerFactory(tmf).build());

        assertNotNull(sslEngine);
        assertTrue(sslEngine.getUseClientMode());
        assertTrue(sslEngine.getEnabledProtocols().length > 0);
        assertTrue(sslEngine.getEnabledCipherSuites().length > 0);
    }

    @Test
    public void test_createSslEngine_cipher_suite() throws Exception {

        final TrustManagerFactory tmf = null;

        final ImmutableList<String> cipherSuite = getFirstSupportedCipherSuite();

        final SSLEngine sslEngine = createSslEngine(embeddedChannel,
                new MqttClientTlsConfigImplBuilder.Default().trustManagerFactory(tmf)
                        .cipherSuites(cipherSuite)
                        .build());

        assertNotNull(sslEngine);

        final String[] enabledCipherSuites = sslEngine.getEnabledCipherSuites();

        assertEquals(1, enabledCipherSuites.length);
        assertEquals(cipherSuite.get(0), enabledCipherSuites[0]);
    }

    @Test
    public void test_createSslEngine_multiple_cipher_suites() throws Exception {

        final TrustManagerFactory tmf = null;

        final ImmutableList<String> cipherSuites = getOtherSupportedCipherSuites();

        final SSLEngine sslEngine = createSslEngine(embeddedChannel,
                new MqttClientTlsConfigImplBuilder.Default().trustManagerFactory(tmf)
                        .cipherSuites(cipherSuites)
                        .build());

        assertNotNull(sslEngine);

        final String[] enabledCipherSuites = sslEngine.getEnabledCipherSuites();

        assertEquals(2, enabledCipherSuites.length);
        assertEquals(cipherSuites.get(0), enabledCipherSuites[0]);
        assertEquals(cipherSuites.get(1), enabledCipherSuites[1]);
    }

    @Test
    public void test_createSslEngine_protocol() throws Exception {

        System.out.println(getEnabledProtocols());

        final TrustManagerFactory tmf = null;

        final ImmutableList<String> protocol = ImmutableList.of("TLSv1");

        final SSLEngine sslEngine = createSslEngine(embeddedChannel,
                new MqttClientTlsConfigImplBuilder.Default().trustManagerFactory(tmf).protocols(protocol).build());

        assertNotNull(sslEngine);

        final String[] enabledProtocols = sslEngine.getEnabledProtocols();

        assertEquals(1, enabledProtocols.length);
        assertEquals(protocol.get(0), enabledProtocols[0]);
    }

    @Test
    public void test_createSslEngine_multiple_protocols() throws Exception {

        final TrustManagerFactory tmf = null;

        final ImmutableList<String> protocols = ImmutableList.of("TLSv1.1", "TLSv1.2");

        final SSLEngine sslEngine = createSslEngine(embeddedChannel,
                new MqttClientTlsConfigImplBuilder.Default().trustManagerFactory(tmf).protocols(protocols).build());

        assertNotNull(sslEngine);

        final String[] enabledProtocols = sslEngine.getEnabledProtocols();

        assertEquals(2, enabledProtocols.length);
        assertEquals(protocols.get(0), enabledProtocols[0]);
        assertEquals(protocols.get(1), enabledProtocols[1]);
    }

    private @NotNull ImmutableList<String> getFirstSupportedCipherSuite() throws Exception {

        final List<String> supportedCipherSuites = getEnabledCipherSuites();

        final List<String> valueList = new ArrayList<>();
        valueList.add(supportedCipherSuites.get(0));

        return ImmutableList.copyOf(valueList);
    }

    private @NotNull ImmutableList<String> getOtherSupportedCipherSuites() throws Exception {

        final List<String> supportedCipherSuites = getEnabledCipherSuites();

        final List<String> valueList = new ArrayList<>();
        valueList.add(supportedCipherSuites.get(1));
        valueList.add(supportedCipherSuites.get(2));

        return ImmutableList.copyOf(valueList);
    }

    private List<String> getEnabledCipherSuites() throws Exception {
        return Arrays.asList(SslContextBuilder.forClient()
                .sslProvider(SslProvider.JDK)
                .build()
                .newEngine(new EmbeddedChannel().alloc())
                .getEnabledCipherSuites());
    }

    private List<String> getEnabledProtocols() throws Exception {
        final SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, null, null);
        final SSLEngine sslEngine = context.createSSLEngine();
        return Arrays.asList(sslEngine.getEnabledProtocols());
    }

    private static @NotNull SSLEngine createSslEngine(
            final @NotNull Channel channel, final @NotNull MqttClientTlsConfigImpl tlsConfig) throws SSLException {

        return MqttTlsInitializer.createSslContext(tlsConfig).newEngine(channel.alloc());
    }
}