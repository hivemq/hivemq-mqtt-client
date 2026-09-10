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

package com.hivemq.client.internal.mqtt.handler.websocket;

import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.MqttClientTransportConfigImpl;
import com.hivemq.client.internal.mqtt.MqttWebSocketConfigImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client.internal.mqtt.ioc.ConnectionScope;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.inject.Inject;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

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

    /**
     * Builds the WebSocket URI without double-encoding the query string.
     * <p>
     * Query strings (especially AWS IoT presigned URLs) may already be URL-encoded. Using the multi-argument URI
     * constructor would encode them again, breaking signatures. This method first builds the URI (which double-encodes),
     * then decodes it back to restore the original encoding.
     *
     * @param scheme      the URI scheme (ws or wss)
     * @param host        the host
     * @param port        the port
     * @param serverPath  the WebSocket server path
     * @param queryString the query string (may be pre-encoded)
     * @return the constructed URI
     * @throws URISyntaxException           if the URI is malformed
     * @throws UnsupportedEncodingException if UTF-8 encoding is not supported
     * @see <a href="https://github.com/hivemq/hivemq-mqtt-client/issues/421">GitHub Issue #421</a>
     */
    static @NotNull URI buildWebSocketUri(
            final @NotNull String scheme,
            final @NotNull String host,
            final int port,
            final @NotNull String serverPath,
            final @NotNull String queryString) throws URISyntaxException, UnsupportedEncodingException {

        // For empty/null query strings, build URI without query to avoid trailing '?'
        if (queryString == null || queryString.isEmpty()) {
            return new URI(scheme, null, host, port, "/" + serverPath, null, null);
        }

        // The multi-argument URI constructor encodes the query string, which double-encodes
        // already-encoded query strings (like AWS presigned URLs). We decode the entire URI
        // to reverse this double-encoding.
        final URI encodedUri = new URI(scheme, null, host, port, "/" + serverPath, queryString, null);
        return new URI(URLDecoder.decode(encodedUri.toString(), StandardCharsets.UTF_8.toString()));
    }

    public void initChannel(
            final @NotNull Channel channel,
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttWebSocketConfigImpl webSocketConfig,
            final @NotNull Consumer<Channel> onSuccess,
            final @NotNull BiConsumer<Channel, Throwable> onError) {

        final URI uri;
        try {
            final MqttClientTransportConfigImpl transportConfig = clientConfig.getCurrentTransportConfig();
            final InetSocketAddress serverAddress = transportConfig.getServerAddress();
            final String scheme = (transportConfig.getRawSslConfig() == null) ? "ws" : "wss";
            uri = buildWebSocketUri(scheme, serverAddress.getHostString(), serverAddress.getPort(),
                    webSocketConfig.getServerPath(), webSocketConfig.getQueryString());
        } catch (final URISyntaxException | UnsupportedEncodingException e) {
            onError.accept(channel, e);
            return;
        }

        final WebSocketClientHandshaker handshaker =
                WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13,
                        webSocketConfig.getSubprotocol(), true, null, MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT,
                        true, false);

        channel.pipeline()
                .addLast(HTTP_CODEC_NAME, new HttpClientCodec())
                .addLast(HTTP_AGGREGATOR_NAME, new HttpObjectAggregator(65_535))
                .addLast(MqttWebSocketHttpHeaders.HTTP_HEADERS, new MqttWebSocketHttpHeaders(webSocketConfig.getHttpHeaders()))
                .addLast(MqttWebsocketHandshakeHandler.NAME,
                        new MqttWebsocketHandshakeHandler(handshaker, webSocketConfig.getHandshakeTimeoutMs(),
                                onSuccess, onError))
                .addLast(MqttWebSocketCodec.NAME, mqttWebSocketCodec);
    }
}
