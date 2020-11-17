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

package com.hivemq.client.mqtt.mqtt5.message.connect;

import com.hivemq.client.internal.mqtt.message.connect.MqttConnectBuilder;
import com.hivemq.client.internal.util.UnsignedDataTypes;
import com.hivemq.client.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5Message;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5MessageType;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Optional;

/**
 * MQTT 5 Connect message. This message is translated from and to an MQTT 5 CONNECT packet.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5Connect extends Mqtt5Message {

    /**
     * The value that disables keep alive.
     */
    int NO_KEEP_ALIVE = 0;
    /**
     * The default keep alive in seconds a client wants to use.
     */
    int DEFAULT_KEEP_ALIVE = 60;
    /**
     * The default whether a client wants to start a clean session.
     */
    boolean DEFAULT_CLEAN_START = true;
    /**
     * The default session expiry interval in seconds a client wants to use.
     */
    long DEFAULT_SESSION_EXPIRY_INTERVAL = 0;
    /**
     * The value that disables session expiry.
     */
    long NO_SESSION_EXPIRY = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE;

    /**
     * Creates a builder for a Connect message.
     *
     * @return the created builder.
     */
    static @NotNull Mqtt5ConnectBuilder builder() {
        return new MqttConnectBuilder.Default();
    }

    /**
     * @return the keep alive in seconds the client wants to use.
     */
    @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int getKeepAlive();

    /**
     * @return whether the client has no session present or wants to clear a present session.
     */
    boolean isCleanStart();

    /**
     * @return the session expiry interval in seconds the client wants to use. The default is {@link
     *         #DEFAULT_SESSION_EXPIRY_INTERVAL}. If it is {@link #NO_SESSION_EXPIRY} the session does not expire.
     */
    @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE) long getSessionExpiryInterval();

    /**
     * @return the restrictions set from the client.
     */
    @NotNull Mqtt5ConnectRestrictions getRestrictions();

    /**
     * @return the optional simple authentication and/or authorization related data of this Connect message.
     */
    @NotNull Optional<Mqtt5SimpleAuth> getSimpleAuth();

    /**
     * @return the optional enhanced authentication and/or authorization mechanism of this Connect message.
     */
    @NotNull Optional<Mqtt5EnhancedAuthMechanism> getEnhancedAuthMechanism();

    /**
     * @return the optional Will Publish of this Connect message.
     */
    @NotNull Optional<Mqtt5WillPublish> getWillPublish();

    /**
     * @return the optional user properties of this Connect message.
     */
    @NotNull Mqtt5UserProperties getUserProperties();

    @Override
    default @NotNull Mqtt5MessageType getType() {
        return Mqtt5MessageType.CONNECT;
    }

    /**
     * Creates a builder for extending this Connect message.
     *
     * @return the created builder.
     */
    @NotNull Mqtt5ConnectBuilder extend();
}
