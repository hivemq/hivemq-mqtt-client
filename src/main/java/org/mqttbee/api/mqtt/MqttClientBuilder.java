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

package org.mqttbee.api.mqtt;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3Client;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3ClientBuilder;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5Client;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ClientBuilder;

/**
 * @author Silvio Giebl
 */
public class MqttClientBuilder extends AbstractMqttClientBuilder<MqttClientBuilder> {

    MqttClientBuilder() {}

    @Override
    protected @NotNull MqttClientBuilder self() {
        return this;
    }

    public @NotNull Mqtt3ClientBuilder useMqttVersion3() {
        return ((AbstractMqttClientBuilder<Mqtt3ClientBuilder>) Mqtt3Client.builder()).init(this);
    }

    public @NotNull Mqtt5ClientBuilder useMqttVersion5() {
        return ((AbstractMqttClientBuilder<Mqtt5ClientBuilder>) Mqtt5Client.builder()).init(this);
    }
}
