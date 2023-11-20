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

package com.hivemq.mqtt.client2.internal.handler.websocket;

import com.hivemq.mqtt.client2.internal.MqttClientConfig;
import com.hivemq.mqtt.client2.internal.MqttTransportConfigImpl;
import com.hivemq.mqtt.client2.internal.MqttWebSocketConfigImpl;
import com.hivemq.mqtt.client2.internal.datatypes.MqttVariableByteInteger;
import com.hivemq.mqtt.client2.internal.ioc.ConnectionScope;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
@ConnectionScope
public class MqttWebSocketInitializer {

    private static final @NotNull String HTTP_CODEC_NAME = "http.codec";
    private static final @NotNull String HTTP_AGGREGATOR_NAME = "http.aggregator";

    private final @NotNull MqttWebSocketCodec mqttWebSocketCodec;

    @Inject
    MqttWebSocketInitializer(final @NotNull MqttWebSocketCodec mqttWebSocketCodec) {
        this.mqttWebSocketCodec = mqttWebSocketCodec;
    }

    public void initChannel(
            final @NotNull Channel channel,
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttWebSocketConfigImpl webSocketConfig,
            final @NotNull Consumer<Channel> onSuccess,
            final @NotNull BiConsumer<Channel, Throwable> onError) {

        final URI uri;
        try {
            final MqttTransportConfigImpl transportConfig = clientConfig.getCurrentTransportConfig();
            final InetSocketAddress serverAddress = transportConfig.getServerAddress();
            uri = new URI((transportConfig.getRawTlsConfig() == null) ? "ws" : "wss", null,
                    serverAddress.getHostString(), serverAddress.getPort(), "/" + webSocketConfig.getPath(),
                    webSocketConfig.getQuery(), null);
        } catch (final URISyntaxException e) {
            onError.accept(channel, e);
            return;
        }

        final Map<String, String> httpHeaders = webSocketConfig.getHttpHeaders();
        final DefaultHttpHeaders headers;
        if (httpHeaders.isEmpty()) {
            headers = null;
        } else {
            headers = new DefaultHttpHeaders();
            httpHeaders.forEach(headers::set);
        }

        final WebSocketClientHandshaker handshaker =
                WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13,
                        webSocketConfig.getSubprotocol(), true, headers,
                        MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT, true, false);

        channel.pipeline()
                .addLast(HTTP_CODEC_NAME, new HttpClientCodec())
                .addLast(HTTP_AGGREGATOR_NAME, new HttpObjectAggregator(65_535))
                .addLast(MqttWebsocketHandshakeHandler.NAME,
                        new MqttWebsocketHandshakeHandler(handshaker, webSocketConfig.getHandshakeTimeoutMs(),
                                onSuccess, onError))
                .addLast(MqttWebSocketCodec.NAME, mqttWebSocketCodec);
    }
}
