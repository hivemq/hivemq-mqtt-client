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

package com.hivemq.mqtt.client2.internal;

import com.hivemq.mqtt.client2.*;
import com.hivemq.mqtt.client2.datatypes.MqttClientIdentifier;
import com.hivemq.mqtt.client2.internal.advanced.MqttAdvancedConfig;
import com.hivemq.mqtt.client2.internal.collections.ImmutableList;
import com.hivemq.mqtt.client2.internal.datatypes.MqttClientIdentifierImpl;
import com.hivemq.mqtt.client2.internal.lifecycle.MqttAutoReconnectImpl;
import com.hivemq.mqtt.client2.internal.lifecycle.MqttAutoReconnectImplBuilder;
import com.hivemq.mqtt.client2.internal.lifecycle.MqttConnectedContextImpl;
import com.hivemq.mqtt.client2.internal.mqtt3.Mqtt3RxClientViewBuilder;
import com.hivemq.mqtt.client2.internal.util.Checks;
import com.hivemq.mqtt.client2.internal.util.MqttChecks;
import com.hivemq.mqtt.client2.lifecycle.MqttAutoReconnect;
import com.hivemq.mqtt.client2.lifecycle.MqttConnectedContext;
import com.hivemq.mqtt.client2.lifecycle.MqttConnectedListener;
import com.hivemq.mqtt.client2.lifecycle.MqttDisconnectedListener;
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
    private ImmutableList.@Nullable Builder<MqttDisconnectedListener> disconnectedListenersBuilder;

    protected MqttRxClientBuilderBase() {}

    protected MqttRxClientBuilderBase(final @NotNull MqttRxClientBuilderBase<?> clientBuilder) {
        super(clientBuilder);
        identifier = clientBuilder.identifier;
        transportConfig = clientBuilder.transportConfig;
        executorConfig = clientBuilder.executorConfig;
        autoReconnect = clientBuilder.autoReconnect;
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
        return ImmutableList.<MqttDisconnectedListener>builder(disconnectedListenersBuilder.getSize() + 1)
                .add(autoReconnect)
                .addAll(disconnectedListenersBuilder.build())
                .build();
    }

    protected @NotNull MqttClientConfig buildClientConfig(
            final @NotNull MqttVersion mqttVersion,
            final @NotNull MqttAdvancedConfig advancedConfig,
            final @NotNull MqttClientConfig.ConnectDefaults connectDefaults,
            final @NotNull ImmutableList<MqttConnectedListener<? super MqttConnectedContextImpl>> connectedListeners) {

        return new MqttClientConfig(mqttVersion, identifier, buildTransportConfig(), executorConfig, advancedConfig,
                connectDefaults, connectedListeners, buildDisconnectedListeners());
    }

    public static class Choose extends MqttRxClientBuilderBase<Choose> implements MqttClientBuilder {

        private ImmutableList.@Nullable Builder<MqttConnectedListener<? super MqttConnectedContext>>
                connectedListenersBuilder;

        @Override
        protected @NotNull Choose self() {
            return this;
        }

        @Override
        public @NotNull MqttClientBuilder addConnectedListener(
                final @Nullable MqttConnectedListener<? super MqttConnectedContext> connectedListener) {
            Checks.notNull(connectedListener, "Connected listener");
            if (connectedListenersBuilder == null) {
                connectedListenersBuilder = ImmutableList.builder();
            }
            connectedListenersBuilder.add(connectedListener);
            return this;
        }

        private @NotNull ImmutableList<MqttConnectedListener<? super MqttConnectedContext>> buildConnectedListeners() {
            return (connectedListenersBuilder == null) ? ImmutableList.of() : connectedListenersBuilder.build();
        }

        @Override
        public @NotNull Mqtt3RxClientViewBuilder useMqttVersion3() {
            return new Mqtt3RxClientViewBuilder(this, buildConnectedListeners());
        }

        @Override
        public @NotNull MqttRxClientBuilder useMqttVersion5() {
            return new MqttRxClientBuilder(this, buildConnectedListeners());
        }
    }
}
