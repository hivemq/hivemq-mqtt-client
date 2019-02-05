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

package org.mqttbee.mqtt.mqtt5;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.mqtt.MqttClientConfig;
import org.mqttbee.mqtt.mqtt5.advanced.Mqtt5ClientAdvancedConfig;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5ClientConfig extends MqttClientConfig {

    @Override
    @NotNull Optional<Mqtt5ClientConnectionConfig> getConnectionConfig();

    @NotNull Mqtt5ClientAdvancedConfig getAdvancedConfig();
}
