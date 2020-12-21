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

package com.hivemq.client2.internal.mqtt.mqtt3;

import com.hivemq.client2.internal.mqtt.*;
import com.hivemq.client2.internal.mqtt.advanced.MqttAdvancedConfig;
import com.hivemq.client2.internal.mqtt.message.auth.MqttSimpleAuth;
import com.hivemq.client2.internal.mqtt.message.auth.mqtt3.Mqtt3SimpleAuthView;
import com.hivemq.client2.internal.mqtt.message.auth.mqtt3.Mqtt3SimpleAuthViewBuilder;
import com.hivemq.client2.internal.mqtt.message.publish.MqttWillPublish;
import com.hivemq.client2.internal.mqtt.message.publish.mqtt3.Mqtt3PublishView;
import com.hivemq.client2.internal.mqtt.message.publish.mqtt3.Mqtt3PublishViewBuilder;
import com.hivemq.client2.internal.util.Checks;
import com.hivemq.client2.mqtt.MqttVersion;
import com.hivemq.client2.mqtt.mqtt3.Mqtt3ClientBuilder;
import com.hivemq.client2.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.client2.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public class Mqtt3RxClientViewBuilder extends MqttRxClientBuilderBase<Mqtt3RxClientViewBuilder>
        implements Mqtt3ClientBuilder {

    private @Nullable MqttSimpleAuth simpleAuth;
    private @Nullable MqttWillPublish willPublish;

    public Mqtt3RxClientViewBuilder() {}

    public Mqtt3RxClientViewBuilder(final @NotNull MqttRxClientBuilderBase<?> clientBuilder) {
        super(clientBuilder);
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

    private @NotNull MqttClientConfig buildClientConfig() {
        return buildClientConfig(MqttVersion.MQTT_3_1_1, MqttAdvancedConfig.DEFAULT,
                MqttClientConfig.ConnectDefaults.of(simpleAuth, null, willPublish));
    }
}
