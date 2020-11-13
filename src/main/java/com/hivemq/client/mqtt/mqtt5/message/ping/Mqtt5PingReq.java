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

package com.hivemq.client.mqtt.mqtt5.message.ping;

import com.hivemq.client.mqtt.mqtt5.message.Mqtt5Message;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5MessageType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * MQTT 5 PingReq message. This message is translated from and to an MQTT 5 PINGREQ packet.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5PingReq extends Mqtt5Message {

    @Override
    default @NotNull Mqtt5MessageType getType() {
        return Mqtt5MessageType.PINGREQ;
    }
}
