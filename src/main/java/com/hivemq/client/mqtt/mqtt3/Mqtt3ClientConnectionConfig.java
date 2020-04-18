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

package com.hivemq.client.mqtt.mqtt3;

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.mqtt.MqttClientConnectionConfig;
import org.jetbrains.annotations.NotNull;

/**
 * Connection configuration of a {@link Mqtt3Client}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt3ClientConnectionConfig extends MqttClientConnectionConfig {

    /**
     * @return the restrictions for messages the client sends.
     */
    @NotNull RestrictionsForClient getRestrictionsForClient();

    /**
     * Restrictions for messages a {@link Mqtt3Client} sends.
     */
    @DoNotImplement
    interface RestrictionsForClient {

        /**
         * Returns the maximum amount of not acknowledged publishes with QoS 1 or 2 the client sends to the server
         * concurrently.
         *
         * @return the maximum amount of not acknowledged publishes with QoS 1 or 2 the client sends to the server
         *         concurrently.
         */
        int getSendMaximum();
    }
}
