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

package org.mqttbee.mqtt.mqtt5.message.auth;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.DoNotImplement;

import java.nio.ByteBuffer;

/**
 * Builder for a {@link Mqtt5EnhancedAuth}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt5EnhancedAuthBuilder {

    /**
     * Sets the optional auth data.
     *
     * @param data the auth data as byte array or <code>null</null> to cancel any previously set auth data.
     * @return the builder.
     */
    @NotNull Mqtt5EnhancedAuthBuilder data(@Nullable byte[] data);

    /**
     * Sets the optional auth data.
     *
     * @param data the auth data as {@link ByteBuffer} or <code>null</null> to cancel any previously set auth data.
     * @return the builder.
     */
    @NotNull Mqtt5EnhancedAuthBuilder data(@Nullable ByteBuffer data);
}
