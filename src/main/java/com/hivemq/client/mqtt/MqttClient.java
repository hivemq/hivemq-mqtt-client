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

package com.hivemq.client.mqtt;

import com.hivemq.client.internal.mqtt.MqttRxClientBuilderBase;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Common interface for MQTT clients.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface MqttClient {

    /**
     * Creates a builder for an MQTT client.
     *
     * @return the created builder for an MQTT client.
     */
    static @NotNull MqttClientBuilder builder() {
        return new MqttRxClientBuilderBase.Choose();
    }

    /**
     * @return the configuration of this client.
     */
    @NotNull MqttClientConfig getConfig();

    /**
     * @return the state of this client.
     * @since 1.1
     */
    default @NotNull MqttClientState getState() {
        return getConfig().getState();
    }
}
