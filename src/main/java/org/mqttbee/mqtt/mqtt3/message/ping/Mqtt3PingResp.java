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

package org.mqttbee.mqtt.mqtt3.message.ping;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.mqtt.mqtt3.message.Mqtt3Message;
import org.mqttbee.mqtt.mqtt3.message.Mqtt3MessageType;

/**
 * MQTT 3 PingResp message. This message is translated from and to a MQTT 3 PINGRESP packet.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt3PingResp extends Mqtt3Message {

    @Override
    default @NotNull Mqtt3MessageType getType() {
        return Mqtt3MessageType.PINGRESP;
    }
}
