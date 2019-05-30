/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt;

import com.hivemq.client.internal.mqtt.advanced.MqttClientAdvancedConfig;
import com.hivemq.client.internal.mqtt.datatypes.MqttClientIdentifierImpl;
import com.hivemq.client.internal.mqtt.ioc.ClientComponent;
import com.hivemq.client.internal.mqtt.ioc.SingletonComponent;
import com.hivemq.client.internal.util.ExecutorUtil;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.MqttWebSocketConfig;
import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import com.hivemq.client.mqtt.lifecycle.MqttClientAutoReconnect;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConfig;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConnectionConfig;
import io.netty.channel.EventLoop;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Silvio Giebl
 */
public class MqttClientConfig implements Mqtt5ClientConfig {

    private final @NotNull MqttVersion mqttVersion;
    private volatile @NotNull MqttClientIdentifierImpl clientIdentifier;
    private final @NotNull InetSocketAddress serverAddress;
    private final @NotNull MqttClientExecutorConfigImpl executorConfig;
    private final @Nullable MqttClientSslConfigImpl sslConfig;
    private final @Nullable MqttWebSocketConfigImpl webSocketConfig;
    private final @NotNull MqttClientAdvancedConfig advancedConfig;
    private final @NotNull ImmutableList<MqttClientDisconnectedListener> disconnectedListeners;

    private final @NotNull ClientComponent clientComponent;

    private volatile @Nullable EventLoop eventLoop;
    private int eventLoopAcquires;
    private long eventLoopAcquireCount;

    private final @NotNull AtomicReference<@NotNull MqttClientState> state;
    private volatile @Nullable MqttClientConnectionConfig connectionConfig;

    public MqttClientConfig(
            final @NotNull MqttVersion mqttVersion, final @NotNull MqttClientIdentifierImpl clientIdentifier,
            final @NotNull InetSocketAddress serverAddress, final @NotNull MqttClientExecutorConfigImpl executorConfig,
            final @Nullable MqttClientSslConfigImpl sslConfig, final @Nullable MqttWebSocketConfigImpl webSocketConfig,
            final @NotNull MqttClientAdvancedConfig advancedConfig,
            final @NotNull ImmutableList<MqttClientDisconnectedListener> disconnectedListeners) {

        this.mqttVersion = mqttVersion;
        this.clientIdentifier = clientIdentifier;
        this.serverAddress = serverAddress;
        this.executorConfig = executorConfig;
        this.sslConfig = sslConfig;
        this.webSocketConfig = webSocketConfig;
        this.advancedConfig = advancedConfig;
        this.disconnectedListeners = disconnectedListeners;

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
    public @NotNull InetSocketAddress getServerAddress() {
        return serverAddress;
    }

    @Override
    public @NotNull String getServerHost() {
        return serverAddress.getHostString();
    }

    @Override
    public int getServerPort() {
        return serverAddress.getPort();
    }

    @Override
    public @NotNull MqttClientExecutorConfigImpl getExecutorConfig() {
        return executorConfig;
    }

    @Override
    public @NotNull Optional<MqttClientSslConfig> getSslConfig() {
        return Optional.ofNullable(sslConfig);
    }

    public @Nullable MqttClientSslConfigImpl getRawSslConfig() {
        return sslConfig;
    }

    @Override
    public @NotNull Optional<MqttWebSocketConfig> getWebSocketConfig() {
        return Optional.ofNullable(webSocketConfig);
    }

    public @Nullable MqttWebSocketConfigImpl getRawWebSocketConfig() {
        return webSocketConfig;
    }

    @Override
    public @NotNull MqttClientAdvancedConfig getAdvancedConfig() {
        return advancedConfig;
    }

    @Override
    public @NotNull Optional<MqttClientAutoReconnect> getAutomaticReconnect() {
        for (final MqttClientDisconnectedListener disconnectedListener : disconnectedListeners) {
            if (disconnectedListener instanceof MqttClientAutoReconnect) {
                return Optional.of((MqttClientAutoReconnect) disconnectedListener);
            }
        }
        return Optional.empty();
    }

    @Override
    public @NotNull ImmutableList<MqttClientDisconnectedListener> getDisconnectedListeners() {
        return disconnectedListeners;
    }

    public @NotNull ClientComponent getClientComponent() {
        return clientComponent;
    }

    public @NotNull EventLoop acquireEventLoop() {
        synchronized (state) {
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
        synchronized (state) {
            if (--eventLoopAcquires == 0) {
                final EventLoop eventLoop = this.eventLoop;
                final long eventLoopAcquireCount = this.eventLoopAcquireCount;
                assert eventLoop != null;
                eventLoop.execute(() -> { // release eventLoop after all tasks are finished
                    synchronized (state) {
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
