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

import com.hivemq.client.mqtt.lifecycle.MqttConnectedContext;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientConfig;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link MqttConnectedContext} with methods specific to an {@link com.hivemq.client.mqtt.mqtt3.Mqtt3Client
 * Mqtt3Client}.
 *
 * @author Silvio Giebl
 * @since 1.1
 */
@ApiStatus.NonExtendable
public interface Mqtt3ConnectedContext extends MqttConnectedContext {

    @Override
    @NotNull Mqtt3ClientConfig getClientConfig();

    /**
     * @return the Connect message that started the connection.
     */
    @NotNull Mqtt3Connect getConnect();

    /**
     * @return the ConnAck message that acknowledged the connection.
     */
    @NotNull Mqtt3ConnAck getConnAck();
}
