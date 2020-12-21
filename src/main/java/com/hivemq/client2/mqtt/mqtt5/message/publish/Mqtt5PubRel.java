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

package com.hivemq.client2.mqtt.mqtt5.message.publish;

import com.hivemq.client2.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client2.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5Message;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5MessageType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * MQTT 5 PubRel message. This message is translated from and to an MQTT 5 PUBREL packet.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5PubRel extends Mqtt5Message {

    /**
     * @return the Reason Code of this PubRel message.
     */
    @NotNull Mqtt5PubRelReasonCode getReasonCode();

    /**
     * @return the optional reason string of this PubRel message.
     */
    @NotNull Optional<MqttUtf8String> getReasonString();

    /**
     * @return the optional user properties of this PubRel message.
     */
    @NotNull Mqtt5UserProperties getUserProperties();

    @Override
    default @NotNull Mqtt5MessageType getType() {
        return Mqtt5MessageType.PUBREL;
    }
}
