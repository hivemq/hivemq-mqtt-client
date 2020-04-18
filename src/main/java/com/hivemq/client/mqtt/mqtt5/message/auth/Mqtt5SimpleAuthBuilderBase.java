/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.mqtt.mqtt5.message.auth;

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Builder base for a {@link Mqtt5SimpleAuth}.
 *
 * @param <C> the type of the complete builder.
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt5SimpleAuthBuilderBase<C extends Mqtt5SimpleAuthBuilderBase<C>> {

    /**
     * Sets the {@link Mqtt5SimpleAuth#getUsername() username}. At least the username or the password is mandatory.
     *
     * @param username the username string.
     * @return the builder that is now complete as the username is set.
     */
    @CheckReturnValue
    @NotNull C username(@NotNull String username);

    /**
     * Sets the {@link Mqtt5SimpleAuth#getUsername() username}. At least the username or the password is mandatory.
     *
     * @param username the username string.
     * @return the builder that is now complete as the username is set.
     */
    @CheckReturnValue
    @NotNull C username(@NotNull MqttUtf8String username);

    /**
     * Sets the {@link Mqtt5SimpleAuth#getPassword() password}. At least the username or the password is mandatory.
     *
     * @param password the password as byte array.
     * @return the builder that is now complete as the password is set.
     */
    @CheckReturnValue
    @NotNull C password(byte @NotNull [] password);

    /**
     * Sets the {@link Mqtt5SimpleAuth#getPassword() password}. At least the username or the password is mandatory.
     *
     * @param password the password as {@link ByteBuffer}.
     * @return the builder that is now complete as the password is set.
     */
    @CheckReturnValue
    @NotNull C password(@NotNull ByteBuffer password);
}
