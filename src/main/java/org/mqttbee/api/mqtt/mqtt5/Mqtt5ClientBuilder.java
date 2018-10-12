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

package org.mqttbee.api.mqtt.mqtt5;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.*;
import org.mqttbee.api.mqtt.datatypes.MqttClientIdentifier;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3ClientBuilder;
import org.mqttbee.api.mqtt.mqtt5.advanced.Mqtt5AdvancedClientData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.MqttClientExecutorConfigImpl;
import org.mqttbee.mqtt.MqttVersion;
import org.mqttbee.mqtt.advanced.MqttAdvancedClientData;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.mqtt5.Mqtt5ClientImpl;
import org.mqttbee.util.MustNotBeImplementedUtil;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ClientBuilder extends MqttClientBuilder {

    private boolean followRedirects = false;
    private boolean allowServerReAuth = false;
    private MqttAdvancedClientData advancedClientData;

    public Mqtt5ClientBuilder(
            @NotNull final MqttClientIdentifierImpl identifier, @NotNull final String serverHost, final int serverPort,
            @Nullable final MqttClientSslConfig sslConfig, @Nullable final MqttWebSocketConfig webSocketConfig,
            @NotNull final MqttClientExecutorConfigImpl executorConfig) {

        Preconditions.checkNotNull(identifier, "Identifier must not be null.");
        Preconditions.checkNotNull(serverHost, "Server host must not be null.");
        Preconditions.checkNotNull(executorConfig, "Executor config must not be null.");

        this.identifier = identifier;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.sslConfig = sslConfig;
        this.webSocketConfig = webSocketConfig;
        this.executorConfig = executorConfig;
    }

    @NotNull
    @Override
    public Mqtt5ClientBuilder identifier(@NotNull final String identifier) {
        super.identifier(identifier);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5ClientBuilder identifier(@NotNull final MqttClientIdentifier identifier) {
        super.identifier(identifier);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5ClientBuilder serverHost(@NotNull final String host) {
        super.serverHost(host);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5ClientBuilder serverPort(final int port) {
        super.serverPort(port);
        return this;
    }

    @NotNull
    @Override
    public Mqtt5ClientBuilder useSslWithDefaultConfig() {
        super.useSslWithDefaultConfig();
        return this;
    }

    @NotNull
    @Override
    public Mqtt5ClientBuilder useSsl(@NotNull final MqttClientSslConfig sslConfig) {
        super.useSsl(sslConfig);
        return this;
    }

    @NotNull
    @Override
    public MqttClientSslConfigBuilder<? extends Mqtt5ClientBuilder> useSsl() {
        return new MqttClientSslConfigBuilder<>(this::useSsl);
    }

    @NotNull
    @Override
    public Mqtt5ClientBuilder useWebSocketWithDefaultConfig() {
        super.useWebSocketWithDefaultConfig();
        return this;
    }

    @NotNull
    @Override
    public Mqtt5ClientBuilder useWebSocket(@NotNull final MqttWebSocketConfig webSocketConfig) {
        super.useWebSocket(webSocketConfig);
        return this;
    }

    @NotNull
    @Override
    public MqttWebSocketConfigBuilder<? extends Mqtt5ClientBuilder> useWebSocket() {
        return new MqttWebSocketConfigBuilder<>(this::useWebSocket);
    }

    @NotNull
    @Override
    public Mqtt5ClientBuilder executorConfig(@NotNull final MqttClientExecutorConfig executorConfig) {
        super.executorConfig(executorConfig);
        return this;
    }

    @NotNull
    @Override
    public MqttClientExecutorConfigBuilder<? extends Mqtt5ClientBuilder> executorConfig() {
        return new MqttClientExecutorConfigBuilder<>(this::executorConfig);
    }

    @NotNull
    @Override
    public Mqtt3ClientBuilder useMqttVersion3() {
        throw new UnsupportedOperationException(
                "Switching MQTT Version is not allowed. Please call useMqttVersion3/5 only once.");
    }

    @NotNull
    @Override
    public Mqtt5ClientBuilder useMqttVersion5() {
        return this;
    }

    @NotNull
    public Mqtt5ClientBuilder followRedirects(final boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    @NotNull
    public Mqtt5ClientBuilder allowServerReAuth(final boolean allowServerReAuth) {
        this.allowServerReAuth = allowServerReAuth;
        return this;
    }

    @NotNull
    public Mqtt5ClientBuilder advancedClientData(@Nullable final Mqtt5AdvancedClientData advancedClientData) {
        this.advancedClientData =
                MustNotBeImplementedUtil.checkNullOrNotImplemented(advancedClientData, MqttAdvancedClientData.class);
        return this;
    }

    public @NotNull Mqtt5RxClient buildRx() {
        return new Mqtt5ClientImpl(buildClientData());
    }

    public @NotNull Mqtt5AsyncClient buildAsync() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public @NotNull Mqtt5BlockingClient buildBlocking() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @NotNull
    private MqttClientData buildClientData() {
        return new MqttClientData(MqttVersion.MQTT_5_0, identifier, serverHost, serverPort, sslConfig, webSocketConfig,
                followRedirects, allowServerReAuth, executorConfig, advancedClientData);
    }

}
