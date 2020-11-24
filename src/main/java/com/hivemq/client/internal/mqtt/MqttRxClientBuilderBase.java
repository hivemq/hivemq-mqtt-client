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

package com.hivemq.client.internal.mqtt;

import com.hivemq.client.internal.mqtt.advanced.MqttAdvancedConfig;
import com.hivemq.client.internal.mqtt.datatypes.MqttClientIdentifierImpl;
import com.hivemq.client.internal.mqtt.lifecycle.MqttAutoReconnectImpl;
import com.hivemq.client.internal.mqtt.lifecycle.MqttAutoReconnectImplBuilder;
import com.hivemq.client.internal.mqtt.mqtt3.Mqtt3RxClientViewBuilder;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.*;
import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import com.hivemq.client.mqtt.lifecycle.MqttAutoReconnect;
import com.hivemq.client.mqtt.lifecycle.MqttConnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectedListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author Silvio Giebl
 */
public abstract class MqttRxClientBuilderBase<B extends MqttRxClientBuilderBase<B>>
        extends MqttTransportConfigImplBuilder<B> {

    private @NotNull MqttClientIdentifierImpl identifier =
            MqttClientIdentifierImpl.REQUEST_CLIENT_IDENTIFIER_FROM_SERVER;
    private @Nullable MqttTransportConfigImpl transportConfig = MqttTransportConfigImpl.DEFAULT;
    private @NotNull MqttExecutorConfigImpl executorConfig = MqttExecutorConfigImpl.DEFAULT;
    private @Nullable MqttAutoReconnectImpl autoReconnect;
    private ImmutableList.@Nullable Builder<MqttConnectedListener> connectedListenersBuilder;
    private ImmutableList.@Nullable Builder<MqttDisconnectedListener> disconnectedListenersBuilder;

    protected MqttRxClientBuilderBase() {}

    protected MqttRxClientBuilderBase(final @NotNull MqttRxClientBuilderBase<?> clientBuilder) {
        super(clientBuilder);
        identifier = clientBuilder.identifier;
        transportConfig = clientBuilder.transportConfig;
        executorConfig = clientBuilder.executorConfig;
        autoReconnect = clientBuilder.autoReconnect;
        connectedListenersBuilder = clientBuilder.connectedListenersBuilder;
        disconnectedListenersBuilder = clientBuilder.disconnectedListenersBuilder;
    }

    protected abstract @NotNull B self();

    public @NotNull B identifier(final @Nullable String identifier) {
        this.identifier = MqttClientIdentifierImpl.of(identifier);
        return self();
    }

    public @NotNull B identifier(final @Nullable MqttClientIdentifier identifier) {
        this.identifier = MqttChecks.clientIdentifier(identifier);
        return self();
    }

    @Override
    public @NotNull B serverAddress(final @Nullable InetSocketAddress address) {
        transportConfig = null;
        return super.serverAddress(address);
    }

    @Override
    public @NotNull B serverHost(final @Nullable String host) {
        transportConfig = null;
        return super.serverHost(host);
    }

    @Override
    public @NotNull B serverHost(final @Nullable InetAddress host) {
        transportConfig = null;
        return super.serverHost(host);
    }

    @Override
    public @NotNull B serverPort(final int port) {
        transportConfig = null;
        return super.serverPort(port);
    }

    @Override
    public @NotNull B localAddress(final @Nullable InetSocketAddress address) {
        transportConfig = null;
        return super.localAddress(address);
    }

    @Override
    public @NotNull B localAddress(final @Nullable String address) {
        transportConfig = null;
        return super.localAddress(address);
    }

    @Override
    public @NotNull B localAddress(final @Nullable InetAddress address) {
        transportConfig = null;
        return super.localAddress(address);
    }

    @Override
    public @NotNull B localPort(final int port) {
        transportConfig = null;
        return super.localPort(port);
    }

    public @NotNull B tls() {
        transportConfig = null;
        return super.tls();
    }

    @Override
    public @NotNull B tlsConfig(final @Nullable MqttTlsConfig tlsConfig) {
        transportConfig = null;
        return super.tlsConfig(tlsConfig);
    }

    public @NotNull B webSocket() {
        transportConfig = null;
        return super.webSocket();
    }

    @Override
    public @NotNull B webSocketConfig(final @Nullable MqttWebSocketConfig webSocketConfig) {
        transportConfig = null;
        return super.webSocketConfig(webSocketConfig);
    }

    @Override
    public @NotNull B proxyConfig(final @Nullable MqttProxyConfig proxyConfig) {
        transportConfig = null;
        return super.proxyConfig(proxyConfig);
    }

    @Override
    public @NotNull B socketConnectTimeout(final long timeout, final @Nullable TimeUnit timeUnit) {
        transportConfig = null;
        return super.socketConnectTimeout(timeout, timeUnit);
    }

    @Override
    public @NotNull B mqttConnectTimeout(final long timeout, final @Nullable TimeUnit timeUnit) {
        transportConfig = null;
        return super.mqttConnectTimeout(timeout, timeUnit);
    }

    public @NotNull B transportConfig(final @Nullable MqttTransportConfig transportConfig) {
        this.transportConfig =
                Checks.notImplemented(transportConfig, MqttTransportConfigImpl.class, "Transport config");
        set(this.transportConfig);
        return self();
    }

    public MqttTransportConfigImplBuilder.@NotNull Nested<B> transportConfigWith() {
        return new MqttTransportConfigImplBuilder.Nested<>(this, this::transportConfig);
    }

    public @NotNull B executorConfig(final @Nullable MqttExecutorConfig executorConfig) {
        this.executorConfig = Checks.notImplemented(executorConfig, MqttExecutorConfigImpl.class, "Executor config");
        return self();
    }

    public MqttExecutorConfigImplBuilder.@NotNull Nested<B> executorConfigWith() {
        return new MqttExecutorConfigImplBuilder.Nested<>(executorConfig, this::executorConfig);
    }

    public @NotNull B automaticReconnect() {
        this.autoReconnect = MqttAutoReconnectImpl.DEFAULT;
        return self();
    }

    public @NotNull B automaticReconnect(final @Nullable MqttAutoReconnect autoReconnect) {
        this.autoReconnect =
                Checks.notImplementedOrNull(autoReconnect, MqttAutoReconnectImpl.class, "Automatic reconnect");
        return self();
    }

    public MqttAutoReconnectImplBuilder.@NotNull Nested<B> automaticReconnectWith() {
        return new MqttAutoReconnectImplBuilder.Nested<>(autoReconnect, this::automaticReconnect);
    }

    public @NotNull B addConnectedListener(final @Nullable MqttConnectedListener connectedListener) {
        Checks.notNull(connectedListener, "Connected listener");
        if (connectedListenersBuilder == null) {
            connectedListenersBuilder = ImmutableList.builder();
        }
        connectedListenersBuilder.add(connectedListener);
        return self();
    }

    public @NotNull B addDisconnectedListener(final @Nullable MqttDisconnectedListener disconnectedListener) {
        Checks.notNull(disconnectedListener, "Disconnected listener");
        if (disconnectedListenersBuilder == null) {
            disconnectedListenersBuilder = ImmutableList.builder();
        }
        disconnectedListenersBuilder.add(disconnectedListener);
        return self();
    }

    @Override
    @NotNull MqttTransportConfigImpl buildTransportConfig() {
        if (transportConfig == null) {
            return super.buildTransportConfig();
        }
        return transportConfig;
    }

    private @NotNull ImmutableList<MqttConnectedListener> buildConnectedListeners() {
        if (connectedListenersBuilder == null) {
            return ImmutableList.of();
        }
        return connectedListenersBuilder.build();
    }

    private @NotNull ImmutableList<MqttDisconnectedListener> buildDisconnectedListeners() {
        if (disconnectedListenersBuilder == null) {
            if (autoReconnect == null) {
                return ImmutableList.of();
            }
            return ImmutableList.of(autoReconnect);
        }
        if (autoReconnect == null) {
            return disconnectedListenersBuilder.build();
        }
        return ImmutableList.<MqttDisconnectedListener>builder(disconnectedListenersBuilder.getSize() + 1).add(
                autoReconnect).addAll(disconnectedListenersBuilder.build()).build();
    }

    protected @NotNull MqttClientConfig buildClientConfig(
            final @NotNull MqttVersion mqttVersion,
            final @NotNull MqttAdvancedConfig advancedConfig,
            final @NotNull MqttClientConfig.ConnectDefaults connectDefaults) {

        return new MqttClientConfig(mqttVersion, identifier, buildTransportConfig(), executorConfig, advancedConfig,
                connectDefaults, buildConnectedListeners(), buildDisconnectedListeners());
    }

    public static class Choose extends MqttRxClientBuilderBase<Choose> implements MqttClientBuilder {

        @Override
        protected @NotNull Choose self() {
            return this;
        }

        @Override
        public @NotNull Mqtt3RxClientViewBuilder useMqttVersion3() {
            return new Mqtt3RxClientViewBuilder(this);
        }

        @Override
        public @NotNull MqttRxClientBuilder useMqttVersion5() {
            return new MqttRxClientBuilder(this);
        }
    }
}
