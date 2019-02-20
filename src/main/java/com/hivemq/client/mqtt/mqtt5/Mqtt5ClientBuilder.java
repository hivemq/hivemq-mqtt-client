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

package com.hivemq.client.mqtt.mqtt5;

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.mqtt.MqttClientBuilderBase;
import com.hivemq.client.mqtt.mqtt5.advanced.Mqtt5ClientAdvancedConfig;
import com.hivemq.client.mqtt.mqtt5.advanced.Mqtt5ClientAdvancedConfigBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for a {@link Mqtt5Client}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt5ClientBuilder extends MqttClientBuilderBase<Mqtt5ClientBuilder> {

    /**
     * Sets the {@link Mqtt5ClientConfig#getAdvancedConfig() advanced configuration}.
     *
     * @param advancedConfig the advanced configuration.
     * @return the builder.
     */
    @NotNull Mqtt5ClientBuilder advancedConfig(@NotNull Mqtt5ClientAdvancedConfig advancedConfig);

    /**
     * Fluent counterpart of {@link #advancedConfig(Mqtt5ClientAdvancedConfig)}.
     * <p>
     * Calling {@link Mqtt5ClientAdvancedConfigBuilder.Nested#applyAdvancedConfig()} on the returned builder has the
     * effect of extending the current advanced configuration.
     *
     * @return the fluent builder for the advanced configuration.
     * @see #advancedConfig(Mqtt5ClientAdvancedConfig)
     */
    @NotNull Mqtt5ClientAdvancedConfigBuilder.Nested<? extends Mqtt5ClientBuilder> advancedConfig();

    /**
     * ø Builds the {@link Mqtt5Client}.
     *
     * @return the built {@link Mqtt5Client}.
     */
    @NotNull Mqtt5Client build();

    /**
     * Builds the {@link Mqtt5RxClient}.
     *
     * @return the built {@link Mqtt5RxClient}.
     */
    @NotNull Mqtt5RxClient buildRx();

    /**
     * Builds the {@link Mqtt5AsyncClient}.
     *
     * @return the built {@link Mqtt5AsyncClient}.
     */
    @NotNull Mqtt5AsyncClient buildAsync();

    /**
     * Builds the {@link Mqtt5BlockingClient}.
     *
     * @return the built {@link Mqtt5BlockingClient}.
     */
    @NotNull Mqtt5BlockingClient buildBlocking();
}
