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

package com.hivemq.client2.mqtt.lifecycle;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Listener which is notified when a client is connected (a successful ConnAck message is received).
 *
 * @author Silvio Giebl
 * @since 1.1
 */
@ApiStatus.OverrideOnly
@FunctionalInterface
public interface MqttConnectedListener {

    /**
     * Listener method which is notified when a client is connected (a successful ConnAck message is received).
     * <p>
     * This method must not block.
     *
     * @param context provides context about the client that is now connected.
     */
    void onConnected(@NotNull MqttConnectedContext context);
}
