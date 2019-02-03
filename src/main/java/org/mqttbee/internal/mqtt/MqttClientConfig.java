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

package org.mqttbee.internal.mqtt;

import io.netty.channel.EventLoop;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.internal.mqtt.advanced.MqttAdvancedClientConfig;
import org.mqttbee.internal.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.internal.mqtt.ioc.ClientComponent;
import org.mqttbee.internal.mqtt.ioc.SingletonComponent;
import org.mqttbee.internal.util.ExecutorUtil;
import org.mqttbee.mqtt.MqttClientSslConfig;
import org.mqttbee.mqtt.MqttClientState;
import org.mqttbee.mqtt.MqttVersion;
import org.mqttbee.mqtt.MqttWebSocketConfig;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifier;
import org.mqttbee.mqtt.mqtt5.Mqtt5ClientConfig;
import org.mqttbee.mqtt.mqtt5.Mqtt5ClientConnectionConfig;
import org.mqttbee.mqtt.mqtt5.advanced.Mqtt5AdvancedClientConfig;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Silvio Giebl
 */
public class MqttClientConfig implements Mqtt5ClientConfig {

    private final @NotNull MqttVersion mqttVersion;
    private volatile @NotNull MqttClientIdentifierImpl clientIdentifier;
    private final @NotNull String serverHost;
    private final int serverPort;
    private final @Nullable MqttClientSslConfigImpl sslConfig;
    private final @Nullable MqttWebSocketConfigImpl webSocketConfig;
    private final boolean allowsServerReAuth;
    private final @NotNull MqttClientExecutorConfigImpl executorConfig;
    private final @Nullable MqttAdvancedClientConfig advancedConfig;

    private final @NotNull ClientComponent clientComponent;

    private volatile @Nullable EventLoop eventLoop;
    private int eventLoopAcquires;
    private long eventLoopAcquireCount;
    private final @NotNull Object eventLoopLock = new Object();

    private final @NotNull AtomicReference<@NotNull MqttClientState> state;
    private volatile @Nullable MqttClientConnectionConfig connectionConfig;

    public MqttClientConfig(
            final @NotNull MqttVersion mqttVersion, final @NotNull MqttClientIdentifierImpl clientIdentifier,
            final @NotNull String serverHost, final int serverPort, final @Nullable MqttClientSslConfigImpl sslConfig,
            final @Nullable MqttWebSocketConfigImpl webSocketConfig, final boolean allowsServerReAuth,
            final @NotNull MqttClientExecutorConfigImpl executorConfig,
            final @Nullable MqttAdvancedClientConfig advancedConfig) {

        this.mqttVersion = mqttVersion;
        this.clientIdentifier = clientIdentifier;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.sslConfig = sslConfig;
        this.webSocketConfig = webSocketConfig;
        this.allowsServerReAuth = allowsServerReAuth;
        this.executorConfig = executorConfig;
        this.advancedConfig = advancedConfig;

        clientComponent = SingletonComponent.INSTANCE.clientComponentBuilder().clientConfig(this).build();

        state = new AtomicReference<>(MqttClientState.DISCONNECTED);
    }

    @Override
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
    public boolean allowsServerReAuth() {
        return allowsServerReAuth;
    }

    @Override
    public @NotNull MqttClientExecutorConfigImpl getExecutorConfig() {
        return executorConfig;
    }

    public @NotNull Optional<Mqtt5AdvancedClientConfig> getAdvancedConfig() {
        return Optional.ofNullable(advancedConfig);
    }

    public @Nullable MqttAdvancedClientConfig getRawAdvancedConfig() {
        return advancedConfig;
    }

    public @NotNull ClientComponent getClientComponent() {
        return clientComponent;
    }

    public @NotNull EventLoop acquireEventLoop() {
        synchronized (eventLoopLock) {
            eventLoopAcquires++;
            eventLoopAcquireCount++;
            EventLoop eventLoop = this.eventLoop;
            if (eventLoop == null) {
                this.eventLoop = eventLoop = SingletonComponent.INSTANCE.nettyEventLoopProvider()
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
                            SingletonComponent.INSTANCE.nettyEventLoopProvider()
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

    @Override
    public @NotNull MqttClientState getState() {
        return state.get();
    }

    public @NotNull AtomicReference<@NotNull MqttClientState> getRawState() {
        return state;
    }

    @Override
    public @NotNull Optional<Mqtt5ClientConnectionConfig> getConnectionConfig() {
        return Optional.ofNullable(connectionConfig);
    }

    public @Nullable MqttClientConnectionConfig getRawConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(final @Nullable MqttClientConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }
}
