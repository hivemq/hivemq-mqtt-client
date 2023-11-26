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

package com.hivemq.mqtt.client2.internal.mqtt3;

import com.hivemq.mqtt.client2.MqttVersion;
import com.hivemq.mqtt.client2.internal.*;
import com.hivemq.mqtt.client2.internal.advanced.MqttAdvancedConfig;
import com.hivemq.mqtt.client2.internal.collections.ImmutableList;
import com.hivemq.mqtt.client2.internal.lifecycle.MqttConnectedContextImpl;
import com.hivemq.mqtt.client2.internal.lifecycle.MqttDisconnectedContextImpl;
import com.hivemq.mqtt.client2.internal.lifecycle.mqtt3.Mqtt3ConnectedContextView;
import com.hivemq.mqtt.client2.internal.lifecycle.mqtt3.Mqtt3DisconnectedContextView;
import com.hivemq.mqtt.client2.internal.message.auth.MqttSimpleAuth;
import com.hivemq.mqtt.client2.internal.message.auth.mqtt3.Mqtt3SimpleAuthView;
import com.hivemq.mqtt.client2.internal.message.auth.mqtt3.Mqtt3SimpleAuthViewBuilder;
import com.hivemq.mqtt.client2.internal.message.publish.MqttWillPublish;
import com.hivemq.mqtt.client2.internal.message.publish.mqtt3.Mqtt3PublishView;
import com.hivemq.mqtt.client2.internal.message.publish.mqtt3.Mqtt3PublishViewBuilder;
import com.hivemq.mqtt.client2.internal.util.Checks;
import com.hivemq.mqtt.client2.lifecycle.MqttConnectedContext;
import com.hivemq.mqtt.client2.lifecycle.MqttConnectedListener;
import com.hivemq.mqtt.client2.lifecycle.MqttDisconnectedContext;
import com.hivemq.mqtt.client2.lifecycle.MqttDisconnectedListener;
import com.hivemq.mqtt.client2.mqtt3.Mqtt3ClientBuilder;
import com.hivemq.mqtt.client2.mqtt3.lifecycle.Mqtt3ConnectedContext;
import com.hivemq.mqtt.client2.mqtt3.lifecycle.Mqtt3DisconnectedContext;
import com.hivemq.mqtt.client2.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.mqtt.client2.mqtt3.message.publish.Mqtt3Publish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public class Mqtt3RxClientViewBuilder extends MqttRxClientBuilderBase<Mqtt3RxClientViewBuilder>
        implements Mqtt3ClientBuilder {

    private ImmutableList.@Nullable Builder<MqttConnectedListener<? super MqttConnectedContextImpl>>
            connectedListenersBuilder;
    private ImmutableList.@Nullable Builder<MqttDisconnectedListener<? super MqttDisconnectedContextImpl>>
            disconnectedListenersBuilder;
    private @Nullable MqttSimpleAuth simpleAuth;
    private @Nullable MqttWillPublish willPublish;

    public Mqtt3RxClientViewBuilder() {}

    public Mqtt3RxClientViewBuilder(
            final @NotNull MqttRxClientBuilderBase<?> clientBuilder,
            final @NotNull ImmutableList<MqttConnectedListener<? super MqttConnectedContext>> connectedListeners,
            final @NotNull ImmutableList<MqttDisconnectedListener<? super MqttDisconnectedContext>> disconnectedListeners) {
        super(clientBuilder);
        if (!connectedListeners.isEmpty()) {
            connectedListenersBuilder = ImmutableList.builder(connectedListeners.size());
            for (final MqttConnectedListener<? super MqttConnectedContext> connectedListener : connectedListeners) {
                connectedListenersBuilder.add(wrapConnectedListener(connectedListener));
            }
        }
        if (!disconnectedListeners.isEmpty()) {
            disconnectedListenersBuilder = ImmutableList.builder(disconnectedListeners.size());
            for (final MqttDisconnectedListener<? super MqttDisconnectedContext> disconnectedListener : disconnectedListeners) {
                disconnectedListenersBuilder.add(wrapDisconnectedListener(disconnectedListener));
            }
        }
    }

    private @NotNull MqttConnectedListener<MqttConnectedContextImpl> wrapConnectedListener(
            final @NotNull MqttConnectedListener<? super Mqtt3ConnectedContextView> delegate) {
        return context -> delegate.onConnected(new Mqtt3ConnectedContextView(context));
    }

    @Override
    public @NotNull Mqtt3RxClientViewBuilder addConnectedListener(
            final @Nullable MqttConnectedListener<? super Mqtt3ConnectedContext> connectedListener) {
        Checks.notNull(connectedListener, "Connected listener");
        if (connectedListenersBuilder == null) {
            connectedListenersBuilder = ImmutableList.builder();
        }
        connectedListenersBuilder.add(wrapConnectedListener(connectedListener));
        return this;
    }

    private @NotNull MqttDisconnectedListener<MqttDisconnectedContextImpl> wrapDisconnectedListener(
            final @NotNull MqttDisconnectedListener<? super Mqtt3DisconnectedContextView> delegate) {
        return context -> delegate.onDisconnected(new Mqtt3DisconnectedContextView(context));
    }

    @Override
    public @NotNull Mqtt3RxClientViewBuilder addDisconnectedListener(
            final @Nullable MqttDisconnectedListener<? super Mqtt3DisconnectedContext> disconnectedListener) {
        Checks.notNull(disconnectedListener, "Disconnected listener");
        if (disconnectedListenersBuilder == null) {
            disconnectedListenersBuilder = ImmutableList.builder();
        }
        disconnectedListenersBuilder.add(wrapDisconnectedListener(disconnectedListener));
        return this;
    }

    @Override
    public @NotNull Mqtt3RxClientViewBuilder simpleAuth(final @Nullable Mqtt3SimpleAuth simpleAuth) {
        this.simpleAuth = (simpleAuth == null) ? null :
                Checks.notImplemented(simpleAuth, Mqtt3SimpleAuthView.class, "Simple auth").getDelegate();
        return this;
    }

    @Override
    public Mqtt3SimpleAuthViewBuilder.@NotNull Nested<Mqtt3RxClientViewBuilder> simpleAuthWith() {
        return new Mqtt3SimpleAuthViewBuilder.Nested<>(this::simpleAuth);
    }

    @Override
    public @NotNull Mqtt3RxClientViewBuilder willPublish(final @Nullable Mqtt3Publish willPublish) {
        this.willPublish = (willPublish == null) ? null :
                Checks.notImplemented(willPublish, Mqtt3PublishView.class, "Will publish").getDelegate().asWill();
        return this;
    }

    @Override
    public Mqtt3PublishViewBuilder.@NotNull WillNested<Mqtt3RxClientViewBuilder> willPublishWith() {
        return new Mqtt3PublishViewBuilder.WillNested<>(this::willPublish);
    }

    @Override
    protected @NotNull Mqtt3RxClientViewBuilder self() {
        return this;
    }

    @Override
    public @NotNull Mqtt3RxClientView build() {
        return buildRx();
    }

    @Override
    public @NotNull Mqtt3RxClientView buildRx() {
        return new Mqtt3RxClientView(buildRxDelegate());
    }

    @Override
    public @NotNull Mqtt3AsyncClientView buildAsync() {
        return new Mqtt3AsyncClientView(buildAsyncDelegate());
    }

    @Override
    public @NotNull Mqtt3BlockingClientView buildBlocking() {
        return new Mqtt3BlockingClientView(buildBlockingDelegate());
    }

    private @NotNull MqttRxClient buildRxDelegate() {
        return new MqttRxClient(buildClientConfig());
    }

    private @NotNull MqttAsyncClient buildAsyncDelegate() {
        return buildRxDelegate().toAsync();
    }

    private @NotNull MqttBlockingClient buildBlockingDelegate() {
        return buildRxDelegate().toBlocking();
    }

    private @NotNull ImmutableList<MqttConnectedListener<? super MqttConnectedContextImpl>> buildConnectedListeners() {
        return connectedListenersBuilder == null ? ImmutableList.of() : connectedListenersBuilder.build();
    }

    private @NotNull ImmutableList<MqttDisconnectedListener<? super MqttDisconnectedContextImpl>> buildDisconnectedListeners() {
        if (disconnectedListenersBuilder == null) {
            if (autoReconnect == null) {
                return ImmutableList.of();
            }
            return ImmutableList.of(autoReconnect);
        }
        if (autoReconnect == null) {
            return disconnectedListenersBuilder.build();
        }
        return ImmutableList.<MqttDisconnectedListener<? super MqttDisconnectedContextImpl>>builder(
                        disconnectedListenersBuilder.getSize() + 1)
                .add(autoReconnect)
                .addAll(disconnectedListenersBuilder.build())
                .build();
    }

    private @NotNull MqttClientConfig buildClientConfig() {
        return buildClientConfig(MqttVersion.MQTT_3_1_1, MqttAdvancedConfig.DEFAULT,
                MqttClientConfig.ConnectDefaults.of(simpleAuth, null, willPublish), buildConnectedListeners(),
                buildDisconnectedListeners());
    }
}
