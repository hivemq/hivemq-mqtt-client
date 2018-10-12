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

package org.mqttbee.api.mqtt.mqtt5;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttClient;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5Client extends MqttClient {

    @Override
    @NotNull Mqtt5ClientData getClientData();

    @NotNull Mqtt5RxClient toRx();

    @NotNull Mqtt5AsyncClient toAsync();

    @NotNull Mqtt5BlockingClient toBlocking();

}
