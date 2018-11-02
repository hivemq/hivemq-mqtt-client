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

package org.mqttbee.api.mqtt.mqtt3.message.connect;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3Message;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.api.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.mqtt.message.connect.mqtt3.Mqtt3ConnectBuilderImpl;

import java.util.Optional;

/**
 * MQTT 3 CONNECT packet.
 */
@DoNotImplement
public interface Mqtt3Connect extends Mqtt3Message {

    @NotNull Mqtt3Connect DEFAULT = builder().build();
    int NO_KEEP_ALIVE = 0;
    int DEFAULT_KEEP_ALIVE = 60;
    boolean DEFAULT_CLEAN_SESSION = true;

    static @NotNull Mqtt3ConnectBuilder builder() {
        return new Mqtt3ConnectBuilderImpl.Impl();
    }

    static @NotNull Mqtt3ConnectBuilder extend(final @NotNull Mqtt3Connect connect) {
        return new Mqtt3ConnectBuilderImpl.Impl(connect);
    }

    /**
     * @return the keep alive in seconds the client wants to use.
     */
    int getKeepAlive();

    /**
     * @return whether the client wants a clean session which lasts for the lifetime of the session. A present session
     *         is cleared.
     */
    boolean isCleanSession();

    /**
     * @return the optional simple authentication and/or authorization related data of this CONNECT packet.
     */
    @NotNull Optional<Mqtt3SimpleAuth> getSimpleAuth();

    /**
     * @return the optional Will Publish of this CONNECT packet.
     */
    @NotNull Optional<Mqtt3Publish> getWillPublish();

    @Override
    default @NotNull Mqtt3MessageType getType() {
        return Mqtt3MessageType.CONNECT;
    }

}
