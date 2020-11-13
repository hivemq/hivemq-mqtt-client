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

import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5Message;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5MessageType;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * MQTT 5 ConnAck packet. This message is translated from and to an MQTT 5 CONNACK packet.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5ConnAck extends Mqtt5Message {

    /**
     * @return the Reason Code of this ConnAck message.
     */
    @NotNull Mqtt5ConnAckReasonCode getReasonCode();

    /**
     * @return whether the server has a session present.
     */
    boolean isSessionPresent();

    /**
     * @return the optional session expiry interval set from the server. If absent, the session expiry interval from the
     *         Connect message is used.
     */
    @NotNull OptionalLong getSessionExpiryInterval();

    /**
     * @return the optional keep alive set from the server. If absent, the keep alive from the Connect message is used.
     */
    @NotNull OptionalInt getServerKeepAlive();

    /**
     * @return the optional client identifier assigned by the server. If absent, the client identifier from the Connect
     *         message is used.
     */
    @NotNull Optional<MqttClientIdentifier> getAssignedClientIdentifier();

    /**
     * @return the optional enhanced authentication and/or authorization related data of this ConnAck message.
     */
    @NotNull Optional<Mqtt5EnhancedAuth> getEnhancedAuth();

    /**
     * @return the restrictions set from the server.
     */
    @NotNull Mqtt5ConnAckRestrictions getRestrictions();

    /**
     * @return the optional response information of this ConnAck message to retrieve a response topic from.
     */
    @NotNull Optional<MqttUtf8String> getResponseInformation();

    /**
     * @return the optional server reference.
     */
    @NotNull Optional<MqttUtf8String> getServerReference();

    /**
     * @return the optional reason string of this ConnAck message.
     */
    @NotNull Optional<MqttUtf8String> getReasonString();

    /**
     * @return the optional user properties of this ConnAck message.
     */
    @NotNull Mqtt5UserProperties getUserProperties();

    @Override
    default @NotNull Mqtt5MessageType getType() {
        return Mqtt5MessageType.CONNACK;
    }
}
