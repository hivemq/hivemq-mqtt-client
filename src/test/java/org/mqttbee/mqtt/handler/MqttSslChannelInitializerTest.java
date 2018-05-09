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
package org.mqttbee.mqtt.handler;

import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.ssl.SslHandler;
import io.reactivex.SingleEmitter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mqttbee.api.mqtt.MqttClientSslData;

import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.message.connect.MqttConnect;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Schäbel
 */
public class MqttSslChannelInitializerTest {

    @Mock
    private MqttConnect mqtt5Connect;

    @Mock
    private SingleEmitter<Mqtt5ConnAck> connAckEmitter;

    @Mock
    private MqttClientData clientData;

    private Channel channel;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        channel = new EmbeddedChannel();
        when(clientData.getSslData()).thenReturn(Optional.empty());
    }

    @Test(expected = IllegalStateException.class)
    public void test_initialize_no_ssldata_present() {

        final MqttSslChannelInitializer mqtt5ChannelInitializer =
                new MqttSslChannelInitializer(mqtt5Connect, connAckEmitter, clientData);

        mqtt5ChannelInitializer.initChannel(channel);
    }

    @Test
    public void test_initialize_default_ssldata() {

        when(clientData.getSslData()).thenReturn(Optional.of(new TestSslData(null, null, null, null, 0)));

        final MqttSslChannelInitializer mqtt5ChannelInitializer =
                new MqttSslChannelInitializer(mqtt5Connect, connAckEmitter, clientData);

        mqtt5ChannelInitializer.initChannel(channel);

        assertNotNull(channel.pipeline().get(SslHandler.class));
    }

    private static class TestSslData implements MqttClientSslData {

        private final KeyManagerFactory keyManagerFactory;
        private final TrustManagerFactory trustManagerFactory;
        private final List<String> cipherSuites;
        private final List<String> protocols;
        private final int handshakeTimeout;

        private TestSslData(
                final KeyManagerFactory keyManagerFactory, final TrustManagerFactory trustManagerFactory,
                final List<String> cipherSuites, final List<String> protocols, final int handshakeTimeout) {
            this.keyManagerFactory = keyManagerFactory;
            this.trustManagerFactory = trustManagerFactory;
            this.cipherSuites = cipherSuites;
            this.protocols = protocols;
            this.handshakeTimeout = handshakeTimeout;
        }

        @Override
        public KeyManagerFactory keyManagerFactory() {
            return keyManagerFactory;
        }

        @Override
        public TrustManagerFactory trustManagerFactory() {
            return trustManagerFactory;
        }

        @Override
        public List<String> cipherSuites() {
            return cipherSuites;
        }

        @Override
        public List<String> protocols() {
            return protocols;
        }

        @Override
        public int handshakeTimeout() {
            return 0;
        }
    }


}