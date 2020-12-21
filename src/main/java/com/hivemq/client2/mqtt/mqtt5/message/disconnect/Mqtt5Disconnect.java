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

package com.hivemq.client2.mqtt.mqtt5.message.disconnect;

import com.hivemq.client2.internal.mqtt.message.disconnect.MqttDisconnectBuilder;
import com.hivemq.client2.internal.util.UnsignedDataTypes;
import com.hivemq.client2.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client2.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5Message;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5MessageType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Optional;
import java.util.OptionalLong;

/**
 * MQTT 5 Disconnect message. This message is translated from and to an MQTT 5 DISCONNECT packet.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5Disconnect extends Mqtt5Message {

    /**
     * Default Reason Code of a Disconnect message.
     */
    @NotNull Mqtt5DisconnectReasonCode DEFAULT_REASON_CODE = Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION;

    /**
     * Creates a builder for a Disconnect message.
     *
     * @return the created builder.
     */
    static @NotNull Mqtt5DisconnectBuilder builder() {
        return new MqttDisconnectBuilder.Default();
    }

    /**
     * @return the Reason Code of this Disconnect message.
     */
    @NotNull Mqtt5DisconnectReasonCode getReasonCode();

    /**
     * @return the optional session expiry interval in seconds, the client disconnects from with this Disconnect
     *         message.
     */
    @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE) @NotNull OptionalLong getSessionExpiryInterval();

    /**
     * @return the optional server reference, which can be used if the server sent this Disconnect message.
     */
    @NotNull Optional<MqttUtf8String> getServerReference();

    /**
     * @return the optional reason string of this Disconnect message.
     */
    @NotNull Optional<MqttUtf8String> getReasonString();

    /**
     * @return the optional user properties of this Disconnect message.
     */
    @NotNull Mqtt5UserProperties getUserProperties();

    @Override
    default @NotNull Mqtt5MessageType getType() {
        return Mqtt5MessageType.DISCONNECT;
    }

    /**
     * Creates a builder for extending this Disconnect message.
     *
     * @return the created builder.
     */
    @NotNull Mqtt5DisconnectBuilder extend();
}
