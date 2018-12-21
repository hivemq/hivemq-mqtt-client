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

package org.mqttbee.mqtt;

import io.netty.channel.EventLoop;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.MqttClientSslConfig;
import org.mqttbee.api.mqtt.MqttWebSocketConfig;
import org.mqttbee.api.mqtt.datatypes.MqttClientIdentifier;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ClientConnectionData;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ClientData;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ServerConnectionData;
import org.mqttbee.api.mqtt.mqtt5.advanced.Mqtt5AdvancedClientData;
import org.mqttbee.mqtt.advanced.MqttAdvancedClientData;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.ioc.ClientComponent;
import org.mqttbee.mqtt.ioc.MqttBeeComponent;
import org.mqttbee.util.ExecutorUtil;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Silvio Giebl
 */
public class MqttClientData implements Mqtt5ClientData {

    private final @NotNull MqttVersion mqttVersion;
    private volatile @NotNull MqttClientIdentifierImpl clientIdentifier;
    private final @NotNull String serverHost;
    private final int serverPort;
    private final @Nullable MqttClientSslConfigImpl sslConfig;
    private final @Nullable MqttWebSocketConfigImpl webSocketConfig;
    private final boolean followsRedirects;
    private final boolean allowsServerReAuth;
    private final @NotNull MqttClientExecutorConfigImpl executorConfig;
    private final @Nullable MqttAdvancedClientData advancedClientData;

    private final @NotNull ClientComponent clientComponent;

    private volatile @Nullable EventLoop eventLoop;
    private int eventLoopAcquires;
    private long eventLoopAcquireCount;
    private final @NotNull Object eventLoopLock = new Object();

    private final @NotNull AtomicReference<@NotNull MqttClientConnectionState> connectionState;
    private volatile @Nullable MqttClientConnectionData clientConnectionData;
    private volatile @Nullable MqttServerConnectionData serverConnectionData;

    public MqttClientData(
            final @NotNull MqttVersion mqttVersion, final @NotNull MqttClientIdentifierImpl clientIdentifier,
            final @NotNull String serverHost, final int serverPort, final @Nullable MqttClientSslConfigImpl sslConfig,
            final @Nullable MqttWebSocketConfigImpl webSocketConfig, final boolean followsRedirects,
            final boolean allowsServerReAuth, final @NotNull MqttClientExecutorConfigImpl executorConfig,
            final @Nullable MqttAdvancedClientData advancedClientData) {

        this.mqttVersion = mqttVersion;
        this.clientIdentifier = clientIdentifier;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.sslConfig = sslConfig;
        this.webSocketConfig = webSocketConfig;
        this.followsRedirects = followsRedirects;
        this.allowsServerReAuth = allowsServerReAuth;
        this.executorConfig = executorConfig;
        this.advancedClientData = advancedClientData;

        clientComponent = MqttBeeComponent.INSTANCE.clientComponentBuilder().clientData(this).build();

        connectionState = new AtomicReference<>(MqttClientConnectionState.DISCONNECTED);
    }

    public @NotNull MqttVersion getMqttVersion() {
        return mqttVersion;
    }

    @Override
    public @NotNull Optional<MqttClientIdentifier> getClientIdentifier() {
        return (clientIdentifier == MqttClientIdentifierImpl.REQUEST_CLIENT_IDENTIFIER_FROM_SERVER) ? Optional.empty() :
                Optional.of(clientIdentifier);
    }

    public @NotNull MqttClientIdentifierImpl getRawClientIdentifier() {
        return clientIdentifier;
    }

    public void setClientIdentifier(final @NotNull MqttClientIdentifierImpl clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    @Override
    public @NotNull String getServerHost() {
        return serverHost;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public boolean usesSsl() {
        return sslConfig != null;
    }

    @Override
    public @NotNull Optional<MqttClientSslConfig> getSslConfig() {
        return Optional.ofNullable(sslConfig);
    }

    public @Nullable MqttClientSslConfigImpl getRawSslConfig() {
        return sslConfig;
    }

    @Override
    public boolean usesWebSocket() {
        return webSocketConfig != null;
    }

    @Override
    public @NotNull Optional<MqttWebSocketConfig> getWebSocketConfig() {
        return Optional.ofNullable(webSocketConfig);
    }

    public @Nullable MqttWebSocketConfigImpl getRawWebSocketConfig() {
        return webSocketConfig;
    }

    @Override
    public boolean followsRedirects() {
        return followsRedirects;
    }

    @Override
    public boolean allowsServerReAuth() {
        return allowsServerReAuth;
    }

    @Override
    public @NotNull MqttClientExecutorConfigImpl getExecutorConfig() {
        return executorConfig;
    }

    public @NotNull Optional<Mqtt5AdvancedClientData> getAdvancedClientData() {
        return Optional.ofNullable(advancedClientData);
    }

    public @Nullable MqttAdvancedClientData getRawAdvancedClientData() {
        return advancedClientData;
    }

    public @NotNull EventLoop acquireEventLoop() {
        synchronized (eventLoopLock) {
            eventLoopAcquires++;
            eventLoopAcquireCount++;
            EventLoop eventLoop = this.eventLoop;
            if (eventLoop == null) {
                this.eventLoop = eventLoop = MqttBeeComponent.INSTANCE.nettyEventLoopProvider()
                        .acquireEventLoop(executorConfig.getRawNettyExecutor(), executorConfig.getRawNettyThreads());
            }
            return eventLoop;
        }
    }

    public void releaseEventLoop() {
        synchronized (eventLoopLock) {
            if (--eventLoopAcquires == 0) {
                final EventLoop eventLoop = this.eventLoop;
                final long eventLoopAcquireCount = this.eventLoopAcquireCount;
                assert eventLoop != null;
                eventLoop.execute(() -> { // release eventLoop after all tasks are finished
                    synchronized (eventLoopLock) {
                        if (eventLoopAcquireCount == this.eventLoopAcquireCount) { // eventLoop has not been reacquired
                            this.eventLoop = null;
                            MqttBeeComponent.INSTANCE.nettyEventLoopProvider()
                                    .releaseEventLoop(executorConfig.getRawNettyExecutor());
                        }
                    }
                });
            }
        }
    }

    public boolean executeInEventLoop(final @NotNull Runnable runnable) {
        final EventLoop eventLoop = this.eventLoop;
        if (eventLoop == null) {
            return false;
        }
        return ExecutorUtil.execute(eventLoop, runnable);
    }

    public @NotNull ClientComponent getClientComponent() {
        return clientComponent;
    }

    @Override
    public @NotNull MqttClientConnectionState getConnectionState() {
        return connectionState.get();
    }

    public @NotNull AtomicReference<@NotNull MqttClientConnectionState> getRawConnectionState() {
        return connectionState;
    }

    @Override
    public @NotNull Optional<Mqtt5ClientConnectionData> getClientConnectionData() {
        return Optional.ofNullable(clientConnectionData);
    }

    public @Nullable MqttClientConnectionData getRawClientConnectionData() {
        return clientConnectionData;
    }

    public void setClientConnectionData(final @Nullable MqttClientConnectionData clientConnectionData) {
        this.clientConnectionData = clientConnectionData;
    }

    @Override
    public @NotNull Optional<Mqtt5ServerConnectionData> getServerConnectionData() {
        return Optional.ofNullable(serverConnectionData);
    }

    public @Nullable MqttServerConnectionData getRawServerConnectionData() {
        return serverConnectionData;
    }

    public void setServerConnectionData(final @Nullable MqttServerConnectionData serverConnectionData) {
        this.serverConnectionData = serverConnectionData;
    }
}
