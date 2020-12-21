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

package com.hivemq.client2.mqtt.mqtt5;

import com.hivemq.client2.mqtt.MqttClientConfig;
import com.hivemq.client2.mqtt.mqtt5.advanced.Mqtt5AdvancedConfig;
import com.hivemq.client2.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism;
import com.hivemq.client2.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Configuration of an {@link Mqtt5Client}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5ClientConfig extends MqttClientConfig {

    @Override
    @NotNull Optional<Mqtt5ClientConnectionConfig> getConnectionConfig();

    /**
     * @return the advanced configuration of the client.
     */
    @NotNull Mqtt5AdvancedConfig getAdvancedConfig();

    /**
     * Returns the optional simple authentication and/or authorization related data of the client.
     * <p>
     * It is used as default if {@link com.hivemq.client2.mqtt.mqtt5.message.connect.Mqtt5Connect#getSimpleAuth()
     * Mqtt5Connect.simpleAuth} is not set during connect.
     * <p>
     * Keep in mind that the data is stored with the client in memory. If you want to use it only during connect or if
     * you use a different token for each connection please set {@link com.hivemq.client2.mqtt.mqtt5.message.connect.Mqtt5Connect#getSimpleAuth()
     * Mqtt5Connect.simpleAuth} instead.
     *
     * @return the optional simple authentication and/or authorization related data of the client.
     * @since 1.1
     */
    @NotNull Optional<Mqtt5SimpleAuth> getSimpleAuth();

    /**
     * Returns the optional enhanced authentication and/or authorization mechanism of the client.
     * <p>
     * It is used as default if {@link com.hivemq.client2.mqtt.mqtt5.message.connect.Mqtt5Connect#getEnhancedAuthMechanism()
     * Mqtt5Connect.enhancedAuthMechanism} is not set during connect.
     * <p>
     * Keep in mind that the mechanism is stored with the client in memory. If you use a different mechanism for each
     * connection please set {@link com.hivemq.client2.mqtt.mqtt5.message.connect.Mqtt5Connect#getEnhancedAuthMechanism()
     * Mqtt5Connect.enhancedAuthMechanism} instead.
     *
     * @return the optional enhanced authentication and/or authorization mechanism of the client.
     * @since 1.1
     */
    @NotNull Optional<Mqtt5EnhancedAuthMechanism> getEnhancedAuthMechanism();

    /**
     * Returns the optional Will Publish of the client.
     * <p>
     * It is used as default if {@link com.hivemq.client2.mqtt.mqtt5.message.connect.Mqtt5Connect#getWillPublish()
     * Mqtt5Connect.willPublish} is not set during connect.
     * <p>
     * Keep in mind that the Will Publish is stored with the client in memory. If you use a different Will Publish for
     * each connection please set {@link com.hivemq.client2.mqtt.mqtt5.message.connect.Mqtt5Connect#getWillPublish()
     * Mqtt5Connect.willPublish} instead.
     *
     * @return the optional Will Publish of the client.
     * @since 1.1
     */
    @NotNull Optional<Mqtt5WillPublish> getWillPublish();
}
