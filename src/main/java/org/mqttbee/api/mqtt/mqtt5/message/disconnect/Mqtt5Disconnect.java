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

package org.mqttbee.api.mqtt.mqtt5.message.disconnect;

import org.mqttbee.annotations.DoNotImplement;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5Message;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;

import java.util.Optional;

/**
 * MQTT 5 DISCONNECT packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5Disconnect extends Mqtt5Message {

    @NotNull
    static Mqtt5DisconnectBuilder<Void> builder() {
        return new Mqtt5DisconnectBuilder<>(null);
    }

    /**
     * @return the reason code of this DISCONNECT packet.
     */
    @NotNull
    Mqtt5DisconnectReasonCode getReasonCode();

    /**
     * @return the optional session expiry interval in seconds, the client disconnects from with this DISCONNECT packet.
     */
    @NotNull
    Optional<Long> getSessionExpiryInterval();

    /**
     * @return the optional server reference, which can be used if the server sent this DISCONNECT packet.
     */
    @NotNull
    Optional<MqttUTF8String> getServerReference();

    /**
     * @return the optional reason string of this DISCONNECT packet.
     */
    @NotNull
    Optional<MqttUTF8String> getReasonString();

    /**
     * @return the optional user properties of this DISCONNECT packet.
     */
    @NotNull
    Mqtt5UserProperties getUserProperties();

    @NotNull
    @Override
    default Mqtt5MessageType getType() {
        return Mqtt5MessageType.DISCONNECT;
    }

}
