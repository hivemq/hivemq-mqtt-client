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

package com.hivemq.client2.mqtt.mqtt5.message.auth;

import com.hivemq.client2.internal.mqtt.message.auth.MqttSimpleAuthBuilder;
import com.hivemq.client2.mqtt.datatypes.MqttUtf8String;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * Simple authentication and/or authorization related data in an {@link com.hivemq.client2.mqtt.mqtt5.message.connect.Mqtt5Connect
 * MQTT 5 Connect message}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5SimpleAuth {

    /**
     * Creates a builder for simple authentication and/or authorization related data.
     *
     * @return the created builder.
     */
    static @NotNull Mqtt5SimpleAuthBuilder builder() {
        return new MqttSimpleAuthBuilder.Default();
    }

    /**
     * @return the optional username.
     */
    @NotNull Optional<MqttUtf8String> getUsername();

    /**
     * @return the optional password.
     */
    @NotNull Optional<ByteBuffer> getPassword();
}
