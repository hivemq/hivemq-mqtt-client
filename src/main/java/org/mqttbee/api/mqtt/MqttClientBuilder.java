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
 *
 */

package org.mqttbee.api.mqtt;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttClientIdentifier;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3ClientBuilder;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ClientBuilder;
import org.mqttbee.mqtt.MqttClientExecutorConfigImpl;
import org.mqttbee.mqtt.MqttClientSslConfigImpl;
import org.mqttbee.mqtt.MqttWebsocketConfigImpl;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.MustNotBeImplementedUtil;

import static org.mqttbee.api.mqtt.MqttClient.*;

/**
 * @author Silvio Giebl
 */
public class MqttClientBuilder {

    protected MqttClientIdentifierImpl identifier = MqttClientIdentifierImpl.REQUEST_CLIENT_IDENTIFIER_FROM_SERVER;
    protected String serverHost = DEFAULT_SERVER_HOST;
    protected int serverPort = DEFAULT_SERVER_PORT;
    private boolean customServerPort = false;
    protected MqttClientSslConfig sslConfig = null;
    protected MqttWebsocketConfig websocketConfig = null;
    protected MqttClientExecutorConfigImpl executorConfig = MqttClientExecutorConfigImpl.DEFAULT;

    protected MqttClientBuilder() {
    }

    @NotNull
    public MqttClientBuilder identifier(@NotNull final String identifier) {
        this.identifier = MqttBuilderUtil.clientIdentifier(identifier);
        return this;
    }

    @NotNull
    public MqttClientBuilder identifier(@NotNull final MqttClientIdentifier identifier) {
        this.identifier = MqttBuilderUtil.clientIdentifier(identifier);
        return this;
    }

    @NotNull
    public MqttClientBuilder serverHost(@NotNull final String host) {
        this.serverHost = Preconditions.checkNotNull(host);
        return this;
    }

    @NotNull
    public MqttClientBuilder serverPort(final int port) {
        this.serverPort = port;
        customServerPort = true;
        return this;
    }

    @NotNull
    public MqttClientBuilder useSslWithDefaultConfig() {
        return useSsl(MqttClientSslConfigImpl.DEFAULT);
    }

    @NotNull
    public MqttClientBuilder useSsl(@NotNull final MqttClientSslConfig sslConfig) {
        if (!customServerPort) {
            if (websocketConfig == null) {
                serverPort = DEFAULT_SERVER_PORT_SSL;
            } else {
                serverPort = DEFAULT_SERVER_PORT_WEBSOCKET_SSL;
            }
        }
        this.sslConfig = Preconditions.checkNotNull(sslConfig);
        return this;
    }

    @NotNull
    public MqttClientSslConfigBuilder<? extends MqttClientBuilder> useSsl() {
        return new MqttClientSslConfigBuilder<>(this::useSsl);
    }

    @NotNull
    public MqttClientBuilder useWebSocketWithDefaultConfig() {
        return useWebSocket(MqttWebsocketConfigImpl.DEFAULT);
    }

    @NotNull
    public MqttClientBuilder useWebSocket(@NotNull final MqttWebsocketConfig websocketConfig) {
        if (!customServerPort) {
            if (sslConfig == null) {
                serverPort = DEFAULT_SERVER_PORT_WEBSOCKET;
            } else {
                serverPort = DEFAULT_SERVER_PORT_WEBSOCKET_SSL;
            }
        }
        this.websocketConfig = Preconditions.checkNotNull(websocketConfig);
        return this;
    }

    @NotNull
    public MqttWebsocketConfigBuilder<? extends MqttClientBuilder> useWebSocket() {
        return new MqttWebsocketConfigBuilder<>(this::useWebSocket);
    }

    @NotNull
    public MqttClientBuilder executorConfig(@NotNull final MqttClientExecutorConfig executorConfig) {
        this.executorConfig =
                MustNotBeImplementedUtil.checkNotImplemented(executorConfig, MqttClientExecutorConfigImpl.class);
        return this;
    }

    @NotNull
    public MqttClientExecutorConfigBuilder<? extends MqttClientBuilder> executorConfig() {
        return new MqttClientExecutorConfigBuilder<>(this::executorConfig);
    }

    @NotNull
    public Mqtt3ClientBuilder useMqttVersion3() {
        return new Mqtt3ClientBuilder(identifier, serverHost, serverPort, sslConfig, websocketConfig, executorConfig);
    }

    @NotNull
    public Mqtt5ClientBuilder useMqttVersion5() {
        return new Mqtt5ClientBuilder(identifier, serverHost, serverPort, sslConfig, websocketConfig, executorConfig);
    }

}
