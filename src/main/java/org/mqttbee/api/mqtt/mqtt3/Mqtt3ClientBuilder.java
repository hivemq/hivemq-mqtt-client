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

package org.mqttbee.api.mqtt.mqtt3;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.AbstractMqttClientBuilder;
import org.mqttbee.mqtt.*;
import org.mqttbee.mqtt.mqtt3.Mqtt3AsyncClientView;
import org.mqttbee.mqtt.mqtt3.Mqtt3BlockingClientView;
import org.mqttbee.mqtt.mqtt3.Mqtt3RxClientView;

/**
 * @author Silvio Giebl
 */
public class Mqtt3ClientBuilder extends AbstractMqttClientBuilder<Mqtt3ClientBuilder> {

    Mqtt3ClientBuilder() {}

    @Override
    protected @NotNull Mqtt3ClientBuilder self() {
        return this;
    }

    public @NotNull Mqtt3Client build() {
        return buildRx();
    }

    public @NotNull Mqtt3RxClient buildRx() {
        return new Mqtt3RxClientView(buildRxDelegate());
    }

    public @NotNull Mqtt3AsyncClient buildAsync() {
        return new Mqtt3AsyncClientView(buildAsyncDelegate());
    }

    public @NotNull Mqtt3BlockingClient buildBlocking() {
        return new Mqtt3BlockingClientView(buildBlockingDelegate());
    }

    private @NotNull MqttRxClient buildRxDelegate() {
        return new MqttRxClient(buildClientData());
    }

    private @NotNull MqttAsyncClient buildAsyncDelegate() {
        return buildRxDelegate().toAsync();
    }

    private @NotNull MqttBlockingClient buildBlockingDelegate() {
        return buildRxDelegate().toBlocking();
    }

    private @NotNull MqttClientData buildClientData() {
        return new MqttClientData(MqttVersion.MQTT_3_1_1, identifier, serverHost, serverPort, sslConfig,
                webSocketConfig, false, false, executorConfig, null);
    }
}
