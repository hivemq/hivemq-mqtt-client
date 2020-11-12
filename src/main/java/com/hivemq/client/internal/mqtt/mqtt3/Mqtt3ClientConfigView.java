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

package com.hivemq.client.internal.mqtt.mqtt3;

import com.hivemq.client.annotations.Immutable;
import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.message.auth.MqttSimpleAuth;
import com.hivemq.client.internal.mqtt.message.auth.mqtt3.Mqtt3SimpleAuthView;
import com.hivemq.client.internal.mqtt.message.publish.MqttWillPublish;
import com.hivemq.client.internal.mqtt.message.publish.mqtt3.Mqtt3PublishView;
import com.hivemq.client.mqtt.MqttClientExecutorConfig;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.MqttTransportConfig;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import com.hivemq.client.mqtt.lifecycle.MqttClientAutoReconnect;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientConfig;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientConnectionConfig;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt3ClientConfigView implements Mqtt3ClientConfig {

    private final @NotNull MqttClientConfig delegate;

    public Mqtt3ClientConfigView(final @NotNull MqttClientConfig delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull MqttVersion getMqttVersion() {
        return delegate.getMqttVersion();
    }

    @Override
    public @NotNull Optional<MqttClientIdentifier> getClientIdentifier() {
        return delegate.getClientIdentifier();
    }

    @Override
    public @NotNull MqttTransportConfig getTransportConfig() {
        return delegate.getTransportConfig();
    }

    @Override
    public @NotNull MqttClientExecutorConfig getExecutorConfig() {
        return delegate.getExecutorConfig();
    }

    @Override
    public @NotNull Optional<MqttClientAutoReconnect> getAutomaticReconnect() {
        return delegate.getAutomaticReconnect();
    }

    @Override
    public @NotNull Optional<Mqtt3SimpleAuth> getSimpleAuth() {
        return Optional.ofNullable(getRawSimpleAuth());
    }

    private @Nullable Mqtt3SimpleAuth getRawSimpleAuth() {
        final MqttSimpleAuth simpleAuth = delegate.getConnectDefaults().getSimpleAuth();
        return (simpleAuth == null) ? null : Mqtt3SimpleAuthView.of(simpleAuth);
    }

    @Override
    public @NotNull Optional<Mqtt3Publish> getWillPublish() {
        return Optional.ofNullable(getRawWillPublish());
    }

    private @Nullable Mqtt3Publish getRawWillPublish() {
        final MqttWillPublish willPublish = delegate.getConnectDefaults().getWillPublish();
        return (willPublish == null) ? null : Mqtt3PublishView.of(willPublish);
    }

    @Override
    public @Immutable @NotNull List<@NotNull MqttClientConnectedListener> getConnectedListeners() {
        return delegate.getConnectedListeners();
    }

    @Override
    public @Immutable @NotNull List<@NotNull MqttClientDisconnectedListener> getDisconnectedListeners() {
        return delegate.getDisconnectedListeners();
    }

    @Override
    public @NotNull MqttClientState getState() {
        return delegate.getState();
    }

    @Override
    public @NotNull Optional<Mqtt3ClientConnectionConfig> getConnectionConfig() {
        return Optional.ofNullable(delegate.getRawConnectionConfig());
    }
}
