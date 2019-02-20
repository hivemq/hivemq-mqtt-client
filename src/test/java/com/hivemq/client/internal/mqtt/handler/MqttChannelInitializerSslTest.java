/*
 * Copyright 2018 The HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.handler;

import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.MqttClientSslConfigImpl;
import com.hivemq.client.internal.mqtt.codec.encoder.MqttEncoder;
import com.hivemq.client.internal.mqtt.handler.auth.MqttAuthHandler;
import com.hivemq.client.internal.mqtt.handler.connect.MqttConnectHandler;
import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectHandler;
import com.hivemq.client.internal.mqtt.handler.websocket.WebSocketBinaryFrameDecoder;
import com.hivemq.client.internal.mqtt.handler.websocket.WebSocketBinaryFrameEncoder;
import com.hivemq.client.internal.rx.SingleFlow;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import dagger.Lazy;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.ssl.SslHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Schäbel
 */
@SuppressWarnings("NullabilityAnnotations")
public class MqttChannelInitializerSslTest {

    @Mock
    private MqttClientConfig clientData;
    @Mock
    private SingleFlow<Mqtt5ConnAck> connAckFlow;
    @Mock
    private MqttEncoder encoder;
    @Mock
    private MqttConnectHandler connectHandler;
    @Mock
    private MqttDisconnectHandler disconnectHandler;
    @Mock
    private MqttAuthHandler authHandler;
    @Mock
    private Lazy<WebSocketBinaryFrameEncoder> webSocketBinaryFrameEncoder;
    @Mock
    private Lazy<WebSocketBinaryFrameDecoder> webSocketBinaryFrameDecoder;

    private Channel channel;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        channel = new EmbeddedChannel();
        when(clientData.getSslConfig()).thenReturn(Optional.empty());
    }

    @Test
    public void test_initialize_default_ssldata() throws Exception {
        when(clientData.getMqttVersion()).thenReturn(MqttVersion.MQTT_5_0);
        final MqttClientSslConfigImpl sslConfig = mock(MqttClientSslConfigImpl.class);
        when(clientData.getSslConfig()).thenReturn(Optional.of(sslConfig));
        when(clientData.getRawSslConfig()).thenReturn(sslConfig);

        final MqttChannelInitializer mqttChannelInitializer =
                new MqttChannelInitializer(clientData, connAckFlow, encoder, connectHandler, disconnectHandler,
                        authHandler, webSocketBinaryFrameEncoder, webSocketBinaryFrameDecoder);

        mqttChannelInitializer.initChannel(channel);

        assertNotNull(channel.pipeline().get(SslHandler.class));
    }

}