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

package org.mqttbee.api.mqtt.mqtt5.message.connect.connack;

import java.util.Optional;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttClientIdentifier;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5Message;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuth;

/**
 * MQTT 5 CONNACK packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5ConnAck extends Mqtt5Message {

    /** @return the reason code of this CONNACK packet. */
    @NotNull
    Mqtt5ConnAckReasonCode getReasonCode();

    /** @return whether the server has a session present. */
    boolean isSessionPresent();

    /**
     * @return the optional session expiry interval set from the server. If absent, the session
     *     expiry interval from the CONNECT packet is used.
     */
    @NotNull
    Optional<Long> getSessionExpiryInterval();

    /**
     * @return the optional keep alive set from the server. If absent, the keep alive from the
     *     CONNECT packet is used.
     */
    @NotNull
    Optional<Integer> getServerKeepAlive();

    /**
     * @return the optional client identifier assigned by the server. If absent, the client
     *     identifier from the CONNECT packet is used.
     */
    @NotNull
    Optional<MqttClientIdentifier> getAssignedClientIdentifier();

    /**
     * @return the optional enhanced authentication and/or authorization related data of this
     *     CONNACK packet.
     */
    @NotNull
    Optional<Mqtt5EnhancedAuth> getEnhancedAuth();

    /** @return the restrictions set from the server. */
    @NotNull
    Mqtt5ConnAckRestrictions getRestrictions();

    /**
     * @return the optional response information of this CONNACK packet to retrieve a response topic
     *     from.
     */
    @NotNull
    Optional<MqttUTF8String> getResponseInformation();

    /** @return the optional server reference. */
    @NotNull
    Optional<MqttUTF8String> getServerReference();

    /** @return the optional reason string of this CONNACK packet. */
    @NotNull
    Optional<MqttUTF8String> getReasonString();

    /** @return the optional user properties of this CONNACK packet. */
    @NotNull
    Mqtt5UserProperties getUserProperties();

    @NotNull
    @Override
    default Mqtt5MessageType getType() {
        return Mqtt5MessageType.CONNACK;
    }
}
