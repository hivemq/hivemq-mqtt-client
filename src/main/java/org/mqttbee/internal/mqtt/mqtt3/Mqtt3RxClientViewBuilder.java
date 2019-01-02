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

package org.mqttbee.internal.mqtt.mqtt3;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3ClientBuilder;
import org.mqttbee.internal.mqtt.*;

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

    public @NotNull Mqtt3RxClientView build() {
        return buildRx();
    }

    public @NotNull Mqtt3RxClientView buildRx() {
        return new Mqtt3RxClientView(buildRxDelegate());
    }

    public @NotNull Mqtt3AsyncClientView buildAsync() {
        return new Mqtt3AsyncClientView(buildAsyncDelegate());
    }

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
        return new MqttClientConfig(MqttVersion.MQTT_3_1_1, identifier, serverHost, serverPort, sslConfig,
                webSocketConfig, false, false, executorConfig, null);
    }
}
