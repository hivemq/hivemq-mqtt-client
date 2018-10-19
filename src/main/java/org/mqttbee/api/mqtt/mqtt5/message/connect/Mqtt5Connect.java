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

package org.mqttbee.api.mqtt.mqtt5.message.connect;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.api.mqtt.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5Message;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.util.UnsignedDataTypes;

import java.util.Optional;
import java.util.function.Function;

/**
 * MQTT 5 CONNECT packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5Connect extends Mqtt5Message {

    @NotNull Mqtt5Connect DEFAULT = builder().build();
    int NO_KEEP_ALIVE = 0;
    int DEFAULT_KEEP_ALIVE = 60;
    boolean DEFAULT_CLEAN_START = true;
    long DEFAULT_SESSION_EXPIRY_INTERVAL = 0;
    long NO_SESSION_EXPIRY = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE;
    boolean DEFAULT_RESPONSE_INFORMATION_REQUESTED = false;
    boolean DEFAULT_PROBLEM_INFORMATION_REQUESTED = true;

    static @NotNull Mqtt5ConnectBuilder<Void> builder() {
        return new Mqtt5ConnectBuilder<>((Function<Mqtt5Connect, Void>) null);
    }

    static @NotNull Mqtt5ConnectBuilder<Void> extend(final @NotNull Mqtt5Connect connect) {
        return new Mqtt5ConnectBuilder<>(connect);
    }

    /**
     * @return the keep alive in seconds the client wants to use.
     */
    int getKeepAlive();

    /**
     * @return whether the client has no session present or wants to clear a present session.
     */
    boolean isCleanStart();

    /**
     * @return the session expiry interval in seconds the client wants to use. The default is {@link
     *         #DEFAULT_SESSION_EXPIRY_INTERVAL}. If it is {@link #NO_SESSION_EXPIRY} the session does not expire.
     */
    long getSessionExpiryInterval();

    /**
     * @return whether the client requests response information from the server. The default is {@link
     *         #DEFAULT_RESPONSE_INFORMATION_REQUESTED}.
     */
    boolean isResponseInformationRequested();

    /**
     * @return whether the client requests problem information from the server. The default is {@link
     *         #DEFAULT_PROBLEM_INFORMATION_REQUESTED}.
     */
    boolean isProblemInformationRequested();

    /**
     * @return the restrictions set from the client.
     */
    @NotNull Mqtt5ConnectRestrictions getRestrictions();

    /**
     * @return the optional simple authentication and/or authorization related data of this CONNECT packet.
     */
    @NotNull Optional<Mqtt5SimpleAuth> getSimpleAuth();

    /**
     * @return the optional enhanced authentication and/or authorization provider of this CONNECT packet.
     */
    @NotNull Optional<Mqtt5EnhancedAuthProvider> getEnhancedAuthProvider();

    /**
     * @return the optional Will Publish of this CONNECT packet.
     */
    @NotNull Optional<Mqtt5WillPublish> getWillPublish();

    /**
     * @return the optional user properties of this CONNECT packet.
     */
    @NotNull Mqtt5UserProperties getUserProperties();

    @Override
    default @NotNull Mqtt5MessageType getType() {
        return Mqtt5MessageType.CONNECT;
    }

}
