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

package com.hivemq.client.internal.mqtt.mqtt3;

import com.hivemq.client.internal.mqtt.*;
import com.hivemq.client.internal.mqtt.advanced.MqttClientAdvancedConfig;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt3RxClientViewBuilder extends MqttRxClientBuilderBase<Mqtt3RxClientViewBuilder>
        implements Mqtt3ClientBuilder {

    public Mqtt3RxClientViewBuilder() {}

    public Mqtt3RxClientViewBuilder(final @NotNull MqttRxClientBuilderBase clientBuilder) {
        super(clientBuilder);
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
        return new MqttClientConfig(MqttVersion.MQTT_3_1_1, identifier, getServerAddress(), executorConfig,
                sslConfig, webSocketConfig, MqttClientAdvancedConfig.DEFAULT);
    }
}
