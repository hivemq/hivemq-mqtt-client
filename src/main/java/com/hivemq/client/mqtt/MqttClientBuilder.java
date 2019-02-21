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

package com.hivemq.client.mqtt;

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for a {@link MqttClient}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface MqttClientBuilder extends MqttClientBuilderBase<MqttClientBuilder> {

    /**
     * Uses {@link MqttVersion#MQTT_3_1_1 MQTT version 3}.
     *
     * @return the builder for the {@link com.hivemq.client.mqtt.mqtt3.Mqtt3Client Mqtt3Client}.
     */
    @NotNull Mqtt3ClientBuilder useMqttVersion3();

    /**
     * Uses {@link MqttVersion#MQTT_5_0 MQTT version 5}.
     *
     * @return the builder for the {@link com.hivemq.client.mqtt.mqtt5.Mqtt5Client Mqtt5Client}.
     */
    @NotNull Mqtt5ClientBuilder useMqttVersion5();
}
