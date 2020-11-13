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

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.internal.mqtt.mqtt3.Mqtt3RxClientViewBuilder;
import com.hivemq.client.mqtt.MqttClient;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * MQTT client according to the MQTT 3.1.1 specification.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt3Client extends MqttClient {

    /**
     * Creates a builder for an MQTT 3 client.
     *
     * @return the created builder for a MQTT 3 client.
     */
    static @NotNull Mqtt3ClientBuilder builder() {
        return new Mqtt3RxClientViewBuilder();
    }

    @Override
    @NotNull Mqtt3ClientConfig getConfig();

    /**
     * Turns the API of this client into a reactive API.
     * <p>
     * The reactive API can be used simultaneously with the other APIs.
     *
     * @return a reactive API for this client.
     */
    @CheckReturnValue
    @NotNull Mqtt3RxClient toRx();

    /**
     * Turns the API of this client into a asynchronous API based on futures and callbacks.
     * <p>
     * The asynchronous API can be used simultaneously with the other APIs.
     *
     * @return a asynchronous API for this client.
     */
    @CheckReturnValue
    @NotNull Mqtt3AsyncClient toAsync();

    /**
     * Turns the API of this client into a blocking API.
     * <p>
     * The blocking API can be used simultaneously with the other APIs.
     *
     * @return a blocking API for this client.
     */
    @CheckReturnValue
    @NotNull Mqtt3BlockingClient toBlocking();
}
