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

package org.mqttbee.mqtt.mqtt5.message.connect;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.internal.mqtt.message.connect.MqttConnectBuilder;
import org.mqttbee.internal.util.UnsignedDataTypes;
import org.mqttbee.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism;
import org.mqttbee.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.mqtt.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import org.mqttbee.mqtt.mqtt5.message.publish.Mqtt5WillPublish;

import java.util.Optional;

/**
 * MQTT 5 Connect message. This message is translated from and to a MQTT 5 CONNECT packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5Connect extends Mqtt5Message {

    int NO_KEEP_ALIVE = 0;
    int DEFAULT_KEEP_ALIVE = 60;
    boolean DEFAULT_CLEAN_START = true;
    long DEFAULT_SESSION_EXPIRY_INTERVAL = 0;
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
