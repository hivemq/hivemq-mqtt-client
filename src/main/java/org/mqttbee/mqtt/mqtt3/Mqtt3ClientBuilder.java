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

package org.mqttbee.mqtt.mqtt3;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.mqtt.MqttClientBuilderBase;

/**
 * Builder for a {@link Mqtt3Client}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt3ClientBuilder extends MqttClientBuilderBase<Mqtt3ClientBuilder> {

    /**
     * Builds the {@link Mqtt3Client}.
     *
     * @return the built {@link Mqtt3Client}.
     */
    @NotNull Mqtt3Client build();

    /**
     * Builds the {@link Mqtt3RxClient}.
     *
     * @return the built {@link Mqtt3RxClient}.
     */
    @NotNull Mqtt3RxClient buildRx();

    /**
     * Builds the {@link Mqtt3AsyncClient}.
     *
     * @return the built {@link Mqtt3AsyncClient}.
     */
    @NotNull Mqtt3AsyncClient buildAsync();

    /**
     * Builds the {@link Mqtt3BlockingClient}.
     *
     * @return the built {@link Mqtt3BlockingClient}.
     */
    @NotNull Mqtt3BlockingClient buildBlocking();
}
