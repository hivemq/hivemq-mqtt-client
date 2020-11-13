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

package com.hivemq.client.mqtt.mqtt3.lifecycle;

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectedContext;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientConfig;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link MqttDisconnectedContext} with methods specific to an {@link com.hivemq.client.mqtt.mqtt3.Mqtt3Client
 * Mqtt3Client}.
 *
 * @author Silvio Giebl
 * @since 1.1
 */
@DoNotImplement
public interface Mqtt3DisconnectedContext extends MqttDisconnectedContext {

    @Override
    @NotNull Mqtt3ClientConfig getClientConfig();

    @Override
    @NotNull Mqtt3Reconnector getReconnector();
}
