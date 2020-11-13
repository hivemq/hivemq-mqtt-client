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
import com.hivemq.client.internal.mqtt.ioc.ClientComponent;
import com.hivemq.client.internal.mqtt.ioc.SingletonComponent;
import com.hivemq.client.internal.mqtt.message.auth.MqttSimpleAuth;
import com.hivemq.client.internal.mqtt.message.publish.MqttWillPublish;
import com.hivemq.client.internal.netty.NettyEventLoopProvider;
import com.hivemq.client.internal.util.ExecutorUtil;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import com.hivemq.client.mqtt.lifecycle.MqttAutoReconnect;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConfig;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConnectionConfig;
import com.hivemq.client.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import io.netty.channel.EventLoop;
import io.netty.handler.ssl.SslContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Silvio Giebl
 */
public class MqttClientConfig implements Mqtt5ClientConfig {

    private final @NotNull MqttVersion mqttVersion;
    private volatile @NotNull MqttClientIdentifierImpl clientIdentifier;
    private final @NotNull MqttTransportConfigImpl transportConfig;
    private final @NotNull MqttExecutorConfigImpl executorConfig;
    private final @NotNull MqttAdvancedConfig advancedConfig;
    private final @NotNull ConnectDefaults connectDefaults;
    private final @NotNull ImmutableList<MqttClientConnectedListener> connectedListeners;
    private final @NotNull ImmutableList<MqttClientDisconnectedListener> disconnectedListeners;

    private final @NotNull ClientComponent clientComponent;

    private volatile @Nullable EventLoop eventLoop;
    private int eventLoopAcquires;
    private long eventLoopAcquireCount;

    private final @NotNull AtomicReference<@NotNull MqttClientState> state;
    private volatile @Nullable MqttClientConnectionConfig connectionConfig;
    private @NotNull MqttTransportConfigImpl currentTransportConfig;
    private @Nullable SslContext currentSslContext;
    private boolean resubscribeIfSessionExpired;
    private boolean republishIfSessionExpired;

    public MqttClientConfig(
            final @NotNull MqttVersion mqttVersion,
            final @NotNull MqttClientIdentifierImpl clientIdentifier,
            final @NotNull MqttTransportConfigImpl transportConfig,
            final @NotNull MqttExecutorConfigImpl executorConfig,
            final @NotNull MqttAdvancedConfig advancedConfig,
            final @NotNull ConnectDefaults connectDefaults,
            final @NotNull ImmutableList<MqttClientConnectedListener> connectedListeners,
            final @NotNull ImmutableList<MqttClientDisconnectedListener> disconnectedListeners) {

        this.mqttVersion = mqttVersion;
        this.clientIdentifier = clientIdentifier;
        this.transportConfig = transportConfig;
        this.executorConfig = executorConfig;
        this.advancedConfig = advancedConfig;
        this.connectDefaults = connectDefaults;
        this.connectedListeners = connectedListeners;
        this.disconnectedListeners = disconnectedListeners;

        clientComponent = SingletonComponent.INSTANCE.clientComponentBuilder().clientConfig(this).build();

        state = new AtomicReference<>(MqttClientState.DISCONNECTED);
        currentTransportConfig = transportConfig;
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
    public @NotNull MqttTransportConfigImpl getTransportConfig() {
        return transportConfig;
    }

    @Override
    public @NotNull MqttExecutorConfigImpl getExecutorConfig() {
        return executorConfig;
    }

    @Override
    public @NotNull MqttAdvancedConfig getAdvancedConfig() {
        return advancedConfig;
    }

    @Override
    public @NotNull Optional<Mqtt5SimpleAuth> getSimpleAuth() {
        return Optional.ofNullable(connectDefaults.simpleAuth);
    }

    @Override
    public @NotNull Optional<Mqtt5EnhancedAuthMechanism> getEnhancedAuthMechanism() {
        return Optional.ofNullable(connectDefaults.enhancedAuthMechanism);
    }

    @Override
    public @NotNull Optional<Mqtt5WillPublish> getWillPublish() {
        return Optional.ofNullable(connectDefaults.willPublish);
    }

    public @NotNull ConnectDefaults getConnectDefaults() {
        return connectDefaults;
    }

    @Override
    public @NotNull Optional<MqttAutoReconnect> getAutomaticReconnect() {
        for (final MqttClientDisconnectedListener disconnectedListener : disconnectedListeners) {
            if (disconnectedListener instanceof MqttAutoReconnect) {
                return Optional.of((MqttAutoReconnect) disconnectedListener);
            }
        }
        return Optional.empty();
    }

    @Override
    public @NotNull ImmutableList<MqttClientConnectedListener> getConnectedListeners() {
        return connectedListeners;
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
                this.eventLoop = eventLoop =
                        NettyEventLoopProvider.INSTANCE.acquireEventLoop(executorConfig.getRawNettyExecutor(),
                                executorConfig.getRawNettyThreads());
            }
            return eventLoop;
        }
    }

    public void releaseEventLoop() {
        synchronized (state) {
            if (--eventLoopAcquires == 0) {
                final EventLoop eventLoop = this.eventLoop;
                final long eventLoopAcquireCount = this.eventLoopAcquireCount;
                assert eventLoop != null : "eventLoopAcquires was > 0 -> eventLoop != null";
                eventLoop.execute(() -> { // release eventLoop after all tasks are finished
                    synchronized (state) {
                        if (eventLoopAcquireCount == this.eventLoopAcquireCount) { // eventLoop has not been reacquired
                            this.eventLoop = null;
                            // releaseEventLoop must be the last statement so everything is cleaned up even if it throws
                            NettyEventLoopProvider.INSTANCE.releaseEventLoop(executorConfig.getRawNettyExecutor());
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

    public @NotNull MqttTransportConfigImpl getCurrentTransportConfig() {
        return currentTransportConfig;
    }

    public void setCurrentTransportConfig(final @NotNull MqttTransportConfigImpl currentTransportConfig) {
        if (!this.currentTransportConfig.equals(currentTransportConfig)) {
            this.currentTransportConfig = currentTransportConfig;
            currentSslContext = null;
        }
    }

    public @Nullable SslContext getCurrentSslContext() {
        return currentSslContext;
    }

    public void setCurrentSslContext(final @Nullable SslContext currentSslContext) {
        this.currentSslContext = currentSslContext;
    }

    public boolean isResubscribeIfSessionExpired() {
        return resubscribeIfSessionExpired;
    }

    public void setResubscribeIfSessionExpired(final boolean resubscribeIfSessionExpired) {
        this.resubscribeIfSessionExpired = resubscribeIfSessionExpired;
    }

    public boolean isRepublishIfSessionExpired() {
        return republishIfSessionExpired;
    }

    public void setRepublishIfSessionExpired(final boolean republishIfSessionExpired) {
        this.republishIfSessionExpired = republishIfSessionExpired;
    }

    public static class ConnectDefaults {

        private static final @NotNull ConnectDefaults EMPTY = new ConnectDefaults(null, null, null);

        public static @NotNull ConnectDefaults of(
                final @Nullable MqttSimpleAuth simpleAuth,
                final @Nullable Mqtt5EnhancedAuthMechanism enhancedAuthMechanism,
                final @Nullable MqttWillPublish willPublish) {

            if ((simpleAuth == null) && (enhancedAuthMechanism == null) && (willPublish == null)) {
                return EMPTY;
            }
            return new ConnectDefaults(simpleAuth, enhancedAuthMechanism, willPublish);
        }

        final @Nullable MqttSimpleAuth simpleAuth;
        final @Nullable Mqtt5EnhancedAuthMechanism enhancedAuthMechanism;
        final @Nullable MqttWillPublish willPublish;

        private ConnectDefaults(
                final @Nullable MqttSimpleAuth simpleAuth,
                final @Nullable Mqtt5EnhancedAuthMechanism enhancedAuthMechanism,
                final @Nullable MqttWillPublish willPublish) {

            this.simpleAuth = simpleAuth;
            this.enhancedAuthMechanism = enhancedAuthMechanism;
            this.willPublish = willPublish;
        }

        public @Nullable MqttSimpleAuth getSimpleAuth() {
            return simpleAuth;
        }

        public @Nullable Mqtt5EnhancedAuthMechanism getEnhancedAuthMechanism() {
            return enhancedAuthMechanism;
        }

        public @Nullable MqttWillPublish getWillPublish() {
            return willPublish;
        }
    }
}
