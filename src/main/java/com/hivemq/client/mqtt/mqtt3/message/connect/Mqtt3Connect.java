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

package com.hivemq.client.mqtt.mqtt3.message.connect;

import com.hivemq.client.internal.mqtt.message.connect.mqtt3.Mqtt3ConnectViewBuilder;
import com.hivemq.client.mqtt.mqtt3.message.Mqtt3Message;
import com.hivemq.client.mqtt.mqtt3.message.Mqtt3MessageType;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * MQTT 3 Connect message. This message is translated from and to an MQTT 3 CONNECT packet.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt3Connect extends Mqtt3Message {

    /**
     * The value that disables keep alive.
     */
    int NO_KEEP_ALIVE = 0;
    /**
     * The default keep alive in seconds a client wants to use.
     */
    int DEFAULT_KEEP_ALIVE = 60;
    /**
     * The default whether a client wants a clean session.
     */
    boolean DEFAULT_CLEAN_SESSION = true;

    /**
     * Creates a builder for a Connect message.
     *
     * @return the created builder.
     */
    static @NotNull Mqtt3ConnectBuilder builder() {
        return new Mqtt3ConnectViewBuilder.Default();
    }

    /**
     * @return the keep alive in seconds the client wants to use.
     */
    int getKeepAlive();

    /**
     * @return whether the client wants a clean session. If <code>true</code> an existing session is cleared.
     */
    boolean isCleanSession();

    /**
     * @return the optional simple authentication and/or authorization related data of this Connect message.
     */
    @NotNull Optional<Mqtt3SimpleAuth> getSimpleAuth();

    /**
     * @return the optional Will Publish of this Connect message.
     */
    @NotNull Optional<Mqtt3Publish> getWillPublish();

    @Override
    default @NotNull Mqtt3MessageType getType() {
        return Mqtt3MessageType.CONNECT;
    }

    /**
     * Creates a builder for extending this Connect message.
     *
     * @return the created builder.
     */
    @NotNull Mqtt3ConnectBuilder extend();
}
